package com.tjhelmuth.config;

import com.tjhelmuth.Authorization;
import com.tjhelmuth.ConnectionSettings;
import com.tjhelmuth.Webcam;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Value @Builder @Jacksonized
public class WebcamConfig {
    String host;
    String protocol;
    String username;
    String password;

    public Webcam build(){
        if(StringUtils.isBlank(host)){
            throw new IllegalArgumentException("host for webcam cannot be blank");
        }

        Authorization auth = StringUtils.isNotBlank(username)
                ? Authorization.usernamePassword(username, password)
                : null;

        var settingsBuilder = ConnectionSettings.builder(host);
        Optional.ofNullable(protocol).ifPresent(settingsBuilder::protocol);
        Optional.ofNullable(auth).ifPresent(settingsBuilder::authorization);

        return Webcam.of(settingsBuilder.build());
    }
}
