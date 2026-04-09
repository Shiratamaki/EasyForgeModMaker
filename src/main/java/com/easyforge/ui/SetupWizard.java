package com.easyforge.ui;

import com.easyforge.EasyForgeApp;
import com.easyforge.core.ProjectSaver;
import com.easyforge.model.*;
import com.easyforge.util.I18n;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;

public class SetupWizard {
    private final Stage stage;
    private final Preferences prefs;

    public SetupWizard() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("EasyForge - 首次启动向导");
        stage.setWidth(500);
        stage.setHeight(400);
        prefs = Preferences.userNodeForPackage(com.easyforge.EasyForgeApp.class);
    }

    public void showAndWait() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("欢迎使用 EasyForge Mod Maker");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label langLabel = new Label("选择语言 / Choose Language:");
        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll("简体中文", "English");
        langCombo.setValue("简体中文");
        langCombo.valueProperty().addListener((obs, old, val) -> {
            if ("English".equals(val)) I18n.setLocale(Locale.US);
            else I18n.setLocale(Locale.SIMPLIFIED_CHINESE);
        });

        Label themeLabel = new Label("选择主题:");
        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("明亮主题", "暗黑主题", "樱花主题");
        themeCombo.setValue("明亮主题");

        Button openExampleBtn = new Button("打开示例项目");
        Button finishBtn = new Button("完成");

        stage.setOnCloseRequest(event -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("确认退出");
            confirm.setHeaderText("是否退出程序？");
            confirm.setContentText("如果您尚未完成设置，可以继续设置。");
            ButtonType exitBtn = new ButtonType("退出程序");
            ButtonType continueBtn = new ButtonType("继续设置");
            confirm.getButtonTypes().setAll(exitBtn, continueBtn);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == exitBtn) {
                Platform.exit();
            } else {
                event.consume();
            }
        });

        finishBtn.setOnAction(e -> {
            saveSettings(langCombo, themeCombo);
            stage.close();
        });

        openExampleBtn.setOnAction(e -> {
            saveSettings(langCombo, themeCombo);
            createAndOpenExampleProject();
        });

        root.getChildren().addAll(title, langLabel, langCombo, themeLabel, themeCombo, openExampleBtn, finishBtn);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void saveSettings(ComboBox<String> langCombo, ComboBox<String> themeCombo) {
        String selectedTheme = themeCombo.getValue();
        String themeKey;
        if ("暗黑主题".equals(selectedTheme)) {
            themeKey = "DARK";
        } else if ("樱花主题".equals(selectedTheme)) {
            themeKey = "SAKURA";
        } else {
            themeKey = "LIGHT";
        }
        prefs.put("theme", themeKey);
        prefs.put("language", langCombo.getValue());
        prefs.putBoolean("setupCompleted", true);
        System.out.println("Saved theme: " + themeKey); // 调试输出
    }

    private void createAndOpenExampleProject() {
        try {
            Path tempDir = Files.createTempDirectory("EasyForgeExample");
            Path projectDir = tempDir.resolve("ExampleMod");
            Files.createDirectories(projectDir);

            ModProject project = new ModProject();
            project.setModId("example_mod");
            project.setModName("Example Mod");
            project.setAuthor("EasyForge");
            project.setMinecraftVersion("1.20.1");
            project.setForgeVersion("47.4.18");
            project.setOutputPath(projectDir.toString());
            project.setMainClassPackage("com.example.example_mod");

            ItemData ruby = new ItemData();
            ruby.setId("ruby");
            ruby.setDisplayName("Ruby");
            ruby.setMaxStackSize(64);
            project.getItems().add(ruby);

            RecipeData recipe = new RecipeData();
            recipe.setOutputItem("example_mod:ruby");
            recipe.setOutputCount(1);
            recipe.setShaped(false);
            Map<Character, String> keys = new HashMap<>();
            keys.put('A', "minecraft:diamond");
            recipe.setKeys(keys);
            project.getRecipes().add(recipe);

            ProjectSaver.save(project);
            generatePlaceholderTexture(projectDir.resolve("src/main/resources/assets/example_mod/textures/item/ruby.png"), "R");

            stage.close();
            EasyForgeApp.openProject(projectDir.toFile());

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("创建示例项目失败: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private void generatePlaceholderTexture(Path output, String text) throws IOException {
        Files.createDirectories(output.getParent());
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(text, 4, 12);
        g.dispose();
        ImageIO.write(img, "png", output.toFile());
    }
}