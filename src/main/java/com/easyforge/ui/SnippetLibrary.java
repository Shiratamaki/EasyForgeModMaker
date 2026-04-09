package com.easyforge.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;

public class SnippetLibrary {
    private final Map<String, String> snippets = new HashMap<>();

    public SnippetLibrary() {
        snippets.put("物品注册",
                "public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register(\"example_item\",\n" +
                        "    () -> new Item(new Item.Properties()));");
        snippets.put("方块注册",
                "public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register(\"example_block\",\n" +
                        "    () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5f)));");
        snippets.put("事件监听",
                "@SubscribeEvent\n" +
                        "public static void onLivingHurt(LivingHurtEvent event) {\n" +
                        "    // 事件处理\n" +
                        "}");
        snippets.put("配方生成 (JSON)",
                "{\n" +
                        "  \"type\": \"minecraft:crafting_shaped\",\n" +
                        "  \"pattern\": [\"###\", \"#X#\", \"###\"],\n" +
                        "  \"key\": {\n" +
                        "    \"#\": {\"item\": \"minecraft:stone\"},\n" +
                        "    \"X\": {\"item\": \"minecraft:diamond\"}\n" +
                        "  },\n" +
                        "  \"result\": {\"item\": \"example_mod:example_item\", \"count\": 1}\n" +
                        "}");
        snippets.put("数据生成器 (DataGen)",
                "public class DataGenerators extends DataProvider {\n" +
                        "    public DataGenerators(DataGenerator generator, ExistingFileHelper helper) {\n" +
                        "        generator.addProvider(true, new RecipeProvider(generator));\n" +
                        "    }\n" +
                        "}");
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("代码片段库");
        stage.setWidth(550);
        stage.setHeight(450);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        javafx.scene.control.ListView<String> listView = new javafx.scene.control.ListView<>();
        listView.getItems().addAll(snippets.keySet());

        javafx.scene.control.TextArea previewArea = new javafx.scene.control.TextArea();
        previewArea.setEditable(false);
        previewArea.setWrapText(true);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                previewArea.setText(snippets.get(selected));
            }
        });

        javafx.scene.control.Button copyBtn = new javafx.scene.control.Button("复制到剪贴板");
        copyBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String code = snippets.get(selected);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(code), null);
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("复制成功");
                alert.setHeaderText(null);
                alert.setContentText("代码已复制到剪贴板");
                alert.showAndWait();
            }
        });

        VBox rightPanel = new VBox(10);
        rightPanel.getChildren().addAll(new javafx.scene.control.Label("预览:"), previewArea, copyBtn);
        rightPanel.setPadding(new Insets(0, 0, 0, 10));

        root.setLeft(listView);
        root.setCenter(rightPanel);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}