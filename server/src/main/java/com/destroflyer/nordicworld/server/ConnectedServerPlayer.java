package com.destroflyer.nordicworld.server;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ConnectedServerPlayer {
    private int connectionId;
    private int playerId;
    private String login;
    private Node node;
    private BetterCharacterControl betterCharacterControl;
    private Vector3f walkDirection;
    private boolean isRunning;
    private Vector3f lastPosition;
    private Quaternion lastRotation;
}
