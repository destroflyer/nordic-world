package com.destroflyer.nordicworld.shared.messages;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Serializable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Message_PlayerUpdate extends AbstractMessage {

  private int playerId;
  private Vector3f position;
  private Quaternion rotation;
  private Vector3f walkDirection;
  private boolean isRunning;

  @Override
  public boolean isReliable() {
    return false;
  }
}