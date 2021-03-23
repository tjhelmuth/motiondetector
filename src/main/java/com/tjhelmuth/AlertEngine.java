package com.tjhelmuth;

import com.tjhelmuth.dispatcher.AlertDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Slf4j
public class AlertEngine implements MotionListener {
    //TODO: make configurable
    private static final Executor THREAD_POOL = Executors.newFixedThreadPool(ForkJoinPool.getCommonPoolParallelism());

    private final List<AlertDispatcher> dispatchers;

    private Set<MediaType> mediaTypes;

    public AlertEngine(List<AlertDispatcher> dispatchers) {
        this.dispatchers = dispatchers;
        if(dispatchers == null || dispatchers.isEmpty()){
            throw new IllegalArgumentException("Dispatchers cannot be null. It doesn't make any sense");
        }

        mediaTypes = dispatchers.stream()
                .map(AlertDispatcher::getMediaType)
                .collect(Collectors.toSet());
    }

    @Override
    public void onMotionDetected(List<MotionFrame> ogSequence) {
        final List<MotionFrame> sequence = new ArrayList<>(ogSequence);
        THREAD_POOL.execute(() -> {
            Instant timestamp = Instant.now();
            log.info("Motion detected at {}, {} frames recorded", DateTimeFormatter.ISO_INSTANT.format(timestamp), sequence.size());

            if(mediaTypes.contains(MediaType.IMAGE)){
                byte[] image = getImageForAlert(sequence);
                dispatchers.stream()
                        .filter(d -> d.getMediaType() == MediaType.IMAGE)
                        .forEach(d -> d.dispatch(timestamp, image));
            }
        });
    }

    private byte[] getImageForAlert(List<MotionFrame> sequence){
        MotionFrame maxMotion = null;
        for(MotionFrame frame : sequence){
            if(maxMotion == null || frame.getMotionArea() > maxMotion.getMotionArea()){
                maxMotion = frame;
            }
        }
        return Util.matToByte(maxMotion.getFrame());
    }

    public enum MediaType {
        IMAGE,
        VIDEO
    }
}
