package com.tjhelmuth;

import lombok.Builder;
import lombok.Value;
import org.bytedeco.opencv.opencv_core.Rect;

import java.util.ArrayList;
import java.util.List;

@Value @Builder
public class DetectionSettings {
    //Amount of color that a pixel must be different from another to be counted as different
    @Builder.Default
    int comparisonThreshold = 64;

    //Total area of all diff rects in frame required to count as motion
    @Builder.Default
    int diffSizeThreshold = 1000;

    @Builder.Default
    List<ImageMask> masks = new ArrayList<>();

    public static DetectionSettings getDefault(){
        return DetectionSettings.builder().build();
    }
}
