package com.tjhelmuth.config;

import com.tjhelmuth.dispatcher.AlertDispatcher;
import com.tjhelmuth.dispatcher.Dispatchers;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Value @Builder @Jacksonized
public class DispatcherConfig {
    String type;

    Map<String, Object> config;

    public AlertDispatcher build(){
        return Dispatchers.ofType(type, config)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Dispatcher %s", type)));
    }
}
