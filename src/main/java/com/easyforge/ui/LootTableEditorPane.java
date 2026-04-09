package com.easyforge.ui;

import com.easyforge.model.LootTableData;
import com.easyforge.model.LootTableData.LootPool;
import com.easyforge.model.LootTableData.LootEntry;
import com.easyforge.model.LootTableData.LootCondition;
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

public class LootTableEditorPane extends VBox {

    private final ObservableList<LootTableData> lootTables = FXCollections.observableArrayList();
    private final FilteredList<LootTableData> filteredTables = new FilteredList<>(lootTables, p -> true);
    private final ListView<LootTableData> tableListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<LootTableData> onLootTableChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_/]+");

    public LootTableEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    public void setProject(ModProject project) {
        this.currentProject = project;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        dirty = false;
    }

    private void markDirty() {
        if (!dirty) {
            dirty = true;
            if (onLootTableChanged != null) {
                onLootTableChanged.accept(null);
            }
        }
    }

    private void initUI() {
        Label listLabel = new Label(I18n.get("loottable.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("loottable.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredTables.setPredicate(lt -> {
                if (val == null || val.isEmpty()) return true;
                return lt.getId().toLowerCase().contains(val.toLowerCase());
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("loottable.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        tableListView.setCellFactory(lv -> new LootTableListCell());
        tableListView.setItems(filteredTables);
        tableListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showEditorForLootTable(newVal);
            } else {
                rightPanel.getChildren().setAll(createEmptyHint());
            }
        });

        Button newBtn = new Button(I18n.get("loottable.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewLootTable());

        VBox leftBox = new VBox(5, listLabel, searchBox, tableListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        lootTables.addListener((ListChangeListener<LootTableData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(lt -> {
                        if (onLootTableChanged != null) onLootTableChanged.accept(lt);
                    });
                }
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(lt -> {
                        if (onLootTableChanged != null) onLootTableChanged.accept(null);
                    });
                }
            }
        });
    }

    private void createNewLootTable() {
        LootTableData newTable = new LootTableData();
        newTable.setId("loot_tables/new_table_" + (lootTables.size() + 1));
        lootTables.add(newTable);
        filteredTables.setPredicate(null);
        tableListView.getSelectionModel().select(newTable);
        tableListView.scrollTo(newTable);
        markDirty();
    }

    private void showEditorForLootTable(LootTableData lootTable) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(lootTable);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(LootTableData lootTable) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("loottable.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteLootTable(lootTable));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(lootTable);
        TitledPane poolsPane = createPoolsCard(lootTable);

        content.getChildren().addAll(titleBar, basicPane, poolsPane);
        return content;
    }

    private TitledPane createBasicInfoCard(LootTableData lootTable) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField idField = new TextField(lootTable.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) {
                idField.setStyle("-fx-border-color: red;");
                return;
            } else {
                idField.setStyle("");
            }
            boolean duplicate = lootTables.stream().filter(lt -> lt != lootTable).anyMatch(lt -> lt.getId().equals(val));
            if (duplicate) {
                idField.setStyle("-fx-border-color: orange;");
                return;
            }
            lootTable.setId(val);
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(lootTable);
            tableListView.refresh();
        });
        addTooltip(idField, I18n.get("loottable.id.tip"));

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "minecraft:block", "minecraft:entity", "minecraft:chest", "minecraft:fishing", "minecraft:gift", "minecraft:archaeology"));
        typeCombo.setValue(lootTable.getType());
        typeCombo.valueProperty().addListener((obs, old, val) -> {
            lootTable.setType(val);
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(lootTable);
        });

        grid.add(createLabel(I18n.get("loottable.id")), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(createLabel(I18n.get("loottable.type")), 2, 0);
        grid.add(typeCombo, 3, 0);

        TitledPane titled = new TitledPane(I18n.get("loottable.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createPoolsCard(LootTableData lootTable) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        ListView<LootPool> poolListView = new ListView<>();
        ObservableList<LootPool> pools = FXCollections.observableArrayList(lootTable.getPools());
        poolListView.setItems(pools);
        poolListView.setCellFactory(lv -> new ListCell<LootPool>() {
            @Override
            protected void updateItem(LootPool pool, boolean empty) {
                super.updateItem(pool, empty);
                if (empty || pool == null) {
                    setText(null);
                } else {
                    setText(String.format(I18n.get("loottable.poolDisplay"), pool.getRolls(), pool.getBonusRolls()));
                }
            }
        });

        Button addPoolBtn = new Button(I18n.get("loottable.addPool"));
        addPoolBtn.setOnAction(e -> {
            LootPool newPool = new LootPool();
            lootTable.getPools().add(newPool);
            pools.setAll(lootTable.getPools());
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(lootTable);
        });
        Button removePoolBtn = new Button(I18n.get("loottable.removePool"));
        removePoolBtn.setOnAction(e -> {
            LootPool selected = poolListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                lootTable.getPools().remove(selected);
                pools.setAll(lootTable.getPools());
                markDirty();
                if (onLootTableChanged != null) onLootTableChanged.accept(lootTable);
            }
        });

        HBox poolButtons = new HBox(5, addPoolBtn, removePoolBtn);

        VBox poolEditor = new VBox(5);
        poolEditor.setPadding(new Insets(5));
        poolEditor.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 5;");
        poolListView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            poolEditor.getChildren().clear();
            if (selected != null) {
                poolEditor.getChildren().add(createPoolEditor(selected, lootTable));
            }
        });

        container.getChildren().addAll(new Label(I18n.get("loottable.pools")), poolListView, poolButtons, poolEditor);

        TitledPane titled = new TitledPane(I18n.get("loottable.pools"), container);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private Node createPoolEditor(LootPool pool, LootTableData parent) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(5));

        Spinner<Integer> rollsSpinner = new Spinner<>(1, 100, pool.getRolls());
        rollsSpinner.valueProperty().addListener((obs, old, val) -> {
            pool.setRolls(val);
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(parent);
        });
        Spinner<Integer> bonusSpinner = new Spinner<>(0, 100, pool.getBonusRolls());
        bonusSpinner.valueProperty().addListener((obs, old, val) -> {
            pool.setBonusRolls(val);
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(parent);
        });

        grid.add(createLabel(I18n.get("loottable.rolls")), 0, 0);
        grid.add(rollsSpinner, 1, 0);
        grid.add(createLabel(I18n.get("loottable.bonusRolls")), 2, 0);
        grid.add(bonusSpinner, 3, 0);

        // 条目列表
        VBox entriesBox = new VBox(5);
        ListView<LootEntry> entryListView = new ListView<>();
        ObservableList<LootEntry> entries = FXCollections.observableArrayList(pool.getEntries());
        entryListView.setItems(entries);
        entryListView.setCellFactory(lv -> new ListCell<LootEntry>() {
            @Override
            protected void updateItem(LootEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                } else {
                    setText(entry.getName() + " x" + entry.getCount() + " (" + I18n.get("loottable.weight") + " " + entry.getWeight() + ")");
                }
            }
        });

        Button addEntryBtn = new Button(I18n.get("loottable.addEntry"));
        addEntryBtn.setOnAction(e -> {
            LootEntry newEntry = new LootEntry();
            newEntry.setName("minecraft:stone");
            pool.getEntries().add(newEntry);
            entries.setAll(pool.getEntries());
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(parent);
        });
        Button removeEntryBtn = new Button(I18n.get("loottable.removeEntry"));
        removeEntryBtn.setOnAction(e -> {
            LootEntry selected = entryListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                pool.getEntries().remove(selected);
                entries.setAll(pool.getEntries());
                markDirty();
                if (onLootTableChanged != null) onLootTableChanged.accept(parent);
            }
        });
        HBox entryButtons = new HBox(5, addEntryBtn, removeEntryBtn);

        VBox entryEditor = new VBox(5);
        entryListView.getSelectionModel().selectedItemProperty().addListener((obs, old, selectedEntry) -> {
            entryEditor.getChildren().clear();
            if (selectedEntry != null) {
                entryEditor.getChildren().add(createEntryEditor(selectedEntry, parent, pool));
            }
        });

        entriesBox.getChildren().addAll(new Label(I18n.get("loottable.entries")), entryListView, entryButtons, entryEditor);

        // 条件列表
        VBox conditionsBox = new VBox(5);
        ListView<LootCondition> conditionListView = new ListView<>();
        ObservableList<LootCondition> conditions = FXCollections.observableArrayList(pool.getConditions());
        conditionListView.setItems(conditions);
        conditionListView.setCellFactory(lv -> new ListCell<LootCondition>() {
            @Override
            protected void updateItem(LootCondition cond, boolean empty) {
                super.updateItem(cond, empty);
                if (empty || cond == null) {
                    setText(null);
                } else {
                    setText(cond.getCondition());
                }
            }
        });

        Button addCondBtn = new Button(I18n.get("loottable.addCondition"));
        addCondBtn.setOnAction(e -> {
            LootCondition newCond = new LootCondition();
            pool.getConditions().add(newCond);
            conditions.setAll(pool.getConditions());
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(parent);
        });
        Button removeCondBtn = new Button(I18n.get("loottable.removeCondition"));
        removeCondBtn.setOnAction(e -> {
            LootCondition selected = conditionListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                pool.getConditions().remove(selected);
                conditions.setAll(pool.getConditions());
                markDirty();
                if (onLootTableChanged != null) onLootTableChanged.accept(parent);
            }
        });
        HBox condButtons = new HBox(5, addCondBtn, removeCondBtn);
        conditionsBox.getChildren().addAll(new Label(I18n.get("loottable.conditions")), conditionListView, condButtons);

        VBox poolDetail = new VBox(10, grid, entriesBox, conditionsBox);
        return poolDetail;
    }

    private Node createEntryEditor(LootEntry entry, LootTableData parent, LootPool pool) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(5));

        // 物品名称字段 + 选择按钮
        TextField nameField = new TextField(entry.getName());
        nameField.textProperty().addListener((obs, old, val) -> {
            entry.setName(val);
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(parent);
        });
        Button pickBtn = new Button(I18n.get("loottable.pick"));
        pickBtn.setGraphic(FontIcon.of(FontAwesomeSolid.SEARCH));
        pickBtn.setOnAction(e -> {
            IdPickerDialog picker = new IdPickerDialog(I18n.get("idpicker.title.item"), VanillaIds.ITEMS);
            picker.showAndWait().ifPresent(nameField::setText);
        });
        HBox nameBox = new HBox(5, nameField, pickBtn);
        nameBox.setPrefWidth(300);

        Spinner<Integer> weightSpinner = new Spinner<>(1, 1000, entry.getWeight());
        weightSpinner.valueProperty().addListener((obs, old, val) -> {
            entry.setWeight(val);
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(parent);
        });
        Spinner<Integer> countSpinner = new Spinner<>(1, 64, entry.getCount());
        countSpinner.valueProperty().addListener((obs, old, val) -> {
            entry.setCount(val);
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(parent);
        });
        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("minecraft:item", "minecraft:loot_table", "minecraft:empty"));
        typeCombo.setValue(entry.getType());
        typeCombo.valueProperty().addListener((obs, old, val) -> {
            entry.setType(val);
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(parent);
        });

        grid.add(createLabel(I18n.get("loottable.entryName")), 0, 0);
        grid.add(nameBox, 1, 0);
        grid.add(createLabel(I18n.get("loottable.entryType")), 2, 0);
        grid.add(typeCombo, 3, 0);
        grid.add(createLabel(I18n.get("loottable.weight")), 0, 1);
        grid.add(weightSpinner, 1, 1);
        grid.add(createLabel(I18n.get("loottable.count")), 2, 1);
        grid.add(countSpinner, 3, 1);

        return grid;
    }

    private void deleteLootTable(LootTableData lootTable) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("loottable.deleteConfirm"), lootTable.getId()));
        confirm.setContentText(I18n.get("loottable.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            lootTables.remove(lootTable);
            if (lootTables.isEmpty()) {
                rightPanel.getChildren().setAll(createEmptyHint());
            }
            markDirty();
            if (onLootTableChanged != null) onLootTableChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("loottable.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }

    private void addTooltip(Control control, String text) {
        Tooltip tip = new Tooltip(text);
        tip.setWrapText(true);
        tip.setMaxWidth(300);
        control.setTooltip(tip);
    }

    public ObservableList<LootTableData> getLootTables() {
        return lootTables;
    }

    public void setLootTables(ObservableList<LootTableData> newTables) {
        this.lootTables.setAll(newTables);
        if (tableListView.getSelectionModel().isEmpty() && !lootTables.isEmpty()) {
            tableListView.getSelectionModel().selectFirst();
        }
        clearDirty();
    }

    public void setOnLootTableChanged(Consumer<LootTableData> callback) {
        this.onLootTableChanged = callback;
    }

    public void refresh() {
        tableListView.refresh();
    }

    private static class LootTableListCell extends ListCell<LootTableData> {
        @Override
        protected void updateItem(LootTableData lootTable, boolean empty) {
            super.updateItem(lootTable, empty);
            if (empty || lootTable == null) {
                setText(null);
                setGraphic(null);
            } else {
                String id = lootTable.getId();
                setText(id);
                FontIcon icon = FontIcon.of(FontAwesomeSolid.BOX);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}