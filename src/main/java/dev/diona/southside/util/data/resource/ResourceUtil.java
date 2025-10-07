package dev.diona.southside.util.data.resource;

import net.minecraft.util.ResourceLocation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author ChengFeng
 * @since 2024/8/13
 **/
public class ResourceUtil {
    public static ResourceLocation getResource(String fileName, ResourceType type) {
        return new ResourceLocation(
                "southside/" +
                        switch (type) {
                            case FONT -> "font/";
                            case ICON -> "icon/";
                            case IMAGE -> "image/";
                            case VIDEO -> "video/";
                        } +
                        fileName
        );
    }

    public static InputStream getResourceAsStream(String fileName, ResourceType type) {
        try {
            String location = "/assets/minecraft/southside/" +
                    switch (type) {
                        case FONT -> "fonts/";
                        case ICON -> "icon/";
                        case IMAGE -> "image/";
                        case VIDEO -> "video/";
                    } +
                    fileName;
            return ResourceUtil.class.getResourceAsStream(location);
        } catch (Exception e) {
            return null;
        }
    }
}
