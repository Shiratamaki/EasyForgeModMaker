package com.easyforge.ui;

import com.easyforge.model.ModProject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class TextureImportDialog {

    private final ModProject project;
    private final List<TextureEntry> entries = new ArrayList<>();
    private ListView<TextureEntry> listView;
    private Stage stage;

    public TextureImportDialog(ModProject project) {
        this.project = project;
    }

    public void showAndWait() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("批量导入纹理");
        stage.setWidth(600);
        stage.setHeight(500);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label dragLabel = new Label("将图片文件拖拽到此区域，或点击下方按钮选择");
        dragLabel.setAlignment(Pos.CENTER);
        dragLabel.setStyle("-fx-border-color: #ccc; -fx-border-width: 2; -fx-border-style: dashed; -fx-padding: 20;");
        dragLabel.setMaxWidth(Double.MAX_VALUE);

        dragLabel.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                e.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            e.consume();
        });
        dragLabel.setOnDragDropped(e -> {
            List<File> files = e.getDragboard().getFiles();
            for (File file : files) {
                String name = file.getName();
                if (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg")) {
                    String id = name.substring(0, name.lastIndexOf('.'));
                    entries.add(new TextureEntry(file, id));
                }
            }
            refreshList();
            e.setDropCompleted(true);
            e.consume();
        });

        Button selectFilesBtn = new Button("选择图片文件");
        selectFilesBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg"));
            List<File> files = chooser.showOpenMultipleDialog(stage);
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    String id = name.substring(0, name.lastIndexOf('.'));
                    entries.add(new TextureEntry(file, id));
                }
                refreshList();
            }
        });

        listView = new ListView<>();
        listView.setCellFactory(lv -> new ListCell<TextureEntry>() {
            @Override
            protected void updateItem(TextureEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    ImageView preview = new ImageView(new Image(entry.file.toURI().toString(), 32, 32, true, true));
                    TextField idField = new TextField(entry.id);
                    idField.textProperty().addListener((obs, old, val) -> entry.id = val);
                    CheckBox importAsItem = new CheckBox("作为物品");
                    CheckBox importAsBlock = new CheckBox("作为方块");
                    importAsItem.setSelected(true);
                    box.getChildren().addAll(preview, idField, importAsItem, importAsBlock);
                    setGraphic(box);
                }
            }
        });
        listView.setPrefHeight(300);

        Button importBtn = new Button("导入");
        importBtn.setOnAction(e -> importTextures());

        Button cancelBtn = new Button("取消");
        cancelBtn.setOnAction(e -> stage.close());

        HBox btnBox = new HBox(10, importBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(dragLabel, selectFilesBtn, listView, btnBox);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void refreshList() {
        listView.setItems(javafx.collections.FXCollections.observableArrayList(entries));
    }

    private void importTextures() {
        if (project == null) {
            showAlert("错误", "没有打开的项目");
            return;
        }
        String modId = project.getModId();
        String projectPath = project.getOutputPath();
        for (TextureEntry entry : entries) {
            try {
                // 复制纹理文件到 item 目录（默认）
                File destDir = new File(projectPath, "src/main/resources/assets/" + modId + "/textures/item");
                if (!destDir.exists()) destDir.mkdirs();
                File destFile = new File(destDir, entry.id + ".png");
                Files.copy(entry.file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // 生成物品模型 JSON（如果勾选）
                if (entry.importAsItem) {
                    generateItemModelJson(projectPath, modId, entry.id);
                }
                if (entry.importAsBlock) {
                    generateBlockModelJson(projectPath, modId, entry.id);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                showAlert("错误", "导入失败: " + ex.getMessage());
            }
        }
        showAlert("成功", "已导入 " + entries.size() + " 个纹理");
        stage.close();
    }

    private void generateItemModelJson(String projectPath, String modId, String id) throws IOException {
        File modelDir = new File(projectPath, "src/main/resources/assets/" + modId + "/models/item");
        if (!modelDir.exists()) modelDir.mkdirs();
        File modelFile = new File(modelDir, id + ".json");
        String json = "{\n" +
                "  \"parent\": \"item/generated\",\n" +
                "  \"textures\": {\n" +
                "    \"layer0\": \"" + modId + ":item/" + id + "\"\n" +
                "  }\n" +
                "}";
        Files.write(modelFile.toPath(), json.getBytes());
    }

    private void generateBlockModelJson(String projectPath, String modId, String id) throws IOException {
        // 方块模型
        File modelDir = new File(projectPath, "src/main/resources/assets/" + modId + "/models/block");
        if (!modelDir.exists()) modelDir.mkdirs();
        File modelFile = new File(modelDir, id + ".json");
        String json = "{\n" +
                "  \"parent\": \"block/cube_all\",\n" +
                "  \"textures\": {\n" +
                "    \"all\": \"" + modId + ":block/" + id + "\"\n" +
                "  }\n" +
                "}";
        Files.write(modelFile.toPath(), json.getBytes());
        // 物品模型（方块物品）
        File itemModelDir = new File(projectPath, "src/main/resources/assets/" + modId + "/models/item");
        if (!itemModelDir.exists()) itemModelDir.mkdirs();
        File itemModelFile = new File(itemModelDir, id + ".json");
        String itemJson = "{\n" +
                "  \"parent\": \"" + modId + ":block/" + id + "\"\n" +
                "}";
        Files.write(itemModelFile.toPath(), itemJson.getBytes());
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private static class TextureEntry {
        File file;
        String id;
        boolean importAsItem = true;
        boolean importAsBlock = false;
        TextureEntry(File file, String id) {
            this.file = file;
            this.id = id;
        }
    }
}