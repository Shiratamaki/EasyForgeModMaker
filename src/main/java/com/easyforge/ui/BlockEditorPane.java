package com.easyforge.ui;

import com.easyforge.model.BlockData;
import com.easyforge.model.ModProject;
import com.easyforge.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class BlockEditorPane extends VBox {

    private final ObservableList<BlockData> blocks = FXCollections.observableArrayList();
    private final FilteredList<BlockData> filteredBlocks = new FilteredList<>(blocks, p -> true);
    private final ListView<BlockData> blockListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<BlockData> onBlockChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    public BlockEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: transparent;");
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onBlockChanged != null) onBlockChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("block.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("block.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredBlocks.setPredicate(block -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return block.getDisplayName().toLowerCase().contains(lower) ||
                        block.getId().toLowerCase().contains(lower);
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("block.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        blockListView.setCellFactory(lv -> new BlockListCell());
        blockListView.setItems(filteredBlocks);
        blockListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForBlock(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("block.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewBlock());

        VBox leftBox = new VBox(5, listLabel, searchBox, blockListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        blocks.addListener((ListChangeListener<BlockData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(b -> { if (onBlockChanged != null) onBlockChanged.accept(b); });
                if (c.wasRemoved()) c.getRemoved().forEach(b -> { if (onBlockChanged != null) onBlockChanged.accept(null); });
            }
        });
    }

    private void createNewBlock() {
        BlockData newBlock = new BlockData();
        newBlock.setId("new_block_" + (blocks.size() + 1));
        newBlock.setDisplayName(I18n.get("block.defaultName"));
        newBlock.setMaterial("STONE");
        newBlock.setHardness(3.0f);
        newBlock.setExplosionResistance(15.0f);
        blocks.add(newBlock);
        filteredBlocks.setPredicate(null);
        blockListView.getSelectionModel().select(newBlock);
        blockListView.scrollTo(newBlock);
        markDirty();
    }

    private void showEditorForBlock(BlockData block) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox editorContent = createEditorContent(block);
        scrollPane.setContent(editorContent);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(BlockData block) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("block.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteBlock(block));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(block);
        TitledPane visualPane = createVisualCard(block);
        TitledPane behaviorPane = createBehaviorCard(block);
        TitledPane texturePane = createTextureCard(block);

        content.getChildren().addAll(titleBar, basicPane, visualPane, behaviorPane, texturePane);
        return content;
    }

    private TitledPane createBasicInfoCard(BlockData block) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField displayNameField = new TextField(block.getDisplayName());
        displayNameField.textProperty().addListener((obs, old, val) -> {
            block.setDisplayName(val);
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
        });
        addTooltip(displayNameField, I18n.get("block.displayName.tip"));

        TextField idField = new TextField(block.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = blocks.stream().filter(b -> b != block).anyMatch(b -> b.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            block.setId(val);
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
            blockListView.refresh();
        });
        addTooltip(idField, I18n.get("block.id.tip"));

        ComboBox<String> materialCombo = new ComboBox<>(FXCollections.observableArrayList(
                "WOOD", "STONE", "METAL", "SAND", "GLASS", "CLAY", "GRASS", "PLANTS", "ROCK", "IRON", "ANVIL"));
        materialCombo.setValue(block.getMaterial());
        materialCombo.valueProperty().addListener((obs, old, val) -> {
            block.setMaterial(val);
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
        });

        Spinner<Double> hardnessSpinner = new Spinner<>(0.5, 50.0, (double) block.getHardness(), 0.5);
        hardnessSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            block.setHardness(val.floatValue());
            if (block.getExplosionResistance() == block.getHardness() * 5) {
                block.setExplosionResistance(val.floatValue() * 5);
            }
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
        });

        Spinner<Double> resistanceSpinner = new Spinner<>(0.0, 1000.0, (double) block.getExplosionResistance(), 1.0);
        resistanceSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            block.setExplosionResistance(val.floatValue());
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
        });

        int row = 0;
        grid.add(createLabel(I18n.get("block.displayName")), 0, row);
        grid.add(displayNameField, 1, row);
        grid.add(createLabel(I18n.get("block.id")), 2, row);
        grid.add(idField, 3, row);

        row++;
        grid.add(createLabel(I18n.get("block.material")), 0, row);
        grid.add(materialCombo, 1, row);
        grid.add(createLabel(I18n.get("block.hardness")), 2, row);
        grid.add(hardnessSpinner, 3, row);

        row++;
        grid.add(createLabel(I18n.get("block.explosionResistance")), 0, row);
        grid.add(resistanceSpinner, 1, row);

        Button copyVanillaBtn = new Button(I18n.get("block.copyVanilla"));
        copyVanillaBtn.setGraphic(FontIcon.of(FontAwesomeSolid.COPY));
        copyVanillaBtn.setOnAction(e -> showVanillaPicker(block));
        grid.add(copyVanillaBtn, 3, row);

        TitledPane titled = new TitledPane(I18n.get("block.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createVisualCard(BlockData block) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        CheckBox transparentCheck = new CheckBox(I18n.get("block.transparent"));
        transparentCheck.setSelected(block.isTransparent());
        transparentCheck.selectedProperty().addListener((obs, old, val) -> {
            block.setTransparent(val);
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
        });
        addTooltip(transparentCheck, I18n.get("block.transparent.tip"));

        CheckBox fullCubeCheck = new CheckBox(I18n.get("block.fullCube"));
        fullCubeCheck.setSelected(block.isFullCube());
        fullCubeCheck.selectedProperty().addListener((obs, old, val) -> {
            block.setFullCube(val);
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
        });
        addTooltip(fullCubeCheck, I18n.get("block.fullCube.tip"));

        ComboBox<String> soundCombo = new ComboBox<>(FXCollections.observableArrayList(
                "WOOD", "STONE", "METAL", "GLASS", "SAND", "GRAVEL", "GRASS", "SNOW", "LADDER", "ANVIL"));
        soundCombo.setValue(block.getSoundType());
        soundCombo.valueProperty().addListener((obs, old, val) -> {
            block.setSoundType(val);
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
        });

        grid.add(transparentCheck, 0, 0);
        grid.add(fullCubeCheck, 1, 0);
        grid.add(createLabel(I18n.get("block.soundType")), 0, 1);
        grid.add(soundCombo, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("block.visual"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createBehaviorCard(BlockData block) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        Spinner<Integer> lightSpinner = new Spinner<>(0, 15, block.getLightLevel());
        lightSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            block.setLightLevel(val);
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
        });

        CheckBox requiresToolCheck = new CheckBox(I18n.get("block.requiresTool"));
        requiresToolCheck.setSelected(block.isRequiresTool());
        requiresToolCheck.selectedProperty().addListener((obs, old, val) -> {
            block.setRequiresTool(val);
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
        });

        CheckBox hasItemCheck = new CheckBox(I18n.get("block.hasItem"));
        hasItemCheck.setSelected(block.isHasItem());
        hasItemCheck.selectedProperty().addListener((obs, old, val) -> {
            block.setHasItem(val);
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
        });

        grid.add(createLabel(I18n.get("block.lightLevel")), 0, 0);
        grid.add(lightSpinner, 1, 0);
        grid.add(requiresToolCheck, 0, 1);
        grid.add(hasItemCheck, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("block.behavior"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createTextureCard(BlockData block) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField textureField = new TextField(block.getTexturePath());
        textureField.textProperty().addListener((obs, old, val) -> {
            block.setTexturePath(val);
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(block);
        });
        textureField.setPromptText("assets/mymod/textures/block/my_block.png");
        addTooltip(textureField, I18n.get("block.texture.tip"));

        textureField.setOnDragOver(this::handleDragOver);
        textureField.setOnDragDropped(e -> {
            String id = getDraggedItemId(e);
            if (id != null) textureField.setText(id);
            e.setDropCompleted(true);
            e.consume();
        });

        Button browseBtn = new Button(I18n.get("browse"));
        browseBtn.setOnAction(e -> browseTexture(textureField));

        Button batchImportBtn = new Button(I18n.get("texture.batchImport"));
        batchImportBtn.setGraphic(FontIcon.of(FontAwesomeSolid.IMAGES));
        batchImportBtn.setOnAction(e -> batchImportTextures());

        HBox textureBox = new HBox(5, textureField, browseBtn, batchImportBtn);
        textureBox.setPrefWidth(400);

        ImageView preview = new ImageView();
        preview.setFitWidth(32);
        preview.setFitHeight(32);
        textureField.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.isEmpty() && currentProject != null) {
                Image img = TextureManager.loadTexture(currentProject.getOutputPath(), val, 32, 32);
                preview.setImage(img);
            } else {
                preview.setImage(null);
            }
        });

        Button generatePlaceholderBtn = new Button(I18n.get("block.generatePlaceholder"));
        generatePlaceholderBtn.setGraphic(FontIcon.of(FontAwesomeSolid.IMAGE));
        generatePlaceholderBtn.setOnAction(e -> generatePlaceholderTexture(block, textureField));

        grid.add(createLabel(I18n.get("block.texture")), 0, 0);
        grid.add(textureBox, 1, 0);
        grid.add(createLabel("预览"), 0, 1);
        grid.add(preview, 1, 1);
        grid.add(generatePlaceholderBtn, 1, 2);

        TitledPane titled = new TitledPane(I18n.get("block.textureTab"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(false);
        return titled;
    }

    private void browseTexture(TextField target) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG图片", "*.png"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            String relative = TextureManager.copyTexture(file, currentProject.getOutputPath(),
                    currentProject.getModId(), "block", getCurrentBlockId());
            if (relative != null) target.setText(relative);
            else target.setText(file.getAbsolutePath());
        }
    }

    private String getCurrentBlockId() {
        BlockData selected = blockListView.getSelectionModel().getSelectedItem();
        return selected != null ? selected.getId() : "unknown";
    }

    private void batchImportTextures() {
        if (currentProject == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("请先打开一个项目");
            alert.showAndWait();
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("texture.batchImportTitle"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG图片", "*.png"));
        List<File> files = chooser.showOpenMultipleDialog(getScene().getWindow());
        if (files == null || files.isEmpty()) return;

        Map<String, String> result = TextureManager.batchImport(files, currentProject.getOutputPath(),
                currentProject.getModId(), "block");
        int success = result.size();
        if (success > 0) {
            for (BlockData block : blocks) {
                String expectedKey = block.getId();
                if (result.containsKey(expectedKey)) {
                    block.setTexturePath(result.get(expectedKey));
                }
            }
            markDirty();
            refresh();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(String.format("成功导入 %d 个纹理文件", success));
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("没有找到匹配的纹理文件（文件名应与方块ID一致）");
            alert.showAndWait();
        }
    }

    private void generatePlaceholderTexture(BlockData block, TextField textureField) {
        if (currentProject == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("请先打开一个项目");
            alert.showAndWait();
            return;
        }
        java.awt.Color bgColor;
        switch (block.getMaterial()) {
            case "WOOD": bgColor = new java.awt.Color(160, 130, 80); break;
            case "STONE": bgColor = new java.awt.Color(120, 120, 120); break;
            case "METAL": bgColor = new java.awt.Color(200, 200, 210); break;
            case "SAND": bgColor = new java.awt.Color(210, 190, 140); break;
            case "GLASS": bgColor = new java.awt.Color(200, 230, 255, 180); break;
            default: bgColor = new java.awt.Color(150, 150, 150);
        }

        int style = 2;  // 使用边框+阴影样式
        String relative = TextureManager.generatePlaceholder(currentProject.getOutputPath(),
                currentProject.getModId(), "block", block.getId(), style, bgColor);
        if (relative != null) {
            block.setTexturePath(relative);
            textureField.setText(relative);
            markDirty();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("已生成占位纹理");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("生成失败");
            alert.showAndWait();
        }
    }

    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
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

    private void showVanillaPicker(BlockData target) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("石头", "石头", "泥土", "草方块", "钻石块", "铁块", "金块", "橡木", "圆石", "玻璃", "沙子");
        dialog.setTitle(I18n.get("block.copyVanilla.title"));
        dialog.setHeaderText(I18n.get("block.copyVanilla.header"));
        dialog.setContentText(I18n.get("block.copyVanilla.content"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            switch (selected) {
                case "石头":
                    target.setMaterial("STONE");
                    target.setHardness(1.5f);
                    target.setExplosionResistance(6.0f);
                    target.setRequiresTool(true);
                    target.setSoundType("STONE");
                    target.setLightLevel(0);
                    target.setTransparent(false);
                    target.setFullCube(true);
                    break;
                case "泥土":
                    target.setMaterial("GRASS");
                    target.setHardness(0.5f);
                    target.setExplosionResistance(0.5f);
                    target.setRequiresTool(false);
                    target.setSoundType("GRAVEL");
                    break;
                case "草方块":
                    target.setMaterial("GRASS");
                    target.setHardness(0.6f);
                    target.setExplosionResistance(0.6f);
                    target.setRequiresTool(false);
                    target.setSoundType("GRASS");
                    break;
                case "钻石块":
                    target.setMaterial("METAL");
                    target.setHardness(5.0f);
                    target.setExplosionResistance(30.0f);
                    target.setRequiresTool(true);
                    target.setSoundType("METAL");
                    target.setLightLevel(0);
                    break;
                case "铁块":
                    target.setMaterial("METAL");
                    target.setHardness(5.0f);
                    target.setExplosionResistance(6.0f);
                    target.setRequiresTool(true);
                    target.setSoundType("METAL");
                    break;
                case "金块":
                    target.setMaterial("METAL");
                    target.setHardness(3.0f);
                    target.setExplosionResistance(6.0f);
                    target.setRequiresTool(true);
                    target.setSoundType("METAL");
                    break;
                case "橡木":
                    target.setMaterial("WOOD");
                    target.setHardness(2.0f);
                    target.setExplosionResistance(3.0f);
                    target.setRequiresTool(false);
                    target.setSoundType("WOOD");
                    break;
                case "圆石":
                    target.setMaterial("STONE");
                    target.setHardness(2.0f);
                    target.setExplosionResistance(6.0f);
                    target.setRequiresTool(true);
                    target.setSoundType("STONE");
                    break;
                case "玻璃":
                    target.setMaterial("GLASS");
                    target.setHardness(0.3f);
                    target.setExplosionResistance(0.3f);
                    target.setRequiresTool(false);
                    target.setSoundType("GLASS");
                    target.setTransparent(true);
                    target.setFullCube(false);
                    break;
                case "沙子":
                    target.setMaterial("SAND");
                    target.setHardness(0.5f);
                    target.setExplosionResistance(0.5f);
                    target.setRequiresTool(false);
                    target.setSoundType("SAND");
                    break;
            }
            showEditorForBlock(target);
            markDirty();
            if (onBlockChanged != null) onBlockChanged.accept(target);
        });
    }

    private void deleteBlock(BlockData block) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("block.deleteConfirm"), block.getDisplayName()));
        confirm.setContentText(I18n.get("block.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            blocks.remove(block);
            if (blocks.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onBlockChanged != null) onBlockChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("block.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<BlockData> getBlocks() { return blocks; }
    public void setBlocks(ObservableList<BlockData> newBlocks) {
        this.blocks.setAll(newBlocks);
        if (blockListView.getSelectionModel().isEmpty() && !blocks.isEmpty()) blockListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnBlockChanged(Consumer<BlockData> callback) { this.onBlockChanged = callback; }
    public void refresh() { blockListView.refresh(); }

    private static class BlockListCell extends ListCell<BlockData> {
        @Override
        protected void updateItem(BlockData block, boolean empty) {
            super.updateItem(block, empty);
            if (empty || block == null) { setText(null); setGraphic(null); }
            else {
                String display = block.getDisplayName();
                String id = block.getId();
                String chineseName = ChineseNames.getChinese(id);
                if (chineseName.isEmpty() && !id.contains(":")) {
                    chineseName = ChineseNames.getChinese("minecraft:" + id);
                }
                if (chineseName != null && !chineseName.isEmpty()) {
                    setText(display + " (" + chineseName + ")");
                } else {
                    setText(display + " [" + id + "]");
                }
                FontIcon icon = FontIcon.of(FontAwesomeSolid.CUBE);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}