package com.easyforge.ui;

import com.easyforge.model.EnchantmentData;
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

public class EnchantmentEditorPane extends VBox {

    private final ObservableList<EnchantmentData> enchantments = FXCollections.observableArrayList();
    private final FilteredList<EnchantmentData> filteredEnchantments = new FilteredList<>(enchantments, p -> true);
    private final ListView<EnchantmentData> enchantmentListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<EnchantmentData> onEnchantmentChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    public EnchantmentEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onEnchantmentChanged != null) onEnchantmentChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("enchantment.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("enchantment.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredEnchantments.setPredicate(en -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return en.getDisplayName().toLowerCase().contains(lower) ||
                        en.getId().toLowerCase().contains(lower);
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("enchantment.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        enchantmentListView.setCellFactory(lv -> new EnchantmentListCell());
        enchantmentListView.setItems(filteredEnchantments);
        enchantmentListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForEnchantment(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("enchantment.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewEnchantment());

        VBox leftBox = new VBox(5, listLabel, searchBox, enchantmentListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        enchantments.addListener((ListChangeListener<EnchantmentData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(e -> { if (onEnchantmentChanged != null) onEnchantmentChanged.accept(e); });
                if (c.wasRemoved()) c.getRemoved().forEach(e -> { if (onEnchantmentChanged != null) onEnchantmentChanged.accept(null); });
            }
        });
    }

    private void createNewEnchantment() {
        EnchantmentData newEnchantment = new EnchantmentData();
        newEnchantment.setId("new_enchantment_" + (enchantments.size() + 1));
        newEnchantment.setDisplayName(I18n.get("enchantment.defaultName"));
        enchantments.add(newEnchantment);
        filteredEnchantments.setPredicate(null);
        enchantmentListView.getSelectionModel().select(newEnchantment);
        enchantmentListView.scrollTo(newEnchantment);
        markDirty();
    }

    private void showEditorForEnchantment(EnchantmentData enchantment) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(enchantment);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(EnchantmentData enchantment) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("enchantment.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteEnchantment(enchantment));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(enchantment);
        TitledPane levelPane = createLevelCard(enchantment);
        TitledPane costPane = createCostCard(enchantment);
        TitledPane flagsPane = createFlagsCard(enchantment);

        content.getChildren().addAll(titleBar, basicPane, levelPane, costPane, flagsPane);
        return content;
    }

    private TitledPane createBasicInfoCard(EnchantmentData enchantment) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField displayField = new TextField(enchantment.getDisplayName());
        displayField.textProperty().addListener((obs, old, val) -> {
            enchantment.setDisplayName(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });
        addTooltip(displayField, I18n.get("enchantment.displayName.tip"));

        TextField idField = new TextField(enchantment.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = enchantments.stream().filter(e -> e != enchantment).anyMatch(e -> e.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            enchantment.setId(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
            enchantmentListView.refresh();
        });
        addTooltip(idField, I18n.get("enchantment.id.tip"));

        TextArea descArea = new TextArea(enchantment.getDescription());
        descArea.setPrefRowCount(2);
        descArea.textProperty().addListener((obs, old, val) -> {
            enchantment.setDescription(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });

        // 从原版附魔复制按钮
        Button copyVanillaBtn = new Button(I18n.get("enchantment.copyVanilla"));
        copyVanillaBtn.setGraphic(FontIcon.of(FontAwesomeSolid.COPY));
        copyVanillaBtn.setOnAction(e -> showVanillaPicker(enchantment));

        grid.add(createLabel(I18n.get("enchantment.displayName")), 0, 0);
        grid.add(displayField, 1, 0);
        grid.add(createLabel(I18n.get("enchantment.id")), 2, 0);
        grid.add(idField, 3, 0);
        grid.add(createLabel(I18n.get("enchantment.description")), 0, 1);
        grid.add(descArea, 1, 1, 3, 1);
        grid.add(copyVanillaBtn, 1, 2);

        TitledPane titled = new TitledPane(I18n.get("enchantment.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createLevelCard(EnchantmentData enchantment) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        Spinner<Integer> minLevelSpinner = new Spinner<>(1, 255, enchantment.getMinLevel());
        minLevelSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            enchantment.setMinLevel(val);
            if (enchantment.getMaxLevel() < val) enchantment.setMaxLevel(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });
        Spinner<Integer> maxLevelSpinner = new Spinner<>(1, 255, enchantment.getMaxLevel());
        maxLevelSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            enchantment.setMaxLevel(val);
            if (enchantment.getMinLevel() > val) enchantment.setMinLevel(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });
        ComboBox<Integer> rarityCombo = new ComboBox<>(FXCollections.observableArrayList(0, 1, 2, 3));
        rarityCombo.setValue(enchantment.getRarity());
        rarityCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Integer r) {
                if (r == null) return "";
                switch (r) {
                    case 0: return I18n.get("enchantment.rarity.common");
                    case 1: return I18n.get("enchantment.rarity.uncommon");
                    case 2: return I18n.get("enchantment.rarity.rare");
                    case 3: return I18n.get("enchantment.rarity.veryRare");
                    default: return "";
                }
            }
            @Override
            public Integer fromString(String s) { return null; }
        });
        rarityCombo.valueProperty().addListener((obs, old, val) -> {
            enchantment.setRarity(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });

        grid.add(createLabel(I18n.get("enchantment.minLevel")), 0, 0);
        grid.add(minLevelSpinner, 1, 0);
        grid.add(createLabel(I18n.get("enchantment.maxLevel")), 2, 0);
        grid.add(maxLevelSpinner, 3, 0);
        grid.add(createLabel(I18n.get("enchantment.rarity")), 0, 1);
        grid.add(rarityCombo, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("enchantment.levels"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createCostCard(EnchantmentData enchantment) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        Spinner<Integer> minBaseSpinner = new Spinner<>(0, 100, enchantment.getMinCostBase());
        minBaseSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            enchantment.setMinCostBase(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });
        Spinner<Integer> minPerSpinner = new Spinner<>(0, 100, enchantment.getMinCostPerLevel());
        minPerSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            enchantment.setMinCostPerLevel(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });
        Spinner<Integer> maxBaseSpinner = new Spinner<>(0, 100, enchantment.getMaxCostBase());
        maxBaseSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            enchantment.setMaxCostBase(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });
        Spinner<Integer> maxPerSpinner = new Spinner<>(0, 100, enchantment.getMaxCostPerLevel());
        maxPerSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            enchantment.setMaxCostPerLevel(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });

        grid.add(createLabel(I18n.get("enchantment.minCostBase")), 0, 0);
        grid.add(minBaseSpinner, 1, 0);
        grid.add(createLabel(I18n.get("enchantment.minCostPerLevel")), 2, 0);
        grid.add(minPerSpinner, 3, 0);
        grid.add(createLabel(I18n.get("enchantment.maxCostBase")), 0, 1);
        grid.add(maxBaseSpinner, 1, 1);
        grid.add(createLabel(I18n.get("enchantment.maxCostPerLevel")), 2, 1);
        grid.add(maxPerSpinner, 3, 1);

        TitledPane titled = new TitledPane(I18n.get("enchantment.cost"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createFlagsCard(EnchantmentData enchantment) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        ComboBox<String> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(
                "BREAKABLE", "WEAPON", "ARMOR", "ARMOR_HEAD", "ARMOR_CHEST", "ARMOR_LEGS", "ARMOR_FEET",
                "DIGGER", "FISHING_ROD", "TRIDENT", "BOW", "CROSSBOW", "VANISHABLE"));
        categoryCombo.setValue(enchantment.getCategory());
        categoryCombo.valueProperty().addListener((obs, old, val) -> {
            enchantment.setCategory(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });

        CheckBox treasureCheck = new CheckBox(I18n.get("enchantment.treasure"));
        treasureCheck.setSelected(enchantment.isTreasure());
        treasureCheck.selectedProperty().addListener((obs, old, val) -> {
            enchantment.setTreasure(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });
        CheckBox curseCheck = new CheckBox(I18n.get("enchantment.curse"));
        curseCheck.setSelected(enchantment.isCurse());
        curseCheck.selectedProperty().addListener((obs, old, val) -> {
            enchantment.setCurse(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });
        CheckBox tradeableCheck = new CheckBox(I18n.get("enchantment.tradeable"));
        tradeableCheck.setSelected(enchantment.isTradeable());
        tradeableCheck.selectedProperty().addListener((obs, old, val) -> {
            enchantment.setTradeable(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });
        CheckBox discoverableCheck = new CheckBox(I18n.get("enchantment.discoverable"));
        discoverableCheck.setSelected(enchantment.isDiscoverable());
        discoverableCheck.selectedProperty().addListener((obs, old, val) -> {
            enchantment.setDiscoverable(val);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(enchantment);
        });

        grid.add(createLabel(I18n.get("enchantment.category")), 0, 0);
        grid.add(categoryCombo, 1, 0);
        grid.add(treasureCheck, 2, 0);
        grid.add(curseCheck, 3, 0);
        grid.add(tradeableCheck, 0, 1);
        grid.add(discoverableCheck, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("enchantment.flags"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private void showVanillaPicker(EnchantmentData target) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("锋利",
                "锋利", "亡灵杀手", "节肢杀手", "击退", "火焰附加", "横扫之刃",
                "保护", "火焰保护", "爆炸保护", "弹射物保护", "摔落保护", "水下呼吸", "水下速掘",
                "效率", "精准采集", "耐久", "时运",
                "力量", "冲击", "火矢", "无限",
                "海之眷顾", "钓饵", "抢夺", "荆棘", "经验修补");
        dialog.setTitle(I18n.get("enchantment.copyVanilla.title"));
        dialog.setHeaderText(I18n.get("enchantment.copyVanilla.header"));
        dialog.setContentText(I18n.get("enchantment.copyVanilla.content"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            switch (selected) {
                case "锋利":
                    target.setId("sharpness");
                    target.setDisplayName("锋利");
                    target.setMaxLevel(5);
                    target.setCategory("WEAPON");
                    target.setMinCostBase(1);
                    target.setMinCostPerLevel(10);
                    target.setMaxCostBase(5);
                    target.setMaxCostPerLevel(20);
                    target.setRarity(1);
                    break;
                case "亡灵杀手":
                    target.setId("smite");
                    target.setDisplayName("亡灵杀手");
                    target.setMaxLevel(5);
                    target.setCategory("WEAPON");
                    break;
                case "节肢杀手":
                    target.setId("bane_of_arthropods");
                    target.setDisplayName("节肢杀手");
                    target.setMaxLevel(5);
                    target.setCategory("WEAPON");
                    break;
                case "击退":
                    target.setId("knockback");
                    target.setDisplayName("击退");
                    target.setMaxLevel(2);
                    target.setCategory("WEAPON");
                    break;
                case "火焰附加":
                    target.setId("fire_aspect");
                    target.setDisplayName("火焰附加");
                    target.setMaxLevel(2);
                    target.setCategory("WEAPON");
                    break;
                case "横扫之刃":
                    target.setId("sweeping");
                    target.setDisplayName("横扫之刃");
                    target.setMaxLevel(3);
                    target.setCategory("WEAPON");
                    break;
                case "保护":
                    target.setId("protection");
                    target.setDisplayName("保护");
                    target.setMaxLevel(4);
                    target.setCategory("ARMOR");
                    target.setMinCostBase(1);
                    target.setMinCostPerLevel(10);
                    target.setMaxCostBase(5);
                    target.setMaxCostPerLevel(20);
                    break;
                case "火焰保护":
                    target.setId("fire_protection");
                    target.setDisplayName("火焰保护");
                    target.setMaxLevel(4);
                    target.setCategory("ARMOR");
                    break;
                case "爆炸保护":
                    target.setId("blast_protection");
                    target.setDisplayName("爆炸保护");
                    target.setMaxLevel(4);
                    target.setCategory("ARMOR");
                    break;
                case "弹射物保护":
                    target.setId("projectile_protection");
                    target.setDisplayName("弹射物保护");
                    target.setMaxLevel(4);
                    target.setCategory("ARMOR");
                    break;
                case "摔落保护":
                    target.setId("feather_falling");
                    target.setDisplayName("摔落保护");
                    target.setMaxLevel(4);
                    target.setCategory("ARMOR_FEET");
                    break;
                case "水下呼吸":
                    target.setId("respiration");
                    target.setDisplayName("水下呼吸");
                    target.setMaxLevel(3);
                    target.setCategory("ARMOR_HEAD");
                    break;
                case "水下速掘":
                    target.setId("aqua_affinity");
                    target.setDisplayName("水下速掘");
                    target.setMaxLevel(1);
                    target.setCategory("ARMOR_HEAD");
                    break;
                case "效率":
                    target.setId("efficiency");
                    target.setDisplayName("效率");
                    target.setMaxLevel(5);
                    target.setCategory("DIGGER");
                    break;
                case "精准采集":
                    target.setId("silk_touch");
                    target.setDisplayName("精准采集");
                    target.setMaxLevel(1);
                    target.setCategory("DIGGER");
                    break;
                case "耐久":
                    target.setId("unbreaking");
                    target.setDisplayName("耐久");
                    target.setMaxLevel(3);
                    target.setCategory("BREAKABLE");
                    break;
                case "时运":
                    target.setId("fortune");
                    target.setDisplayName("时运");
                    target.setMaxLevel(3);
                    target.setCategory("DIGGER");
                    break;
                case "力量":
                    target.setId("power");
                    target.setDisplayName("力量");
                    target.setMaxLevel(5);
                    target.setCategory("BOW");
                    break;
                case "冲击":
                    target.setId("punch");
                    target.setDisplayName("冲击");
                    target.setMaxLevel(2);
                    target.setCategory("BOW");
                    break;
                case "火矢":
                    target.setId("flame");
                    target.setDisplayName("火矢");
                    target.setMaxLevel(1);
                    target.setCategory("BOW");
                    break;
                case "无限":
                    target.setId("infinity");
                    target.setDisplayName("无限");
                    target.setMaxLevel(1);
                    target.setCategory("BOW");
                    break;
                case "海之眷顾":
                    target.setId("luck_of_the_sea");
                    target.setDisplayName("海之眷顾");
                    target.setMaxLevel(3);
                    target.setCategory("FISHING_ROD");
                    break;
                case "钓饵":
                    target.setId("lure");
                    target.setDisplayName("钓饵");
                    target.setMaxLevel(3);
                    target.setCategory("FISHING_ROD");
                    break;
                case "抢夺":
                    target.setId("looting");
                    target.setDisplayName("抢夺");
                    target.setMaxLevel(3);
                    target.setCategory("WEAPON");
                    break;
                case "荆棘":
                    target.setId("thorns");
                    target.setDisplayName("荆棘");
                    target.setMaxLevel(3);
                    target.setCategory("ARMOR");
                    break;
                case "经验修补":
                    target.setId("mending");
                    target.setDisplayName("经验修补");
                    target.setMaxLevel(1);
                    target.setCategory("BREAKABLE");
                    target.setTreasure(true);
                    break;
            }
            showEditorForEnchantment(target);
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(target);
        });
    }

    private void deleteEnchantment(EnchantmentData enchantment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("enchantment.deleteConfirm"), enchantment.getDisplayName()));
        confirm.setContentText(I18n.get("enchantment.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            enchantments.remove(enchantment);
            if (enchantments.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onEnchantmentChanged != null) onEnchantmentChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("enchantment.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<EnchantmentData> getEnchantments() { return enchantments; }
    public void setEnchantments(ObservableList<EnchantmentData> newEnchantments) {
        this.enchantments.setAll(newEnchantments);
        if (enchantmentListView.getSelectionModel().isEmpty() && !enchantments.isEmpty()) enchantmentListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnEnchantmentChanged(Consumer<EnchantmentData> callback) { this.onEnchantmentChanged = callback; }
    public void refresh() { enchantmentListView.refresh(); }

    private static class EnchantmentListCell extends ListCell<EnchantmentData> {
        @Override
        protected void updateItem(EnchantmentData enchantment, boolean empty) {
            super.updateItem(enchantment, empty);
            if (empty || enchantment == null) { setText(null); setGraphic(null); }
            else {
                String display = enchantment.getDisplayName();
                String id = enchantment.getId();
                setText(display + " [" + id + "]");
                FontIcon icon = FontIcon.of(FontAwesomeSolid.MAGIC);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}