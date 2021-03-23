package com.tjhelmuth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

@UtilityClass
@Slf4j
public class ConfigLoader {
    public static final String DEFAULT_FILE_LOCATION = "config.yaml";
    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    public AppConfig loadConfiguration(){
        File configFile = loadConfigFile();
        AppConfig config = null;
        try {
            config = YAML.readValue(configFile, AppConfig.class);
        } catch (IOException e) {
            log.error("Error reading configuration file", e);
            throw new RuntimeException(e);
        }

        return config;
    }

    private File loadConfigFile(){
        File resolvedConfigurationFile = null;

        String customFileLocation = System.getProperty("config.location");
        if(StringUtils.isNotBlank(customFileLocation)){
            File customFile = new File(customFileLocation);
            if(customFile.exists()){
                resolvedConfigurationFile = customFile;
            }
        }

        if(resolvedConfigurationFile == null){
            File defaultFile = new File(DEFAULT_FILE_LOCATION);
            if(defaultFile.exists()){
                resolvedConfigurationFile = defaultFile;
            }
        }

        if(resolvedConfigurationFile == null){
            log.error("Unable to find configuration file. See readme.");
            throw new RuntimeException("Unable to find configuration file");
        }

        return resolvedConfigurationFile;
    }
}
