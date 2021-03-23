package com.tjhelmuth;

import java.util.List;

/**
 * Asynchronous handler for when motion is detected
 */
public interface MotionListener {
    void onMotionDetected(List<MotionFrame> sequence);
}
