package com.easyforge.ui;

import com.easyforge.model.WorldGenData;
import com.easyforge.model.ModProject;
import com.easyforge.util.I18n;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class WorldGenEditorPane extends VBox {

    private final ObservableList<WorldGenData> worldGens = FXCollections.observableArrayList();
    private final FilteredList<WorldGenData> filteredWorldGens = new FilteredList<>(worldGens, p -> true);
    private final ListView<WorldGenData> worldGenListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<WorldGenData> onWorldGenChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    public WorldGenEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onWorldGenChanged != null) onWorldGenChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("worldgen.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("worldgen.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredWorldGens.setPredicate(wg -> {
                if (val == null || val.isEmpty()) return true;
                return wg.getId().toLowerCase().contains(val.toLowerCase());
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("worldgen.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        worldGenListView.setCellFactory(lv -> new WorldGenListCell());
        worldGenListView.setItems(filteredWorldGens);
        worldGenListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForWorldGen(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("worldgen.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewWorldGen());

        VBox leftBox = new VBox(5, listLabel, searchBox, worldGenListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        worldGens.addListener((ListChangeListener<WorldGenData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(wg -> { if (onWorldGenChanged != null) onWorldGenChanged.accept(wg); });
                if (c.wasRemoved()) c.getRemoved().forEach(wg -> { if (onWorldGenChanged != null) onWorldGenChanged.accept(null); });
            }
        });
    }

    private void createNewWorldGen() {
        WorldGenData newGen = new WorldGenData();
        newGen.setId("new_worldgen_" + (worldGens.size() + 1));
        worldGens.add(newGen);
        filteredWorldGens.setPredicate(null);
        worldGenListView.getSelectionModel().select(newGen);
        worldGenListView.scrollTo(newGen);
        markDirty();
    }

    private void showEditorForWorldGen(WorldGenData worldGen) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(worldGen);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(WorldGenData worldGen) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("worldgen.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteWorldGen(worldGen));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(worldGen);
        TitledPane configPane = createConfigCard(worldGen);

        content.getChildren().addAll(titleBar, basicPane, configPane);
        return content;
    }

    private TitledPane createBasicInfoCard(WorldGenData worldGen) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField idField = new TextField(worldGen.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = worldGens.stream().filter(w -> w != worldGen).anyMatch(w -> w.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            worldGen.setId(val);
            markDirty(); if (onWorldGenChanged != null) onWorldGenChanged.accept(worldGen);
            worldGenListView.refresh();
        });
        addTooltip(idField, I18n.get("worldgen.id.tip"));

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("overworld", "nether", "end"));
        typeCombo.setValue(worldGen.getType());
        typeCombo.valueProperty().addListener((obs, old, val) -> {
            worldGen.setType(val);
            markDirty(); if (onWorldGenChanged != null) onWorldGenChanged.accept(worldGen);
        });

        grid.add(createLabel(I18n.get("worldgen.id")), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(createLabel(I18n.get("worldgen.type")), 2, 0);
        grid.add(typeCombo, 3, 0);

        TitledPane titled = new TitledPane(I18n.get("worldgen.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createConfigCard(WorldGenData worldGen) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField noiseField = new TextField(worldGen.getNoiseSettings());
        noiseField.textProperty().addListener((obs, old, val) -> {
            worldGen.setNoiseSettings(val);
            markDirty(); if (onWorldGenChanged != null) onWorldGenChanged.accept(worldGen);
        });
        addTooltip(noiseField, I18n.get("worldgen.noise.tip"));

        TextField biomeField = new TextField(worldGen.getBiomeSource());
        biomeField.textProperty().addListener((obs, old, val) -> {
            worldGen.setBiomeSource(val);
            markDirty(); if (onWorldGenChanged != null) onWorldGenChanged.accept(worldGen);
        });
        addTooltip(biomeField, I18n.get("worldgen.biomeSource.tip"));

        TextField seedField = new TextField(worldGen.getSeed());
        seedField.textProperty().addListener((obs, old, val) -> {
            worldGen.setSeed(val);
            markDirty(); if (onWorldGenChanged != null) onWorldGenChanged.accept(worldGen);
        });
        addTooltip(seedField, I18n.get("worldgen.seed.tip"));

        grid.add(createLabel(I18n.get("worldgen.noiseSettings")), 0, 0);
        grid.add(noiseField, 1, 0);
        grid.add(createLabel(I18n.get("worldgen.biomeSource")), 2, 0);
        grid.add(biomeField, 3, 0);
        grid.add(createLabel(I18n.get("worldgen.seed")), 0, 1);
        grid.add(seedField, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("worldgen.config"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private void deleteWorldGen(WorldGenData worldGen) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("worldgen.deleteConfirm"), worldGen.getId()));
        confirm.setContentText(I18n.get("worldgen.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            worldGens.remove(worldGen);
            if (worldGens.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onWorldGenChanged != null) onWorldGenChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("worldgen.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<WorldGenData> getWorldGens() { return worldGens; }
    public void setWorldGens(ObservableList<WorldGenData> newWorldGens) {
        this.worldGens.setAll(newWorldGens);
        if (worldGenListView.getSelectionModel().isEmpty() && !worldGens.isEmpty()) worldGenListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnWorldGenChanged(Consumer<WorldGenData> callback) { this.onWorldGenChanged = callback; }
    public void refresh() { worldGenListView.refresh(); }

    private static class WorldGenListCell extends ListCell<WorldGenData> {
        @Override
        protected void updateItem(WorldGenData worldGen, boolean empty) {
            super.updateItem(worldGen, empty);
            if (empty || worldGen == null) { setText(null); setGraphic(null); }
            else {
                String id = worldGen.getId();
                String type = worldGen.getType();
                setText(id + " (" + type + ")");
                FontIcon icon = FontIcon.of(FontAwesomeSolid.GLOBE);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}