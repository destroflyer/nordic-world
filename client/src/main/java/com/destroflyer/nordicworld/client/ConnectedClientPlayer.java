package com.destroflyer.nordicworld.client;

import com.destroflyer.nordicworld.client.models.ModelObject;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ConnectedClientPlayer {
    private ModelObject modelObject;
    private String currentAnimationName;
}
