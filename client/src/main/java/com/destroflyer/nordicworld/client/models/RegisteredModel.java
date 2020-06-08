/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.destroflyer.nordicworld.client.models;

import com.destroflyer.nordicworld.client.JMonkeyUtil;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.animation.SkeletonControl;
import com.jme3.scene.Node;

/**
 *
 * @author Carl
 */
public class RegisteredModel {

    public RegisteredModel(Node node) {
        this.node = node;
    }
    private Node node;

    public void initialize(ModelObject modelObject) {
        // Animation
        AnimControl animationControl = node.getControl(AnimControl.class);
        if (animationControl != null) {
            AnimChannel animationChannel = animationControl.createChannel();
            // Copy the animation of the original model for the others
            if (this != modelObject.getOriginalRegisteredModel()) {
                AnimChannel activeOriginalAnimationChannel = modelObject.getOriginalRegisteredModel().getAnimationChannel();
                if (activeOriginalAnimationChannel.getAnimationName() != null) {
                    setAnimationName(activeOriginalAnimationChannel.getAnimationName());
                    JMonkeyUtil.copyAnimation(activeOriginalAnimationChannel, animationChannel);
                }
            }
        }
        // HardwareSkinning
        JMonkeyUtil.setHardwareSkinningPreferred(node, true);
    }

    public Node requestBoneAttachmentsNode(String boneName) {
        SkeletonControl skeletonControl = node.getControl(SkeletonControl.class);
        return skeletonControl.getAttachmentsNode(boneName);
    }

    public void setAnimationName(String animationName) {
        AnimChannel animationChannel = getAnimationChannel();
        try {
            animationChannel.setAnim(animationName);
        } catch(IllegalArgumentException ex) {
            stopAndRewindAnimation();
        }
    }

    public void setAnimationProperties(float loopDuration, boolean isLoop) {
        AnimChannel animationChannel = getAnimationChannel();
        if (animationChannel.getAnimationName() != null) {
            animationChannel.setSpeed(animationChannel.getAnimMaxTime() / loopDuration);
            animationChannel.setLoopMode(isLoop ? LoopMode.Loop : LoopMode.DontLoop);
        }
    }

    public void stopAndRewindAnimation() {
        AnimChannel animationChannel = getAnimationChannel();
        if (animationChannel != null) {
            animationChannel.reset(true);
        }
    }

    public AnimChannel getAnimationChannel() {
        AnimControl animationControl = node.getControl(AnimControl.class);
        return ((animationControl != null) ? animationControl.getChannel(0) : null);
    }

    public Node getNode() {
        return node;
    }
}
