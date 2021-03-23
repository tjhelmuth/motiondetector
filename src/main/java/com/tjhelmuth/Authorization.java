package com.tjhelmuth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value @AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Authorization {
    String username;
    String password;

    public static Authorization usernamePassword(String username, String password){
        if(StringUtils.isBlank(username)){
            throw new IllegalArgumentException("Username cannot be blank");
        }

        return new Authorization(StringUtils.strip(username), StringUtils.strip(password));
    }

    /**
     * Get the part of the url that is username:password@ or username@
     * @return
     */
    String getUrlPart(){
        String prefix = StringUtils.isBlank(password) ? username : String.format("%s:%s", username, password);
        return prefix + "@";
    }
}
