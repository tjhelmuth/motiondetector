package com.tjhelmuth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class CaptureThread extends Thread {
    @Getter
    FrameGrabber grabber;

    Function<FrameGrabber, Frame> grabFn;

    CopyOnWriteArrayList<VideoSourceListener> listeners;

    CaptureThread(FrameGrabber grabber, Function<FrameGrabber, Frame> grabFn, CopyOnWriteArrayList<VideoSourceListener> listeners) {
        if(ObjectUtils.anyNull(grabFn, grabber)){
            throw new IllegalArgumentException("Frame Grabber and Grab Function cannot be null");
        }

        this.grabber = grabber;
        this.grabFn = grabFn;
        this.listeners = listeners;
    }


    @Override
    public void run() {
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            log.error("Uh Oh", e);
        }

        log.info("Starting video capture with configuration: {}x{} @ {} fps", grabber.getImageWidth(), grabber.getImageHeight(), grabber.getFrameRate());

        boolean firstFrame = true;
        while(!this.isInterrupted()){
            Frame frame = nextFrame();

            if(firstFrame){
                listeners.forEach(l -> l.onConfiguration(getVideoInfo()));
                firstFrame = false;
            }

            if(frame == null){
                listeners.forEach(VideoSourceListener::onLastFrame);
                this.interrupt();
            }

            listeners.forEach(l -> l.onFrame(frame));
        }
    }

    /**
     * Returns information about the video stream
     * @return info if we are capturing, otherwise empty
     */
    VideoInfo getVideoInfo(){
        int width = grabber.getImageWidth();
        int height = grabber.getImageHeight();
        int fps = (int) grabber.getFrameRate();

        return new VideoInfo(width, height, fps);
    }

    void registerListener(VideoSourceListener listener){
        listeners.add(listener);
    }

    void stopCapturing(){
        try {
            grabber.stop();
            grabber.release();
        } catch (FrameGrabber.Exception e) {
            log.error("Error stopping capture", e);
        } finally {
            this.interrupt();
        }
    }

    private Frame nextFrame(){
        return grabFn.apply(grabber);
    }
}
