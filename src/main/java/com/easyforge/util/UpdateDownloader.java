package com.easyforge.util;

import javafx.application.Platform;
import javafx.concurrent.Task;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class UpdateDownloader {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .followRedirects(true)
            .build();

    /**
     * 下载文件到临时目录，并在下载完成后打开/执行
     * @param downloadUrl 直接下载链接（GitHub Release 的 asset URL）
     * @param onProgress 进度回调 (已下载字节, 总字节)
     * @param onComplete 下载完成回调，参数为下载的文件
     * @param onError 错误回调
     */
    public static void downloadAndInstall(String downloadUrl,
                                          Consumer<Double> onProgress,
                                          Consumer<File> onComplete,
                                          Consumer<String> onError) {
        Task<File> task = new Task<>() {
            @Override
            protected File call() throws Exception {
                Request request = new Request.Builder().url(downloadUrl).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Download failed: " + response.code());
                    }
                    ResponseBody body = response.body();
                    if (body == null) throw new IOException("Empty response body");

                    long contentLength = body.contentLength();
                    InputStream is = body.byteStream();
                    // 创建临时文件，保留扩展名
                    String urlPath = downloadUrl;
                    String ext = "";
                    if (urlPath.contains(".")) {
                        ext = urlPath.substring(urlPath.lastIndexOf('.'));
                        if (ext.length() > 5) ext = ".tmp";
                    } else {
                        ext = ".tmp";
                    }
                    Path tempFile = Files.createTempFile("EasyForgeUpdate_", ext);
                    File outputFile = tempFile.toFile();
                    outputFile.deleteOnExit();

                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[8192];
                        long totalRead = 0;
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            if (isCancelled()) {
                                outputFile.delete();
                                throw new InterruptedException("Download cancelled");
                            }
                            fos.write(buffer, 0, len);
                            totalRead += len;
                            if (contentLength > 0) {
                                final double progress = (double) totalRead / contentLength;
                                updateProgress(progress, 1.0);
                                if (onProgress != null) {
                                    Platform.runLater(() -> onProgress.accept(progress));
                                }
                            }
                        }
                    }
                    return outputFile;
                }
            }
        };

        task.setOnSucceeded(e -> {
            File downloaded = task.getValue();
            if (onComplete != null) onComplete.accept(downloaded);
        });
        task.setOnFailed(e -> {
            if (onError != null) onError.accept(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    /**
     * 根据操作系统打开安装文件
     * @param file 下载的安装文件
     * @return 是否成功启动
     */
    public static boolean openInstaller(File file) {
        if (!file.exists()) return false;
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                // Windows: 直接运行 .exe 或 .msi
                pb = new ProcessBuilder("cmd.exe", "/c", "start", "\"\"", file.getAbsolutePath());
            } else if (os.contains("mac")) {
                // macOS: 打开 .dmg
                pb = new ProcessBuilder("open", file.getAbsolutePath());
            } else {
                // Linux: 尝试用 xdg-open
                pb = new ProcessBuilder("xdg-open", file.getAbsolutePath());
            }
            pb.start();
            return true;
        } catch (IOException e) {
            LogUtil.error("打开安装文件失败", e);
            return false;
        }
    }
}