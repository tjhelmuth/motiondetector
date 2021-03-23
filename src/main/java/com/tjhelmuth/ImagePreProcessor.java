package com.tjhelmuth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Processes an image to make it suitable for motion detection
 *
 * We do this in place on the Mat instance
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class ImagePreProcessor {
    private static final Integer DESIRED_WIDTH = 720;
    private final List<ImageMask> maskDefs;
    private List<Rect> resolvedMasks = new ArrayList<>();
    private Size scaledSize;

    public void prepareForComparison(Mat frame){
        resize(frame, frame, scaledSize);
        GaussianBlur(frame, frame, new Size(9, 9), 2);
        cvtColor(frame, frame, COLOR_BGRA2GRAY);

        //apply masks over image to remove the rects as consideration for motion
        resolvedMasks.forEach(mask -> rectangle(frame, mask, Scalar.BLACK, -1, LINE_8, 0));
    }

    public void setSize(Size frameSize){
        var aspectRatio = (float) frameSize.width() / frameSize.height();
        this.scaledSize = new Size(DESIRED_WIDTH, Math.round(DESIRED_WIDTH / aspectRatio));
        this.resolvedMasks = this.maskDefs.stream()
                .map(msk -> msk.getRekt(scaledSize))
                .collect(Collectors.toList());

        log.debug("Resolved masks to {}", Util.rectsToString(this.resolvedMasks));
    }

}
