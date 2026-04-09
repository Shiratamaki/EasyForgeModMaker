package com.easyforge.core.downloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MirrorHelper {
    private static final String[] MAVEN_MIRRORS = {
            "https://bmclapi2.bangbang93.com/maven/",
            "https://maven.aliyun.com/repository/public/",
            "https://repo.huaweicloud.com/repository/maven/",
            "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/",
            "https://maven.minecraftforge.net/"
    };

    public static String getFastestMaven() {
        for (String url : MAVEN_MIRRORS) {
            if (isReachable(url)) return url;
        }
        return MAVEN_MIRRORS[MAVEN_MIRRORS.length - 1];
    }

    private static boolean isReachable(String urlStr) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(2000);
            conn.setRequestMethod("HEAD");
            int code = conn.getResponseCode();
            return code == 200 || code == 403;
        } catch (IOException e) {
            return false;
        }
    }
}