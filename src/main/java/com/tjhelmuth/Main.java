package com.tjhelmuth;

import com.tjhelmuth.config.AppConfig;
import com.tjhelmuth.config.ConfigLoader;
import com.tjhelmuth.dispatcher.AlertDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;


@Slf4j
public class Main {
    public static void main(String[] args) {
        AppConfig configuration = ConfigLoader.loadConfiguration();
        log.info("Configuration loaded: {}", configuration);

        List<ImageMask> masks = List.of(ImageMask.of(2, 4));
        boolean isMaskDebugging = Arrays.stream(args).anyMatch(arg -> StringUtils.containsIgnoreCase(arg, "--maskdebug"));

        Webcam webcam = configuration.buildWebcams().get(0);
        List<AlertDispatcher> dispatchers = configuration.buildDispatchers();

        if(isMaskDebugging){
            doMaskDebug(webcam, masks);
        } else {
            doMotionDetection(webcam, masks, dispatchers);
        }
    }

    private static void doMotionDetection(Webcam webcam, List<ImageMask> masks, List<AlertDispatcher> dispatchers){
        AlertEngine alertEngine = new AlertEngine(dispatchers);

        DetectionSettings settings = DetectionSettings.builder()
                .masks(masks)
                .build();

        MotionDetectionEngine detectionEngine = new MotionDetectionEngine(settings, List.of(alertEngine));
        webcam.registerListener(detectionEngine);

        webcam.start();
    }

    private static void doMaskDebug(Webcam webcam, List<ImageMask> masks){
        log.info("Generating mask debug image");
        webcam.registerListener(new MaskDebugger(masks, () -> {
            webcam.stop();
            System.exit(0);
        }));
        webcam.start();
    }
}
