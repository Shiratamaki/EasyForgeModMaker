package com.easyforge.ui;

import javafx.scene.Scene;

public class ThemeManager {
    public enum Theme { LIGHT, DARK, SAKURA }

    public static void applyTheme(Scene scene, Theme theme) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String themeCss;
        switch (theme) {
            case DARK: themeCss = "/css/dark-theme.css"; break;
            case SAKURA: themeCss = "/css/sakura-theme.css"; break;
            default: themeCss = "/css/light-theme.css";
        }
        // 使用 toExternalForm() 获取正确的 URL 字符串（支持 jar:file: 格式）
        var modernUrl = ThemeManager.class.getResource("/css/modern.css");
        var themeUrl = ThemeManager.class.getResource(themeCss);
        if (modernUrl != null) {
            scene.getStylesheets().add(modernUrl.toExternalForm());
        }
        if (themeUrl != null) {
            scene.getStylesheets().add(themeUrl.toExternalForm());
        }
    }
}