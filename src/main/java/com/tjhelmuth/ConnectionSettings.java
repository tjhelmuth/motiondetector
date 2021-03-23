package com.tjhelmuth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value @AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder(builderMethodName = "hiddenBuilder")
public class ConnectionSettings {
    Authorization authorization;
    String host;
    @Builder.Default
    String protocol = "http";

    public static ConnectionSettingsBuilder builder(String host){
        if(StringUtils.isBlank(StringUtils.strip(host))){
            throw new IllegalArgumentException("IP cannot be blank");
        }

        return hiddenBuilder()
                .host(host);
    }

    public String toUrl(){
        String hostPart = authorization == null ? host : String.format("%s%s", authorization.getUrlPart(), host);
        return String.format("%s://%s", protocol, hostPart);
    }

    public static class ConnectionSettingsBuilder {
        public ConnectionSettingsBuilder authorization(String username, String password){
            this.authorization = Authorization.usernamePassword(username, password);
            return this;
        }

        public ConnectionSettingsBuilder authorization(Authorization authorization){
            this.authorization = authorization;
            return this;
        }
    }
}
