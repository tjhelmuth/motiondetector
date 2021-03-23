package com.tjhelmuth.dispatcher;

import java.util.Map;

public interface DispatcherProvider {
    String getType();

    AlertDispatcher create(Map<String, Object> configuration);
}
