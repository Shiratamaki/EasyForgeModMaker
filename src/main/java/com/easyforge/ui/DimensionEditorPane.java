package com.easyforge.ui;

import com.easyforge.model.DimensionData;
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

public class DimensionEditorPane extends VBox {

    private final ObservableList<DimensionData> dimensions = FXCollections.observableArrayList();
    private final FilteredList<DimensionData> filteredDimensions = new FilteredList<>(dimensions, p -> true);
    private final ListView<DimensionData> dimensionListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<DimensionData> onDimensionChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    public DimensionEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onDimensionChanged != null) onDimensionChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("dimension.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("dimension.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredDimensions.setPredicate(d -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return d.getDisplayName().toLowerCase().contains(lower) ||
                        d.getId().toLowerCase().contains(lower);
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("dimension.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        dimensionListView.setCellFactory(lv -> new DimensionListCell());
        dimensionListView.setItems(filteredDimensions);
        dimensionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForDimension(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("dimension.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewDimension());

        VBox leftBox = new VBox(5, listLabel, searchBox, dimensionListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        dimensions.addListener((ListChangeListener<DimensionData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(d -> { if (onDimensionChanged != null) onDimensionChanged.accept(d); });
                if (c.wasRemoved()) c.getRemoved().forEach(d -> { if (onDimensionChanged != null) onDimensionChanged.accept(null); });
            }
        });
    }

    private void createNewDimension() {
        DimensionData newDim = new DimensionData();
        newDim.setId("new_dimension_" + (dimensions.size() + 1));
        newDim.setDisplayName(I18n.get("dimension.defaultName"));
        dimensions.add(newDim);
        filteredDimensions.setPredicate(null);
        dimensionListView.getSelectionModel().select(newDim);
        dimensionListView.scrollTo(newDim);
        markDirty();
    }

    private void showEditorForDimension(DimensionData dimension) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(dimension);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(DimensionData dimension) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("dimension.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteDimension(dimension));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(dimension);
        TitledPane environmentPane = createEnvironmentCard(dimension);
        TitledPane spawnPane = createSpawnCard(dimension);
        TitledPane effectsPane = createEffectsCard(dimension);

        content.getChildren().addAll(titleBar, basicPane, environmentPane, spawnPane, effectsPane);
        return content;
    }

    private TitledPane createBasicInfoCard(DimensionData dimension) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField displayField = new TextField(dimension.getDisplayName());
        displayField.textProperty().addListener((obs, old, val) -> {
            dimension.setDisplayName(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });
        addTooltip(displayField, I18n.get("dimension.displayName.tip"));

        TextField idField = new TextField(dimension.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = dimensions.stream().filter(d -> d != dimension).anyMatch(d -> d.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            dimension.setId(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
            dimensionListView.refresh();
        });
        addTooltip(idField, I18n.get("dimension.id.tip"));

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("OVERWORLD", "NETHER", "END"));
        typeCombo.setValue(dimension.getType());
        typeCombo.valueProperty().addListener((obs, old, val) -> {
            dimension.setType(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });

        grid.add(createLabel(I18n.get("dimension.displayName")), 0, 0);
        grid.add(displayField, 1, 0);
        grid.add(createLabel(I18n.get("dimension.id")), 2, 0);
        grid.add(idField, 3, 0);
        grid.add(createLabel(I18n.get("dimension.type")), 0, 1);
        grid.add(typeCombo, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("dimension.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createEnvironmentCard(DimensionData dimension) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        Spinner<Integer> seaLevelSpinner = new Spinner<>(0, 255, dimension.getSeaLevel());
        seaLevelSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            dimension.setSeaLevel(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });

        CheckBox skyLightCheck = new CheckBox(I18n.get("dimension.skyLight"));
        skyLightCheck.setSelected(dimension.isHasSkyLight());
        skyLightCheck.selectedProperty().addListener((obs, old, val) -> {
            dimension.setHasSkyLight(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });
        CheckBox ceilingCheck = new CheckBox(I18n.get("dimension.ceiling"));
        ceilingCheck.setSelected(dimension.isHasCeiling());
        ceilingCheck.selectedProperty().addListener((obs, old, val) -> {
            dimension.setHasCeiling(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });
        CheckBox ultrawarmCheck = new CheckBox(I18n.get("dimension.ultrawarm"));
        ultrawarmCheck.setSelected(dimension.isUltrawarm());
        ultrawarmCheck.selectedProperty().addListener((obs, old, val) -> {
            dimension.setUltrawarm(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });
        CheckBox naturalCheck = new CheckBox(I18n.get("dimension.natural"));
        naturalCheck.setSelected(dimension.isNatural());
        naturalCheck.selectedProperty().addListener((obs, old, val) -> {
            dimension.setNatural(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });
        CheckBox piglinSafeCheck = new CheckBox(I18n.get("dimension.piglinSafe"));
        piglinSafeCheck.setSelected(dimension.isPiglinSafe());
        piglinSafeCheck.selectedProperty().addListener((obs, old, val) -> {
            dimension.setPiglinSafe(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });

        grid.add(createLabel(I18n.get("dimension.seaLevel")), 0, 0);
        grid.add(seaLevelSpinner, 1, 0);
        grid.add(skyLightCheck, 2, 0);
        grid.add(ceilingCheck, 3, 0);
        grid.add(ultrawarmCheck, 0, 1);
        grid.add(naturalCheck, 1, 1);
        grid.add(piglinSafeCheck, 2, 1);

        TitledPane titled = new TitledPane(I18n.get("dimension.environment"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createSpawnCard(DimensionData dimension) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        CheckBox bedCheck = new CheckBox(I18n.get("dimension.bedWorks"));
        bedCheck.setSelected(dimension.isBedWorks());
        bedCheck.selectedProperty().addListener((obs, old, val) -> {
            dimension.setBedWorks(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });
        CheckBox respawnCheck = new CheckBox(I18n.get("dimension.respawnAnchorWorks"));
        respawnCheck.setSelected(dimension.isRespawnAnchorWorks());
        respawnCheck.selectedProperty().addListener((obs, old, val) -> {
            dimension.setRespawnAnchorWorks(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });
        CheckBox raidsCheck = new CheckBox(I18n.get("dimension.hasRaids"));
        raidsCheck.setSelected(dimension.isHasRaids());
        raidsCheck.selectedProperty().addListener((obs, old, val) -> {
            dimension.setHasRaids(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });

        grid.add(bedCheck, 0, 0);
        grid.add(respawnCheck, 1, 0);
        grid.add(raidsCheck, 2, 0);

        TitledPane titled = new TitledPane(I18n.get("dimension.spawn"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createEffectsCard(DimensionData dimension) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        Spinner<Integer> lightLevelSpinner = new Spinner<>(0, 15, dimension.getMonsterSpawnLightLevel());
        lightLevelSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            dimension.setMonsterSpawnLightLevel(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });
        Spinner<Integer> blockLightSpinner = new Spinner<>(0, 15, dimension.getMonsterSpawnBlockLightLimit());
        blockLightSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            dimension.setMonsterSpawnBlockLightLimit(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });

        TextField infiniburnField = new TextField(dimension.getInfiniburn());
        infiniburnField.textProperty().addListener((obs, old, val) -> {
            dimension.setInfiniburn(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });
        TextField effectsField = new TextField(dimension.getEffects());
        effectsField.textProperty().addListener((obs, old, val) -> {
            dimension.setEffects(val);
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(dimension);
        });

        grid.add(createLabel(I18n.get("dimension.monsterSpawnLightLevel")), 0, 0);
        grid.add(lightLevelSpinner, 1, 0);
        grid.add(createLabel(I18n.get("dimension.monsterSpawnBlockLightLimit")), 2, 0);
        grid.add(blockLightSpinner, 3, 0);
        grid.add(createLabel(I18n.get("dimension.infiniburn")), 0, 1);
        grid.add(infiniburnField, 1, 1, 3, 1);
        grid.add(createLabel(I18n.get("dimension.effects")), 0, 2);
        grid.add(effectsField, 1, 2, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("dimension.effects"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private void deleteDimension(DimensionData dimension) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("dimension.deleteConfirm"), dimension.getDisplayName()));
        confirm.setContentText(I18n.get("dimension.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dimensions.remove(dimension);
            if (dimensions.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onDimensionChanged != null) onDimensionChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("dimension.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<DimensionData> getDimensions() { return dimensions; }
    public void setDimensions(ObservableList<DimensionData> newDimensions) {
        this.dimensions.setAll(newDimensions);
        if (dimensionListView.getSelectionModel().isEmpty() && !dimensions.isEmpty()) dimensionListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnDimensionChanged(Consumer<DimensionData> callback) { this.onDimensionChanged = callback; }
    public void refresh() { dimensionListView.refresh(); }

    private static class DimensionListCell extends ListCell<DimensionData> {
        @Override
        protected void updateItem(DimensionData dimension, boolean empty) {
            super.updateItem(dimension, empty);
            if (empty || dimension == null) { setText(null); setGraphic(null); }
            else {
                String display = dimension.getDisplayName();
                String id = dimension.getId();
                setText(display + " [" + id + "]");
                FontIcon icon = FontIcon.of(FontAwesomeSolid.GLOBE);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}