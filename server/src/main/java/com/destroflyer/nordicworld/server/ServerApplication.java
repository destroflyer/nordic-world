package com.destroflyer.nordicworld.server;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.destroflyer.nordicworld.shared.*;
import com.destroflyer.nordicworld.shared.messages.*;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.*;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerApplication extends SimpleApplication {

    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.SEVERE);
        MessageSerializer.registerClasses();

        ServerApplication app = new ServerApplication();
        AppSettings appSettings = new AppSettings(true);
        appSettings.setFrameRate(60);
        app.setSettings(appSettings);
        app.start(JmeContext.Type.Headless);
    }

    public static String ASSETS_ROOT;
    private Server server;
    private JWTVerifier authTokenVerifier;
    private BulletAppState bulletAppState;
    private HashMap<Integer, ConnectedServerPlayer> connectedPlayers = new HashMap<>();

    @Override
    public void simpleInitApp() {
        authTokenVerifier = AuthTokenService.createVerifier();
        try {
            server = Network.createServer(6143);
            server.addConnectionListener(new ConnectionListener() {

                @Override
                public void connectionAdded(Server server, HostedConnection connection) {
                    System.out.println("Client #" + connection.getId() + " connected.");
                }

                @Override
                public void connectionRemoved(Server server, HostedConnection connection) {
                    ConnectedServerPlayer connectedServerPlayer = onPlayerDisconnected(connection.getId());
                    System.out.println("Client #" + connection.getId() + " disconnected -> Logout '" + connectedServerPlayer.getLogin() + "' (User #" + connectedServerPlayer.getPlayerId() + ").");
                    System.out.println("Total Players: " + connectedPlayers.size());
                    sendToAllPlayers(new Message_PlayerLeft(connectedServerPlayer.getPlayerId()));
                }
            });
            server.addMessageListener((source, m) -> {
                Message_Login message = (Message_Login) m;
                DecodedJWT decodedJWT = authTokenVerifier.verify(message.getAuthToken());
                Map<String, Object> user = decodedJWT.getClaim("user").asMap();
                int playerId = (int) user.get("id");
                String login = (String) user.get("login");
                System.out.println("Client #" + source.getId() + " authenticated as '" + login + "' (User #" + playerId + ").");
                onPlayerConnected(source.getId(), playerId, login);
                System.out.println("Total Players: " + connectedPlayers.size());
                for (ConnectedServerPlayer connectedServerPlayer : connectedPlayers.values()) {
                    Message_PlayerJoined message_PlayerJoined = new Message_PlayerJoined(connectedServerPlayer.getPlayerId(), connectedServerPlayer.getLogin());
                    if (connectedServerPlayer.getPlayerId() == playerId) {
                        sendToAllPlayers(message_PlayerJoined);
                    } else {
                        source.send(message_PlayerJoined);
                    }
                    source.send(generatePlayerUpdateMessage(connectedServerPlayer));
                }
                source.send(new Message_InitializationFinished(playerId));
            }, Message_Login.class);
            server.addMessageListener((source, m) -> {
                Message_SetWalkDirection message = (Message_SetWalkDirection) m;
                setPlayerWalkDirection(source.getId(), message.getWalkDirection());
            }, Message_SetWalkDirection.class);
            server.addMessageListener((source, m) -> {
                Message_SetRunning message = (Message_SetRunning) m;
                setPlayerRunning(source.getId(), message.isRunning());
            }, Message_SetRunning.class);
            server.addMessageListener((source, m) -> {
                jump(source.getId());
            }, Message_Jump.class);
            server.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        ASSETS_ROOT = FileManager.getFileContent("./assets.ini");
        assetManager.registerLocator(ASSETS_ROOT, FileLocator.class);

        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        Spatial village = ServerMapAssets.loadVillage(assetManager);
        rootNode.attachChild(village);
        addToPhysicsSpace(village);

        System.out.println("Server started.");
    }

    private void addToPhysicsSpace(Spatial spatial) {
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (Spatial child : node.getChildren()) {
                addToPhysicsSpace(child);
            }
        } else {
            bulletAppState.getPhysicsSpace().add(spatial);
        }
    }

    private void onPlayerConnected(int clientId, int playerId, String login) {
        Node node = new Node();
        node.setLocalTranslation(new Vector3f(-40, 20, -45));
        rootNode.attachChild(node);

        BetterCharacterControl betterCharacterControl = new BetterCharacterControl(0.4f, 2f, 60);
        betterCharacterControl.setJumpForce(new Vector3f(0, 350, 0));
        node.addControl(betterCharacterControl);
        bulletAppState.getPhysicsSpace().add(betterCharacterControl);

        ConnectedServerPlayer connectedServerPlayer = ConnectedServerPlayer.builder()
                .playerId(playerId)
                .login(login)
                .node(node)
                .betterCharacterControl(betterCharacterControl)
                .walkDirection(new Vector3f())
                .isRunning(false)
                .lastPosition(new Vector3f())
                .lastRotation(new Quaternion())
                .build();
        connectedPlayers.put(clientId, connectedServerPlayer);
    }

    private ConnectedServerPlayer onPlayerDisconnected(int clientId) {
        ConnectedServerPlayer connectedServerPlayer = connectedPlayers.remove(clientId);
        rootNode.detachChild(connectedServerPlayer.getNode());
        bulletAppState.getPhysicsSpace().remove(connectedServerPlayer.getBetterCharacterControl());
        return connectedServerPlayer;
    }

    private void setPlayerWalkDirection(int clientId, Vector3f walkDirection) {
        ConnectedServerPlayer connectedServerPlayer = connectedPlayers.get(clientId);
        connectedServerPlayer.getWalkDirection().set(walkDirection);
        updateEffectiveWalkDirection(connectedServerPlayer);
    }

    private void setPlayerRunning(int clientId, boolean isRunning) {
        ConnectedServerPlayer connectedServerPlayer = connectedPlayers.get(clientId);
        connectedServerPlayer.setRunning(isRunning);
        updateEffectiveWalkDirection(connectedServerPlayer);
    }

    private void updateEffectiveWalkDirection(ConnectedServerPlayer connectedServerPlayer) {
        BetterCharacterControl betterCharacterControl = connectedServerPlayer.getBetterCharacterControl();
        Vector3f effectiveWalkDirection = connectedServerPlayer.getWalkDirection().mult(connectedServerPlayer.isRunning() ? 50 : 10);
        betterCharacterControl.setViewDirection(effectiveWalkDirection);
        betterCharacterControl.setWalkDirection(effectiveWalkDirection);
    }

    private void jump(int clientId) {
        ConnectedServerPlayer connectedServerPlayer = connectedPlayers.get(clientId);
        BetterCharacterControl betterCharacterControl = connectedServerPlayer.getBetterCharacterControl();
        betterCharacterControl.jump();
    }

    @Override
    public void simpleUpdate(float lastTimePerFrame) {
        super.simpleUpdate(lastTimePerFrame);
        for (ConnectedServerPlayer connectedServerPlayer : connectedPlayers.values()) {
            Vector3f lastPosition = connectedServerPlayer.getLastPosition();
            Quaternion lastRotation = connectedServerPlayer.getLastRotation();
            Vector3f currentPosition = connectedServerPlayer.getNode().getLocalTranslation();
            Quaternion currentRotation = connectedServerPlayer.getNode().getLocalRotation();
            if ((!currentPosition.equals(lastPosition)) || (!lastRotation.equals(currentRotation))) {
                sendToAllPlayers(generatePlayerUpdateMessage(connectedServerPlayer));
            }
        }
    }

    private Message_PlayerUpdate generatePlayerUpdateMessage(ConnectedServerPlayer connectedServerPlayer) {
        int playerId = connectedServerPlayer.getPlayerId();
        Vector3f position = connectedServerPlayer.getNode().getLocalTranslation();
        Quaternion rotation = connectedServerPlayer.getNode().getLocalRotation();
        Vector3f walkDirection = connectedServerPlayer.getWalkDirection();
        boolean isRunning = connectedServerPlayer.isRunning();
        return new Message_PlayerUpdate(playerId, position, rotation, walkDirection, isRunning);
    }

    private void sendToAllPlayers(Message message) {
        for (int connectionId : connectedPlayers.keySet()) {
            server.getConnection(connectionId).send(message);
        }
    }
}
