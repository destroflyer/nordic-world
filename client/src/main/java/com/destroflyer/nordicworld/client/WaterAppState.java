/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.destroflyer.nordicworld.client;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.water.SimpleWaterProcessor;
import com.jme3.water.WaterFilter;

/**
 *
 * @author Carl
 */
public class WaterAppState extends BaseAppState{
    
    public static final float DEFAULT_SPEED = 0.5f;
    private SimpleApplication mainApplication;
    private SimpleWaterProcessor waterProcessor;

    @Override
    protected void initialize(Application app) {
        mainApplication = (SimpleApplication) app;
        waterProcessor = new SimpleWaterProcessor(mainApplication.getAssetManager());
        waterProcessor.setReflectionScene(mainApplication.getRootNode());
        waterProcessor.setLightPosition(new Vector3f(30, 10, -30));
        waterProcessor.setDistortionScale(0.1f);
        waterProcessor.setWaveSpeed(0.015f);
        mainApplication.getViewPort().addProcessor(waterProcessor);
    }

    public Geometry createWaterPlane(Vector3f position, Vector2f size){
        Quad quad = new Quad(size.getX(), size.getY());
        quad.scaleTextureCoordinates(new Vector2f((quad.getWidth() / 20), (quad.getHeight() / 20)));
        Geometry waterPlane = new Geometry("", quad);
        waterPlane.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        waterPlane.setMaterial(getMaterial());
        float z = (position.getZ() + size.getY());
        waterPlane.setLocalTranslation(position.getX(), position.getY(), z);
        return waterPlane;
    }
    
    public Material getMaterial(){
        return waterProcessor.getMaterial();
    }

    public WaterFilter createWaterFilter(Vector3f position, Vector2f size){
        WaterFilter waterFilter  = new WaterFilter(mainApplication.getRootNode(), new Vector3f(1, -4, 1).normalizeLocal());
        if (size != null) {
            waterFilter.setShapeType(WaterFilter.AreaShape.Square);
            waterFilter.setCenter(position.add((size.getX() / 2), 0, (size.getY() / 2)));
            waterFilter.setRadius(Math.max((size.getX() / 2), (size.getY() / 2)));
        }
        waterFilter.setFoamIntensity(0.2f);
        waterFilter.setFoamHardness(0.8f);
        waterFilter.setRefractionStrength(0.2f);
        waterFilter.setShininess(0.5f);
        waterFilter.setSpeed(DEFAULT_SPEED);
        waterFilter.setUseRipples(false);
        waterFilter.setWaterTransparency(0.2f);
        waterFilter.setWaterColor(new ColorRGBA(0, 0.1f, 1, 1));
        waterFilter.setWaterHeight(position.getY());
        return waterFilter;
    }

    @Override
    protected void cleanup(Application app) {
        app.getViewPort().removeProcessor(waterProcessor);
    }

    @Override
    protected void onEnable() {
        
    }

    @Override
    protected void onDisable() {
        
    }
}
