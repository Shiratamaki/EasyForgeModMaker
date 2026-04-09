package com.easyforge.ui;

import com.easyforge.model.CommandData;
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

public class CommandEditorPane extends VBox {

    private final ObservableList<CommandData> commands = FXCollections.observableArrayList();
    private final FilteredList<CommandData> filteredCommands = new FilteredList<>(commands, p -> true);
    private final ListView<CommandData> commandListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<CommandData> onCommandChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9_.]+");

    public CommandEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onCommandChanged != null) onCommandChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("command.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("command.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredCommands.setPredicate(cmd -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return (cmd.getName() != null && cmd.getName().toLowerCase().contains(lower)) ||
                        (cmd.getId() != null && cmd.getId().toLowerCase().contains(lower));
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("command.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        commandListView.setCellFactory(lv -> new CommandListCell());
        commandListView.setItems(filteredCommands);
        commandListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForCommand(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("command.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewCommand());

        VBox leftBox = new VBox(5, listLabel, searchBox, commandListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        commands.addListener((ListChangeListener<CommandData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(cmd -> { if (onCommandChanged != null) onCommandChanged.accept(cmd); });
                if (c.wasRemoved()) c.getRemoved().forEach(cmd -> { if (onCommandChanged != null) onCommandChanged.accept(null); });
            }
        });
    }

    private void createNewCommand() {
        CommandData newCmd = new CommandData();
        newCmd.setId("new_command_" + (commands.size() + 1));
        newCmd.setName("example_command");
        commands.add(newCmd);
        filteredCommands.setPredicate(null);
        commandListView.getSelectionModel().select(newCmd);
        commandListView.scrollTo(newCmd);
        markDirty();
    }

    private void showEditorForCommand(CommandData command) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(command);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(CommandData command) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("command.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteCommand(command));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(command);
        TitledPane configPane = createConfigCard(command);

        content.getChildren().addAll(titleBar, basicPane, configPane);
        return content;
    }

    private TitledPane createBasicInfoCard(CommandData command) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField idField = new TextField(command.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = commands.stream().filter(c -> c != command).anyMatch(c -> c.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            command.setId(val);
            markDirty(); if (onCommandChanged != null) onCommandChanged.accept(command);
            commandListView.refresh();
        });
        addTooltip(idField, I18n.get("command.id.tip"));

        TextField nameField = new TextField(command.getName());
        nameField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!NAME_PATTERN.matcher(val).matches()) { nameField.setStyle("-fx-border-color: red;"); return; }
            else nameField.setStyle("");
            command.setName(val);
            markDirty(); if (onCommandChanged != null) onCommandChanged.accept(command);
            commandListView.refresh();
        });
        addTooltip(nameField, I18n.get("command.name.tip"));

        grid.add(createLabel(I18n.get("command.id")), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(createLabel(I18n.get("command.name")), 2, 0);
        grid.add(nameField, 3, 0);

        TitledPane titled = new TitledPane(I18n.get("command.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createConfigCard(CommandData command) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField permissionField = new TextField(command.getPermission());
        permissionField.textProperty().addListener((obs, old, val) -> {
            command.setPermission(val);
            markDirty(); if (onCommandChanged != null) onCommandChanged.accept(command);
        });
        addTooltip(permissionField, I18n.get("command.permission.tip"));

        TextArea descArea = new TextArea(command.getDescription());
        descArea.setPrefRowCount(2);
        descArea.textProperty().addListener((obs, old, val) -> {
            command.setDescription(val);
            markDirty(); if (onCommandChanged != null) onCommandChanged.accept(command);
        });
        addTooltip(descArea, I18n.get("command.description.tip"));

        TextField execField = new TextField(command.getExecutionClass());
        execField.textProperty().addListener((obs, old, val) -> {
            command.setExecutionClass(val);
            markDirty(); if (onCommandChanged != null) onCommandChanged.accept(command);
        });
        addTooltip(execField, I18n.get("command.executionClass.tip"));

        grid.add(createLabel(I18n.get("command.permission")), 0, 0);
        grid.add(permissionField, 1, 0, 3, 1);
        grid.add(createLabel(I18n.get("command.description")), 0, 1);
        grid.add(descArea, 1, 1, 3, 1);
        grid.add(createLabel(I18n.get("command.executionClass")), 0, 2);
        grid.add(execField, 1, 2, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("command.config"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private void deleteCommand(CommandData command) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("command.deleteConfirm"), command.getName()));
        confirm.setContentText(I18n.get("command.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            commands.remove(command);
            if (commands.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onCommandChanged != null) onCommandChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("command.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<CommandData> getCommands() { return commands; }
    public void setCommands(ObservableList<CommandData> newCommands) {
        this.commands.setAll(newCommands);
        if (commandListView.getSelectionModel().isEmpty() && !commands.isEmpty()) commandListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnCommandChanged(Consumer<CommandData> callback) { this.onCommandChanged = callback; }
    public void refresh() { commandListView.refresh(); }

    private static class CommandListCell extends ListCell<CommandData> {
        @Override
        protected void updateItem(CommandData command, boolean empty) {
            super.updateItem(command, empty);
            if (empty || command == null) { setText(null); setGraphic(null); }
            else {
                String name = command.getName();
                String id = command.getId();
                setText("/" + name + " [" + id + "]");
                FontIcon icon = FontIcon.of(FontAwesomeSolid.TERMINAL);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}