package com.tjhelmuth;

import org.bytedeco.javacv.Frame;

public interface VideoSourceListener {
    void onConfiguration(VideoInfo videoInfo);
    void onFrame(Frame frame);
    default void onLastFrame(){}
}
