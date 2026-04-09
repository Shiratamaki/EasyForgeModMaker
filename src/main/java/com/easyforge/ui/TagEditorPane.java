package com.easyforge.ui;

import com.easyforge.model.TagData;
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

public class TagEditorPane extends VBox {

    private final ObservableList<TagData> tags = FXCollections.observableArrayList();
    private final FilteredList<TagData> filteredTags = new FilteredList<>(tags, p -> true);
    private final ListView<TagData> tagListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<TagData> onTagChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_/:.]+");

    public TagEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onTagChanged != null) onTagChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("tag.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("tag.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredTags.setPredicate(tag -> {
                if (val == null || val.isEmpty()) return true;
                return tag.getId().toLowerCase().contains(val.toLowerCase());
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("tag.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        tagListView.setCellFactory(lv -> new TagListCell());
        tagListView.setItems(filteredTags);
        tagListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForTag(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("tag.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewTag());

        VBox leftBox = new VBox(5, listLabel, searchBox, tagListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        tags.addListener((ListChangeListener<TagData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(t -> { if (onTagChanged != null) onTagChanged.accept(t); });
                if (c.wasRemoved()) c.getRemoved().forEach(t -> { if (onTagChanged != null) onTagChanged.accept(null); });
            }
        });
    }

    private void createNewTag() {
        TagData newTag = new TagData();
        newTag.setId("minecraft:tags/new_tag_" + (tags.size() + 1));
        newTag.setType("items");
        tags.add(newTag);
        filteredTags.setPredicate(null);
        tagListView.getSelectionModel().select(newTag);
        tagListView.scrollTo(newTag);
        markDirty();
    }

    private void showEditorForTag(TagData tag) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(tag);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(TagData tag) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("tag.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteTag(tag));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(tag);
        TitledPane valuesPane = createValuesCard(tag);

        content.getChildren().addAll(titleBar, basicPane, valuesPane);
        return content;
    }

    private TitledPane createBasicInfoCard(TagData tag) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField idField = new TextField(tag.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = tags.stream().filter(t -> t != tag).anyMatch(t -> t.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            tag.setId(val);
            markDirty(); if (onTagChanged != null) onTagChanged.accept(tag);
            tagListView.refresh();
        });
        addTooltip(idField, I18n.get("tag.id.tip"));

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("blocks", "items", "fluids", "entity_types"));
        typeCombo.setValue(tag.getType());
        typeCombo.valueProperty().addListener((obs, old, val) -> {
            tag.setType(val);
            markDirty(); if (onTagChanged != null) onTagChanged.accept(tag);
        });

        CheckBox replaceCheck = new CheckBox(I18n.get("tag.replace"));
        replaceCheck.setSelected(tag.isReplace());
        replaceCheck.selectedProperty().addListener((obs, old, val) -> {
            tag.setReplace(val);
            markDirty(); if (onTagChanged != null) onTagChanged.accept(tag);
        });

        grid.add(createLabel(I18n.get("tag.id")), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(createLabel(I18n.get("tag.type")), 2, 0);
        grid.add(typeCombo, 3, 0);
        grid.add(replaceCheck, 0, 1);

        TitledPane titled = new TitledPane(I18n.get("tag.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createValuesCard(TagData tag) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        ListView<String> valuesList = new ListView<>();
        valuesList.setItems(FXCollections.observableArrayList(tag.getValues()));
        valuesList.setPrefHeight(200);

        TextField newValueField = new TextField();
        newValueField.setPromptText(I18n.get("tag.addValue"));
        Button pickBtn = new Button(I18n.get("tag.pick"));
        pickBtn.setGraphic(FontIcon.of(FontAwesomeSolid.SEARCH));
        pickBtn.setOnAction(e -> {
            IdPickerDialog picker = new IdPickerDialog(I18n.get("idpicker.title.item"), VanillaIds.ITEMS);
            picker.showAndWait().ifPresent(newValueField::setText);
        });

        Button addBtn = new Button(I18n.get("btn.add"));
        addBtn.setOnAction(e -> {
            String val = newValueField.getText().trim();
            if (!val.isEmpty()) {
                tag.getValues().add(val);
                valuesList.getItems().setAll(tag.getValues());
                newValueField.clear();
                markDirty(); if (onTagChanged != null) onTagChanged.accept(tag);
            }
        });
        Button removeBtn = new Button(I18n.get("btn.remove"));
        removeBtn.setOnAction(e -> {
            String selected = valuesList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                tag.getValues().remove(selected);
                valuesList.getItems().setAll(tag.getValues());
                markDirty(); if (onTagChanged != null) onTagChanged.accept(tag);
            }
        });

        HBox controlBox = new HBox(5, newValueField, pickBtn, addBtn, removeBtn);

        grid.add(createLabel(I18n.get("tag.values")), 0, 0);
        grid.add(valuesList, 0, 1);
        grid.add(controlBox, 0, 2);

        TitledPane titled = new TitledPane(I18n.get("tag.values"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private void deleteTag(TagData tag) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("tag.deleteConfirm"), tag.getId()));
        confirm.setContentText(I18n.get("tag.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            tags.remove(tag);
            if (tags.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onTagChanged != null) onTagChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("tag.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<TagData> getTags() { return tags; }
    public void setTags(ObservableList<TagData> newTags) {
        this.tags.setAll(newTags);
        if (tagListView.getSelectionModel().isEmpty() && !tags.isEmpty()) tagListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnTagChanged(Consumer<TagData> callback) { this.onTagChanged = callback; }
    public void refresh() { tagListView.refresh(); }

    private static class TagListCell extends ListCell<TagData> {
        @Override
        protected void updateItem(TagData tag, boolean empty) {
            super.updateItem(tag, empty);
            if (empty || tag == null) { setText(null); setGraphic(null); }
            else {
                String id = tag.getId();
                String type = tag.getType();
                setText(id + " (" + type + ")");
                FontIcon icon = FontIcon.of(FontAwesomeSolid.TAGS);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}