package com.tjhelmuth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
@Getter
public class Webcam implements VideoSource {
    @Getter(AccessLevel.NONE)
    final CopyOnWriteArrayList<VideoSourceListener> listeners = new CopyOnWriteArrayList<>();

    final ConnectionSettings connectionSettings;

    @Getter(AccessLevel.NONE)
    volatile CaptureThread thread = null;

    public static Webcam of(ConnectionSettings connectionSettings){
        return new Webcam(connectionSettings);
    }

    @Override
    public VideoSource registerListener(VideoSourceListener listener) {
        this.listeners.add(listener);
        if(this.thread != null){
            this.thread.registerListener(listener);
        }
        return this;
    }

    @Override
    public void start() {
        String url = connectionSettings.toUrl();

        log.info("Opening webcam at {}", url);

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);
        CaptureThread thread = new CaptureThread(grabber, Util.grabberFn(grabber), listeners);

        thread.start();
        this.thread = thread;
    }

    @Override
    public void stop() {
        this.thread.stopCapturing();
    }

}
