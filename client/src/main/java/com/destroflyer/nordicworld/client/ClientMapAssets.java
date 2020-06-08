package com.destroflyer.nordicworld.client;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class ClientMapAssets {

    public static Spatial loadVillage(AssetManager assetManager) {
        Node village = (Node) assetManager.loadModel("models/nordic_village/nordic_village.j3o");
        ((Node) village.getChild(0)).detachChildNamed("pPlane479");
        village.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        loadVillageSpatial(village, assetManager);
        return village;
    }

    private static void loadVillageSpatial(Spatial spatial, AssetManager assetManager) {
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (Spatial child : node.getChildren()) {
                loadVillageSpatial(child, assetManager);
            }
        } else {
            Geometry geometry = (Geometry) spatial;
            Material material = geometry.getMaterial();
            material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
            material.setFloat("Shininess", 50);
            if (geometry.getName().startsWith("polySurface1659 pPlane464")) {
                geometry.setName("pPlane464 polySurface99991");
            }
            if (geometry.getName().startsWith("pPlane464 polySurface")) {
                material.setColor("Diffuse", new ColorRGBA(0.7f, 0.4f, 0.15f, 1));
                material.setTexture("DiffuseMap", assetManager.loadTexture("models/nordic_village/ground.jpg"));
                material.setTexture("AlphaMap", assetManager.loadTexture("models/nordic_village/ground.jpg"));
                material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                material.getAdditionalRenderState().setDepthWrite(false);
                geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
                geometry.setShadowMode(RenderQueue.ShadowMode.Off);
                material.setFloat("Shininess", 1);
            } else if (
                geometry.getName().startsWith("pCylinder") // Walls
             || geometry.getName().startsWith("polySurface1159 pCylinder40") // Wall Front Left
             || geometry.getName().startsWith("polySurface1158") // Wall Front Left Inside
             || geometry.getName().startsWith("polySurface1154") // Wall Front Right Inside
             || geometry.getName().startsWith("pCylinder40 polySurface1149") // Wall Front Right
            ) {
                ColorRGBA diffuse = (ColorRGBA) material.getParam("Diffuse").getValue();
                // Wall Top
                if ((diffuse.getRed() == 0.67f) && (diffuse.getGreen() == 0.33f) && (diffuse.getBlue() == 0.18f)) {
                    material.setFloat("Shininess", 1);
                }
                // Wall Bottom
                if ((diffuse.getRed() == 0.17f) && (diffuse.getGreen() == 0.07f) && (diffuse.getBlue() == 0.03f)) {
                    material.setFloat("Shininess", 1);
                }
                // Middle Green Trees
                if ((diffuse.getRed() == 0.23f) && (diffuse.getGreen() == 0.4f) && (diffuse.getBlue() == 0.01f)) {
                    material.setFloat("Shininess", 1);
                }
                // Dark Green Trees
                if ((diffuse.getRed() == 0.02f) && (diffuse.getGreen() == 0.14f) && (diffuse.getBlue() == 0.04f)) {
                    material.setFloat("Shininess", 1);
                }
                // Light Green Trees
                if ((diffuse.getRed() == 0.3f) && (diffuse.getGreen() == 0.35f) && (diffuse.getBlue() == 0.07f)) {
                    material.setFloat("Shininess", 1);
                }
            } else if (geometry.getName().equals("pPlane464 base1")) {
                material.setFloat("Shininess", 1);
            }
            ColorRGBA diffuse = (ColorRGBA) material.getParam("Diffuse").getValue();
            material.setColor("Ambient", diffuse.mult(0.3f));
        }
    }
}
