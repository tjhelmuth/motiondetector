package com.tjhelmuth;

import lombok.Value;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;

import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

/**
 * Mask composed of squares applied to frames to rule those squares out from being considered
 * for motion detection.
 *
 * Currently they are just drawn over with solid black so they never change.
 */
@Value
public class ImageMask {
    public static final int CELL_SIZE = 50;
    int topLeft;
    int bottomRight;

    public static ImageMask of(int tl, int br){
        return new ImageMask(tl, br);
    }

    public Rect getRekt(Size imgSize){
        int cols = (int) Math.ceil((double) imgSize.width() / CELL_SIZE);

        int leftCol = topLeft % cols;
        int rightCol = bottomRight % cols;

        int topRow = topLeft / cols;
        int bottomRow = bottomRight / cols;

        //we will always have at least CELL_SIZE width/height
        int width = ((rightCol - leftCol) * CELL_SIZE) + CELL_SIZE;
        int height = ((bottomRow - topRow) * CELL_SIZE) + CELL_SIZE;

        return new Rect(leftCol * CELL_SIZE, topRow * CELL_SIZE, width, height);
    }
}
