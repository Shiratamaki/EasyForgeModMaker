package com.easyforge.ui;

import com.easyforge.model.RecipeData;
import com.easyforge.util.I18n;
import com.easyforge.util.ImageLoader;
import com.easyforge.util.VanillaIds;
import com.easyforge.ui.IdPickerDialog;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 可视化配方编辑器 - 支持九宫格拖拽、模板、有序/无序切换、实时预览
 */
public class RecipeGridEditor extends VBox {

    private static final int SIZE = 3;
    private final TextField[][] grid = new TextField[SIZE][SIZE];
    private final GridPane gridPane = new GridPane();

    private final BooleanProperty shaped = new SimpleBooleanProperty(true);
    private TextField outputItemField;
    private Spinner<Integer> outputCountSpinner;
    private ImageView outputPreview;
    private Label errorLabel;

    private RecipeData currentRecipe;

    // 可选：用于外部拖拽源（暂未使用，保留以备扩展）
    private ObservableList<String> availableItems;

    public RecipeGridEditor() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    private void initUI() {
        HBox toolbar = createToolbar();
        VBox gridArea = createGridArea();
        VBox outputArea = createOutputArea();
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);
        HBox previewArea = createPreviewArea();

        getChildren().addAll(toolbar, gridArea, outputArea, previewArea, errorLabel);

        shaped.addListener((obs, old, val) -> {});
        outputItemField.textProperty().addListener((obs, old, val) -> validateAndPreview());
        outputCountSpinner.valueProperty().addListener((obs, old, val) -> validateAndPreview());
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j].textProperty().addListener((obs, old, val) -> validateAndPreview());
            }
        }
    }

    private HBox createToolbar() {
        CheckBox shapedCheck = new CheckBox(I18n.get("recipe.shaped"));
        shapedCheck.setSelected(true);
        shaped.bind(shapedCheck.selectedProperty());

        Button templateBtn = new Button(I18n.get("recipe.template"));
        templateBtn.setGraphic(FontIcon.of(FontAwesomeSolid.COPY));
        templateBtn.setOnAction(e -> showTemplateMenu());

        Button clearBtn = new Button(I18n.get("recipe.clear"));
        clearBtn.setGraphic(FontIcon.of(FontAwesomeSolid.ERASER));
        clearBtn.setOnAction(e -> clear());

        HBox toolbar = new HBox(10, shapedCheck, templateBtn, clearBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        return toolbar;
    }

    private VBox createGridArea() {
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5;");

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                TextField field = new TextField();
                field.setPrefWidth(80);
                field.setPromptText(I18n.get("recipe.grid.prompt"));
                field.setOnDragOver(this::handleDragOver);
                field.setOnDragDropped(e -> handleDragDropped(e, field));
                field.setOnDragDetected(e -> handleDragDetected(e, field));
                grid[row][col] = field;
                gridPane.add(field, col, row);
            }
        }
        VBox vbox = new VBox(5, new Label(I18n.get("recipe.grid.title")), gridPane);
        vbox.setAlignment(Pos.CENTER);
        return vbox;
    }

    private VBox createOutputArea() {
        outputItemField = new TextField();
        outputItemField.setPromptText(I18n.get("recipe.output.prompt"));
        outputItemField.setPrefWidth(200);
        outputItemField.setOnDragOver(this::handleDragOver);
        outputItemField.setOnDragDropped(e -> {
            String id = getDraggedItemId(e);
            if (id != null) outputItemField.setText(id);
            e.setDropCompleted(true);
            e.consume();
        });

        Button pickerBtn = new Button(I18n.get("recipe.pick"));
        pickerBtn.setGraphic(FontIcon.of(FontAwesomeSolid.SEARCH));
        pickerBtn.setOnAction(e -> {
            IdPickerDialog picker = new IdPickerDialog(I18n.get("idpicker.title.item"), VanillaIds.ITEMS);
            picker.showAndWait().ifPresent(outputItemField::setText);
        });
        HBox outputBox = new HBox(5, outputItemField, pickerBtn);
        outputBox.setPrefWidth(400);

        outputCountSpinner = new Spinner<>(1, 64, 1);
        outputCountSpinner.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.add(new Label(I18n.get("recipe.output")), 0, 0);
        grid.add(outputBox, 1, 0);
        grid.add(new Label(I18n.get("recipe.outputCount")), 2, 0);
        grid.add(outputCountSpinner, 3, 0);

        VBox vbox = new VBox(5, new Label(I18n.get("recipe.outputArea")), grid);
        vbox.setAlignment(Pos.CENTER_LEFT);
        return vbox;
    }

    private HBox createPreviewArea() {
        outputPreview = new ImageView();
        outputPreview.setFitWidth(32);
        outputPreview.setFitHeight(32);
        Label previewLabel = new Label(I18n.get("recipe.preview"));
        HBox box = new HBox(5, previewLabel, outputPreview);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // ==================== 拖拽处理 ====================
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event, TextField target) {
        String id = getDraggedItemId(event);
        if (id != null) {
            target.setText(id);
            event.setDropCompleted(true);
        } else {
            event.setDropCompleted(false);
        }
        event.consume();
    }

    private void handleDragDetected(MouseEvent event, TextField source) {
        String text = source.getText();
        if (text != null && !text.isEmpty()) {
            Dragboard db = source.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            db.setContent(content);
            event.consume();
        }
    }

    private String getDraggedItemId(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasString()) {
            String id = db.getString();
            if (id.contains(":") || Pattern.matches("[a-z0-9_]+", id)) {
                return id;
            }
        }
        return null;
    }

    // ==================== 模板功能 ====================
    private void showTemplateMenu() {
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(createTemplateItem("剑", new String[]{" A ", " A ", " B "},
                Map.of('A', "minecraft:stick", 'B', "minecraft:iron_ingot")));
        menu.getItems().add(createTemplateItem("镐", new String[]{"AAA", " B ", " B "},
                Map.of('A', "minecraft:iron_ingot", 'B', "minecraft:stick")));
        menu.getItems().add(createTemplateItem("斧", new String[]{"AA ", "AB ", " B "},
                Map.of('A', "minecraft:iron_ingot", 'B', "minecraft:stick")));
        menu.getItems().add(createTemplateItem("锹", new String[]{" A ", " B ", " B "},
                Map.of('A', "minecraft:iron_ingot", 'B', "minecraft:stick")));
        menu.getItems().add(createTemplateItem("锄", new String[]{"AA ", " B ", " B "},
                Map.of('A', "minecraft:iron_ingot", 'B', "minecraft:stick")));
        menu.getItems().add(createTemplateItem("圆石台阶", new String[]{"AAA", "   ", "   "},
                Map.of('A', "minecraft:cobblestone")));
        menu.show(gridPane, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private MenuItem createTemplateItem(String name, String[] shape, Map<Character, String> keys) {
        MenuItem item = new MenuItem(name);
        item.setOnAction(e -> applyTemplate(shape, keys));
        return item;
    }

    private void applyTemplate(String[] shape, Map<Character, String> keys) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j].clear();
            }
        }
        for (int row = 0; row < shape.length && row < SIZE; row++) {
            String line = shape[row];
            for (int col = 0; col < line.length() && col < SIZE; col++) {
                char ch = line.charAt(col);
                if (ch != ' ') {
                    String itemId = keys.get(ch);
                    if (itemId != null) {
                        grid[row][col].setText(itemId);
                    }
                }
            }
        }
        shaped.set(true);
        validateAndPreview();
    }

    // ==================== 数据转换 ====================
    public RecipeData getRecipe() {
        RecipeData data = new RecipeData();
        data.setOutputItem(outputItemField.getText().trim());
        data.setOutputCount(outputCountSpinner.getValue());
        data.setShaped(shaped.get());

        if (shaped.get()) {
            List<String> shapeList = new ArrayList<>();
            Map<Character, String> keys = new HashMap<>();
            char nextKey = 'A';

            for (int row = 0; row < SIZE; row++) {
                StringBuilder line = new StringBuilder();
                for (int col = 0; col < SIZE; col++) {
                    String itemId = grid[row][col].getText().trim();
                    if (itemId.isEmpty()) {
                        line.append(' ');
                    } else {
                        Character existingKey = null;
                        for (Map.Entry<Character, String> entry : keys.entrySet()) {
                            if (entry.getValue().equals(itemId)) {
                                existingKey = entry.getKey();
                                break;
                            }
                        }
                        if (existingKey != null) {
                            line.append(existingKey);
                        } else {
                            char key = nextKey++;
                            keys.put(key, itemId);
                            line.append(key);
                        }
                    }
                }
                String lineStr = line.toString();
                if (!lineStr.trim().isEmpty()) {
                    shapeList.add(lineStr);
                }
            }
            if (shapeList.isEmpty()) {
                data.setShape(new String[0]);
                data.setKeys(new HashMap<>());
            } else {
                data.setShape(shapeList.toArray(new String[0]));
                data.setKeys(keys);
            }
        } else {
            Map<Character, String> keys = new HashMap<>();
            char nextKey = 'A';
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    String itemId = grid[row][col].getText().trim();
                    if (!itemId.isEmpty() && !keys.containsValue(itemId)) {
                        keys.put(nextKey++, itemId);
                    }
                }
            }
            data.setShape(null);
            data.setKeys(keys);
        }
        return data;
    }

    public void setRecipe(RecipeData recipe) {
        if (recipe == null) {
            clear();
            return;
        }
        currentRecipe = recipe;
        outputItemField.setText(recipe.getOutputItem() != null ? recipe.getOutputItem() : "");
        outputCountSpinner.getValueFactory().setValue(recipe.getOutputCount());
        shaped.set(recipe.isShaped());

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j].clear();
            }
        }

        if (recipe.isShaped() && recipe.getShape() != null) {
            String[] shape = recipe.getShape();
            Map<Character, String> keys = recipe.getKeys();
            for (int row = 0; row < shape.length && row < SIZE; row++) {
                String line = shape[row];
                for (int col = 0; col < line.length() && col < SIZE; col++) {
                    char ch = line.charAt(col);
                    if (ch != ' ') {
                        String itemId = keys.get(ch);
                        if (itemId != null) {
                            grid[row][col].setText(itemId);
                        }
                    }
                }
            }
        } else if (!recipe.isShaped() && recipe.getKeys() != null) {
            List<String> items = new ArrayList<>(recipe.getKeys().values());
            int idx = 0;
            for (int row = 0; row < SIZE && idx < items.size(); row++) {
                for (int col = 0; col < SIZE && idx < items.size(); col++) {
                    grid[row][col].setText(items.get(idx++));
                }
            }
        }
        validateAndPreview();
    }

    public void clear() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j].clear();
            }
        }
        outputItemField.clear();
        outputCountSpinner.getValueFactory().setValue(1);
        shaped.set(true);
        outputPreview.setImage(null);
        errorLabel.setText("");
    }

    // 可选：设置输出物品（供外部双击列表调用）
    public void setOutputItem(String itemId) {
        outputItemField.setText(itemId);
    }

    // ==================== 验证与预览 ====================
    private void validateAndPreview() {
        StringBuilder errors = new StringBuilder();
        String outputId = outputItemField.getText().trim();
        if (outputId.isEmpty()) {
            errors.append(I18n.get("recipe.error.noOutput")).append("\n");
        } else if (!outputId.contains(":") && !outputId.matches("[a-z0-9_]+")) {
            errors.append(I18n.get("recipe.error.invalidOutput")).append("\n");
        }

        boolean hasAnyItem = false;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                String text = grid[i][j].getText().trim();
                if (!text.isEmpty()) {
                    hasAnyItem = true;
                    if (!text.contains(":") && !text.matches("[a-z0-9_]+")) {
                        errors.append(String.format(I18n.get("recipe.error.invalidItem"), text)).append("\n");
                    }
                }
            }
        }
        if (!hasAnyItem) {
            errors.append(I18n.get("recipe.error.noMaterials")).append("\n");
        }
        errorLabel.setText(errors.toString());
        updatePreview();
    }

    private void updatePreview() {
        String path = outputItemField.getText().trim();
        if (path.isEmpty()) {
            outputPreview.setImage(null);
            return;
        }
        String[] parts = path.contains(":") ? path.split(":") : new String[]{"minecraft", path};
        String modId = parts[0];
        String itemId = parts[1];
        String url = String.format("/assets/%s/textures/item/%s.png", modId, itemId);
        ImageLoader.loadImageAsync(url, 32, 32, img -> {
            javafx.application.Platform.runLater(() -> {
                if (img != null) outputPreview.setImage(img);
                else outputPreview.setImage(null);
            });
        });
    }

    // 供外部设置可用物品列表（预留）
    public void setAvailableItems(ObservableList<String> items) {
        this.availableItems = items;
    }
}