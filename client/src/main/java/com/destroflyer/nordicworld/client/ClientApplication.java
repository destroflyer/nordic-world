package com.destroflyer.nordicworld.client;

import com.destroflyer.nordicworld.shared.*;
import com.destroflyer.nordicworld.client.models.ModelObject;
import com.destroflyer.nordicworld.shared.messages.*;
import com.jme3.animation.SkeletonControl;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Network;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.RadialBlurFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.TransparentComparator;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientApplication extends SimpleApplication implements ActionListener {

    public static void main(String[] args) {
        String authToken = args[0];

        try {
            FileOutputStream logFileOutputStream = new FileOutputStream("./log.txt");
            System.setOut(new PrintStream(new MultipleOutputStream(System.out, logFileOutputStream)));
            System.setErr(new PrintStream(new MultipleOutputStream(System.err, logFileOutputStream)));
        } catch (FileNotFoundException ex) {
            System.err.println("Error while accessing log file: " + ex.getMessage());
        }

        Logger.getLogger("").setLevel(Level.SEVERE);
        Logger.getLogger(SkeletonControl.class.getName()).setLevel(Level.SEVERE);
        MessageSerializer.registerClasses();

        ClientApplication app = new ClientApplication(authToken);
        AppSettings appSettings = new AppSettings(true);
        appSettings.setTitle("Nordic World [Version 0.1]");
        appSettings.setWidth(1280);
        appSettings.setHeight(720);
        appSettings.setGammaCorrection(true);
        app.setSettings(appSettings);
        app.setShowSettings(false);
        app.start();
    }

    public ClientApplication(String authToken) {
        this.authToken = authToken;
    }
    public static String ASSETS_ROOT;
    private String authToken;
    private Client client;
    private boolean[] arrowKeys = new boolean[4];
    private boolean isRunning;
    private Vector3f tmpWalkDirection = new Vector3f();
    private Vector3f walkDirection = new Vector3f();
    private ChaseCamera chaseCamera;
    private FilterPostProcessor filterPostProcessor;
    private boolean hasRadialBlurFilter;
    private RadialBlurFilter radialBlurFilter;
    private HashMap<Integer, ConnectedClientPlayer> connectedPlayers = new HashMap<>();
    private boolean isInitialized;

    @Override
    public void simpleInitApp() {
        ASSETS_ROOT = FileManager.getFileContent("./assets.ini");
        assetManager.registerLocator(ASSETS_ROOT, FileLocator.class);
        MaterialFactory.setAssetManager(assetManager);

        try {
            client = Network.connectToServer(Settings.get("server_game_host"), Settings.getInteger("server_game_port"));
            client.addMessageListener((source, m) -> {
                Message_PlayerJoined message = (Message_PlayerJoined) m;
                enqueue(() -> onPlayerJoined(message.getId(), message.getLogin()));
            }, Message_PlayerJoined.class);
            client.addMessageListener((source, m) -> {
                Message_PlayerLeft message = (Message_PlayerLeft) m;
                enqueue(() -> onPlayerLeft(message.getId()));
            }, Message_PlayerLeft.class);
            client.addMessageListener((source, m) -> {
                Message_InitializationFinished message = (Message_InitializationFinished) m;
                enqueue(() -> initializeWorld(message.getOwnPlayerId()));
            }, Message_InitializationFinished.class);
            client.addMessageListener((source, m) -> {
                Message_PlayerUpdate message = (Message_PlayerUpdate) m;
                enqueue(() -> updatePlayerTransform(message.getPlayerId(), message.getPosition(), message.getRotation(), message.getWalkDirection(), message.isRunning()));
            }, Message_PlayerUpdate.class);
            client.start();
            client.send(new Message_Login(authToken));
        } catch (IOException ex) {
            ex.printStackTrace();
            closeWithErrorMessage(ex.getMessage());
        }
    }

    private void initializeWorld(int ownPlayerId) {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(1));
        rootNode.addLight(ambient);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0.5f, -1, 0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(1));
        rootNode.addLight(sun);

        filterPostProcessor = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(filterPostProcessor);

        DirectionalLightShadowFilter directionalLightShadowFilter = new DirectionalLightShadowFilter(assetManager, 4096, 4);
        directionalLightShadowFilter.setLight(sun);
        filterPostProcessor.addFilter(directionalLightShadowFilter);

        filterPostProcessor.addFilter(new SSAOFilter(3, 20, 3, 0.2f));

        radialBlurFilter = new RadialBlurFilter();

        stateManager.attach(new WaterAppState() {

            @Override
            protected void initialize(Application app) {
                super.initialize(app);
                WaterFilter waterFilter = createWaterFilter(new Vector3f(0, -2.69f, 0), null);
                filterPostProcessor.addFilter(waterFilter);
            }
        });

        addSky("miramar");

        Spatial village = ClientMapAssets.loadVillage(assetManager);
        village.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(village);

        initControls();

        flyCam.setMoveSpeed(150);
        ModelObject ownModelObject = connectedPlayers.get(ownPlayerId).getModelObject();
        chaseCamera = new ChaseCamera(cam, ownModelObject, inputManager);
        chaseCamera.setDefaultDistance(6);
        chaseCamera.setLookAtOffset(new Vector3f(0, 2, 0));

        viewPort.getQueue().setGeometryComparator(RenderQueue.Bucket.Transparent, new TransparentComparator() {

            @Override
            public int compare(Geometry o1, Geometry o2) {
                Integer number1 = getSurfaceNumber(o1);
                Integer number2 = getSurfaceNumber(o2);
                if ((number1 != null) && (number2 != null)) {
                    return number2 - number1;
                } else {
                    return super.compare(o1, o2);
                }
            }

            private Integer getSurfaceNumber(Geometry geometry) {
                if (geometry.getName().startsWith("pPlane464 polySurface")) {
                    String substring = geometry.getName().substring("pPlane464 polySurface".length());
                    return Integer.parseInt(substring);
                } else if (geometry.getName().equals("pPlane464 base")) {
                    return 199999;
                }
                return null;
            }
        });

        isInitialized = true;
    }

    private void addSky(String skyName) {
        Texture textureWest = assetManager.loadTexture("textures/skies/" + skyName + "/left.png");
        Texture textureEast = assetManager.loadTexture("textures/skies/" + skyName + "/right.png");
        Texture textureNorth = assetManager.loadTexture("textures/skies/" + skyName + "/front.png");
        Texture textureSouth = assetManager.loadTexture("textures/skies/" + skyName + "/back.png");
        Texture textureUp = assetManager.loadTexture("textures/skies/" + skyName + "/up.png");
        Texture textureDown = assetManager.loadTexture("textures/skies/" + skyName + "/down.png");
        rootNode.attachChild(SkyFactory.createSky(assetManager, textureWest, textureEast, textureNorth, textureSouth, textureUp, textureDown));
    }

    private void onPlayerJoined(int playerId, String login) {
        System.out.println("Player #" + playerId + " joined ('" + login + "').");
        ModelObject modelObject = new ModelObject(null, "models/dwarf_warrior/skin.xml");
        modelObject.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(modelObject);

        ConnectedClientPlayer connectedClientPlayer = ConnectedClientPlayer.builder()
                .modelObject(modelObject)
                .build();
        connectedPlayers.put(playerId, connectedClientPlayer);
    }

    private void onPlayerLeft(int playerId) {
        System.out.println("Player #" + playerId + " left.");
        ConnectedClientPlayer connectedClientPlayer = connectedPlayers.remove(playerId);
        rootNode.detachChild(connectedClientPlayer.getModelObject());
    }

    private void updatePlayerTransform(int playerId, Vector3f position, Quaternion rotation, Vector3f walkDirection, boolean isRunning) {
        ConnectedClientPlayer connectedClientPlayer = connectedPlayers.get(playerId);
        ModelObject modelObject = connectedClientPlayer.getModelObject();
        modelObject.setLocalTranslation(position);
        modelObject.setLocalRotation(rotation);

        if (walkDirection.length() > 0) {
            setPlayerAnimation(connectedClientPlayer, "run3", 0.75f * (isRunning ? 0.2f : 1));
        } else {
            setPlayerAnimation(connectedClientPlayer, "idle1", 7.875f);
        }
    }

    private void setPlayerAnimation(ConnectedClientPlayer connectedClientPlayer, String animationName, float loopDuration) {
        if (animationName.equals(connectedClientPlayer.getCurrentAnimationName())) {
            connectedClientPlayer.getModelObject().setAnimationProperties(loopDuration, true);
        } else {
            connectedClientPlayer.getModelObject().playAnimation(animationName, loopDuration);
            connectedClientPlayer.setCurrentAnimationName(animationName);
        }
    }

    @Override
    public void simpleUpdate(float lastTimePerFrame) {
        if (isInitialized) {
            tmpWalkDirection.set(walkDirection);

            Vector3f camDir = cam.getDirection();
            Vector3f camLeft = cam.getLeft();
            walkDirection.set(0, 0, 0);
            if (arrowKeys[0]) {
                walkDirection.addLocal(camDir);
            }
            if (arrowKeys[1]) {
                walkDirection.addLocal(camLeft.negate());
            }
            if (arrowKeys[2]) {
                walkDirection.addLocal(camDir.negate());
            }
            if (arrowKeys[3]) {
                walkDirection.addLocal(camLeft);
            }
            walkDirection.setY(0).normalizeLocal();

            if (isRunning && (walkDirection.length() > 0)) {
                if (!hasRadialBlurFilter) {
                    filterPostProcessor.addFilter(radialBlurFilter);
                    hasRadialBlurFilter = true;
                }
            } else {
                if (hasRadialBlurFilter) {
                    filterPostProcessor.removeFilter(radialBlurFilter);
                    hasRadialBlurFilter = false;
                }
            }

            if (!walkDirection.equals(tmpWalkDirection)) {
                client.send(new Message_SetWalkDirection(walkDirection));
            }
        }
    }

    private void initControls() {
        inputManager.addMapping("move_left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("move_right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("move_up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("move_down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("run", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addListener(this, "move_left");
        inputManager.addListener(this, "move_right");
        inputManager.addListener(this, "move_up");
        inputManager.addListener(this, "move_down");
        inputManager.addListener(this, "jump");
        inputManager.addListener(this, "run");
    }

    @Override
    public void onAction(String actionName, boolean value, float lastTimePerFrame) {
        if(actionName.equals("move_up")) {
            arrowKeys[0] = value;
        } else if(actionName.equals("move_right")) {
            arrowKeys[1] = value;
        } else if(actionName.equals("move_left")) {
            arrowKeys[3] = value;
        } else if(actionName.equals("move_down")) {
            arrowKeys[2] = value;
        } else if(actionName.equals("jump") && value) {
            client.send(new Message_Jump());
        } else if(actionName.equals("run")) {
            isRunning = value;
            client.send(new Message_SetRunning(isRunning));
        }
    }

    private void closeWithErrorMessage(String message) {
        stop();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, message, "Nordic World - Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void destroy() {
        super.destroy();
        // There is most likely a better way to make sure the process ends
        System.exit(0);
    }
}
