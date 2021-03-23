package com.tjhelmuth;

/**
 * base interface for registering a source of video content
 */
public interface VideoSource {
    VideoSource registerListener(VideoSourceListener listener);

    void start();

    void stop();
}
