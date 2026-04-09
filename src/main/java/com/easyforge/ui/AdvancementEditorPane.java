package com.easyforge.ui;

import com.easyforge.model.AdvancementData;
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
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class AdvancementEditorPane extends VBox {

    private final ObservableList<AdvancementData> advancements = FXCollections.observableArrayList();
    private final FilteredList<AdvancementData> filteredAdvancements = new FilteredList<>(advancements, p -> true);
    private final ListView<AdvancementData> advancementListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<AdvancementData> onAdvancementChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_/]+");

    public AdvancementEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: transparent;");
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onAdvancementChanged != null) onAdvancementChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("advancement.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("advancement.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredAdvancements.setPredicate(a -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return a.getDisplayName().toLowerCase().contains(lower) ||
                        a.getId().toLowerCase().contains(lower);
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("advancement.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // 单元格换行和动态行高
        advancementListView.setCellFactory(lv -> new ListCell<AdvancementData>() {
            {
                setWrapText(true);
                setPrefWidth(Control.USE_COMPUTED_SIZE);
            }
            @Override
            protected void updateItem(AdvancementData advancement, boolean empty) {
                super.updateItem(advancement, empty);
                if (empty || advancement == null) {
                    setText(null);
                } else {
                    String display = advancement.getDisplayName();
                    String id = advancement.getId();
                    String text = display + " [" + id + "]";
                    setText(text);
                    if (text.length() > 40) setStyle("-fx-pref-height: 40;");
                    else setStyle("-fx-pref-height: 25;");
                }
            }
        });
        advancementListView.setItems(filteredAdvancements);
        advancementListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForAdvancement(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("advancement.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewAdvancement());

        VBox leftBox = new VBox(5, listLabel, searchBox, advancementListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        advancements.addListener((ListChangeListener<AdvancementData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(a -> { if (onAdvancementChanged != null) onAdvancementChanged.accept(a); });
                if (c.wasRemoved()) c.getRemoved().forEach(a -> { if (onAdvancementChanged != null) onAdvancementChanged.accept(null); });
            }
        });
    }

    private void createNewAdvancement() {
        AdvancementData newAdv = new AdvancementData();
        newAdv.setId("advancements/new_advancement_" + (advancements.size() + 1));
        newAdv.setDisplayName(I18n.get("advancement.defaultName"));
        advancements.add(newAdv);
        filteredAdvancements.setPredicate(null);
        advancementListView.getSelectionModel().select(newAdv);
        advancementListView.scrollTo(newAdv);
        markDirty();
    }

    private void showEditorForAdvancement(AdvancementData advancement) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(advancement);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(AdvancementData advancement) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("advancement.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteAdvancement(advancement));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(advancement);
        TitledPane displayPane = createDisplayCard(advancement);
        TitledPane triggerPane = createTriggerCard(advancement);
        TitledPane rewardPane = createRewardCard(advancement);

        content.getChildren().addAll(titleBar, basicPane, displayPane, triggerPane, rewardPane);
        return content;
    }

    private TitledPane createBasicInfoCard(AdvancementData advancement) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField idField = new TextField(advancement.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = advancements.stream().filter(a -> a != advancement).anyMatch(a -> a.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            advancement.setId(val);
            markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
            advancementListView.refresh();
        });
        addTooltip(idField, I18n.get("advancement.id.tip"));

        TextField parentField = new TextField(advancement.getParent());
        parentField.textProperty().addListener((obs, old, val) -> {
            advancement.setParent(val);
            markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
        });
        addTooltip(parentField, I18n.get("advancement.parent.tip"));

        grid.add(createLabel(I18n.get("advancement.id")), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(createLabel(I18n.get("advancement.parent")), 2, 0);
        grid.add(parentField, 3, 0);

        TitledPane titled = new TitledPane(I18n.get("advancement.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createDisplayCard(AdvancementData advancement) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField titleField = new TextField(advancement.getDisplayName());
        titleField.textProperty().addListener((obs, old, val) -> {
            advancement.setDisplayName(val);
            markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
        });
        addTooltip(titleField, I18n.get("advancement.title.tip"));

        TextField descField = new TextField(advancement.getDescription());
        descField.textProperty().addListener((obs, old, val) -> {
            advancement.setDescription(val);
            markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
        });
        addTooltip(descField, I18n.get("advancement.description.tip"));

        TextField iconField = new TextField(advancement.getIcon());
        iconField.textProperty().addListener((obs, old, val) -> {
            advancement.setIcon(val);
            markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
        });
        addTooltip(iconField, I18n.get("advancement.icon.tip"));

        Button iconPicker = new Button(I18n.get("advancement.pickIcon"));
        iconPicker.setGraphic(FontIcon.of(FontAwesomeSolid.SEARCH));
        iconPicker.setOnAction(e -> {
            IdPickerDialog picker = new IdPickerDialog(I18n.get("idpicker.title.item"), VanillaIds.ITEMS);
            picker.showAndWait().ifPresent(iconField::setText);
        });
        HBox iconBox = new HBox(5, iconField, iconPicker);
        iconBox.setPrefWidth(400);

        ComboBox<String> frameCombo = new ComboBox<>(FXCollections.observableArrayList("task", "challenge", "goal"));
        frameCombo.setValue(advancement.getFrame());
        frameCombo.valueProperty().addListener((obs, old, val) -> {
            advancement.setFrame(val);
            markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
        });

        CheckBox toastCheck = new CheckBox(I18n.get("advancement.showToast"));
        toastCheck.setSelected(advancement.isShowToast());
        toastCheck.selectedProperty().addListener((obs, old, val) -> {
            advancement.setShowToast(val);
            markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
        });
        CheckBox announceCheck = new CheckBox(I18n.get("advancement.announceToChat"));
        announceCheck.setSelected(advancement.isAnnounceToChat());
        announceCheck.selectedProperty().addListener((obs, old, val) -> {
            advancement.setAnnounceToChat(val);
            markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
        });
        CheckBox hiddenCheck = new CheckBox(I18n.get("advancement.hidden"));
        hiddenCheck.setSelected(advancement.isHidden());
        hiddenCheck.selectedProperty().addListener((obs, old, val) -> {
            advancement.setHidden(val);
            markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
        });

        grid.add(createLabel(I18n.get("advancement.title")), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(createLabel(I18n.get("advancement.description")), 2, 0);
        grid.add(descField, 3, 0);
        grid.add(createLabel(I18n.get("advancement.icon")), 0, 1);
        grid.add(iconBox, 1, 1);
        grid.add(createLabel(I18n.get("advancement.frame")), 2, 1);
        grid.add(frameCombo, 3, 1);
        grid.add(toastCheck, 0, 2);
        grid.add(announceCheck, 1, 2);
        grid.add(hiddenCheck, 2, 2);

        TitledPane titled = new TitledPane(I18n.get("advancement.display"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createTriggerCard(AdvancementData advancement) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField triggerField = new TextField(advancement.getTrigger());
        triggerField.textProperty().addListener((obs, old, val) -> {
            advancement.setTrigger(val);
            markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
        });
        addTooltip(triggerField, I18n.get("advancement.trigger.tip"));

        ListView<String> criteriaList = new ListView<>();
        criteriaList.setItems(FXCollections.observableArrayList(advancement.getCriteria()));
        criteriaList.setPrefHeight(100);
        TextField newCriteriaField = new TextField();
        newCriteriaField.setPromptText(I18n.get("advancement.addCriteria"));
        Button addCriteriaBtn = new Button(I18n.get("btn.add"));
        addCriteriaBtn.setOnAction(e -> {
            String val = newCriteriaField.getText().trim();
            if (!val.isEmpty()) {
                advancement.getCriteria().add(val);
                criteriaList.getItems().setAll(advancement.getCriteria());
                newCriteriaField.clear();
                markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
            }
        });
        Button removeCriteriaBtn = new Button(I18n.get("btn.remove"));
        removeCriteriaBtn.setOnAction(e -> {
            String selected = criteriaList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                advancement.getCriteria().remove(selected);
                criteriaList.getItems().setAll(advancement.getCriteria());
                markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
            }
        });
        HBox criteriaControl = new HBox(5, newCriteriaField, addCriteriaBtn, removeCriteriaBtn);

        grid.add(createLabel(I18n.get("advancement.trigger")), 0, 0);
        grid.add(triggerField, 1, 0, 3, 1);
        grid.add(createLabel(I18n.get("advancement.criteria")), 0, 1);
        grid.add(criteriaList, 1, 1, 3, 1);
        grid.add(criteriaControl, 1, 2, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("advancement.triggerConditions"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createRewardCard(AdvancementData advancement) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        ListView<String> rewardsList = new ListView<>();
        rewardsList.setItems(FXCollections.observableArrayList(advancement.getRewards()));
        rewardsList.setPrefHeight(100);
        TextField newRewardField = new TextField();
        newRewardField.setPromptText(I18n.get("advancement.addReward"));
        Button addRewardBtn = new Button(I18n.get("btn.add"));
        addRewardBtn.setOnAction(e -> {
            String val = newRewardField.getText().trim();
            if (!val.isEmpty()) {
                advancement.getRewards().add(val);
                rewardsList.getItems().setAll(advancement.getRewards());
                newRewardField.clear();
                markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
            }
        });
        Button removeRewardBtn = new Button(I18n.get("btn.remove"));
        removeRewardBtn.setOnAction(e -> {
            String selected = rewardsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                advancement.getRewards().remove(selected);
                rewardsList.getItems().setAll(advancement.getRewards());
                markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(advancement);
            }
        });
        HBox rewardControl = new HBox(5, newRewardField, addRewardBtn, removeRewardBtn);

        grid.add(createLabel(I18n.get("advancement.rewards")), 0, 0);
        grid.add(rewardsList, 1, 0, 3, 1);
        grid.add(rewardControl, 1, 1, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("advancement.rewards"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private void deleteAdvancement(AdvancementData advancement) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("advancement.deleteConfirm"), advancement.getDisplayName()));
        confirm.setContentText(I18n.get("advancement.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            advancements.remove(advancement);
            if (advancements.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onAdvancementChanged != null) onAdvancementChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("advancement.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<AdvancementData> getAdvancements() { return advancements; }
    public void setAdvancements(ObservableList<AdvancementData> newAdvancements) {
        this.advancements.setAll(newAdvancements);
        if (advancementListView.getSelectionModel().isEmpty() && !advancements.isEmpty()) advancementListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnAdvancementChanged(Consumer<AdvancementData> callback) { this.onAdvancementChanged = callback; }
    public void refresh() { advancementListView.refresh(); }
}