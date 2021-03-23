package com.tjhelmuth;


import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Slf4j
public class MotionDetectionEngine implements VideoSourceListener {
    private final DetectionSettings settings;
    private final ImagePreProcessor imageProcessor;
    private final MotionRecorder motionRecorder;
    private final OpenCVFrameConverter<Mat> frameConverter = new OpenCVFrameConverter.ToMat();

    private final List<MotionListener> listeners;

    private Mat current;
    private final StopWatch stopWatch = new StopWatch();

    public MotionDetectionEngine(DetectionSettings settings, List<MotionListener> listeners) {
        this.settings = settings;
        this.listeners = listeners;
        this.motionRecorder = new MotionRecorder(-1, settings);
        this.imageProcessor = new ImagePreProcessor(settings.getMasks());
    }

    @Override
    public void onConfiguration(VideoInfo videoInfo) {
        imageProcessor.setSize(new Size(videoInfo.getWidth(), videoInfo.getHeight()));
        motionRecorder.setFps(videoInfo.getFps());
    }

    @Override
    public void onFrame(Frame frame) {
        Mat mat = frameConverter.convert(frame);
        submit(mat);
    }

    public synchronized void submit(Mat frame){
        if(frame == null){
            log.warn("Null frame passed to motion detection engine");
            return;
        }
        stopWatch.reset();
        stopWatch.start();

        Mat newFrame = frame.clone();

        imageProcessor.prepareForComparison(newFrame);

        Mat previous = current;
        current = newFrame;

        //if this is the first frame, we cant do anything
        if(previous == null){
            return;
        }

        MotionResult result = findMotionRects(current, previous);

        boolean isMotion = result.getTotalArea() > settings.getDiffSizeThreshold();
        if(isMotion){
            for(Rect rect : result.getRects()){
                rectangle(frame, rect, new Scalar(0, 0, 255, 1));
            }
        }

        if(isMotion || motionRecorder.isRecording()){
            motionRecorder.submit(frame, result);
        }

        motionRecorder.getSequence()
                .ifPresent(sequence -> {
                    listeners.forEach(l -> l.onMotionDetected(sequence));
                    motionRecorder.reset();
                });


        stopWatch.stop();
        if(stopWatch.getTime() > 50){
            log.info("Frame processing took {} ms", stopWatch.getTime());
        }
    }

    /**
     * Compares the 2 frames and looks for differences in the pictures, using set thresholds.
     *
     * Differences passing the thresholds are returned as rectangles where the motion occurred.
     */
    public MotionResult findMotionRects(Mat frame1, Mat frame2){
        Mat diff = new Mat();
        absdiff(frame1, frame2, diff);
        threshold(diff, diff, settings.getComparisonThreshold(), 255, THRESH_BINARY);

        MatVector contours = new MatVector();
        findContours(diff, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        Rect[] rects = new Rect[(int) contours.size()];

        int totalArea = 0;
        for(int i = 0; i < contours.size(); i++){
            Mat contour = contours.get(i);
            Rect rect = boundingRect(contour);
            totalArea += rect.area();
            rects[i] = rect;
        }

        return new MotionResult(totalArea, rects);
    }

    @Value
    static class MotionResult {
        long totalArea;
        Rect[] rects;
    }
}
