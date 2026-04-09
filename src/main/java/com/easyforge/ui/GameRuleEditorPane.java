package com.easyforge.ui;

import com.easyforge.model.GameRuleData;
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

public class GameRuleEditorPane extends VBox {

    private final ObservableList<GameRuleData> gameRules = FXCollections.observableArrayList();
    private final FilteredList<GameRuleData> filteredRules = new FilteredList<>(gameRules, p -> true);
    private final ListView<GameRuleData> ruleListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<GameRuleData> onGameRuleChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_.]+");

    public GameRuleEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onGameRuleChanged != null) onGameRuleChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("gamerule.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("gamerule.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredRules.setPredicate(rule -> {
                if (val == null || val.isEmpty()) return true;
                return rule.getId().toLowerCase().contains(val.toLowerCase());
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("gamerule.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        ruleListView.setCellFactory(lv -> new GameRuleListCell());
        ruleListView.setItems(filteredRules);
        ruleListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForGameRule(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("gamerule.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewGameRule());

        VBox leftBox = new VBox(5, listLabel, searchBox, ruleListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        gameRules.addListener((ListChangeListener<GameRuleData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(r -> { if (onGameRuleChanged != null) onGameRuleChanged.accept(r); });
                if (c.wasRemoved()) c.getRemoved().forEach(r -> { if (onGameRuleChanged != null) onGameRuleChanged.accept(null); });
            }
        });
    }

    private void createNewGameRule() {
        GameRuleData newRule = new GameRuleData();
        newRule.setId("new_gamerule_" + (gameRules.size() + 1));
        newRule.setType("boolean");
        newRule.setDefaultValue(true);
        gameRules.add(newRule);
        filteredRules.setPredicate(null);
        ruleListView.getSelectionModel().select(newRule);
        ruleListView.scrollTo(newRule);
        markDirty();
    }

    private void showEditorForGameRule(GameRuleData rule) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(rule);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(GameRuleData rule) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("gamerule.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteGameRule(rule));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(rule);
        TitledPane valuePane = createValueCard(rule);

        content.getChildren().addAll(titleBar, basicPane, valuePane);
        return content;
    }

    private TitledPane createBasicInfoCard(GameRuleData rule) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField idField = new TextField(rule.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = gameRules.stream().filter(r -> r != rule).anyMatch(r -> r.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            rule.setId(val);
            markDirty(); if (onGameRuleChanged != null) onGameRuleChanged.accept(rule);
            ruleListView.refresh();
        });
        addTooltip(idField, I18n.get("gamerule.id.tip"));

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("boolean", "int", "float"));
        typeCombo.setValue(rule.getType());
        typeCombo.valueProperty().addListener((obs, old, val) -> {
            rule.setType(val);
            markDirty(); if (onGameRuleChanged != null) onGameRuleChanged.accept(rule);
            showEditorForGameRule(rule);
        });

        TextArea descArea = new TextArea(rule.getDescription());
        descArea.setPrefRowCount(2);
        descArea.textProperty().addListener((obs, old, val) -> {
            rule.setDescription(val);
            markDirty(); if (onGameRuleChanged != null) onGameRuleChanged.accept(rule);
        });

        grid.add(createLabel(I18n.get("gamerule.id")), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(createLabel(I18n.get("gamerule.type")), 2, 0);
        grid.add(typeCombo, 3, 0);
        grid.add(createLabel(I18n.get("gamerule.description")), 0, 1);
        grid.add(descArea, 1, 1, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("gamerule.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createValueCard(GameRuleData rule) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        String type = rule.getType();
        Node defaultValueNode;

        if ("boolean".equals(type)) {
            CheckBox boolCheck = new CheckBox(I18n.get("gamerule.defaultValue"));
            boolCheck.setSelected(rule.isDefaultValue());
            boolCheck.selectedProperty().addListener((obs, old, val) -> {
                rule.setDefaultValue(val);
                markDirty(); if (onGameRuleChanged != null) onGameRuleChanged.accept(rule);
            });
            defaultValueNode = boolCheck;
        } else if ("int".equals(type)) {
            Spinner<Integer> intSpinner = new Spinner<>(Integer.MIN_VALUE, Integer.MAX_VALUE, rule.getDefaultInt());
            intSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
                rule.setDefaultInt(val);
                markDirty(); if (onGameRuleChanged != null) onGameRuleChanged.accept(rule);
            });
            defaultValueNode = intSpinner;
        } else {
            Spinner<Double> floatSpinner = new Spinner<>(-Float.MAX_VALUE, Float.MAX_VALUE, (double) rule.getDefaultFloat(), 0.1);
            floatSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
                rule.setDefaultFloat(val.floatValue());
                markDirty(); if (onGameRuleChanged != null) onGameRuleChanged.accept(rule);
            });
            defaultValueNode = floatSpinner;
        }

        grid.add(createLabel(I18n.get("gamerule.defaultValue")), 0, 0);
        grid.add(defaultValueNode, 1, 0);

        TitledPane titled = new TitledPane(I18n.get("gamerule.value"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private void deleteGameRule(GameRuleData rule) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("gamerule.deleteConfirm"), rule.getId()));
        confirm.setContentText(I18n.get("gamerule.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            gameRules.remove(rule);
            if (gameRules.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onGameRuleChanged != null) onGameRuleChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("gamerule.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<GameRuleData> getGameRules() { return gameRules; }
    public void setGameRules(ObservableList<GameRuleData> newRules) {
        this.gameRules.setAll(newRules);
        if (ruleListView.getSelectionModel().isEmpty() && !gameRules.isEmpty()) ruleListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnGameRuleChanged(Consumer<GameRuleData> callback) { this.onGameRuleChanged = callback; }
    public void refresh() { ruleListView.refresh(); }

    private static class GameRuleListCell extends ListCell<GameRuleData> {
        @Override
        protected void updateItem(GameRuleData rule, boolean empty) {
            super.updateItem(rule, empty);
            if (empty || rule == null) { setText(null); setGraphic(null); }
            else {
                String id = rule.getId();
                String type = rule.getType();
                setText(id + " (" + type + ")");
                FontIcon icon = FontIcon.of(FontAwesomeSolid.COG);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}