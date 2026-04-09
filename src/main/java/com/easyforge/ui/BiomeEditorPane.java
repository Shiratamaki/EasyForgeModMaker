package com.easyforge.ui;

import com.easyforge.model.BiomeData;
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

public class BiomeEditorPane extends VBox {

    private final ObservableList<BiomeData> biomes = FXCollections.observableArrayList();
    private final FilteredList<BiomeData> filteredBiomes = new FilteredList<>(biomes, p -> true);
    private final ListView<BiomeData> biomeListView = new ListView<>();
    private final StackPane rightPanel = new StackPane();
    private final TextField searchField = new TextField();

    private Consumer<BiomeData> onBiomeChanged;
    private ModProject currentProject;
    private boolean dirty = false;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    public BiomeEditorPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        initUI();
    }

    public void setProject(ModProject project) { this.currentProject = project; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    private void markDirty() { if (!dirty) { dirty = true; if (onBiomeChanged != null) onBiomeChanged.accept(null); } }

    private void initUI() {
        Label listLabel = new Label(I18n.get("biome.editor.title"));
        listLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchField.setPromptText(I18n.get("biome.search"));
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredBiomes.setPredicate(biome -> {
                if (val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return biome.getDisplayName().toLowerCase().contains(lower) ||
                        biome.getId().toLowerCase().contains(lower);
            });
        });
        HBox searchBox = new HBox(5, new Label(I18n.get("biome.search") + ":"), searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        biomeListView.setCellFactory(lv -> new BiomeListCell());
        biomeListView.setItems(filteredBiomes);
        biomeListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showEditorForBiome(newVal);
            else rightPanel.getChildren().setAll(createEmptyHint());
        });

        Button newBtn = new Button(I18n.get("biome.new"));
        newBtn.setGraphic(FontIcon.of(FontAwesomeSolid.PLUS));
        newBtn.setOnAction(e -> createNewBiome());

        VBox leftBox = new VBox(5, listLabel, searchBox, biomeListView, newBtn);
        leftBox.setPrefWidth(280);
        leftBox.setMinWidth(220);
        leftBox.setSpacing(8);

        rightPanel.getChildren().add(createEmptyHint());

        SplitPane splitPane = new SplitPane(leftBox, rightPanel);
        splitPane.setDividerPositions(0.28);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);

        biomes.addListener((ListChangeListener<BiomeData>) c -> {
            markDirty();
            while (c.next()) {
                if (c.wasAdded()) c.getAddedSubList().forEach(b -> { if (onBiomeChanged != null) onBiomeChanged.accept(b); });
                if (c.wasRemoved()) c.getRemoved().forEach(b -> { if (onBiomeChanged != null) onBiomeChanged.accept(null); });
            }
        });
    }

    private void createNewBiome() {
        BiomeData newBiome = new BiomeData();
        newBiome.setId("new_biome_" + (biomes.size() + 1));
        newBiome.setDisplayName(I18n.get("biome.defaultName"));
        biomes.add(newBiome);
        filteredBiomes.setPredicate(null);
        biomeListView.getSelectionModel().select(newBiome);
        biomeListView.scrollTo(newBiome);
        markDirty();
    }

    private void showEditorForBiome(BiomeData biome) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox content = createEditorContent(biome);
        scrollPane.setContent(content);
        rightPanel.getChildren().setAll(scrollPane);
    }

    private VBox createEditorContent(BiomeData biome) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button(I18n.get("biome.delete"));
        deleteBtn.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH));
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteBiome(biome));
        titleBar.getChildren().add(deleteBtn);

        TitledPane basicPane = createBasicInfoCard(biome);
        TitledPane climatePane = createClimateCard(biome);
        TitledPane colorPane = createColorCard(biome);

        content.getChildren().addAll(titleBar, basicPane, climatePane, colorPane);
        return content;
    }

    private TitledPane createBasicInfoCard(BiomeData biome) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField displayField = new TextField(biome.getDisplayName());
        displayField.textProperty().addListener((obs, old, val) -> {
            biome.setDisplayName(val);
            markDirty(); if (onBiomeChanged != null) onBiomeChanged.accept(biome);
        });
        addTooltip(displayField, I18n.get("biome.displayName.tip"));

        TextField idField = new TextField(biome.getId());
        idField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) return;
            if (!ID_PATTERN.matcher(val).matches()) { idField.setStyle("-fx-border-color: red;"); return; }
            else idField.setStyle("");
            boolean duplicate = biomes.stream().filter(b -> b != biome).anyMatch(b -> b.getId().equals(val));
            if (duplicate) { idField.setStyle("-fx-border-color: orange;"); return; }
            biome.setId(val);
            markDirty(); if (onBiomeChanged != null) onBiomeChanged.accept(biome);
            biomeListView.refresh();
        });
        addTooltip(idField, I18n.get("biome.id.tip"));

        // 从原版生物群系复制按钮
        Button copyVanillaBtn = new Button(I18n.get("biome.copyVanilla"));
        copyVanillaBtn.setGraphic(FontIcon.of(FontAwesomeSolid.COPY));
        copyVanillaBtn.setOnAction(e -> showVanillaPicker(biome));

        grid.add(createLabel(I18n.get("biome.displayName")), 0, 0);
        grid.add(displayField, 1, 0);
        grid.add(createLabel(I18n.get("biome.id")), 2, 0);
        grid.add(idField, 3, 0);
        grid.add(copyVanillaBtn, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("biome.basicInfo"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createClimateCard(BiomeData biome) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        ComboBox<String> precipCombo = new ComboBox<>(FXCollections.observableArrayList("RAIN", "SNOW", "NONE"));
        precipCombo.setValue(biome.getPrecipitation());
        precipCombo.valueProperty().addListener((obs, old, val) -> {
            biome.setPrecipitation(val);
            markDirty(); if (onBiomeChanged != null) onBiomeChanged.accept(biome);
        });

        Spinner<Double> tempSpinner = new Spinner<>(-0.5, 2.0, (double) biome.getTemperature(), 0.05);
        tempSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            biome.setTemperature(val.floatValue());
            markDirty(); if (onBiomeChanged != null) onBiomeChanged.accept(biome);
        });

        Spinner<Double> downfallSpinner = new Spinner<>(0.0, 1.0, (double) biome.getDownfall(), 0.05);
        downfallSpinner.getValueFactory().valueProperty().addListener((obs, old, val) -> {
            biome.setDownfall(val.floatValue());
            markDirty(); if (onBiomeChanged != null) onBiomeChanged.accept(biome);
        });

        grid.add(createLabel(I18n.get("biome.precipitation")), 0, 0);
        grid.add(precipCombo, 1, 0);
        grid.add(createLabel(I18n.get("biome.temperature")), 2, 0);
        grid.add(tempSpinner, 3, 0);
        grid.add(createLabel(I18n.get("biome.downfall")), 0, 1);
        grid.add(downfallSpinner, 1, 1);

        TitledPane titled = new TitledPane(I18n.get("biome.climate"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TitledPane createColorCard(BiomeData biome) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        // 颜色选择辅助：使用颜色选择器或直接输入十六进制
        TextField waterColorField = createColorField(biome.getWaterColor(), val -> biome.setWaterColor(val));
        TextField waterFogField = createColorField(biome.getWaterFogColor(), val -> biome.setWaterFogColor(val));
        TextField fogColorField = createColorField(biome.getFogColor(), val -> biome.setFogColor(val));
        TextField grassColorField = createColorField(biome.getGrassColor(), val -> biome.setGrassColor(val));
        TextField foliageColorField = createColorField(biome.getFoliageColor(), val -> biome.setFoliageColor(val));

        grid.add(createLabel(I18n.get("biome.waterColor")), 0, 0);
        grid.add(waterColorField, 1, 0);
        grid.add(createLabel(I18n.get("biome.waterFogColor")), 2, 0);
        grid.add(waterFogField, 3, 0);
        grid.add(createLabel(I18n.get("biome.fogColor")), 0, 1);
        grid.add(fogColorField, 1, 1);
        grid.add(createLabel(I18n.get("biome.grassColor")), 2, 1);
        grid.add(grassColorField, 3, 1);
        grid.add(createLabel(I18n.get("biome.foliageColor")), 0, 2);
        grid.add(foliageColorField, 1, 2);

        TitledPane titled = new TitledPane(I18n.get("biome.colors"), grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        return titled;
    }

    private TextField createColorField(int initialColor, Consumer<Integer> setter) {
        TextField field = new TextField(String.format("#%06X", initialColor & 0xFFFFFF));
        field.textProperty().addListener((obs, old, val) -> {
            try {
                String hex = val.startsWith("#") ? val.substring(1) : val;
                int color = Integer.parseInt(hex, 16);
                setter.accept(color);
                markDirty(); if (onBiomeChanged != null) onBiomeChanged.accept(null);
                field.setStyle("");
            } catch (NumberFormatException e) {
                field.setStyle("-fx-border-color: red;");
            }
        });
        field.setPromptText("#RRGGBB");
        return field;
    }

    private void showVanillaPicker(BiomeData target) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("平原",
                "平原", "沙漠", "森林", "针叶林", "沼泽", "丛林", "恶地", "热带草原", "冰刺之地",
                "海洋", "深海", "河流", "沙滩", "山地", "雪原", "下界荒地", "绯红森林", "诡异森林", "末地");
        dialog.setTitle(I18n.get("biome.copyVanilla.title"));
        dialog.setHeaderText(I18n.get("biome.copyVanilla.header"));
        dialog.setContentText(I18n.get("biome.copyVanilla.content"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            switch (selected) {
                case "平原":
                    target.setTemperature(0.8f);
                    target.setDownfall(0.4f);
                    target.setPrecipitation("RAIN");
                    target.setGrassColor(0x91BD59);
                    target.setFoliageColor(0x77AB2F);
                    target.setWaterColor(0x3F76E4);
                    target.setWaterFogColor(0x050533);
                    target.setFogColor(0xC0D8FF);
                    break;
                case "沙漠":
                    target.setTemperature(2.0f);
                    target.setDownfall(0.0f);
                    target.setPrecipitation("NONE");
                    target.setGrassColor(0xBFB755);
                    target.setFoliageColor(0xAEA42A);
                    target.setWaterColor(0x3F76E4);
                    break;
                case "森林":
                    target.setTemperature(0.7f);
                    target.setDownfall(0.8f);
                    target.setPrecipitation("RAIN");
                    target.setGrassColor(0x79C05A);
                    target.setFoliageColor(0x59AE30);
                    break;
                case "针叶林":
                    target.setTemperature(-0.5f);
                    target.setDownfall(0.8f);
                    target.setPrecipitation("RAIN");
                    target.setGrassColor(0x86B87F);
                    target.setFoliageColor(0x68A464);
                    break;
                case "沼泽":
                    target.setTemperature(0.8f);
                    target.setDownfall(0.9f);
                    target.setPrecipitation("RAIN");
                    target.setGrassColor(0x6A7039);
                    target.setFoliageColor(0x6A7039);
                    target.setWaterColor(0x4C6559);
                    break;
                case "丛林":
                    target.setTemperature(0.95f);
                    target.setDownfall(0.9f);
                    target.setPrecipitation("RAIN");
                    target.setGrassColor(0x59C93C);
                    target.setFoliageColor(0x30BB0B);
                    break;
                case "恶地":
                    target.setTemperature(2.0f);
                    target.setDownfall(0.0f);
                    target.setPrecipitation("NONE");
                    target.setGrassColor(0x9E814D);
                    target.setFoliageColor(0x9E814D);
                    break;
                case "下界荒地":
                    target.setTemperature(2.0f);
                    target.setDownfall(0.0f);
                    target.setPrecipitation("NONE");
                    target.setGrassColor(0xBFB755);
                    target.setFoliageColor(0xAEA42A);
                    target.setWaterColor(0x3F76E4);
                    target.setFogColor(0x330000);
                    break;
                case "末地":
                    target.setTemperature(0.5f);
                    target.setDownfall(0.5f);
                    target.setPrecipitation("NONE");
                    target.setGrassColor(0x8EB971);
                    target.setFoliageColor(0x71A74D);
                    target.setWaterColor(0x3F76E4);
                    target.setFogColor(0xA080A0);
                    break;
            }
            showEditorForBiome(target);
            markDirty(); if (onBiomeChanged != null) onBiomeChanged.accept(target);
        });
    }

    private void deleteBiome(BiomeData biome) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm"));
        confirm.setHeaderText(String.format(I18n.get("biome.deleteConfirm"), biome.getDisplayName()));
        confirm.setContentText(I18n.get("biome.deleteConfirmDetail"));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            biomes.remove(biome);
            if (biomes.isEmpty()) rightPanel.getChildren().setAll(createEmptyHint());
            markDirty(); if (onBiomeChanged != null) onBiomeChanged.accept(null);
        }
    }

    private Node createEmptyHint() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(I18n.get("biome.emptyHint"));
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        box.getChildren().add(label);
        return box;
    }

    private Label createLabel(String text) { Label label = new Label(text); label.setStyle("-fx-font-weight: bold;"); return label; }
    private void addTooltip(Control control, String text) { Tooltip tip = new Tooltip(text); tip.setWrapText(true); tip.setMaxWidth(300); control.setTooltip(tip); }

    public ObservableList<BiomeData> getBiomes() { return biomes; }
    public void setBiomes(ObservableList<BiomeData> newBiomes) {
        this.biomes.setAll(newBiomes);
        if (biomeListView.getSelectionModel().isEmpty() && !biomes.isEmpty()) biomeListView.getSelectionModel().selectFirst();
        clearDirty();
    }
    public void setOnBiomeChanged(Consumer<BiomeData> callback) { this.onBiomeChanged = callback; }
    public void refresh() { biomeListView.refresh(); }

    private static class BiomeListCell extends ListCell<BiomeData> {
        @Override
        protected void updateItem(BiomeData biome, boolean empty) {
            super.updateItem(biome, empty);
            if (empty || biome == null) { setText(null); setGraphic(null); }
            else {
                String display = biome.getDisplayName();
                String id = biome.getId();
                setText(display + " [" + id + "]");
                FontIcon icon = FontIcon.of(FontAwesomeSolid.TREE);
                icon.setIconSize(14);
                setGraphic(icon);
            }
        }
    }
}