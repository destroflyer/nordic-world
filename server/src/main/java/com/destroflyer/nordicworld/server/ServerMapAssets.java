package com.destroflyer.nordicworld.server;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class ServerMapAssets {

    public static Spatial loadVillage(AssetManager assetManager) {
        Node village = (Node) assetManager.loadModel("models/nordic_village/nordic_village.j3o");
        ((Node) village.getChild(0)).detachChildNamed("pPlane479");
        loadVillageSpatial(village);
        return village;
    }

    private static void loadVillageSpatial(Spatial spatial) {
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (Spatial child : node.getChildren()) {
                loadVillageSpatial(child);
            }
        } else {
            Geometry geometry = (Geometry) spatial;
            boolean hasCollisionShape = true;
            if (geometry.getName().startsWith("polySurface1659 pPlane464")) {
                geometry.setName("pPlane464 polySurface99991");
            }
            if (geometry.getName().startsWith("pPlane464 polySurface")) {
                hasCollisionShape = false;
            }

            if (hasCollisionShape) {
                geometry.addControl(new RigidBodyControl(0));
            }
        }
    }
}
