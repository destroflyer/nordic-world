package com.destroflyer.nordicworld.server;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.BetterCharacterControl;

public class BestCharacterControl extends BetterCharacterControl {

    public BestCharacterControl(float radius, float height, float mass) {
        super(radius, height, mass);
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        // Prevent bouncing off small slopes on the ground
        if (isOnGround()) {
            // TODO: velocity.setY(0); (Decide if this is still needed with Minie)
        }
        super.prePhysicsTick(space, tpf);
    }
}
