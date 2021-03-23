package com.tjhelmuth.dispatcher;

import com.tjhelmuth.AlertEngine.MediaType;

import java.time.Instant;

public interface AlertDispatcher {
    default MediaType getMediaType(){
        return MediaType.VIDEO;
    }

    void dispatch(Instant timestamp, byte[] media);
}
