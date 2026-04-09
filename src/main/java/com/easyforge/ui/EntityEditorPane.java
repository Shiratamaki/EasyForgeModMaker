package com.easyforge.ui;

import com.easyforge.model.EntityData;
import com.easyforge.model.EntityType;
import com.easyforge.model.ModProject;
import com.easyforge.util.I18n;
import com.easyforge.util.VanillaIds;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class EntityEditorPane extends VBox {

    private final ObservableList<EntityData> entities = FXCollections.observableArrayList();
    private final FilteredList<EntityData> filteredEntities = new FilteredList<>(entities, p -> true);
    private final ListView<EntityData> entityListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<EntityData> onEntityChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    public EntityEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onEntityChanged != null) onEntityChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("entity.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("entity.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredEntities.setPredicate(entity -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return entity.getDisplayName().toLowerCase().contains(lower) ||
                        entity.getId().toLowerCase().contains(lower);
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("entity.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        entityListView.setCellFactory(lv -> new EntityListCell());
        entityListView.setItems(filteredEntities);
        entityListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForEntity(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("entity.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewEntity());

        VBox leftBox = new VBox(5, listLabel, searchBox, entityListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        entities.addListener((ListChangeListener<EntityData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(e -> { if (onEntityChanged != null) onEntityChanged.accept(e); });
                if (c.wasRemoved()) c.getRemoved().forEach(e -> { if (onEntityChanged != null) onEntityChanged.accept(null); });
            }
        });
    }

    private void createNewEntity() {
        EntityData newEntity = new EntityData();
        newEntity.setId("new_entity_" + (entities.size() + 1));
        newEntity.setDisplayName(I18n.get("entity.defaultName"));
        entities.add(newEntity);
        filteredEntities.setPredicate(null);
        entityListView.getSelectionModel().select(newEntity);
        entityListView.scrollTo(newEntity);
        markDirty();
    }

    private void showEditorForEntity(EntityData entity) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(entity);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(EntityData entity) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("entity.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteEntity(entity));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(entity);
        TitledPane statsPane = createStatsCard(entity);
        TitledPane spawnPane = createSpawnCard(entity);
        TitledPane visualPane = createVisualCard(entity);

        content.getChildren().addAll(titleBar, basicPane, statsPane, spawnPane, visualPane);
        return content;
    }

    private TitledPane createBasicInfoCard(EntityData entity) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField displayField = new TextField(entity.getDisplayName());
        displayField.textProperty().addListener((obs, old, val) -> {
            entity.setDisplayName(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });
        addTooltip(displayField, I18n.get("entity.displayName.tip"));

        TextField idField = new TextField(entity.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = entities.stream().filter(e -> e != entity).anyMatch(e -> e.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            entity.setId(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
            entityListView.refresh();
        });
        addTooltip(idField, I18n.get("entity.id.tip"));

        ComboBox<EntityType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(EntityType.values()));
        typeCombo.setValue(entity.getType());
        typeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(EntityType t) { return t == null ? "" : t.getDisplayName(); }
            @Override
            public EntityType fromString(String s) { return null; }
        });
        typeCombo.valueProperty().addListener((obs, old, val) -> {
            entity.setType(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });

        CheckBox bossCheck = new CheckBox(I18n.get("entity.boss"));
        bossCheck.setSelected(entity.isBoss());
        bossCheck.selectedProperty().addListener((obs, old, val) -> {
            entity.setBoss(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });
        CheckBox tamableCheck = new CheckBox(I18n.get("entity.tamable"));
        tamableCheck.setSelected(entity.isTamable());
        tamableCheck.selectedProperty().addListener((obs, old, val) -> {
            entity.setTamable(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });
        CheckBox rideableCheck = new CheckBox(I18n.get("entity.rideable"));
        rideableCheck.setSelected(entity.isRideable());
        rideableCheck.selectedProperty().addListener((obs, old, val) -> {
            entity.setRideable(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });

        grid.add(createLabel(I18n.get("entity.displayName")), 0, 0);
        grid.add(displayField, 1, 0);
        grid.add(createLabel(I18n.get("entity.id")), 2, 0);
        grid.add(idField, 3, 0);
        grid.add(createLabel(I18n.get("entity.type")), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(bossCheck, 2, 1);
        grid.add(tamableCheck, 0, 2);
        grid.add(rideableCheck, 1, 2);

        TitledPane titled = new TitledPane(I18n.get("entity.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createStatsCard(EntityData entity) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        Spinner<Double> healthSpinner = new Spinner<>(1.0, 1024.0, (double) entity.getHealth(), 0.5);
        healthSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            entity.setHealth(val.floatValue());
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });
        Spinner<Double> attackSpinner = new Spinner<>(0.0, 100.0, (double) entity.getAttackDamage(), 0.5);
        attackSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            entity.setAttackDamage(val.floatValue());
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });
        Spinner<Double> speedSpinner = new Spinner<>(0.0, 2.0, (double) entity.getSpeed(), 0.05);
        speedSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            entity.setSpeed(val.floatValue());
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });
        Spinner<Double> knockbackSpinner = new Spinner<>(0.0, 1.0, (double) entity.getKnockbackResistance(), 0.05);
        knockbackSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            entity.setKnockbackResistance(val.floatValue());
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });

        grid.add(createLabel(I18n.get("entity.health")), 0, 0);
        grid.add(healthSpinner, 1, 0);
        grid.add(createLabel(I18n.get("entity.attackDamage")), 2, 0);
        grid.add(attackSpinner, 3, 0);
        grid.add(createLabel(I18n.get("entity.speed")), 0, 1);
        grid.add(speedSpinner, 1, 1);
        grid.add(createLabel(I18n.get("entity.knockbackResistance")), 2, 1);
        grid.add(knockbackSpinner, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("entity.stats"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createSpawnCard(EntityData entity) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        Spinner<Integer> weightSpinner = new Spinner<>(0, 100, entity.getSpawnWeight());
        weightSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            entity.setSpawnWeight(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });
        Spinner<Integer> minSpinner = new Spinner<>(1, 16, entity.getMinGroupSize());
        minSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            entity.setMinGroupSize(val);
            if (entity.getMaxGroupSize() < val) entity.setMaxGroupSize(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });
        Spinner<Integer> maxSpinner = new Spinner<>(1, 16, entity.getMaxGroupSize());
        maxSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            entity.setMaxGroupSize(val);
            if (entity.getMinGroupSize() > val) entity.setMinGroupSize(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });

        // 生成生物群系字段 + 选择按钮
        TextField biomesField = new TextField(entity.getSpawnBiomeTypes());
        biomesField.textProperty().addListener((obs, old, val) -> {
            entity.setSpawnBiomeTypes(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });
        addTooltip(biomesField, I18n.get("entity.biomes.tip"));

        Button pickBiomeBtn = new Button(I18n.get("entity.pickBiome"));
        pickBiomeBtn.setGraphic(FontIcon.of(FontAwesomeSolid.SEARCH));
        pickBiomeBtn.setOnAction(e -> {
            IdPickerDialog picker = new IdPickerDialog(I18n.get("idpicker.title.biome"), VanillaIds.BIOMES);
            picker.showAndWait().ifPresent(selected -> {
                String current = biomesField.getText().trim();
                if (current.isEmpty()) {
                    biomesField.setText(selected);
                } else {
                    biomesField.setText(current + "," + selected);
                }
            });
        });
        HBox biomesBox = new HBox(5, biomesField, pickBiomeBtn);
        biomesBox.setPrefWidth(400);

        grid.add(createLabel(I18n.get("entity.spawnWeight")), 0, 0);
        grid.add(weightSpinner, 1, 0);
        grid.add(createLabel(I18n.get("entity.groupSize")), 2, 0);
        HBox groupBox = new HBox(5, minSpinner, new Label("~"), maxSpinner);
        grid.add(groupBox, 3, 0);
        grid.add(createLabel(I18n.get("entity.spawnBiomes")), 0, 1);
        grid.add(biomesBox, 1, 1, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("entity.spawn"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createVisualCard(EntityData entity) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField textureField = new TextField(entity.getTexturePath());
        textureField.textProperty().addListener((obs, old, val) -> {
            entity.setTexturePath(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });
        TextField modelField = new TextField(entity.getModelPath());
        modelField.textProperty().addListener((obs, old, val) -> {
            entity.setModelPath(val);
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(entity);
        });

        Button textureBrowse = new Button(I18n.get("browse"));
        textureBrowse.setOnAction(e -> browseFile(textureField, "png"));
        Button modelBrowse = new Button(I18n.get("browse"));
        modelBrowse.setOnAction(e -> browseFile(modelField, "json"));

        HBox textureBox = new HBox(5, textureField, textureBrowse);
        HBox modelBox = new HBox(5, modelField, modelBrowse);
        textureBox.setPrefWidth(400);
        modelBox.setPrefWidth(400);

        grid.add(createLabel(I18n.get("entity.texture")), 0, 0);
        grid.add(textureBox, 1, 0);
        grid.add(createLabel(I18n.get("entity.model")), 0, 1);
        grid.add(modelBox, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("entity.visual"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(false);
        return titled;
    }

    private void browseFile(TextField target, String extension) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extension.toUpperCase() + "文件", "*." + extension));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) target.setText(file.getAbsolutePath());
    }

    private void deleteEntity(EntityData entity) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("entity.deleteConfirm"), entity.getDisplayName()));
        confirm.setContentText(I18n.get("entity.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            entities.remove(entity);
            if (entities.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onEntityChanged != null) onEntityChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("entity.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<EntityData> getEntities() { return entities; }
    public void setEntities(ObservableList<EntityData> newEntities) {
        this.entities.setAll(newEntities);
        if (entityListView.getSelectionModel().isEmpty() && !entities.isEmpty()) entityListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnEntityChanged(Consumer<EntityData> callback) { this.onEntityChanged = callback; }
    public void refresh() { entityListView.refresh(); }

    private static class EntityListCell extends ListCell<EntityData> {
        @Override
        protected void updateItem(EntityData entity, boolean empty) {
            super.updateItem(entity, empty);
            if (empty || entity == null) { setText(null); setGraphic(null); }
            else {
                String display = entity.getDisplayName();
                String id = entity.getId();
                setText(display + " [" + id + "]");
                FontIcon icon = FontIcon.of(FontAwesomeSolid.PAW);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}