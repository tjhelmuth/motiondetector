package com.tjhelmuth;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@UtilityClass
public class Util {
    Frame grabQuiet(FFmpegFrameGrabber grabber){
        try {
            return grabber.grabImage();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    Frame grabQuiet(FrameGrabber grabber){
        try {
            return grabber.grab();
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    Function<FrameGrabber, Frame> grabberFn(FrameGrabber grabber){
        if(grabber instanceof FFmpegFrameGrabber){
            return (g) -> grabQuiet((FFmpegFrameGrabber) g);
        }

        return Util::grabQuiet;
    }

    private static final Java2DFrameConverter IMG_CONVERTER = new Java2DFrameConverter();
    private static final OpenCVFrameConverter MAT_CONVERTER = new OpenCVFrameConverter.ToMat();

    @SneakyThrows
    byte[] matToByte(Mat frame){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Frame rawFrame = MAT_CONVERTER.convert(frame);
        BufferedImage image = IMG_CONVERTER.convert(rawFrame);
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    String rectToString(Rect rect){
        return String.format("[%d,%d | %d x %d]", rect.x(), rect.y(), rect.width(), rect.height());
    }

    String rectsToString(List<Rect> rects){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rects.size(); i++) {
            sb.append(rectToString(rects.get(i)));
            if(i < rects.size() - 1){
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
