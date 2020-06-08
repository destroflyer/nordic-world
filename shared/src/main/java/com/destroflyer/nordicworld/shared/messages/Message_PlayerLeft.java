package com.destroflyer.nordicworld.shared.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Serializable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Message_PlayerLeft extends AbstractMessage {
  private int id;
}