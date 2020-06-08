package com.destroflyer.nordicworld.shared;

import com.destroflyer.nordicworld.shared.messages.*;
import com.jme3.network.serializing.Serializer;

public class MessageSerializer {

    public static void registerClasses() {
        Serializer.registerClasses(
                Message_Jump.class,
                Message_InitializationFinished.class,
                Message_Login.class,
                Message_PlayerJoined.class,
                Message_PlayerLeft.class,
                Message_PlayerUpdate.class,
                Message_SetRunning.class,
                Message_SetWalkDirection.class
        );
    }
}
