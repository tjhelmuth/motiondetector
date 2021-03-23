package com.tjhelmuth.config;

import com.tjhelmuth.Webcam;
import com.tjhelmuth.dispatcher.AlertDispatcher;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.stream.Collectors;

@Value @Builder @Jacksonized
public class AppConfig {
    List<WebcamConfig> webcams;
    List<DispatcherConfig> dispatchers;

    public List<Webcam> buildWebcams(){
        return webcams.stream()
                .map(WebcamConfig::build)
                .collect(Collectors.toList());
    }

    public List<AlertDispatcher> buildDispatchers(){
        return dispatchers.stream()
                .map(DispatcherConfig::build)
                .collect(Collectors.toList());
    }
}
