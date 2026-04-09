package com.easyforge.util;

import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 图片加载器 - 支持异步加载、缓存（LRU + 弱引用）、自动取消未完成请求
 */
public class ImageLoader {
    private static final Logger LOGGER = Logger.getLogger(ImageLoader.class.getName());
    private static final int MAX_CACHE_SIZE = 100;  // 最大缓存图片数量
    private static final Map<String, WeakReference<Image>> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<Image>> PENDING = new ConcurrentHashMap<>();

    /**
     * 异步加载图片（带缓存）
     * @param url 图片 URL（文件路径或网络地址）
     * @param width 目标宽度（<=0 表示原始宽度）
     * @param height 目标高度（<=0 表示原始高度）
     * @param callback 加载完成回调（在 JavaFX 线程执行）
     */
    public static void loadImageAsync(String url, int width, int height, Consumer<Image> callback) {
        // 检查缓存
        WeakReference<Image> ref = CACHE.get(url);
        if (ref != null) {
            Image cached = ref.get();
            if (cached != null && !cached.isError()) {
                Platform.runLater(() -> callback.accept(cached));
                return;
            } else {
                CACHE.remove(url);
            }
        }

        // 检查是否已有进行中的请求
        if (PENDING.containsKey(url)) {
            PENDING.get(url).thenAccept(img -> Platform.runLater(() -> callback.accept(img)));
            return;
        }

        // 新请求
        CompletableFuture<Image> future = CompletableFuture.supplyAsync(() -> {
            try {
                Image img;
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    img = new Image(url, width, height, true, true);
                } else if (url.startsWith("file:")) {
                    img = new Image(url, width, height, true, true);
                } else {
                    File file = new File(url);
                    if (file.exists()) {
                        img = new Image(file.toURI().toString(), width, height, true, true);
                    } else {
                        // 尝试从 classpath 加载
                        InputStream is = ImageLoader.class.getResourceAsStream(url);
                        if (is != null) {
                            img = new Image(is, width, height, true, true);
                        } else {
                            throw new IllegalArgumentException("Image not found: " + url);
                        }
                    }
                }
                return img;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load image: " + url, e);
                return null;
            }
        });

        future.thenAccept(img -> {
            PENDING.remove(url);
            if (img != null && !img.isError()) {
                // 缓存管理：限制大小
                if (CACHE.size() >= MAX_CACHE_SIZE) {
                    // 移除最早的一个（近似 LRU，简单实现）
                    String firstKey = CACHE.keySet().iterator().next();
                    CACHE.remove(firstKey);
                }
                CACHE.put(url, new WeakReference<>(img));
            }
            Platform.runLater(() -> callback.accept(img));
        });

        PENDING.put(url, future);
    }

    /**
     * 取消指定 URL 的待加载任务（如果尚未完成）
     */
    public static void cancelPending(String url) {
        CompletableFuture<Image> future = PENDING.remove(url);
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
    }

    /**
     * 清空缓存
     */
    public static void clearCache() {
        CACHE.clear();
        // 取消所有待加载任务
        PENDING.values().forEach(f -> f.cancel(true));
        PENDING.clear();
    }

    /**
     * 同步加载图片（不推荐在 UI 线程使用）
     */
    public static Image loadImageSync(String url, int width, int height) {
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return new Image(url, width, height, true, true);
            } else if (url.startsWith("file:")) {
                return new Image(url, width, height, true, true);
            } else {
                File file = new File(url);
                if (file.exists()) {
                    return new Image(file.toURI().toString(), width, height, true, true);
                } else {
                    InputStream is = ImageLoader.class.getResourceAsStream(url);
                    if (is != null) {
                        return new Image(is, width, height, true, true);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load image sync: " + url, e);
        }
        return null;
    }
}