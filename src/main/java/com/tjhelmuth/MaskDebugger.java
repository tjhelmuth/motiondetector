package com.tjhelmuth;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import static com.tjhelmuth.ImageMask.CELL_SIZE;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

import java.util.Collections;
import java.util.List;

/**
 * Prints a screen capture from the video stream of the configured webcam with a grid of 50x50 pixel squares
 * drawn on the image with a numeric label. This label can be used to mask parts of the stream to prevent the
 * comparison being run on them.
 */
@Slf4j
public class MaskDebugger implements VideoSourceListener {
    private final ImagePreProcessor imagePreProcessor;
    private final OpenCVFrameConverter<Mat> matConverter = new OpenCVFrameConverter.ToMat();

    private final Runnable finishHandler;

    public MaskDebugger(List<ImageMask> masks, Runnable finishHandler) {
        this.finishHandler = finishHandler;
        this.imagePreProcessor = new ImagePreProcessor(masks);
    }

    @Override
    public void onConfiguration(VideoInfo videoInfo) {
        imagePreProcessor.setSize(new Size(videoInfo.getWidth(), videoInfo.getHeight()));
    }

    @Override
    public void onFrame(Frame rawFrame) {
        Mat frame = matConverter.convert(rawFrame);
        imagePreProcessor.prepareForComparison(frame);

        Size imgSize = imagePreProcessor.getScaledSize();

        log.info("Drawing debug grid for image size {}x{}", imgSize.width(), imgSize.height());

        int cell = 0;
        for(int y = 0; y < imgSize.height(); y+=50){
            for(int x = 0; x < imgSize.width(); x+=50){
                var rect = new Rect(x, y, CELL_SIZE, CELL_SIZE);
                rectangle(frame, rect, Scalar.RED);

                Point textBottomLeft = new Point(x + 8, y + CELL_SIZE - 8);
                putText(frame, String.valueOf(cell), textBottomLeft, FONT_HERSHEY_PLAIN,0.8, Scalar.RED);
                cell++;
            }
        }

        imwrite("./images/maskdebug.png", frame);
        finishHandler.run();
    }
}
