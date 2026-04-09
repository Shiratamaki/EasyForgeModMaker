package com.easyforge.ui;

import javafx.scene.Scene;

public class ThemeManager {
    public enum Theme { LIGHT, DARK, SAKURA }

    public static void applyTheme(Scene scene, Theme theme) {
        if (scene == null) {
            System.err.println("ThemeManager.applyTheme: scene is null");
            return;
        }
        scene.getStylesheets().clear();
        String themeCss;
        switch (theme) {
            case DARK: themeCss = "/css/dark-theme.css"; break;
            case SAKURA: themeCss = "/css/sakura-theme.css"; break;
            default: themeCss = "/css/light-theme.css";
        }
        // 加载基础样式
        var modernUrl = ThemeManager.class.getResource("/css/modern.css");
        if (modernUrl != null) {
            scene.getStylesheets().add(modernUrl.toExternalForm());
            System.out.println("Loaded modern.css");
        } else {
            System.err.println("modern.css not found!");
        }
        // 加载主题样式
        var themeUrl = ThemeManager.class.getResource(themeCss);
        if (themeUrl != null) {
            scene.getStylesheets().add(themeUrl.toExternalForm());
            System.out.println("Loaded " + themeCss);
        } else {
            System.err.println("Theme CSS not found: " + themeCss);
        }
    }
}