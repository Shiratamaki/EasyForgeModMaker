package com.easyforge;

import com.easyforge.controller.MainController;
import com.easyforge.ui.SetupWizard;
import com.easyforge.ui.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;

public class EasyForgeApp extends Application {

    private static Stage primaryStage;
    private static MainController mainController;

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("应用程序启动失败，请检查错误信息。");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            EasyForgeApp.primaryStage = primaryStage;

            Preferences prefs = Preferences.userNodeForPackage(EasyForgeApp.class);
            if (!prefs.getBoolean("setupCompleted", false)) {
                SetupWizard wizard = new SetupWizard();
                wizard.showAndWait();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();
            mainController = loader.getController();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("EasyForge Mod Maker");
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);

            // 使用 getResourceAsStream 加载窗口图标
            try (InputStream iconStream = getClass().getResourceAsStream("/icons/icon.png")) {
                if (iconStream != null) {
                    Image icon = new Image(iconStream);
                    primaryStage.getIcons().add(icon);
                } else {
                    System.err.println("窗口图标未找到: /icons/icon.png");
                }
            } catch (Exception e) {
                System.err.println("无法加载窗口图标: " + e.getMessage());
            }

            // 应用保存的主题
            String themePref = prefs.get("theme", "LIGHT");
            ThemeManager.Theme theme;
            switch (themePref) {
                case "DARK": theme = ThemeManager.Theme.DARK; break;
                case "SAKURA": theme = ThemeManager.Theme.SAKURA; break;
                default: theme = ThemeManager.Theme.LIGHT;
            }
            mainController.setTheme(theme);

            primaryStage.show();
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("启动过程中发生错误，请检查控制台输出。");
            Platform.exit();
        }
    }

    public static void openProject(File projectDir) {
        Platform.runLater(() -> {
            if (mainController != null) {
                mainController.loadProject(projectDir);
                if (primaryStage != null) {
                    primaryStage.toFront();
                }
            } else {
                try {
                    new EasyForgeApp().start(new Stage());
                    Platform.runLater(() -> {
                        if (mainController != null) {
                            mainController.loadProject(projectDir);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}