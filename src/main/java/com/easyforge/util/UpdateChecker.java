package com.easyforge.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class UpdateChecker {
    private static final String REPO_OWNER = "Shiratamaki";   // 请修改为您的GitHub用户名
    private static final String REPO_NAME = "EasyForgeModMaker";
    private static final String API_URL = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/releases/latest";
    public static final String CURRENT_VERSION = "1.0.0";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void checkForUpdate(UpdateCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder().url(API_URL).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        callback.onError("无法获取更新信息: HTTP " + response.code());
                        return;
                    }
                    String body = response.body().string();
                    JsonNode json = mapper.readTree(body);
                    String latestVersion = json.get("tag_name").asText().replace("v", "");
                    String releaseNotes = json.has("body") ? json.get("body").asText() : "";

                    // 获取直接下载链接（根据操作系统选择 asset）
                    JsonNode assets = json.get("assets");
                    String downloadUrl = null;
                    String os = System.getProperty("os.name").toLowerCase();
                    String targetSuffix = "";
                    if (os.contains("win")) targetSuffix = ".exe";
                    else if (os.contains("mac")) targetSuffix = ".dmg";
                    else targetSuffix = ".deb";

                    if (assets != null && assets.isArray()) {
                        for (JsonNode asset : assets) {
                            String name = asset.get("name").asText();
                            if (name.endsWith(targetSuffix)) {
                                downloadUrl = asset.get("browser_download_url").asText();
                                break;
                            }
                        }
                    }
                    if (downloadUrl == null) {
                        // 回退到 Release 页面 URL
                        downloadUrl = json.get("html_url").asText();
                    }

                    if (isNewerVersion(latestVersion, CURRENT_VERSION)) {
                        callback.onUpdateAvailable(latestVersion, downloadUrl, releaseNotes);
                    } else {
                        callback.onNoUpdate();
                    }
                }
            } catch (IOException e) {
                LogUtil.error("检查更新失败", e);
                callback.onError("检查更新失败: " + e.getMessage());
            }
        }).start();
    }

    private static boolean isNewerVersion(String latest, String current) {
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");
        int length = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            int l = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            int c = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            if (l != c) return l > c;
        }
        return false;
    }

    public interface UpdateCallback {
        void onUpdateAvailable(String version, String downloadUrl, String releaseNotes);
        void onNoUpdate();
        void onError(String message);
    }
}