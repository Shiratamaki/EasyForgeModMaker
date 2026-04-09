package com.easyforge.controller;

import com.easyforge.core.ProjectParser;
import com.easyforge.core.ProjectSaver;
import com.easyforge.core.generator.ProjectGenerator;
import com.easyforge.export.ProjectExporter;
import com.easyforge.export.ProjectImporter;
import com.easyforge.git.GitManager;
import com.easyforge.model.*;
import com.easyforge.template.ProjectTemplateManager;
import com.easyforge.ui.*;
import com.easyforge.util.*;
import com.easyforge.validator.ProjectValidator;
import com.easyforge.validator.ValidationError;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.eclipse.jgit.api.Status;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {

    @FXML private StackPane rootStackPane;
    @FXML private TreeView<File> fileTreeView;
    @FXML private TabPane tabPane;
    @FXML private SplitPane mainSplitPane;
    @FXML private Tab projectWizardTab;
    @FXML private Tab itemEditorTab;
    @FXML private Tab blockEditorTab;
    @FXML private Tab recipeEditorTab;
    @FXML private Tab aiChatTab;
    @FXML private Tab dependencyTab;
    @FXML private Tab fluidEditorTab;
    @FXML private Tab entityEditorTab;
    @FXML private Tab biomeEditorTab;
    @FXML private Tab enchantmentEditorTab;
    @FXML private Tab structureEditorTab;
    @FXML private Tab smithingTab;
    @FXML private Tab dimensionTab;
    @FXML private Tab smeltingTab;
    @FXML private Tab brewingTab;
    @FXML private Tab advancementTab;
    @FXML private Tab commandTab;
    @FXML private Tab tagTab;
    @FXML private Tab lootTableTab;
    @FXML private Tab gameRuleTab;
    @FXML private Tab worldGenTab;

    // 菜单项
    @FXML private Menu fileMenu;
    @FXML private Menu editMenu;
    @FXML private Menu themeMenu;
    @FXML private Menu helpMenu;
    @FXML private Menu languageMenu;
    @FXML private MenuItem newMenuItem;
    @FXML private MenuItem openMenuItem;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem undoMenuItem;
    @FXML private MenuItem redoMenuItem;
    @FXML private MenuItem lightThemeMenuItem;
    @FXML private MenuItem darkThemeMenuItem;
    @FXML private MenuItem sakuraThemeMenuItem;
    @FXML private MenuItem checkUpdateMenuItem;
    @FXML private MenuItem snippetMenuItem;
    @FXML private MenuItem helpDocMenuItem;
    @FXML private MenuItem logDirMenuItem;
    @FXML private MenuItem aboutMenuItem;
    @FXML private MenuItem chineseMenuItem;
    @FXML private MenuItem englishMenuItem;
    @FXML private MenuItem exportMenuItem;
    @FXML private MenuItem importMenuItem;
    @FXML private MenuItem validateMenuItem;
    @FXML private MenuItem gitInitMenuItem;
    @FXML private MenuItem gitCommitMenuItem;
    @FXML private MenuItem gitPushMenuItem;
    @FXML private MenuItem gitPullMenuItem;
    @FXML private MenuItem gitStatusMenuItem;

    // 项目向导控件
    @FXML private ComboBox<String> mcVersionCombo;
    @FXML private ComboBox<String> forgeVersionCombo;
    @FXML private TextField modIdField;
    @FXML private TextField modNameField;
    @FXML private TextField authorField;
    @FXML private TextField outputPathField;
    @FXML private Button browseOutputBtn;
    @FXML private Button generateProjectBtn;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> templateCombo;

    // 配方编辑器控件
    @FXML private TableView<RecipeData> recipeTableView;
    @FXML private TableColumn<RecipeData, String> recipeOutputCol;
    @FXML private TableColumn<RecipeData, Integer> recipeCountCol;
    @FXML private TableColumn<RecipeData, Boolean> recipeShapedCol;
    @FXML private VBox recipeEditorContainer;
    @FXML private TextField itemSearchField;
    @FXML private ListView<String> itemListView;
    private RecipeGridEditor recipeGridEditor;
    private ObservableList<String> availableItemsList;

    // 依赖管理控件
    @FXML private TableView<Dependency> dependencyTableView;
    @FXML private TableColumn<Dependency, String> depModIdCol;
    @FXML private TableColumn<Dependency, Boolean> depMandatoryCol;
    @FXML private TableColumn<Dependency, String> depVersionCol;
    @FXML private TableColumn<Dependency, String> depOrderingCol;
    @FXML private TableColumn<Dependency, String> depSideCol;
    @FXML private TextField depModIdField;
    @FXML private CheckBox depMandatoryCheck;
    @FXML private TextField depVersionField;
    @FXML private ComboBox<String> depOrderingCombo;
    @FXML private ComboBox<String> depSideCombo;
    @FXML private Button addDepBtn;
    @FXML private Button updateDepBtn;
    @FXML private Button deleteDepBtn;

    // 各编辑器容器
    @FXML private VBox fluidEditorContainer;
    @FXML private VBox entityEditorContainer;
    @FXML private VBox biomeEditorContainer;
    @FXML private VBox enchantmentEditorContainer;
    @FXML private VBox structureEditorContainer;
    @FXML private VBox smithingContainer;
    @FXML private VBox dimensionContainer;
    @FXML private VBox smeltingContainer;
    @FXML private VBox brewingContainer;
    @FXML private VBox advancementContainer;
    @FXML private VBox commandContainer;
    @FXML private VBox tagContainer;
    @FXML private VBox lootTableContainer;
    @FXML private VBox gameRuleContainer;
    @FXML private VBox worldGenContainer;

    // 新的编辑器实例（新手友好）
    private ItemEditorPane itemEditorPane;
    private BlockEditorPane blockEditorPane;
    private FluidEditorPane fluidEditorPane;
    private EntityEditorPane entityEditorPane;
    private BiomeEditorPane biomeEditorPane;
    private EnchantmentEditorPane enchantmentEditorPane;
    private StructureEditorPane structureEditorPane;
    private SmithingRecipeEditorPane smithingEditorPane;
    private DimensionEditorPane dimensionEditorPane;
    private SmeltingRecipeEditorPane smeltingEditorPane;
    private BrewingRecipeEditorPane brewingEditorPane;
    private AdvancementEditorPane advancementEditorPane;
    private CommandEditorPane commandEditorPane;
    private TagEditorPane tagEditorPane;
    private LootTableEditorPane lootTableEditorPane;
    private GameRuleEditorPane gameRuleEditorPane;
    private WorldGenEditorPane worldGenEditorPane;

    // 全局项目数据
    private ModProject currentProject;
    private FileTreeManager fileTreeManager;
    private SakuraBackground sakuraBackground;
    private boolean sakuraAdded = false;
    private GitManager gitManager;

    // 可观察列表
    private ObservableList<ItemData> observableItems;
    private ObservableList<BlockData> observableBlocks;
    private ObservableList<RecipeData> observableRecipes;
    private ObservableList<Dependency> observableDependencies;

    // 撤销/重做管理器
    private final UndoRedoManager undoRedoManager = new UndoRedoManager();

    // 主题应用标志
    private boolean themeApplied = false;

    // 主题设置方法
    public void setTheme(ThemeManager.Theme theme) {
        javafx.application.Platform.runLater(() -> {
            Scene scene = rootStackPane.getScene();
            if (scene != null) {
                ThemeManager.applyTheme(scene, theme);
                if (theme == ThemeManager.Theme.SAKURA) {
                    addSakura();
                } else {
                    removeSakura();
                }
                themeApplied = true;
            } else {
                System.err.println("setTheme: scene is null, retrying...");
                javafx.application.Platform.runLater(() -> {
                    Scene scene2 = rootStackPane.getScene();
                    if (scene2 != null) {
                        ThemeManager.applyTheme(scene2, theme);
                        if (theme == ThemeManager.Theme.SAKURA) addSakura();
                        else removeSakura();
                    } else {
                        System.err.println("setTheme: scene still null");
                    }
                });
            }
        });
    }

    @FXML
    public void initialize() {
        // 调整左侧文件树宽度和选项卡标签最小宽度
        if (mainSplitPane != null) {
            mainSplitPane.setDividerPositions(0.2);
        }
        if (tabPane != null) {
            tabPane.setTabMinWidth(80);
        }

        fileTreeView.setVisible(false);
        itemEditorTab.setDisable(true);
        blockEditorTab.setDisable(true);
        recipeEditorTab.setDisable(true);
        aiChatTab.setDisable(true);
        if (dependencyTab != null) dependencyTab.setDisable(true);
        fluidEditorTab.setDisable(true);
        entityEditorTab.setDisable(true);
        biomeEditorTab.setDisable(true);
        enchantmentEditorTab.setDisable(true);
        structureEditorTab.setDisable(true);
        smithingTab.setDisable(true);
        dimensionTab.setDisable(true);
        smeltingTab.setDisable(true);
        brewingTab.setDisable(true);
        advancementTab.setDisable(true);
        commandTab.setDisable(true);
        tagTab.setDisable(true);
        lootTableTab.setDisable(true);
        gameRuleTab.setDisable(true);
        worldGenTab.setDisable(true);

        fileTreeManager = new FileTreeManager(fileTreeView, null);
        setupFileTreeContextMenu();

        mcVersionCombo.getItems().addAll("1.20.1", "1.19.2", "1.18.2");
        forgeVersionCombo.getItems().addAll("47.4.18", "43.4.0", "40.2.0");
        templateCombo.getItems().addAll("无模板", "基础物品模组", "基础方块模组");
        templateCombo.setValue("无模板");

        // 物品编辑器
        itemEditorPane = new ItemEditorPane();
        itemEditorTab.setContent(itemEditorPane);

        // 方块编辑器
        blockEditorPane = new BlockEditorPane();
        blockEditorTab.setContent(blockEditorPane);

        // 配方编辑器
        recipeGridEditor = new RecipeGridEditor();
        recipeEditorContainer.getChildren().add(recipeGridEditor);

        recipeOutputCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getOutputItem()));
        recipeCountCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getOutputCount()));
        recipeShapedCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().isShaped()));

        recipeTableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) recipeGridEditor.setRecipe(selected);
            else recipeGridEditor.clear();
        });

        // 物品列表侧边栏（用于配方编辑器）
        availableItemsList = FXCollections.observableArrayList();
        itemListView.setItems(availableItemsList);
        itemSearchField.textProperty().addListener((obs, old, val) -> filterItemList(val));
        itemListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = itemListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    String pureId = selected.split(" ")[0];
                    recipeGridEditor.setOutputItem(pureId);
                }
            }
        });
        itemListView.setOnDragDetected(event -> {
            String selected = itemListView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.equals("没有匹配的物品") && !selected.equals("请先创建或打开项目")) {
                String pureId = selected.split(" ")[0];
                Dragboard db = itemListView.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putString(pureId);
                db.setContent(content);
                event.consume();
            }
        });

        // 依赖管理
        if (dependencyTableView != null) {
            depModIdCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getModId()));
            depMandatoryCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().isMandatory()));
            depVersionCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getVersionRange()));
            depOrderingCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getOrdering()));
            depSideCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getSide()));
            dependencyTableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
                if (selected != null) loadDependencyToForm(selected);
            });
            depOrderingCombo.getItems().addAll("NONE", "BEFORE", "AFTER");
            depSideCombo.getItems().addAll("BOTH", "CLIENT", "SERVER");
        }

        // 初始化所有编辑器
        fluidEditorPane = new FluidEditorPane();
        fluidEditorContainer.getChildren().setAll(fluidEditorPane);
        entityEditorPane = new EntityEditorPane();
        entityEditorContainer.getChildren().setAll(entityEditorPane);
        biomeEditorPane = new BiomeEditorPane();
        biomeEditorContainer.getChildren().setAll(biomeEditorPane);
        enchantmentEditorPane = new EnchantmentEditorPane();
        enchantmentEditorContainer.getChildren().setAll(enchantmentEditorPane);
        structureEditorPane = new StructureEditorPane();
        structureEditorContainer.getChildren().setAll(structureEditorPane);
        smithingEditorPane = new SmithingRecipeEditorPane();
        smithingContainer.getChildren().setAll(smithingEditorPane);
        dimensionEditorPane = new DimensionEditorPane();
        dimensionContainer.getChildren().setAll(dimensionEditorPane);
        smeltingEditorPane = new SmeltingRecipeEditorPane();
        smeltingContainer.getChildren().setAll(smeltingEditorPane);
        brewingEditorPane = new BrewingRecipeEditorPane();
        brewingContainer.getChildren().setAll(brewingEditorPane);
        advancementEditorPane = new AdvancementEditorPane();
        advancementContainer.getChildren().setAll(advancementEditorPane);
        commandEditorPane = new CommandEditorPane();
        commandContainer.getChildren().setAll(commandEditorPane);
        tagEditorPane = new TagEditorPane();
        tagContainer.getChildren().setAll(tagEditorPane);
        lootTableEditorPane = new LootTableEditorPane();
        lootTableContainer.getChildren().setAll(lootTableEditorPane);
        gameRuleEditorPane = new GameRuleEditorPane();
        gameRuleContainer.getChildren().setAll(gameRuleEditorPane);
        worldGenEditorPane = new WorldGenEditorPane();
        worldGenContainer.getChildren().setAll(worldGenEditorPane);

        browseOutputBtn.setOnAction(e -> browseOutputPath());
        generateProjectBtn.setOnAction(e -> generateNewProject());
        if (addDepBtn != null) addDepBtn.setOnAction(e -> addDependency());
        if (updateDepBtn != null) updateDepBtn.setOnAction(e -> updateDependency());
        if (deleteDepBtn != null) deleteDepBtn.setOnAction(e -> deleteDependency());

        if (addDepBtn != null) addDepBtn.setGraphic(new FontIcon(FontAwesomeSolid.PLUS));
        if (updateDepBtn != null) updateDepBtn.setGraphic(new FontIcon(FontAwesomeSolid.EDIT));
        if (deleteDepBtn != null) deleteDepBtn.setGraphic(new FontIcon(FontAwesomeSolid.TRASH_ALT));
        browseOutputBtn.setGraphic(new FontIcon(FontAwesomeSolid.FOLDER_OPEN));
        browseOutputBtn.setContentDisplay(ContentDisplay.LEFT);
        generateProjectBtn.setGraphic(new FontIcon(FontAwesomeSolid.COG));
        generateProjectBtn.setContentDisplay(ContentDisplay.LEFT);

        // 主题监听：只注册快捷键，不再设置样式表
        rootStackPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                        undoRedoManager.undo();
                        event.consume();
                    } else if (event.isControlDown() && event.getCode() == KeyCode.Y) {
                        undoRedoManager.redo();
                        event.consume();
                    }
                });
            }
        });

        applyI18n();

        Platform.runLater(() -> {
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> onCheckForUpdates());
            delay.play();
        });
    }

    private void setupFileTreeContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem newFile = new MenuItem(I18n.get("file.new"));
        MenuItem newFolder = new MenuItem(I18n.get("folder.new"));
        MenuItem delete = new MenuItem(I18n.get("delete"));
        MenuItem rename = new MenuItem(I18n.get("rename"));
        newFile.setOnAction(e -> fileTreeManager.createNewFile());
        newFolder.setOnAction(e -> fileTreeManager.createNewFolder());
        delete.setOnAction(e -> fileTreeManager.deleteSelected());
        rename.setOnAction(e -> fileTreeManager.renameSelected());
        menu.getItems().addAll(newFile, newFolder, delete, rename);
        fileTreeView.setContextMenu(menu);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean confirmDelete(String type, String name) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.get("confirm.title"));
        alert.setHeaderText(I18n.get("confirm.deleteHeader", type));
        alert.setContentText(I18n.get("confirm.deleteContent", type, name));
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showValidationErrors(List<ValidationError> errors) {
        if (errors.isEmpty()) return;
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        Label title = new Label("发现 " + errors.size() + " 个问题：");
        title.setStyle("-fx-font-weight: bold;");
        content.getChildren().add(title);
        ListView<String> listView = new ListView<>();
        for (ValidationError err : errors) {
            String prefix = err.getSeverity() == ValidationError.Severity.ERROR ? "❌ " : "⚠️ ";
            listView.getItems().add(prefix + "[" + err.getCategory() + "] " + err.getMessage() + " (建议: " + err.getSuggestedFix() + ")");
        }
        content.getChildren().add(listView);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("项目验证");
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    private void applyI18n() {
        if (fileMenu != null) fileMenu.setText(I18n.get("menu.file"));
        if (editMenu != null) editMenu.setText(I18n.get("menu.edit"));
        if (themeMenu != null) themeMenu.setText(I18n.get("menu.theme"));
        if (helpMenu != null) helpMenu.setText(I18n.get("menu.help"));
        if (languageMenu != null) languageMenu.setText(I18n.get("menu.language"));
        if (newMenuItem != null) newMenuItem.setText(I18n.get("menu.new"));
        if (openMenuItem != null) openMenuItem.setText(I18n.get("menu.open"));
        if (saveMenuItem != null) saveMenuItem.setText(I18n.get("menu.save"));
        if (exitMenuItem != null) exitMenuItem.setText(I18n.get("menu.exit"));
        if (undoMenuItem != null) undoMenuItem.setText(I18n.get("menu.undo"));
        if (redoMenuItem != null) redoMenuItem.setText(I18n.get("menu.redo"));
        if (lightThemeMenuItem != null) lightThemeMenuItem.setText(I18n.get("theme.light"));
        if (darkThemeMenuItem != null) darkThemeMenuItem.setText(I18n.get("theme.dark"));
        if (sakuraThemeMenuItem != null) sakuraThemeMenuItem.setText(I18n.get("theme.sakura"));
        if (checkUpdateMenuItem != null) checkUpdateMenuItem.setText(I18n.get("menu.checkUpdate"));
        if (snippetMenuItem != null) snippetMenuItem.setText(I18n.get("menu.snippet"));
        if (helpDocMenuItem != null) helpDocMenuItem.setText(I18n.get("menu.helpDoc"));
        if (logDirMenuItem != null) logDirMenuItem.setText(I18n.get("menu.logDir"));
        if (aboutMenuItem != null) aboutMenuItem.setText(I18n.get("menu.about"));
        if (chineseMenuItem != null) chineseMenuItem.setText(I18n.get("lang.zh"));
        if (englishMenuItem != null) englishMenuItem.setText(I18n.get("lang.en"));
        if (exportMenuItem != null) exportMenuItem.setText(I18n.get("menu.export"));
        if (importMenuItem != null) importMenuItem.setText(I18n.get("menu.import"));
        if (validateMenuItem != null) validateMenuItem.setText(I18n.get("menu.validate"));
        if (gitInitMenuItem != null) gitInitMenuItem.setText(I18n.get("git.init"));
        if (gitCommitMenuItem != null) gitCommitMenuItem.setText(I18n.get("git.commit"));
        if (gitPushMenuItem != null) gitPushMenuItem.setText(I18n.get("git.push"));
        if (gitPullMenuItem != null) gitPullMenuItem.setText(I18n.get("git.pull"));
        if (gitStatusMenuItem != null) gitStatusMenuItem.setText(I18n.get("git.status"));

        projectWizardTab.setText(I18n.get("tab.wizard"));
        itemEditorTab.setText(I18n.get("tab.items"));
        blockEditorTab.setText(I18n.get("tab.blocks"));
        recipeEditorTab.setText(I18n.get("tab.recipes"));
        aiChatTab.setText(I18n.get("tab.ai"));
        if (dependencyTab != null) dependencyTab.setText(I18n.get("tab.dependencies"));
        fluidEditorTab.setText(I18n.get("tab.fluids"));
        entityEditorTab.setText(I18n.get("tab.entities"));
        biomeEditorTab.setText(I18n.get("tab.biomes"));
        enchantmentEditorTab.setText(I18n.get("tab.enchantments"));
        structureEditorTab.setText(I18n.get("tab.structures"));
        smithingTab.setText(I18n.get("tab.smithing"));
        dimensionTab.setText(I18n.get("tab.dimensions"));
        smeltingTab.setText(I18n.get("tab.smelting"));
        brewingTab.setText(I18n.get("tab.brewing"));
        advancementTab.setText(I18n.get("tab.advancements"));
        commandTab.setText(I18n.get("tab.commands"));
        tagTab.setText(I18n.get("tab.tags"));
        lootTableTab.setText(I18n.get("tab.lootTables"));
        gameRuleTab.setText(I18n.get("tab.gameRules"));
        worldGenTab.setText(I18n.get("tab.worldGen"));

        if (addDepBtn != null) addDepBtn.setText(I18n.get("btn.add"));
        if (updateDepBtn != null) updateDepBtn.setText(I18n.get("btn.update"));
        if (deleteDepBtn != null) deleteDepBtn.setText(I18n.get("btn.delete"));
        browseOutputBtn.setText(I18n.get("btn.browse"));
        generateProjectBtn.setText(I18n.get("btn.generate"));
        statusLabel.setText(I18n.get("status.ready"));
    }

    private void browseOutputPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择项目存放目录");
        File dir = chooser.showDialog(rootStackPane.getScene().getWindow());
        if (dir != null) outputPathField.setText(dir.getAbsolutePath());
    }

    private void generateNewProject() {
        String mc = mcVersionCombo.getValue();
        String forge = forgeVersionCombo.getValue();
        String modId = modIdField.getText().trim();
        String modName = modNameField.getText().trim();
        String author = authorField.getText().trim();
        String path = outputPathField.getText().trim();
        String template = templateCombo.getValue();

        if (mc == null || forge == null || modId.isEmpty() || path.isEmpty()) {
            showAlert("错误", "请填写所有必填项");
            return;
        }
        if (!modId.matches("[a-z0-9_]+")) {
            showAlert("错误", "模组ID只能包含小写字母、数字和下划线");
            return;
        }

        currentProject = new ModProject();
        currentProject.setModId(modId);
        currentProject.setModName(modName);
        currentProject.setAuthor(author);
        currentProject.setOutputPath(path);
        currentProject.setMinecraftVersion(mc);
        currentProject.setForgeVersion(forge);
        currentProject.setMainClassPackage(modId.replace('_', '.'));

        progressBar.setVisible(true);
        statusLabel.setText("正在生成项目...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    if (!"无模板".equals(template)) {
                        String templateName = "basic_items".equals(template) ? "basic_items" : "basic_blocks";
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("{{modId}}", modId);
                        placeholders.put("{{package}}", currentProject.getMainClassPackage());
                        placeholders.put("{{modName}}", modName);
                        placeholders.put("{{author}}", author);
                        ProjectTemplateManager.applyTemplate(templateName, Paths.get(path), placeholders);
                    } else {
                        Map<String, String> params = new HashMap<>();
                        params.put("minecraftVersion", mc);
                        params.put("forgeVersion", forge);
                        params.put("modId", modId);
                        params.put("modName", modName);
                        params.put("author", author);
                        params.put("projectPath", path);
                        new ProjectGenerator().generate(params);
                    }
                    ProjectSaver.save(currentProject);
                } catch (Exception e) {
                    LogUtil.error("生成项目失败", e);
                    throw e;
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            statusLabel.setText("项目生成成功！");
            showAlert("成功", "项目已生成在 " + path);
            afterProjectLoaded();
        });
        task.setOnFailed(e -> {
            progressBar.setVisible(false);
            statusLabel.setText("生成失败");
            Throwable ex = task.getException();
            LogUtil.error("生成项目任务失败", ex);
            showAlert("失败", ex.getMessage());
        });
        new Thread(task).start();
    }

    @FXML private void onOpenProject() {
        DirectoryChooser chooser = new DirectoryChooser();
        File dir = chooser.showDialog(rootStackPane.getScene().getWindow());
        if (dir != null) {
            try {
                ModProject loaded = ProjectSaver.load(dir.toPath());
                if (loaded != null) {
                    currentProject = loaded;
                } else {
                    currentProject = ProjectParser.parseFromDirectory(dir);
                    if (currentProject.getModId() == null) {
                        showAlert("错误", "无法识别项目结构，请确保是有效的 Forge 模组项目。");
                        return;
                    }
                }
                afterProjectLoaded();
                showAlert("打开项目", "已加载项目: " + dir.getName());
            } catch (Exception ex) {
                LogUtil.error("打开项目失败", ex);
                showAlert("错误", "无法加载项目: " + ex.getMessage());
            }
        }
    }

    private void afterProjectLoaded() {
        fileTreeManager.setCurrentProjectPath(new File(currentProject.getOutputPath()));
        fileTreeView.setVisible(true);
        itemEditorTab.setDisable(false);
        blockEditorTab.setDisable(false);
        recipeEditorTab.setDisable(false);
        aiChatTab.setDisable(false);
        if (dependencyTab != null) dependencyTab.setDisable(false);
        fluidEditorTab.setDisable(false);
        entityEditorTab.setDisable(false);
        biomeEditorTab.setDisable(false);
        enchantmentEditorTab.setDisable(false);
        structureEditorTab.setDisable(false);
        smithingTab.setDisable(false);
        dimensionTab.setDisable(false);
        smeltingTab.setDisable(false);
        brewingTab.setDisable(false);
        advancementTab.setDisable(false);
        commandTab.setDisable(false);
        tagTab.setDisable(false);
        lootTableTab.setDisable(false);
        gameRuleTab.setDisable(false);
        worldGenTab.setDisable(false);

        observableItems = FXCollections.observableArrayList(currentProject.getItems());
        observableBlocks = FXCollections.observableArrayList(currentProject.getBlocks());
        observableRecipes = FXCollections.observableArrayList(currentProject.getRecipes());

        if (itemEditorPane != null) {
            itemEditorPane.setProject(currentProject);
            itemEditorPane.setItems(observableItems);
        }
        if (blockEditorPane != null) {
            blockEditorPane.setProject(currentProject);
            blockEditorPane.setBlocks(observableBlocks);
        }
        if (fluidEditorPane != null) {
            fluidEditorPane.setProject(currentProject);
            fluidEditorPane.setFluids(FXCollections.observableArrayList(currentProject.getFluids()));
        }
        if (entityEditorPane != null) {
            entityEditorPane.setProject(currentProject);
            entityEditorPane.setEntities(FXCollections.observableArrayList(currentProject.getEntities()));
        }
        if (biomeEditorPane != null) {
            biomeEditorPane.setProject(currentProject);
            biomeEditorPane.setBiomes(FXCollections.observableArrayList(currentProject.getBiomes()));
        }
        if (enchantmentEditorPane != null) {
            enchantmentEditorPane.setProject(currentProject);
            enchantmentEditorPane.setEnchantments(FXCollections.observableArrayList(currentProject.getEnchantments()));
        }
        if (structureEditorPane != null) {
            structureEditorPane.setProject(currentProject);
            structureEditorPane.setStructures(FXCollections.observableArrayList(currentProject.getStructures()));
        }
        if (smithingEditorPane != null) {
            smithingEditorPane.setProject(currentProject);
            smithingEditorPane.setRecipes(FXCollections.observableArrayList(currentProject.getSmithingRecipes()));
        }
        if (dimensionEditorPane != null) {
            dimensionEditorPane.setProject(currentProject);
            dimensionEditorPane.setDimensions(FXCollections.observableArrayList(currentProject.getDimensions()));
        }
        if (smeltingEditorPane != null) {
            smeltingEditorPane.setProject(currentProject);
            smeltingEditorPane.setRecipes(FXCollections.observableArrayList(currentProject.getSmeltingRecipes()));
        }
        if (brewingEditorPane != null) {
            brewingEditorPane.setProject(currentProject);
            brewingEditorPane.setRecipes(FXCollections.observableArrayList(currentProject.getBrewingRecipes()));
        }
        if (advancementEditorPane != null) {
            advancementEditorPane.setProject(currentProject);
            advancementEditorPane.setAdvancements(FXCollections.observableArrayList(currentProject.getAdvancements()));
        }
        if (commandEditorPane != null) {
            commandEditorPane.setProject(currentProject);
            commandEditorPane.setCommands(FXCollections.observableArrayList(currentProject.getCommands()));
        }
        if (tagEditorPane != null) {
            tagEditorPane.setProject(currentProject);
            tagEditorPane.setTags(FXCollections.observableArrayList(currentProject.getTags()));
        }
        if (lootTableEditorPane != null) {
            lootTableEditorPane.setProject(currentProject);
            lootTableEditorPane.setLootTables(FXCollections.observableArrayList(currentProject.getLootTables()));
        }
        if (gameRuleEditorPane != null) {
            gameRuleEditorPane.setProject(currentProject);
            gameRuleEditorPane.setGameRules(FXCollections.observableArrayList(currentProject.getGameRules()));
        }
        if (worldGenEditorPane != null) {
            worldGenEditorPane.setProject(currentProject);
            worldGenEditorPane.setWorldGens(FXCollections.observableArrayList(currentProject.getWorldGenConfigs()));
        }

        recipeTableView.setItems(observableRecipes);
        if (dependencyTableView != null) {
            observableDependencies = FXCollections.observableArrayList(currentProject.getDependencies());
            dependencyTableView.setItems(observableDependencies);
        }

        filterItemList(null);
        undoRedoManager.clear();
        gitManager = new GitManager(new File(currentProject.getOutputPath()));
        ImageLoader.clearCache();
        System.gc();
    }

    private void filterItemList(String filter) {
        if (currentProject == null) {
            availableItemsList.setAll(List.of("请先创建或打开项目"));
            return;
        }
        List<String> allItems = new ArrayList<>();
        for (ItemData item : currentProject.getItems()) {
            String id = currentProject.getModId() + ":" + item.getId();
            String chinese = ChineseNames.getChinese(id);
            String display = chinese.isEmpty() ? id : id + " (" + chinese + ")";
            allItems.add(display);
        }
        for (BlockData block : currentProject.getBlocks()) {
            String id = currentProject.getModId() + ":" + block.getId();
            String chinese = ChineseNames.getChinese(id);
            String display = chinese.isEmpty() ? id : id + " (" + chinese + ")";
            allItems.add(display);
        }
        for (String vanillaId : VanillaItems.COMMON_IDS) {
            String fullId = vanillaId.contains(":") ? vanillaId : "minecraft:" + vanillaId;
            if (!allItems.stream().anyMatch(s -> s.startsWith(fullId + " "))) {
                String chinese = ChineseNames.getChinese(fullId);
                String display = chinese.isEmpty() ? fullId : fullId + " (" + chinese + ")";
                allItems.add(display);
            }
        }
        allItems.sort(String::compareTo);

        if (filter == null || filter.isEmpty()) {
            availableItemsList.setAll(allItems);
        } else {
            String lowerFilter = filter.toLowerCase();
            List<String> filtered = allItems.stream()
                    .filter(s -> s.toLowerCase().contains(lowerFilter))
                    .collect(Collectors.toList());
            if (filtered.isEmpty()) {
                availableItemsList.setAll(List.of("没有匹配的物品"));
            } else {
                availableItemsList.setAll(filtered);
            }
        }
    }

    @FXML private void clearRecipeGrid() {
        recipeGridEditor.clear();
    }

    @FXML private void addRecipe() {
        RecipeData newRecipe = recipeGridEditor.getRecipe();
        if (newRecipe.getOutputItem().isEmpty()) {
            showAlert("错误", "输出物品ID不能为空");
            return;
        }
        undoRedoManager.execute(() -> {
            observableRecipes.add(newRecipe);
            currentProject.getRecipes().add(newRecipe);
            recipeTableView.refresh();
            recipeGridEditor.clear();
        }, () -> {
            observableRecipes.remove(newRecipe);
            currentProject.getRecipes().remove(newRecipe);
            recipeTableView.refresh();
        });
    }

    @FXML private void updateRecipe() {
        RecipeData selected = recipeTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要更新的配方");
            return;
        }
        RecipeData newRecipe = recipeGridEditor.getRecipe();
        if (newRecipe.getOutputItem().isEmpty()) {
            showAlert("错误", "输出物品ID不能为空");
            return;
        }
        RecipeData oldState = copyRecipe(selected);
        undoRedoManager.execute(() -> {
            selected.setOutputItem(newRecipe.getOutputItem());
            selected.setOutputCount(newRecipe.getOutputCount());
            selected.setShaped(newRecipe.isShaped());
            selected.setShape(newRecipe.getShape());
            selected.setKeys(newRecipe.getKeys());
            recipeTableView.refresh();
            recipeGridEditor.clear();
        }, () -> {
            selected.setOutputItem(oldState.getOutputItem());
            selected.setOutputCount(oldState.getOutputCount());
            selected.setShaped(oldState.isShaped());
            selected.setShape(oldState.getShape());
            selected.setKeys(oldState.getKeys());
            recipeTableView.refresh();
            recipeGridEditor.clear();
        });
    }

    @FXML private void deleteRecipe() {
        RecipeData selected = recipeTableView.getSelectionModel().getSelectedItem();
        if (selected != null && confirmDelete("配方", selected.getOutputItem())) {
            RecipeData copy = copyRecipe(selected);
            undoRedoManager.execute(() -> {
                observableRecipes.remove(selected);
                currentProject.getRecipes().remove(selected);
                recipeGridEditor.clear();
            }, () -> {
                observableRecipes.add(copy);
                currentProject.getRecipes().add(copy);
                recipeTableView.getSelectionModel().select(copy);
                recipeGridEditor.setRecipe(copy);
            });
        }
    }

    private RecipeData copyRecipe(RecipeData original) {
        if (original == null) return null;
        RecipeData copy = new RecipeData();
        copy.setOutputItem(original.getOutputItem());
        copy.setOutputCount(original.getOutputCount());
        copy.setShaped(original.isShaped());
        if (original.getShape() != null) {
            String[] shapeCopy = new String[original.getShape().length];
            System.arraycopy(original.getShape(), 0, shapeCopy, 0, original.getShape().length);
            copy.setShape(shapeCopy);
        }
        Map<Character, String> keysCopy = new HashMap<>(original.getKeys());
        copy.setKeys(keysCopy);
        return copy;
    }

    private void loadDependencyToForm(Dependency dep) {
        depModIdField.setText(dep.getModId());
        depMandatoryCheck.setSelected(dep.isMandatory());
        depVersionField.setText(dep.getVersionRange());
        depOrderingCombo.setValue(dep.getOrdering());
        depSideCombo.setValue(dep.getSide());
    }

    private void addDependency() {
        if (depModIdField.getText().trim().isEmpty()) {
            showAlert("错误", "模组ID不能为空");
            return;
        }
        Dependency dep = new Dependency(
                depModIdField.getText().trim(),
                depMandatoryCheck.isSelected(),
                depVersionField.getText().trim(),
                depOrderingCombo.getValue(),
                depSideCombo.getValue()
        );
        observableDependencies.add(dep);
        currentProject.getDependencies().add(dep);
        clearDependencyForm();
    }

    private void updateDependency() {
        Dependency selected = dependencyTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要更新的依赖");
            return;
        }
        selected.setModId(depModIdField.getText().trim());
        selected.setMandatory(depMandatoryCheck.isSelected());
        selected.setVersionRange(depVersionField.getText().trim());
        selected.setOrdering(depOrderingCombo.getValue());
        selected.setSide(depSideCombo.getValue());
        dependencyTableView.refresh();
        clearDependencyForm();
    }

    private void deleteDependency() {
        Dependency selected = dependencyTableView.getSelectionModel().getSelectedItem();
        if (selected != null && confirmDelete("依赖", selected.getModId())) {
            observableDependencies.remove(selected);
            currentProject.getDependencies().remove(selected);
            clearDependencyForm();
        }
    }

    private void clearDependencyForm() {
        depModIdField.clear();
        depMandatoryCheck.setSelected(true);
        depVersionField.clear();
        depOrderingCombo.setValue("NONE");
        depSideCombo.setValue("BOTH");
        dependencyTableView.getSelectionModel().clearSelection();
    }

    @FXML private void onBatchImportItems() {
        if (currentProject == null) {
            showAlert("错误", "请先打开一个项目");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择 CSV 文件");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV文件", "*.csv"));
        File file = chooser.showOpenDialog(rootStackPane.getScene().getWindow());
        if (file == null) return;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            int startLine = lines.get(0).toLowerCase().contains("id") ? 1 : 0;
            List<ItemData> newItems = new ArrayList<>();
            for (int i = startLine; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                String id = parts[0].trim();
                String name = parts[1].trim();
                int maxStack = Integer.parseInt(parts[2].trim());
                String texture = parts.length > 3 ? parts[3].trim() : "";
                if (!id.matches("[a-z0-9_]+")) {
                    showAlert("警告", "跳过无效物品ID: " + id);
                    continue;
                }
                ItemData item = new ItemData(id, name, maxStack);
                item.setTexturePath(texture);
                newItems.add(item);
            }

            if (!newItems.isEmpty()) {
                undoRedoManager.execute(() -> {
                    observableItems.addAll(newItems);
                    currentProject.getItems().addAll(newItems);
                    itemEditorPane.setItems(observableItems);
                }, () -> {
                    observableItems.removeAll(newItems);
                    currentProject.getItems().removeAll(newItems);
                    itemEditorPane.setItems(observableItems);
                });
                showAlert("成功", "已导入 " + newItems.size() + " 个物品");
            } else {
                showAlert("提示", "没有找到有效物品数据");
            }
        } catch (Exception e) {
            LogUtil.error("批量导入失败", e);
            showAlert("错误", "导入失败: " + e.getMessage());
        }
    }

    @FXML private void onImportTextures() {
        if (currentProject == null) {
            showAlert("错误", "请先打开一个项目");
            return;
        }
        TextureImportDialog dialog = new TextureImportDialog(currentProject);
        dialog.showAndWait();
    }

    @FXML private void onBuildMod() {
        if (currentProject == null) {
            showAlert("错误", "没有打开的项目");
            return;
        }
        File projectDir = new File(currentProject.getOutputPath());
        if (!projectDir.exists()) {
            showAlert("错误", "项目目录不存在");
            return;
        }
        try {
            ProjectSaver.save(currentProject);
            new ProjectGenerator().generateFullFromProject(currentProject);
        } catch (Exception e) {
            LogUtil.error("保存项目失败", e);
            showAlert("错误", "保存项目失败: " + e.getMessage());
            return;
        }

        Task<Void> buildTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ProcessBuilder pb;
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    pb = new ProcessBuilder("cmd.exe", "/c", "gradlew.bat", "build");
                } else {
                    pb = new ProcessBuilder("./gradlew", "build");
                }
                pb.directory(projectDir);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        LogUtil.info("[BUILD] " + line);
                    }
                }
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new Exception("Gradle build 失败，退出码: " + exitCode);
                }
                return null;
            }
        };
        buildTask.setOnSucceeded(e -> {
            showAlert("成功", "模组打包完成！\n输出目录: " + projectDir + "\\build\\libs");
        });
        buildTask.setOnFailed(e -> {
            LogUtil.error("打包失败", buildTask.getException());
            showAlert("失败", "打包失败: " + buildTask.getException().getMessage());
        });
        new Thread(buildTask).start();
    }

    @FXML private void onSaveProject() {
        if (currentProject == null) {
            showAlert("错误", "没有打开的项目");
            return;
        }
        try {
            ProjectSaver.save(currentProject);
            new ProjectGenerator().generateFullFromProject(currentProject);
            if (itemEditorPane != null) itemEditorPane.clearDirty();
            if (blockEditorPane != null) blockEditorPane.clearDirty();
            if (fluidEditorPane != null) fluidEditorPane.clearDirty();
            if (entityEditorPane != null) entityEditorPane.clearDirty();
            if (biomeEditorPane != null) biomeEditorPane.clearDirty();
            if (enchantmentEditorPane != null) enchantmentEditorPane.clearDirty();
            if (structureEditorPane != null) structureEditorPane.clearDirty();
            if (smithingEditorPane != null) smithingEditorPane.clearDirty();
            if (dimensionEditorPane != null) dimensionEditorPane.clearDirty();
            if (smeltingEditorPane != null) smeltingEditorPane.clearDirty();
            if (brewingEditorPane != null) brewingEditorPane.clearDirty();
            if (advancementEditorPane != null) advancementEditorPane.clearDirty();
            if (commandEditorPane != null) commandEditorPane.clearDirty();
            if (tagEditorPane != null) tagEditorPane.clearDirty();
            if (lootTableEditorPane != null) lootTableEditorPane.clearDirty();
            if (gameRuleEditorPane != null) gameRuleEditorPane.clearDirty();
            if (worldGenEditorPane != null) worldGenEditorPane.clearDirty();
            showAlert("成功", "项目已保存并重新生成 Forge 代码");
        } catch (Exception ex) {
            LogUtil.error("保存项目失败", ex);
            showAlert("失败", ex.getMessage());
        }
    }

    @FXML private void onExportProject() {
        if (currentProject == null) {
            showAlert("错误", "没有打开的项目");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("导出项目");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EasyForge项目文件", "*.efmod"));
        File file = chooser.showSaveDialog(rootStackPane.getScene().getWindow());
        if (file != null) {
            try {
                ProjectExporter.export(new File(currentProject.getOutputPath()), file);
                showAlert("成功", "项目已导出到: " + file.getAbsolutePath());
            } catch (Exception ex) {
                LogUtil.error("导出项目失败", ex);
                showAlert("失败", "导出失败: " + ex.getMessage());
            }
        }
    }

    @FXML private void onImportProject() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("导入项目");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EasyForge项目文件", "*.efmod"));
        File file = chooser.showOpenDialog(rootStackPane.getScene().getWindow());
        if (file == null) return;
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("选择导入目标目录");
        File targetDir = dirChooser.showDialog(rootStackPane.getScene().getWindow());
        if (targetDir == null) return;
        try {
            ProjectImporter.importProject(file, targetDir);
            ModProject imported = ProjectSaver.load(targetDir.toPath());
            if (imported != null) {
                currentProject = imported;
                afterProjectLoaded();
                showAlert("成功", "项目已导入并加载");
            } else {
                showAlert("成功", "项目已导入，但无法自动加载，请手动打开项目");
            }
        } catch (Exception ex) {
            LogUtil.error("导入项目失败", ex);
            showAlert("失败", "导入失败: " + ex.getMessage());
        }
    }

    @FXML private void onGitInit() {
        if (currentProject == null) {
            showAlert("错误", "没有打开的项目");
            return;
        }
        if (gitManager == null) {
            gitManager = new GitManager(new File(currentProject.getOutputPath()));
        }
        if (gitManager.isGitRepo()) {
            showAlert("提示", "已经是 Git 仓库");
            return;
        }
        boolean success = gitManager.init();
        if (success) {
            showAlert("成功", "Git 仓库已初始化");
            addGitIgnore();
        } else {
            showAlert("失败", "初始化失败");
        }
    }

    private void addGitIgnore() {
        File gitignore = new File(currentProject.getOutputPath(), ".gitignore");
        if (!gitignore.exists()) {
            String content = "# Gradle\n.gradle/\nbuild/\n!gradle/wrapper/gradle-wrapper.jar\n\n# IDE\n.idea/\n*.iml\nout/\n.classpath\n.project\n.settings/\n\n# Logs\nlogs/\n*.log\n\n# OS\n.DS_Store\nThumbs.db\n";
            try {
                Files.write(gitignore.toPath(), content.getBytes());
            } catch (IOException e) {
                LogUtil.error("创建 .gitignore 失败", e);
            }
        }
    }

    @FXML private void onGitCommit() {
        if (!checkGitAvailable()) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("提交更改");
        dialog.setHeaderText("输入提交信息");
        dialog.setContentText("提交信息:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(message -> {
            boolean added = gitManager.addAll();
            if (!added) {
                showAlert("错误", "添加文件到暂存区失败");
                return;
            }
            boolean committed = gitManager.commit(message, null, null);
            if (committed) {
                showAlert("成功", "提交成功");
            } else {
                showAlert("失败", "提交失败，请确保已配置 Git 用户信息");
            }
        });
    }

    @FXML private void onGitPush() {
        if (!checkGitAvailable()) return;
        Dialog<ButtonType> pushDialog = new Dialog<>();
        pushDialog.setTitle("推送");
        pushDialog.setHeaderText("推送到远程仓库");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField remoteField = new TextField("origin");
        TextField branchField = new TextField(gitManager.getCurrentBranch());
        TextField urlField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        grid.addRow(0, new Label("远程名称:"), remoteField);
        grid.addRow(1, new Label("分支:"), branchField);
        grid.addRow(2, new Label("远程 URL:"), urlField);
        grid.addRow(3, new Label("用户名:"), usernameField);
        grid.addRow(4, new Label("密码/Token:"), passwordField);
        pushDialog.getDialogPane().setContent(grid);
        pushDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pushDialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String remote = remoteField.getText();
                String branch = branchField.getText();
                String url = urlField.getText();
                String username = usernameField.getText();
                String password = passwordField.getText();
                if (!url.isEmpty()) {
                    showAlert("提示", "请先手动添加远程仓库：\ngit remote add " + remote + " " + url);
                    return;
                }
                boolean pushed = gitManager.push(remote, branch, username, password);
                if (pushed) {
                    showAlert("成功", "推送成功");
                } else {
                    showAlert("失败", "推送失败");
                }
            }
        });
    }

    @FXML private void onGitPull() {
        if (!checkGitAvailable()) return;
        Dialog<ButtonType> pullDialog = new Dialog<>();
        pullDialog.setTitle("拉取");
        pullDialog.setHeaderText("从远程仓库拉取");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField remoteField = new TextField("origin");
        TextField branchField = new TextField(gitManager.getCurrentBranch());
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        grid.addRow(0, new Label("远程名称:"), remoteField);
        grid.addRow(1, new Label("分支:"), branchField);
        grid.addRow(2, new Label("用户名:"), usernameField);
        grid.addRow(3, new Label("密码/Token:"), passwordField);
        pullDialog.getDialogPane().setContent(grid);
        pullDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pullDialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean pulled = gitManager.pull(remoteField.getText(), branchField.getText(),
                        usernameField.getText(), passwordField.getText());
                if (pulled) {
                    showAlert("成功", "拉取成功");
                } else {
                    showAlert("失败", "拉取失败");
                }
            }
        });
    }

    @FXML private void onGitStatus() {
        if (!checkGitAvailable()) return;
        Status status = gitManager.getStatus();
        if (status == null) {
            showAlert("错误", "无法获取状态");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("分支: ").append(gitManager.getCurrentBranch()).append("\n\n");
        if (!status.getAdded().isEmpty()) {
            sb.append("已添加:\n");
            status.getAdded().forEach(f -> sb.append("  ").append(f).append("\n"));
        }
        if (!status.getChanged().isEmpty()) {
            sb.append("已修改:\n");
            status.getChanged().forEach(f -> sb.append("  ").append(f).append("\n"));
        }
        if (!status.getModified().isEmpty()) {
            sb.append("已修改(工作区):\n");
            status.getModified().forEach(f -> sb.append("  ").append(f).append("\n"));
        }
        if (!status.getUntracked().isEmpty()) {
            sb.append("未跟踪:\n");
            status.getUntracked().forEach(f -> sb.append("  ").append(f).append("\n"));
        }
        if (sb.length() == 0) {
            sb.append("没有更改");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Git 状态");
        alert.setHeaderText(null);
        alert.setContentText(sb.toString());
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    private boolean checkGitAvailable() {
        if (currentProject == null) {
            showAlert("错误", "没有打开的项目");
            return false;
        }
        if (gitManager == null) {
            gitManager = new GitManager(new File(currentProject.getOutputPath()));
        }
        if (!gitManager.isGitRepo()) {
            showAlert("提示", "当前项目不是 Git 仓库，请先初始化");
            return false;
        }
        return true;
    }

    @FXML private void onNewProject() {
        tabPane.getSelectionModel().select(projectWizardTab);
    }

    @FXML private void onExit() {
        Platform.exit();
    }

    @FXML private void onAbout() {
        showAlert(I18n.get("about.title"), I18n.get("about.content", UpdateChecker.CURRENT_VERSION));
    }

    @FXML private void onOpenLogDir() {
        File logDir = LogUtil.getLogDirectory();
        if (logDir.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(logDir);
            } catch (IOException e) {
                LogUtil.error("打开日志目录失败", e);
                showAlert("错误", "无法打开日志目录: " + e.getMessage());
            }
        } else {
            showAlert("提示", "日志目录不存在");
        }
    }

    @FXML private void onUndo() {
        undoRedoManager.undo();
    }

    @FXML private void onRedo() {
        undoRedoManager.redo();
    }

    @FXML private void onCheckForUpdates() {
        UpdateChecker.checkForUpdate(new UpdateChecker.UpdateCallback() {
            @Override
            public void onUpdateAvailable(String version, String downloadUrl, String releaseNotes) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(I18n.get("update.available"));
                    alert.setHeaderText(I18n.get("update.header", version));
                    alert.setContentText(I18n.get("update.content", UpdateChecker.CURRENT_VERSION, releaseNotes));
                    ButtonType downloadBtn = new ButtonType(I18n.get("update.download"));
                    ButtonType laterBtn = new ButtonType(I18n.get("update.later"));
                    alert.getButtonTypes().setAll(downloadBtn, laterBtn);
                    alert.showAndWait().ifPresent(btn -> {
                        if (btn == downloadBtn) {
                            try {
                                java.awt.Desktop.getDesktop().browse(java.net.URI.create(downloadUrl));
                            } catch (IOException e) {
                                LogUtil.error("打开下载页面失败", e);
                            }
                        }
                    });
                });
            }
            @Override
            public void onNoUpdate() {
                LogUtil.info("已是最新版本: " + UpdateChecker.CURRENT_VERSION);
            }
            @Override
            public void onError(String message) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(I18n.get("update.error"));
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.showAndWait();
                });
            }
        });
    }

    @FXML private void onOpenSnippetLibrary() {
        SnippetLibrary library = new SnippetLibrary();
        library.show();
    }

    @FXML private void onOpenHelp() {
        HelpView help = new HelpView();
        help.show();
    }

    @FXML private void switchToChinese() {
        I18n.setLocale(Locale.SIMPLIFIED_CHINESE);
        applyI18n();
    }

    @FXML private void switchToEnglish() {
        I18n.setLocale(Locale.US);
        applyI18n();
    }

    @FXML private void onValidateProject() {
        if (currentProject == null) {
            showAlert("错误", "没有打开的项目");
            return;
        }
        List<ValidationError> errors = ProjectValidator.validate(currentProject);
        showValidationErrors(errors);
    }

    private void addSakura() {
        if (sakuraAdded) return;
        sakuraBackground = new SakuraBackground();
        rootStackPane.getChildren().add(0, sakuraBackground);
        sakuraBackground.prefWidthProperty().bind(rootStackPane.widthProperty());
        sakuraBackground.prefHeightProperty().bind(rootStackPane.heightProperty());
        sakuraAdded = true;
    }

    private void removeSakura() {
        if (!sakuraAdded) return;
        rootStackPane.getChildren().remove(sakuraBackground);
        sakuraBackground.stop();
        sakuraBackground = null;
        sakuraAdded = false;
    }

    @FXML private void switchToLightTheme() {
        removeSakura();
        Scene scene = rootStackPane.getScene();
        if (scene != null) ThemeManager.applyTheme(scene, ThemeManager.Theme.LIGHT);
    }

    @FXML private void switchToDarkTheme() {
        removeSakura();
        Scene scene = rootStackPane.getScene();
        if (scene != null) ThemeManager.applyTheme(scene, ThemeManager.Theme.DARK);
    }

    @FXML private void switchToSakuraTheme() {
        addSakura();
        Scene scene = rootStackPane.getScene();
        if (scene != null) ThemeManager.applyTheme(scene, ThemeManager.Theme.SAKURA);
    }

    // 加载外部项目（供 EasyForgeApp 调用）
    public void loadProject(File projectDir) {
        try {
            ModProject loaded = ProjectSaver.load(projectDir.toPath());
            if (loaded != null) {
                this.currentProject = loaded;
                afterProjectLoaded();
            } else {
                showAlert("错误", "无法加载项目");
            }
        } catch (Exception e) {
            LogUtil.error("加载项目失败", e);
            showAlert("错误", "加载失败: " + e.getMessage());
        }
    }
}