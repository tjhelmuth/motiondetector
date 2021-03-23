package com.tjhelmuth;

import com.tjhelmuth.MotionDetectionEngine.MotionResult;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.Mat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Record sequences of motion and determined when a sequence of frames constitutes a good representation of the
 * motion that happened.
 *
 */
@Slf4j
public class MotionRecorder {
    private static final Duration DEFAULT_MAX_LENGTH = Duration.ofSeconds(10);
    private static final Duration DEFAULT_MIN_LENGTH = Duration.ofSeconds(2);
    private static final Duration MOTION_TAIL_DURATION = Duration.ofSeconds(1);

    //current sequence of frames
    private List<MotionFrame> sequence;

    private int fps;
    private final DetectionSettings detectionSettings;

    private State state = State.RECORDING;

    public MotionRecorder(int fps, DetectionSettings detectionSettings) {
        this.fps = fps;
        this.detectionSettings = detectionSettings;
        sequence = new ArrayList<>();
    }

    public void submit(Mat ogFrame, MotionResult result){
        MotionFrame frame = buildFrame(ogFrame, result);

        //reset after finishing capturing a sequence
        if(this.state == State.CAPTURED){
            log.trace("Clearing sequence and starting over");
            sequence.clear();
        }

        this.state = State.RECORDING;

        sequence.add(frame);

        if(atEndOfSequence()){
            if(isSequenceValid()){
                this.state = State.CAPTURED;
                return;
            }

            log.trace("Ditching invalid sequence");
            sequence.clear();
            this.state = State.RECORDING;
        }
    }

    void setFps(int fps){
        this.fps = fps;
        this.state = State.WAITING;
        this.sequence = new ArrayList<>(getFramesOverDuration(DEFAULT_MAX_LENGTH) + 1);
    }

    /**
     * Return the finished motion sequence if we are done with a sequence, otherwise empty
     */
    public Optional<List<MotionFrame>> getSequence(){
        return state == State.CAPTURED
                ? Optional.of(sequence)
                : Optional.empty();
    }

    public boolean isRecording(){
        return state == State.RECORDING;
    }

    public void reset(){
        log.trace("Resetting");
        sequence.clear();
        state = State.WAITING;
    }

    private boolean isSequenceValid(){
        Duration estimatedLength = estimateSequenceDuration();
        return estimatedLength.compareTo(DEFAULT_MIN_LENGTH) > 0;
    }

    /**
     * Are we at the end of a sequence of motion that we want to send off
     */
    private boolean atEndOfSequence(){
        Duration estimatedDuration = estimateSequenceDuration();
        if(log.isTraceEnabled()){
            log.trace("Checking for end of sequence, estimated duration: {}", estimatedDuration);
        }

        if(estimatedDuration.compareTo(DEFAULT_MAX_LENGTH) > 0){
            log.trace("Estimated duration is > threshold, ending sequence");
            return true;
        }

        return hasMotionEnded();
    }

    private boolean hasMotionEnded(){
        //number of frames we should check for motion at the end of the sequence
        int tailFrames = getFramesOverDuration(MOTION_TAIL_DURATION);

        //dont check if motion has ended too early
        if(sequence.size() < tailFrames * 4){
            return false;
        }

        log.trace("Checking {} frames to see if motion has ended", tailFrames);

        //Require motion in 50%??? of frames in the tail we're checking
        //so for example at 15 fps, we'd want motion over the threshold in 7 frames
        int framesWithMotion = 0;
        for(int i = sequence.size() - 1; i > sequence.size() - (tailFrames + 1); i--){
            if(sequence.get(i).getMotionArea() > detectionSettings.getDiffSizeThreshold()){
                framesWithMotion++;
            }
        }

        log.trace("Found {} frames with motion", framesWithMotion);

        int desiredThreshold = Math.round(tailFrames / 2.f);
        return framesWithMotion > desiredThreshold;
    }

    private int getFramesOverDuration(Duration duration){
        return (int) duration.toMillis() * fps * 1000;
    }

    private Duration estimateSequenceDuration(){
        float seconds = sequence.size() / (float) fps;
        float ms = seconds * 1000;

        return Duration.ofMillis(Math.round(ms));
    }

    private MotionFrame buildFrame(Mat ogFrame, MotionResult result){
        return new MotionFrame(ogFrame.clone(), result.getTotalArea());
    }

    private enum State {
        WAITING,
        RECORDING,
        CAPTURED
    }
}
