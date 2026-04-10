package com.easyforge.util;

import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageLoader {
    private static final Logger LOGGER = Logger.getLogger(ImageLoader.class.getName());
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<Image>> PENDING = new ConcurrentHashMap<>();

    public static void loadImageAsync(String resourcePath, int width, int height, Consumer<Image> callback) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            Platform.runLater(() -> callback.accept(null));
            return;
        }

        // 检查缓存
        if (CACHE.containsKey(resourcePath)) {
            Platform.runLater(() -> callback.accept(CACHE.get(resourcePath)));
            return;
        }

        // 防止重复加载
        if (PENDING.containsKey(resourcePath)) {
            PENDING.get(resourcePath).thenAccept(img -> Platform.runLater(() -> callback.accept(img)));
            return;
        }

        CompletableFuture<Image> future = CompletableFuture.supplyAsync(() -> {
            try (InputStream is = ImageLoader.class.getResourceAsStream(resourcePath)) {
                if (is == null) {
                    LOGGER.warning("Image not found: " + resourcePath);
                    return null;
                }
                Image img;
                if (width > 0 && height > 0) {
                    img = new Image(is, width, height, true, true);
                } else {
                    img = new Image(is);
                }
                return img;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load image: " + resourcePath, e);
                return null;
            }
        });

        future.thenAccept(img -> {
            PENDING.remove(resourcePath);
            if (img != null && !img.isError()) {
                CACHE.put(resourcePath, img);
            }
            Platform.runLater(() -> callback.accept(img));
        });

        PENDING.put(resourcePath, future);
    }

    public static void clearCache() {
        CACHE.clear();
        PENDING.values().forEach(f -> f.cancel(true));
        PENDING.clear();
    }
}