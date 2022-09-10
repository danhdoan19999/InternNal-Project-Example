package com.nals.rw360.helpers;

import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public final class FileHelper {
    private static final int DEFAULT_RESIZE_WIDTH = 800;

    private FileHelper() {
    }

    public static void cropImage(final InputStream inputStream,
                                 final Path destPath,
                                 final int x1, final int x2,
                                 final int y1, final int y2)
        throws IOException {
        BufferedImage croppedImage = ImageIO.read(inputStream)
                                            .getSubimage(x1, y1, x2 - x1, y2 - y1);
        ImageIO.write(croppedImage, getExtension(destPath.getFileName().toString()), destPath.toFile());
    }

    public static void resizeImage(final InputStream inputStream, final Path destPath)
        throws IOException {
        resizeImage(inputStream, destPath, DEFAULT_RESIZE_WIDTH);
    }

    public static void resizeImage(final InputStream inputStream,
                                   final Path destPath,
                                   final int maxWidth)
        throws IOException {
        BufferedImage originImage = ImageIO.read(inputStream);

        int width = originImage.getWidth();
        int height = originImage.getHeight();

        float ratio = (float) width / height;

        if (width > maxWidth) {
            ratio = (float) maxWidth / width;
        }

        height = Math.round(height * ratio);
        width = Math.round(width * ratio);

        BufferedImage resizedImage = new BufferedImage(width, height, originImage.getType());

        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(originImage, 0, 0, null);
        graphics.dispose();

        ImageIO.write(resizedImage, getExtension(destPath.getFileName().toString()), destPath.toFile());
    }

    public static String getExtension(final String filename) {
        if (StringUtils.hasText(filename)) {
            int index = indexOfExtension(filename);
            return index == -1 ? "" : filename.substring(index + 1);
        }
        return filename;
    }

    public static int indexOfExtension(final String filename) {
        if (StringUtils.hasText(filename)) {
            int extensionPos = filename.lastIndexOf(46);
            int lastSeparator = indexOfLastSeparator(filename);
            return lastSeparator > extensionPos ? -1 : extensionPos;
        }
        return -1;
    }

    public static int indexOfLastSeparator(final String filename) {
        if (StringUtils.hasText(filename)) {
            int lastUnixPos = filename.lastIndexOf(47);
            int lastWindowsPos = filename.lastIndexOf(92);
            return Math.max(lastUnixPos, lastWindowsPos);
        }
        return -1;
    }

    public static String concatPath(final String... paths) {
        if (paths.length == 0) {
            return "";
        }

        return Path.of("", paths).toString();
    }
}
