package com.easyforge.ui;

import com.easyforge.model.BrewingRecipeData;
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

public class BrewingRecipeEditorPane extends VBox {

    private final ObservableList<BrewingRecipeData> recipes = FXCollections.observableArrayList();
    private final FilteredList<BrewingRecipeData> filteredRecipes = new FilteredList<>(recipes, p -> true);
    private final ListView<BrewingRecipeData> recipeListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<BrewingRecipeData> onRecipeChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    public BrewingRecipeEditorPane() {
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
        Label listLabel = new Label(I18n.get("brewing.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("brewing.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredRecipes.setPredicate(r -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return (r.getResult() != null && r.getResult().toLowerCase().contains(lower)) ||
                        (r.getId() != null && r.getId().toLowerCase().contains(lower));
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("brewing.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // 单元格换行和动态行高
        recipeListView.setCellFactory(lv -> new ListCell<BrewingRecipeData>() {
            {
                setWrapText(true);
                setPrefWidth(Control.USE_COMPUTED_SIZE);
            }
            @Override
            protected void updateItem(BrewingRecipeData recipe, boolean empty) {
                super.updateItem(recipe, empty);
                if (empty || recipe == null) {
                    setText(null);
                } else {
                    String result = recipe.getResult();
                    String id = recipe.getId();
                    String display = (result != null && !result.isEmpty() ? result : "未设置") + " [" + id + "]";
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

        Button newBtn = new Button(I18n.get("brewing.new"));
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

        recipes.addListener((ListChangeListener<BrewingRecipeData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(r -> { if (onRecipeChanged != null) onRecipeChanged.accept(r); });
                if (c.wasRemoved()) c.getRemoved().forEach(r -> { if (onRecipeChanged != null) onRecipeChanged.accept(null); });
            }
        });
    }

    private void createNewRecipe() {
        BrewingRecipeData newRecipe = new BrewingRecipeData();
        newRecipe.setId("new_brewing_" + (recipes.size() + 1));
        recipes.add(newRecipe);
        filteredRecipes.setPredicate(null);
        recipeListView.getSelectionModel().select(newRecipe);
        recipeListView.scrollTo(newRecipe);
        markDirty();
    }

    private void showEditorForRecipe(BrewingRecipeData recipe) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(recipe);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(BrewingRecipeData recipe) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("brewing.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteRecipe(recipe));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(recipe);
        TitledPane recipePane = createRecipeCard(recipe);

        content.getChildren().addAll(titleBar, basicPane, recipePane);
        return content;
    }

    private TitledPane createBasicInfoCard(BrewingRecipeData recipe) {
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
        addTooltip(idField, I18n.get("brewing.id.tip"));

        grid.add(createLabel(I18n.get("brewing.id")), 0, 0);
        grid.add(idField, 1, 0);

        TitledPane titled = new TitledPane(I18n.get("brewing.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createRecipeCard(BrewingRecipeData recipe) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField inputField = createIdField(recipe.getInput(), val -> recipe.setInput(val));
        inputField.setPromptText(I18n.get("brewing.input.tip"));
        Button inputPicker = createPickerButton(I18n.get("idpicker.title.item"), VanillaIds.ITEMS, inputField);

        TextField ingredientField = createIdField(recipe.getIngredient(), val -> recipe.setIngredient(val));
        ingredientField.setPromptText(I18n.get("brewing.ingredient.tip"));
        Button ingredientPicker = createPickerButton(I18n.get("idpicker.title.item"), VanillaIds.ITEMS, ingredientField);

        TextField resultField = createIdField(recipe.getResult(), val -> recipe.setResult(val));
        resultField.setPromptText(I18n.get("brewing.result.tip"));
        Button resultPicker = createPickerButton(I18n.get("idpicker.title.item"), VanillaIds.ITEMS, resultField);

        Spinner<Integer> countSpinner = new Spinner<>(1, 64, recipe.getCount());
        countSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            recipe.setCount(val);
            markDirty(); if (onRecipeChanged != null) onRecipeChanged.accept(recipe);
        });

        grid.add(createLabel(I18n.get("brewing.input")), 0, 0);
        grid.add(new HBox(5, inputField, inputPicker), 1, 0);
        grid.add(createLabel(I18n.get("brewing.ingredient")), 2, 0);
        grid.add(new HBox(5, ingredientField, ingredientPicker), 3, 0);
        grid.add(createLabel(I18n.get("brewing.result")), 0, 1);
        grid.add(new HBox(5, resultField, resultPicker), 1, 1);
        grid.add(createLabel(I18n.get("brewing.count")), 2, 1);
        grid.add(countSpinner, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("brewing.recipe"), grid);
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
        Button btn = new Button(I18n.get("brewing.pick"));
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

    private void deleteRecipe(BrewingRecipeData recipe) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("brewing.deleteConfirm"), recipe.getId()));
        confirm.setContentText(I18n.get("brewing.deleteConfirmDetail"));
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
        Label label = new Label(I18n.get("brewing.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<BrewingRecipeData> getRecipes() { return recipes; }
    public void setRecipes(ObservableList<BrewingRecipeData> newRecipes) {
        this.recipes.setAll(newRecipes);
        if (recipeListView.getSelectionModel().isEmpty() && !recipes.isEmpty()) recipeListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnRecipeChanged(Consumer<BrewingRecipeData> callback) { this.onRecipeChanged = callback; }
    public void refresh() { recipeListView.refresh(); }
}