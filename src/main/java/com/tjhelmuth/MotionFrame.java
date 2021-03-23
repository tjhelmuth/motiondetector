package com.tjhelmuth;

import lombok.Value;
import org.bytedeco.opencv.opencv_core.Mat;

@Value
public class MotionFrame {
    Mat frame;

    //Area in pixels of the rectangles of difference detected in the images
    long motionArea;
}
