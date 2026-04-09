package com.easyforge;

import com.easyforge.controller.MainController;
import com.easyforge.core.generator.ProjectGenerator;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class EasyForgeApp extends Application {

    private static Stage primaryStage;
    private static MainController mainController;

    @Override
    public void start(Stage primaryStage) throws Exception {
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

        // 设置窗口图标
        try {
            InputStream iconStream = getClass().getResourceAsStream("/icons/icon.png");
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                primaryStage.getIcons().add(icon);
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

        // 处理命令行参数（必须在界面显示后）
        handleCommandLineArgs();
    }

    private void handleCommandLineArgs() {
        Parameters params = getParameters();
        List<String> args = params.getRaw();
        if (args.isEmpty()) return;

        String firstArg = args.get(0);
        if ("--open".equals(firstArg) && args.size() > 1) {
            String projectPath = args.get(1);
            File projectFile = new File(projectPath);
            if (projectFile.exists()) {
                Platform.runLater(() -> mainController.loadProject(projectFile));
            } else {
                System.err.println("项目路径不存在: " + projectPath);
            }
        } else if ("--generate".equals(firstArg) && args.size() > 1) {
            String configPath = args.get(1);
            generateFromConfig(configPath);
            Platform.exit();
        } else if ("--help".equals(firstArg)) {
            printHelp();
            Platform.exit();
        }
    }

    private void generateFromConfig(String configPath) {
        try {
            Path path = Path.of(configPath);
            String json = Files.readString(path);
            // 简单解析 JSON（可使用 Gson，这里简化）
            // 实际应解析为 Map，然后调用 ProjectGenerator.generate
            System.out.println("从配置文件生成项目: " + configPath);
            // 示例：调用生成器
            // Map<String, String> params = new Gson().fromJson(json, Map.class);
            // new ProjectGenerator().generate(params);
        } catch (Exception e) {
            System.err.println("生成项目失败: " + e.getMessage());
        }
    }

    private void printHelp() {
        System.out.println("EasyForge Mod Maker 命令行用法:");
        System.out.println("  --open <项目路径>    打开指定项目");
        System.out.println("  --generate <配置文件> 从JSON配置文件生成项目");
        System.out.println("  --help               显示此帮助信息");
    }

    public static void openProject(File projectDir) {
        Platform.runLater(() -> {
            if (mainController != null) {
                mainController.loadProject(projectDir);
                if (primaryStage != null) primaryStage.toFront();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}