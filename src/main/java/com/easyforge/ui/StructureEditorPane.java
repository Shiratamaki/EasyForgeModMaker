package com.easyforge.ui;

import com.easyforge.model.StructureData;
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
import javafx.stage.FileChooser;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class StructureEditorPane extends VBox {

    private final ObservableList<StructureData> structures = FXCollections.observableArrayList();
    private final FilteredList<StructureData> filteredStructures = new FilteredList<>(structures, p -> true);
    private final ListView<StructureData> structureListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<StructureData> onStructureChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    public StructureEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onStructureChanged != null) onStructureChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("structure.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("structure.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredStructures.setPredicate(s -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return s.getDisplayName().toLowerCase().contains(lower) ||
                        s.getId().toLowerCase().contains(lower);
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("structure.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        structureListView.setCellFactory(lv -> new StructureListCell());
        structureListView.setItems(filteredStructures);
        structureListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForStructure(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("structure.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewStructure());

        VBox leftBox = new VBox(5, listLabel, searchBox, structureListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        structures.addListener((ListChangeListener<StructureData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(s -> { if (onStructureChanged != null) onStructureChanged.accept(s); });
                if (c.wasRemoved()) c.getRemoved().forEach(s -> { if (onStructureChanged != null) onStructureChanged.accept(null); });
            }
        });
    }

    private void createNewStructure() {
        StructureData newStructure = new StructureData();
        newStructure.setId("new_structure_" + (structures.size() + 1));
        newStructure.setDisplayName(I18n.get("structure.defaultName"));
        structures.add(newStructure);
        filteredStructures.setPredicate(null);
        structureListView.getSelectionModel().select(newStructure);
        structureListView.scrollTo(newStructure);
        markDirty();
    }

    private void showEditorForStructure(StructureData structure) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(structure);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(StructureData structure) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("structure.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteStructure(structure));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(structure);
        TitledPane placementPane = createPlacementCard(structure);
        TitledPane filePane = createFileCard(structure);

        content.getChildren().addAll(titleBar, basicPane, placementPane, filePane);
        return content;
    }

    private TitledPane createBasicInfoCard(StructureData structure) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField displayField = new TextField(structure.getDisplayName());
        displayField.textProperty().addListener((obs, old, val) -> {
            structure.setDisplayName(val);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(structure);
        });
        addTooltip(displayField, I18n.get("structure.displayName.tip"));

        TextField idField = new TextField(structure.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = structures.stream().filter(s -> s != structure).anyMatch(s -> s.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            structure.setId(val);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(structure);
            structureListView.refresh();
        });
        addTooltip(idField, I18n.get("structure.id.tip"));

        // 从原版结构复制按钮
        Button copyVanillaBtn = new Button(I18n.get("structure.copyVanilla"));
        copyVanillaBtn.setGraphic(FontIcon.of(FontAwesomeSolid.COPY));
        copyVanillaBtn.setOnAction(e -> showVanillaPicker(structure));

        grid.add(createLabel(I18n.get("structure.displayName")), 0, 0);
        grid.add(displayField, 1, 0);
        grid.add(createLabel(I18n.get("structure.id")), 2, 0);
        grid.add(idField, 3, 0);
        grid.add(copyVanillaBtn, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("structure.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createPlacementCard(StructureData structure) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("FEATURE", "CONFIGURED_FEATURE", "PLACEMENT"));
        typeCombo.setValue(structure.getType());
        typeCombo.valueProperty().addListener((obs, old, val) -> {
            structure.setType(val);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(structure);
        });

        ComboBox<String> placementCombo = new ComboBox<>(FXCollections.observableArrayList("RANDOM_SPREAD", "RANDOM_PATCH", "SURFACE"));
        placementCombo.setValue(structure.getPlacementType());
        placementCombo.valueProperty().addListener((obs, old, val) -> {
            structure.setPlacementType(val);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(structure);
        });

        Spinner<Integer> raritySpinner = new Spinner<>(1, 100, structure.getRarity());
        raritySpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            structure.setRarity(val);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(structure);
        });
        Spinner<Integer> countSpinner = new Spinner<>(1, 50, structure.getCount());
        countSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            structure.setCount(val);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(structure);
        });
        Spinner<Integer> spreadSpinner = new Spinner<>(1, 50, structure.getSpread());
        spreadSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            structure.setSpread(val);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(structure);
        });

        Spinner<Integer> minYSpinner = new Spinner<>(0, 255, structure.getMinY());
        minYSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            structure.setMinY(val);
            if (structure.getMaxY() < val) structure.setMaxY(val);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(structure);
        });
        Spinner<Integer> maxYSpinner = new Spinner<>(0, 255, structure.getMaxY());
        maxYSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            structure.setMaxY(val);
            if (structure.getMinY() > val) structure.setMinY(val);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(structure);
        });

        TextField biomesField = new TextField(structure.getBiomes());
        biomesField.textProperty().addListener((obs, old, val) -> {
            structure.setBiomes(val);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(structure);
        });
        addTooltip(biomesField, I18n.get("structure.biomes.tip"));

        grid.add(createLabel(I18n.get("structure.type")), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(createLabel(I18n.get("structure.placementType")), 2, 0);
        grid.add(placementCombo, 3, 0);
        grid.add(createLabel(I18n.get("structure.rarity")), 0, 1);
        grid.add(raritySpinner, 1, 1);
        grid.add(createLabel(I18n.get("structure.count")), 2, 1);
        grid.add(countSpinner, 3, 1);
        grid.add(createLabel(I18n.get("structure.spread")), 0, 2);
        grid.add(spreadSpinner, 1, 2);
        grid.add(createLabel(I18n.get("structure.minY")), 2, 2);
        grid.add(minYSpinner, 3, 2);
        grid.add(createLabel(I18n.get("structure.maxY")), 0, 3);
        grid.add(maxYSpinner, 1, 3);
        grid.add(createLabel(I18n.get("structure.biomes")), 2, 3);
        grid.add(biomesField, 3, 3);

        TitledPane titled = new TitledPane(I18n.get("structure.placement"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createFileCard(StructureData structure) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField fileField = new TextField(structure.getStructureFile());
        fileField.textProperty().addListener((obs, old, val) -> {
            structure.setStructureFile(val);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(structure);
        });
        Button browseBtn = new Button(I18n.get("browse"));
        browseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("NBT文件", "*.nbt"));
            File file = chooser.showOpenDialog(getScene().getWindow());
            if (file != null) fileField.setText(file.getAbsolutePath());
        });
        HBox fileBox = new HBox(5, fileField, browseBtn);
        fileBox.setPrefWidth(400);

        grid.add(createLabel(I18n.get("structure.structureFile")), 0, 0);
        grid.add(fileBox, 1, 0);

        TitledPane titled = new TitledPane(I18n.get("structure.file"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(false);
        return titled;
    }

    private void showVanillaPicker(StructureData target) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("村庄", "村庄", "沙漠神殿", "丛林神庙", "海底遗迹", "下界要塞", "末地城", "掠夺者前哨站");
        dialog.setTitle(I18n.get("structure.copyVanilla.title"));
        dialog.setHeaderText(I18n.get("structure.copyVanilla.header"));
        dialog.setContentText(I18n.get("structure.copyVanilla.content"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            switch (selected) {
                case "村庄":
                    target.setRarity(25);
                    target.setCount(1);
                    target.setSpread(7);
                    target.setMinY(0);
                    target.setMaxY(255);
                    target.setBiomes("plains,desert,savanna,taiga");
                    break;
                case "沙漠神殿":
                    target.setRarity(10);
                    target.setCount(1);
                    target.setSpread(8);
                    target.setMinY(0);
                    target.setMaxY(80);
                    target.setBiomes("desert");
                    break;
                case "丛林神庙":
                    target.setRarity(15);
                    target.setCount(1);
                    target.setSpread(6);
                    target.setMinY(0);
                    target.setMaxY(80);
                    target.setBiomes("jungle");
                    break;
                case "海底遗迹":
                    target.setRarity(5);
                    target.setCount(1);
                    target.setSpread(12);
                    target.setMinY(30);
                    target.setMaxY(60);
                    target.setBiomes("deep_ocean");
                    break;
                case "下界要塞":
                    target.setRarity(8);
                    target.setCount(1);
                    target.setSpread(9);
                    target.setMinY(30);
                    target.setMaxY(80);
                    target.setBiomes("nether_wastes");
                    break;
                case "末地城":
                    target.setRarity(20);
                    target.setCount(1);
                    target.setSpread(10);
                    target.setMinY(0);
                    target.setMaxY(255);
                    target.setBiomes("end_highlands");
                    break;
                case "掠夺者前哨站":
                    target.setRarity(12);
                    target.setCount(1);
                    target.setSpread(8);
                    target.setMinY(0);
                    target.setMaxY(255);
                    target.setBiomes("plains,desert,savanna,taiga");
                    break;
            }
            showEditorForStructure(target);
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(target);
        });
    }

    private void deleteStructure(StructureData structure) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("structure.deleteConfirm"), structure.getDisplayName()));
        confirm.setContentText(I18n.get("structure.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            structures.remove(structure);
            if (structures.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onStructureChanged != null) onStructureChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("structure.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<StructureData> getStructures() { return structures; }
    public void setStructures(ObservableList<StructureData> newStructures) {
        this.structures.setAll(newStructures);
        if (structureListView.getSelectionModel().isEmpty() && !structures.isEmpty()) structureListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnStructureChanged(Consumer<StructureData> callback) { this.onStructureChanged = callback; }
    public void refresh() { structureListView.refresh(); }

    private static class StructureListCell extends ListCell<StructureData> {
        @Override
        protected void updateItem(StructureData structure, boolean empty) {
            super.updateItem(structure, empty);
            if (empty || structure == null) { setText(null); setGraphic(null); }
            else {
                String display = structure.getDisplayName();
                String id = structure.getId();
                setText(display + " [" + id + "]");
                FontIcon icon = FontIcon.of(FontAwesomeSolid.CITY);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}