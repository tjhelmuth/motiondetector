package com.tjhelmuth.dispatcher;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class DiscordDispatcherProvider implements DispatcherProvider {
    public static final String TYPE = "discord";
    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public AlertDispatcher create(Map<String, Object> configuration) {
        String url = (String) configuration.get("url");
        if(StringUtils.isBlank(url)){
            throw new IllegalArgumentException("URL is required for discord dispatcher");
        }
        return new DiscordDispatcher(url);
    }
}
