package com.easyforge.ui;

import com.easyforge.model.SmeltingRecipeData;
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
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class SmeltingRecipeEditorPane extends VBox {

    private final ObservableList<SmeltingRecipeData> recipes = FXCollections.observableArrayList();
    private final FilteredList<SmeltingRecipeData> filteredRecipes = new FilteredList<>(recipes, p -> true);
    private final ListView<SmeltingRecipeData> recipeListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<SmeltingRecipeData> onRecipeChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    public SmeltingRecipeEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: transparent;");
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onRecipeChanged != null) onRecipeChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("smelting.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("smelting.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredRecipes.setPredicate(r -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return (r.getOutput() != null && r.getOutput().toLowerCase().contains(lower)) ||
                        (r.getId() != null && r.getId().toLowerCase().contains(lower));
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("smelting.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // 单元格换行和动态行高
        recipeListView.setCellFactory(lv -> new ListCell<SmeltingRecipeData>() {
            {
                setWrapText(true);
                setPrefWidth(Control.USE_COMPUTED_SIZE);
            }
            @Override
            protected void updateItem(SmeltingRecipeData recipe, boolean empty) {
                super.updateItem(recipe, empty);
                if (empty || recipe == null) {
                    setText(null);
                } else {
                    String output = recipe.getOutput();
                    String id = recipe.getId();
                    String display = (output != null && !output.isEmpty() ? output : "未设置") + " [" + id + "]";
                    setText(display);
                    if (display.length() > 40) setStyle("-fx-pref-height: 40;");
                    else setStyle("-fx-pref-height: 25;");
                }
            }
        });
        recipeListView.setItems(filteredRecipes);
        recipeListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForRecipe(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("smelting.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewRecipe());

        VBox leftBox = new VBox(5, listLabel, searchBox, recipeListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        recipes.addListener((ListChangeListener<SmeltingRecipeData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(r -> { if (onRecipeChanged != null) onRecipeChanged.accept(r); });
                if (c.wasRemoved()) c.getRemoved().forEach(r -> { if (onRecipeChanged != null) onRecipeChanged.accept(null); });
            }
        });
    }

    private void createNewRecipe() {
        SmeltingRecipeData newRecipe = new SmeltingRecipeData();
        newRecipe.setId("new_smelting_" + (recipes.size() + 1));
        recipes.add(newRecipe);
        filteredRecipes.setPredicate(null);
        recipeListView.getSelectionModel().select(newRecipe);
        recipeListView.scrollTo(newRecipe);
        markDirty();
    }

    private void showEditorForRecipe(SmeltingRecipeData recipe) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(recipe);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(SmeltingRecipeData recipe) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("smelting.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteRecipe(recipe));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(recipe);
        TitledPane recipePane = createRecipeCard(recipe);

        content.getChildren().addAll(titleBar, basicPane, recipePane);
        return content;
    }

    private TitledPane createBasicInfoCard(SmeltingRecipeData recipe) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField idField = new TextField(recipe.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = recipes.stream().filter(r -> r != recipe).anyMatch(r -> r.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            recipe.setId(val);
            markDirty(); if (onRecipeChanged != null) onRecipeChanged.accept(recipe);
            recipeListView.refresh();
        });
        addTooltip(idField, I18n.get("smelting.id.tip"));

        grid.add(createLabel(I18n.get("smelting.id")), 0, 0);
        grid.add(idField, 1, 0);

        TitledPane titled = new TitledPane(I18n.get("smelting.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createRecipeCard(SmeltingRecipeData recipe) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField inputField = createIdField(recipe.getInput(), val -> recipe.setInput(val));
        inputField.setPromptText(I18n.get("smelting.input.tip"));
        Button inputPicker = createPickerButton(I18n.get("idpicker.title.item"), VanillaIds.ITEMS, inputField);

        TextField outputField = createIdField(recipe.getOutput(), val -> recipe.setOutput(val));
        outputField.setPromptText(I18n.get("smelting.output.tip"));
        Button outputPicker = createPickerButton(I18n.get("idpicker.title.item"), VanillaIds.ITEMS, outputField);

        Spinner<Integer> countSpinner = new Spinner<>(1, 64, recipe.getCount());
        countSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            recipe.setCount(val);
            markDirty(); if (onRecipeChanged != null) onRecipeChanged.accept(recipe);
        });

        Spinner<Double> expSpinner = new Spinner<>(0.0, 100.0, (double) recipe.getExperience(), 0.1);
        expSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            recipe.setExperience(val.floatValue());
            markDirty(); if (onRecipeChanged != null) onRecipeChanged.accept(recipe);
        });

        Spinner<Integer> timeSpinner = new Spinner<>(1, 72000, recipe.getCookingTime());
        timeSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            recipe.setCookingTime(val);
            markDirty(); if (onRecipeChanged != null) onRecipeChanged.accept(recipe);
        });

        grid.add(createLabel(I18n.get("smelting.input")), 0, 0);
        grid.add(new HBox(5, inputField, inputPicker), 1, 0);
        grid.add(createLabel(I18n.get("smelting.output")), 2, 0);
        grid.add(new HBox(5, outputField, outputPicker), 3, 0);
        grid.add(createLabel(I18n.get("smelting.count")), 0, 1);
        grid.add(countSpinner, 1, 1);
        grid.add(createLabel(I18n.get("smelting.experience")), 2, 1);
        grid.add(expSpinner, 3, 1);
        grid.add(createLabel(I18n.get("smelting.cookingTime")), 0, 2);
        grid.add(timeSpinner, 1, 2);

        TitledPane titled = new TitledPane(I18n.get("smelting.recipe"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TextField createIdField(String initialValue, Consumer<String> setter) {
        TextField field = new TextField(initialValue);
        field.textProperty().addListener((obs, old, val) -> {
            setter.accept(val);
            markDirty(); if (onRecipeChanged != null) onRecipeChanged.accept(null);
        });
        field.setOnDragOver(this::handleDragOver);
        field.setOnDragDropped(e -> {
            String id = getDraggedItemId(e);
            if (id != null) field.setText(id);
            e.setDropCompleted(true);
            e.consume();
        });
        return field;
    }

    private Button createPickerButton(String title, java.util.List<VanillaIds.IdEntry> items, TextField target) {
        Button btn = new Button(I18n.get("smelting.pick"));
        btn.setGraphic(FontIcon.of(FontAwesomeSolid.SEARCH));
        btn.setOnAction(e -> {
            IdPickerDialog picker = new IdPickerDialog(title, items);
            picker.showAndWait().ifPresent(target::setText);
        });
        return btn;
    }

    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasString()) event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        event.consume();
    }

    private String getDraggedItemId(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasString()) {
            String id = db.getString();
            if (id.contains(":") || Pattern.matches("[a-z0-9_]+", id)) return id;
        }
        return null;
    }

    private void deleteRecipe(SmeltingRecipeData recipe) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("smelting.deleteConfirm"), recipe.getId()));
        confirm.setContentText(I18n.get("smelting.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            recipes.remove(recipe);
            if (recipes.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onRecipeChanged != null) onRecipeChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("smelting.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<SmeltingRecipeData> getRecipes() { return recipes; }
    public void setRecipes(ObservableList<SmeltingRecipeData> newRecipes) {
        this.recipes.setAll(newRecipes);
        if (recipeListView.getSelectionModel().isEmpty() && !recipes.isEmpty()) recipeListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnRecipeChanged(Consumer<SmeltingRecipeData> callback) { this.onRecipeChanged = callback; }
    public void refresh() { recipeListView.refresh(); }
}