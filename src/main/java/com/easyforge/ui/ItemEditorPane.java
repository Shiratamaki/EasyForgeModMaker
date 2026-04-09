package com.easyforge.ui;

import com.easyforge.model.ItemData;
import com.easyforge.model.ItemType;
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

public class ItemEditorPane extends VBox {

    private final ObservableList<ItemData> items = FXCollections.observableArrayList();
    private final FilteredList<ItemData> filteredItems = new FilteredList<>(items, p -> true);
    private final ListView<ItemData> itemListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<ItemData> onItemChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    public ItemEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: transparent;");
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onItemChanged != null) onItemChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("item.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("item.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredItems.setPredicate(item -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return item.getDisplayName().toLowerCase().contains(lower) ||
                        item.getId().toLowerCase().contains(lower);
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("item.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        itemListView.setCellFactory(lv -> new ItemListCell());
        itemListView.setItems(filteredItems);
        itemListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForItem(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newItemBtn = new Button(I18n.get("item.new"));
        newItemBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newItemBtn.setOnAction(e -> createNewItem());

        VBox leftBox = new VBox(5, listLabel, searchBox, itemListView, newItemBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        items.addListener((ListChangeListener<ItemData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(item -> { if (onItemChanged != null) onItemChanged.accept(item); });
                if (c.wasRemoved()) c.getRemoved().forEach(item -> { if (onItemChanged != null) onItemChanged.accept(null); });
            }
        });
    }

    private void createNewItem() {
        ItemData newItem = new ItemData();
        String baseId = "new_item_" + (items.size() + 1);
        newItem.setId(baseId);
        newItem.setDisplayName(I18n.get("item.defaultName"));
        newItem.setMaxStackSize(64);
        newItem.setType(ItemType.NORMAL);
        items.add(newItem);
        filteredItems.setPredicate(null);
        itemListView.getSelectionModel().select(newItem);
        itemListView.scrollTo(newItem);
        markDirty();
    }

    private void showEditorForItem(ItemData item) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox editorContent = createEditorContent(item);
        scrollPane.setContent(editorContent);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(ItemData item) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("item.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteItem(item));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(item);
        TitledPane toolPane = createToolWeaponCard(item);
        TitledPane armorPane = createArmorCard(item);
        TitledPane advancedPane = createAdvancedCard(item);

        Runnable updateVisibility = () -> {
            boolean isToolOrWeapon = item.getType().isTool() || item.getType().isWeapon();
            boolean isArmor = item.getType().isArmor();
            toolPane.setVisible(isToolOrWeapon);
            toolPane.setManaged(isToolOrWeapon);
            armorPane.setVisible(isArmor);
            armorPane.setManaged(isArmor);
        };
        updateVisibility.run();

        content.getChildren().addAll(titleBar, basicPane, toolPane, armorPane, advancedPane);
        return content;
    }

    private TitledPane createBasicInfoCard(ItemData item) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField displayNameField = new TextField(item.getDisplayName());
        displayNameField.textProperty().addListener((obs, old, val) -> {
            item.setDisplayName(val);
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });
        displayNameField.setPromptText(I18n.get("item.displayName.prompt"));
        addTooltip(displayNameField, I18n.get("item.displayName.tip"));

        TextField idField = new TextField(item.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = items.stream().filter(i -> i != item).anyMatch(i -> i.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            item.setId(val);
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
            itemListView.refresh();
        });
        addTooltip(idField, I18n.get("item.id.tip"));

        Spinner<Integer> stackSpinner = new Spinner<>(1, 64, item.getMaxStackSize());
        stackSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            item.setMaxStackSize(val);
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });

        ComboBox<ItemType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(ItemType.values()));
        typeCombo.setValue(item.getType());
        typeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ItemType type) {
                return type == null ? "" : type.getDisplayName();
            }
            @Override
            public ItemType fromString(String string) { return null; }
        });
        typeCombo.valueProperty().addListener((obs, old, newType) -> {
            item.setType(newType);
            showEditorForItem(item);
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });

        int row = 0;
        grid.add(createLabel(I18n.get("item.displayName")), 0, row);
        grid.add(displayNameField, 1, row);
        grid.add(createLabel(I18n.get("item.id")), 2, row);
        grid.add(idField, 3, row);

        row++;
        grid.add(createLabel(I18n.get("item.maxStack")), 0, row);
        grid.add(stackSpinner, 1, row);
        grid.add(createLabel(I18n.get("item.type")), 2, row);
        grid.add(typeCombo, 3, row);

        Button copyVanillaBtn = new Button(I18n.get("item.copyVanilla"));
        copyVanillaBtn.setGraphic(FontIcon.of(FontAwesomeSolid.COPY));
        copyVanillaBtn.setOnAction(e -> showVanillaPicker(item));
        grid.add(copyVanillaBtn, 1, row + 1);

        Button generatePlaceholderBtn = new Button(I18n.get("item.generatePlaceholder"));
        generatePlaceholderBtn.setGraphic(FontIcon.of(FontAwesomeSolid.IMAGE));
        generatePlaceholderBtn.setOnAction(e -> generatePlaceholderTexture(item));
        grid.add(generatePlaceholderBtn, 3, row + 1);

        TitledPane titled = new TitledPane(I18n.get("item.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createToolWeaponCard(ItemData item) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        ComboBox<String> materialCombo = new ComboBox<>(FXCollections.observableArrayList("WOOD", "STONE", "IRON", "GOLD", "DIAMOND", "NETHERITE"));
        materialCombo.setValue(item.getToolMaterial());
        materialCombo.valueProperty().addListener((obs, old, val) -> {
            item.setToolMaterial(val);
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });

        Spinner<Integer> durabilitySpinner = new Spinner<>(1, 5000, item.getDurability());
        durabilitySpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            item.setDurability(val);
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });

        Spinner<Double> damageSpinner = new Spinner<>(0.0, 100.0, (double) item.getAttackDamage(), 0.5);
        damageSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            item.setAttackDamage(val.floatValue());
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });

        Spinner<Double> speedSpinner = new Spinner<>(-4.0, 4.0, (double) item.getAttackSpeed(), 0.1);
        speedSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            item.setAttackSpeed(val.floatValue());
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });

        grid.add(createLabel(I18n.get("item.toolMaterial")), 0, 0);
        grid.add(materialCombo, 1, 0);
        grid.add(createLabel(I18n.get("item.durability")), 2, 0);
        grid.add(durabilitySpinner, 3, 0);
        grid.add(createLabel(I18n.get("item.attackDamage")), 0, 1);
        grid.add(damageSpinner, 1, 1);
        grid.add(createLabel(I18n.get("item.attackSpeed")), 2, 1);
        grid.add(speedSpinner, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("item.toolWeapon"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createArmorCard(ItemData item) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        ComboBox<String> armorMaterialCombo = new ComboBox<>(FXCollections.observableArrayList("LEATHER", "CHAIN", "IRON", "GOLD", "DIAMOND", "NETHERITE"));
        armorMaterialCombo.setValue(item.getArmorMaterial());
        armorMaterialCombo.valueProperty().addListener((obs, old, val) -> {
            item.setArmorMaterial(val);
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });

        Spinner<Integer> armorValueSpinner = new Spinner<>(0, 30, item.getArmorValue());
        armorValueSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            item.setArmorValue(val);
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });

        Spinner<Double> toughnessSpinner = new Spinner<>(0.0, 10.0, (double) item.getToughness(), 0.5);
        toughnessSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            item.setToughness(val.floatValue());
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });

        Spinner<Double> knockbackSpinner = new Spinner<>(0.0, 1.0, (double) item.getKnockbackResistance(), 0.1);
        knockbackSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            item.setKnockbackResistance(val.floatValue());
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });

        grid.add(createLabel(I18n.get("item.armorMaterial")), 0, 0);
        grid.add(armorMaterialCombo, 1, 0);
        grid.add(createLabel(I18n.get("item.armorValue")), 2, 0);
        grid.add(armorValueSpinner, 3, 0);
        grid.add(createLabel(I18n.get("item.toughness")), 0, 1);
        grid.add(toughnessSpinner, 1, 1);
        grid.add(createLabel(I18n.get("item.knockbackResistance")), 2, 1);
        grid.add(knockbackSpinner, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("item.armor"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createAdvancedCard(ItemData item) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField textureField = new TextField(item.getTexturePath());
        textureField.textProperty().addListener((obs, old, val) -> {
            item.setTexturePath(val);
            markDirty(); if (onItemChanged != null) onItemChanged.accept(item);
        });
        textureField.setPromptText("assets/mymod/textures/item/my_item.png");
        addTooltip(textureField, I18n.get("item.texture.tip"));

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

        grid.add(createLabel(I18n.get("item.texture")), 0, 0);
        grid.add(textureBox, 1, 0);
        grid.add(createLabel("预览"), 0, 1);
        grid.add(preview, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("item.advanced"), grid);
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
                    currentProject.getModId(), "item", getCurrentItemId());
            if (relative != null) target.setText(relative);
            else target.setText(file.getAbsolutePath());
        }
    }

    private String getCurrentItemId() {
        ItemData selected = itemListView.getSelectionModel().getSelectedItem();
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
                currentProject.getModId(), "item");
        int success = result.size();
        if (success > 0) {
            for (ItemData item : items) {
                String expectedKey = item.getId();
                if (result.containsKey(expectedKey)) {
                    item.setTexturePath(result.get(expectedKey));
                }
            }
            markDirty();
            refresh();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(String.format("成功导入 %d 个纹理文件", success));
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("没有找到匹配的纹理文件（文件名应与物品ID一致）");
            alert.showAndWait();
        }
    }

    private void generatePlaceholderTexture(ItemData item) {
        if (currentProject == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("请先打开一个项目");
            alert.showAndWait();
            return;
        }
        java.awt.Color bgColor;
        if (item.getType().isTool() || item.getType().isWeapon()) {
            bgColor = new java.awt.Color(200, 100, 100);
        } else if (item.getType().isArmor()) {
            bgColor = new java.awt.Color(100, 100, 200);
        } else {
            bgColor = new java.awt.Color(150, 150, 150);
        }
        int style = 2;  // 使用边框+阴影样式
        String relative = TextureManager.generatePlaceholder(currentProject.getOutputPath(),
                currentProject.getModId(), "item", item.getId(), style, bgColor);
        if (relative != null) {
            item.setTexturePath(relative);
            markDirty();
            refresh();
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

    private void showVanillaPicker(ItemData target) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("钻石剑",
                "钻石剑", "铁剑", "金剑", "钻石镐", "铁镐", "钻石斧", "铁斧",
                "钻石锹", "铁锹", "钻石锄", "铁锄", "弓", "箭", "牛排", "金苹果",
                "钻石头盔", "钻石胸甲", "钻石护腿", "钻石靴子",
                "铁头盔", "铁胸甲", "铁护腿", "铁靴子");
        dialog.setTitle(I18n.get("item.copyVanilla.title"));
        dialog.setHeaderText(I18n.get("item.copyVanilla.header"));
        dialog.setContentText(I18n.get("item.copyVanilla.content"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            switch (selected) {
                case "钻石剑":
                    target.setAttackDamage(7.0f);
                    target.setAttackSpeed(-2.4f);
                    target.setDurability(1561);
                    target.setToolMaterial("DIAMOND");
                    target.setType(ItemType.WEAPON);
                    break;
                case "铁剑":
                    target.setAttackDamage(6.0f);
                    target.setAttackSpeed(-2.4f);
                    target.setDurability(250);
                    target.setToolMaterial("IRON");
                    target.setType(ItemType.WEAPON);
                    break;
                case "金剑":
                    target.setAttackDamage(4.0f);
                    target.setAttackSpeed(-2.4f);
                    target.setDurability(32);
                    target.setToolMaterial("GOLD");
                    target.setType(ItemType.WEAPON);
                    break;
                case "钻石镐":
                    target.setAttackDamage(5.0f);
                    target.setAttackSpeed(-2.8f);
                    target.setDurability(1561);
                    target.setToolMaterial("DIAMOND");
                    target.setType(ItemType.PICKAXE);
                    break;
                case "铁镐":
                    target.setAttackDamage(4.0f);
                    target.setAttackSpeed(-2.8f);
                    target.setDurability(250);
                    target.setToolMaterial("IRON");
                    target.setType(ItemType.PICKAXE);
                    break;
                case "钻石斧":
                    target.setAttackDamage(7.0f);
                    target.setAttackSpeed(-3.0f);
                    target.setDurability(1561);
                    target.setToolMaterial("DIAMOND");
                    target.setType(ItemType.AXE);
                    break;
                case "铁斧":
                    target.setAttackDamage(6.0f);
                    target.setAttackSpeed(-3.0f);
                    target.setDurability(250);
                    target.setToolMaterial("IRON");
                    target.setType(ItemType.AXE);
                    break;
                case "钻石锹":
                    target.setAttackDamage(4.5f);
                    target.setAttackSpeed(-3.0f);
                    target.setDurability(1561);
                    target.setToolMaterial("DIAMOND");
                    target.setType(ItemType.SHOVEL);
                    break;
                case "铁锹":
                    target.setAttackDamage(3.5f);
                    target.setAttackSpeed(-3.0f);
                    target.setDurability(250);
                    target.setToolMaterial("IRON");
                    target.setType(ItemType.SHOVEL);
                    break;
                case "钻石锄":
                    target.setAttackDamage(1.0f);
                    target.setAttackSpeed(-3.0f);
                    target.setDurability(1561);
                    target.setToolMaterial("DIAMOND");
                    target.setType(ItemType.HOE);
                    break;
                case "铁锄":
                    target.setAttackDamage(1.0f);
                    target.setAttackSpeed(-3.0f);
                    target.setDurability(250);
                    target.setToolMaterial("IRON");
                    target.setType(ItemType.HOE);
                    break;
                case "弓":
                    target.setAttackDamage(0.0f);
                    target.setAttackSpeed(-2.4f);
                    target.setDurability(384);
                    target.setToolMaterial("WOOD");
                    target.setType(ItemType.TOOL);
                    break;
                case "箭":
                    target.setMaxStackSize(64);
                    target.setType(ItemType.NORMAL);
                    break;
                case "牛排":
                    target.setMaxStackSize(64);
                    target.setType(ItemType.NORMAL);
                    break;
                case "金苹果":
                    target.setMaxStackSize(64);
                    target.setType(ItemType.NORMAL);
                    break;
                case "钻石头盔":
                    target.setArmorMaterial("DIAMOND");
                    target.setArmorValue(3);
                    target.setToughness(2.0f);
                    target.setDurability(363);
                    target.setType(ItemType.ARMOR_HELMET);
                    break;
                case "钻石胸甲":
                    target.setArmorMaterial("DIAMOND");
                    target.setArmorValue(8);
                    target.setToughness(2.0f);
                    target.setDurability(528);
                    target.setType(ItemType.ARMOR_CHESTPLATE);
                    break;
                case "钻石护腿":
                    target.setArmorMaterial("DIAMOND");
                    target.setArmorValue(6);
                    target.setToughness(2.0f);
                    target.setDurability(495);
                    target.setType(ItemType.ARMOR_LEGGINGS);
                    break;
                case "钻石靴子":
                    target.setArmorMaterial("DIAMOND");
                    target.setArmorValue(3);
                    target.setToughness(2.0f);
                    target.setDurability(429);
                    target.setType(ItemType.ARMOR_BOOTS);
                    break;
                case "铁头盔":
                    target.setArmorMaterial("IRON");
                    target.setArmorValue(2);
                    target.setToughness(0.0f);
                    target.setDurability(165);
                    target.setType(ItemType.ARMOR_HELMET);
                    break;
                case "铁胸甲":
                    target.setArmorMaterial("IRON");
                    target.setArmorValue(6);
                    target.setToughness(0.0f);
                    target.setDurability(240);
                    target.setType(ItemType.ARMOR_CHESTPLATE);
                    break;
                case "铁护腿":
                    target.setArmorMaterial("IRON");
                    target.setArmorValue(5);
                    target.setToughness(0.0f);
                    target.setDurability(225);
                    target.setType(ItemType.ARMOR_LEGGINGS);
                    break;
                case "铁靴子":
                    target.setArmorMaterial("IRON");
                    target.setArmorValue(2);
                    target.setToughness(0.0f);
                    target.setDurability(195);
                    target.setType(ItemType.ARMOR_BOOTS);
                    break;
            }
            showEditorForItem(target);
            markDirty();
            if (onItemChanged != null) onItemChanged.accept(target);
        });
    }

    private void deleteItem(ItemData item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("item.deleteConfirm"), item.getDisplayName()));
        confirm.setContentText(I18n.get("item.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            items.remove(item);
            if (items.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onItemChanged != null) onItemChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("item.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<ItemData> getItems() { return items; }
    public void setItems(ObservableList<ItemData> newItems) {
        this.items.setAll(newItems);
        if (itemListView.getSelectionModel().isEmpty() && !items.isEmpty()) itemListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnItemChanged(Consumer<ItemData> callback) { this.onItemChanged = callback; }
    public void refresh() { itemListView.refresh(); }

    private static class ItemListCell extends ListCell<ItemData> {
        @Override
        protected void updateItem(ItemData item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setGraphic(null); }
            else {
                String display = item.getDisplayName();
                String id = item.getId();
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