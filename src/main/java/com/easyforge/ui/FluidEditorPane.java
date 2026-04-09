package com.easyforge.ui;

import com.easyforge.model.FluidData;
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

public class FluidEditorPane extends VBox {

    private final ObservableList<FluidData> fluids = FXCollections.observableArrayList();
    private final FilteredList<FluidData> filteredFluids = new FilteredList<>(fluids, p -> true);
    private final ListView<FluidData> fluidListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<FluidData> onFluidChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    public FluidEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onFluidChanged != null) onFluidChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("fluid.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("fluid.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredFluids.setPredicate(fluid -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return fluid.getDisplayName().toLowerCase().contains(lower) ||
                        fluid.getId().toLowerCase().contains(lower);
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("fluid.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        fluidListView.setCellFactory(lv -> new FluidListCell());
        fluidListView.setItems(filteredFluids);
        fluidListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForFluid(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("fluid.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewFluid());

        VBox leftBox = new VBox(5, listLabel, searchBox, fluidListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        fluids.addListener((ListChangeListener<FluidData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(f -> { if (onFluidChanged != null) onFluidChanged.accept(f); });
                if (c.wasRemoved()) c.getRemoved().forEach(f -> { if (onFluidChanged != null) onFluidChanged.accept(null); });
            }
        });
    }

    private void createNewFluid() {
        FluidData newFluid = new FluidData();
        newFluid.setId("new_fluid_" + (fluids.size() + 1));
        newFluid.setDisplayName(I18n.get("fluid.defaultName"));
        newFluid.setDensity(1000);
        newFluid.setViscosity(1000);
        fluids.add(newFluid);
        filteredFluids.setPredicate(null);
        fluidListView.getSelectionModel().select(newFluid);
        fluidListView.scrollTo(newFluid);
        markDirty();
    }

    private void showEditorForFluid(FluidData fluid) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(fluid);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(FluidData fluid) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("fluid.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteFluid(fluid));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(fluid);
        TitledPane physicalPane = createPhysicalCard(fluid);
        TitledPane texturePane = createTextureCard(fluid);

        content.getChildren().addAll(titleBar, basicPane, physicalPane, texturePane);
        return content;
    }

    private TitledPane createBasicInfoCard(FluidData fluid) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField displayField = new TextField(fluid.getDisplayName());
        displayField.textProperty().addListener((obs, old, val) -> {
            fluid.setDisplayName(val);
            markDirty(); if (onFluidChanged != null) onFluidChanged.accept(fluid);
        });
        addTooltip(displayField, I18n.get("fluid.displayName.tip"));

        TextField idField = new TextField(fluid.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = fluids.stream().filter(f -> f != fluid).anyMatch(f -> f.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            fluid.setId(val);
            markDirty(); if (onFluidChanged != null) onFluidChanged.accept(fluid);
            fluidListView.refresh();
        });
        addTooltip(idField, I18n.get("fluid.id.tip"));

        // 自动生成的字段（只读展示）
        TextField blockIdField = new TextField(fluid.getBlockId());
        blockIdField.setEditable(false);
        TextField bucketIdField = new TextField(fluid.getBucketId());
        bucketIdField.setEditable(false);

        grid.add(createLabel(I18n.get("fluid.displayName")), 0, 0);
        grid.add(displayField, 1, 0);
        grid.add(createLabel(I18n.get("fluid.id")), 2, 0);
        grid.add(idField, 3, 0);
        grid.add(createLabel(I18n.get("fluid.blockId")), 0, 1);
        grid.add(blockIdField, 1, 1);
        grid.add(createLabel(I18n.get("fluid.bucketId")), 2, 1);
        grid.add(bucketIdField, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("fluid.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createPhysicalCard(FluidData fluid) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        Spinner<Integer> densitySpinner = new Spinner<>(0, 5000, fluid.getDensity());
        densitySpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            fluid.setDensity(val);
            markDirty(); if (onFluidChanged != null) onFluidChanged.accept(fluid);
        });
        Spinner<Integer> viscositySpinner = new Spinner<>(0, 5000, fluid.getViscosity());
        viscositySpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            fluid.setViscosity(val);
            markDirty(); if (onFluidChanged != null) onFluidChanged.accept(fluid);
        });
        Spinner<Integer> tempSpinner = new Spinner<>(0, 2000, fluid.getTemperature());
        tempSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            fluid.setTemperature(val);
            markDirty(); if (onFluidChanged != null) onFluidChanged.accept(fluid);
        });
        Spinner<Integer> lightSpinner = new Spinner<>(0, 15, fluid.getLightLevel());
        lightSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            fluid.setLightLevel(val);
            markDirty(); if (onFluidChanged != null) onFluidChanged.accept(fluid);
        });
        CheckBox luminantCheck = new CheckBox(I18n.get("fluid.luminant"));
        luminantCheck.setSelected(fluid.isLuminant());
        luminantCheck.selectedProperty().addListener((obs, old, val) -> {
            fluid.setLuminant(val);
            markDirty(); if (onFluidChanged != null) onFluidChanged.accept(fluid);
        });
        CheckBox gaseousCheck = new CheckBox(I18n.get("fluid.gaseous"));
        gaseousCheck.setSelected(fluid.isGaseous());
        gaseousCheck.selectedProperty().addListener((obs, old, val) -> {
            fluid.setGaseous(val);
            markDirty(); if (onFluidChanged != null) onFluidChanged.accept(fluid);
        });

        grid.add(createLabel(I18n.get("fluid.density")), 0, 0);
        grid.add(densitySpinner, 1, 0);
        grid.add(createLabel(I18n.get("fluid.viscosity")), 2, 0);
        grid.add(viscositySpinner, 3, 0);
        grid.add(createLabel(I18n.get("fluid.temperature")), 0, 1);
        grid.add(tempSpinner, 1, 1);
        grid.add(createLabel(I18n.get("fluid.lightLevel")), 2, 1);
        grid.add(lightSpinner, 3, 1);
        grid.add(luminantCheck, 0, 2);
        grid.add(gaseousCheck, 1, 2);

        TitledPane titled = new TitledPane(I18n.get("fluid.physical"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createTextureCard(FluidData fluid) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField stillField = new TextField(fluid.getTextureStill());
        stillField.textProperty().addListener((obs, old, val) -> {
            fluid.setTextureStill(val);
            markDirty(); if (onFluidChanged != null) onFluidChanged.accept(fluid);
        });
        TextField flowingField = new TextField(fluid.getTextureFlowing());
        flowingField.textProperty().addListener((obs, old, val) -> {
            fluid.setTextureFlowing(val);
            markDirty(); if (onFluidChanged != null) onFluidChanged.accept(fluid);
        });

        Button stillBrowse = new Button(I18n.get("browse"));
        stillBrowse.setOnAction(e -> browseTexture(stillField));
        Button flowingBrowse = new Button(I18n.get("browse"));
        flowingBrowse.setOnAction(e -> browseTexture(flowingField));

        HBox stillBox = new HBox(5, stillField, stillBrowse);
        HBox flowingBox = new HBox(5, flowingField, flowingBrowse);
        stillBox.setPrefWidth(400);
        flowingBox.setPrefWidth(400);

        grid.add(createLabel(I18n.get("fluid.stillTexture")), 0, 0);
        grid.add(stillBox, 1, 0);
        grid.add(createLabel(I18n.get("fluid.flowingTexture")), 0, 1);
        grid.add(flowingBox, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("fluid.texture"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(false);
        return titled;
    }

    private void browseTexture(TextField target) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG图片", "*.png"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) target.setText(file.getAbsolutePath());
    }

    private void deleteFluid(FluidData fluid) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("fluid.deleteConfirm"), fluid.getDisplayName()));
        confirm.setContentText(I18n.get("fluid.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            fluids.remove(fluid);
            if (fluids.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onFluidChanged != null) onFluidChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("fluid.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<FluidData> getFluids() { return fluids; }
    public void setFluids(ObservableList<FluidData> newFluids) {
        this.fluids.setAll(newFluids);
        if (fluidListView.getSelectionModel().isEmpty() && !fluids.isEmpty()) fluidListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnFluidChanged(Consumer<FluidData> callback) { this.onFluidChanged = callback; }
    public void refresh() { fluidListView.refresh(); }

    private static class FluidListCell extends ListCell<FluidData> {
        @Override
        protected void updateItem(FluidData fluid, boolean empty) {
            super.updateItem(fluid, empty);
            if (empty || fluid == null) { setText(null); setGraphic(null); }
            else {
                String display = fluid.getDisplayName();
                String id = fluid.getId();
                setText(display + " [" + id + "]");
                FontIcon icon = FontIcon.of(FontAwesomeSolid.TINT);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}