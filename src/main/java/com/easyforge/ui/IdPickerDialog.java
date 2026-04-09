package com.easyforge.ui;

import com.easyforge.util.I18n;
import com.easyforge.util.VanillaIds;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 通用 ID 选择器对话框 - 支持搜索过滤，返回选中的 ID
 */
public class IdPickerDialog {

    private final Stage dialog;
    private final ListView<VanillaIds.IdEntry> listView;
    private final TextField searchField;
    private final ObservableList<VanillaIds.IdEntry> allItems;
    private final FilteredList<VanillaIds.IdEntry> filteredItems;
    private String selectedId;

    /**
     * 创建一个 ID 选择器
     * @param title 对话框标题
     * @param items 要显示的 ID 列表（来自 VanillaIds 的某个列表）
     */
    public IdPickerDialog(String title, List<VanillaIds.IdEntry> items) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.setTitle(title);
        dialog.setResizable(true);
        dialog.setMinWidth(400);
        dialog.setMinHeight(500);

        // 主布局
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // 顶部搜索栏
        searchField = new TextField();
        searchField.setPromptText(I18n.get("idpicker.search"));
        HBox topBox = new HBox(5, new Label(I18n.get("idpicker.search") + ":"), searchField);
        topBox.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(topBox);

        // 中间列表
        allItems = FXCollections.observableArrayList(items);
        filteredItems = new FilteredList<>(allItems, p -> true);
        listView = new ListView<>(filteredItems);
        listView.setCellFactory(lv -> new ListCell<VanillaIds.IdEntry>() {
            @Override
            protected void updateItem(VanillaIds.IdEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) setText(null);
                else setText(entry.getDisplay());
            }
        });
        root.setCenter(listView);

        // 底部按钮
        HBox bottomBox = new HBox(10);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        bottomBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Button okBtn = new Button(I18n.get("btn.ok"));
        Button cancelBtn = new Button(I18n.get("btn.cancel"));
        okBtn.setOnAction(e -> {
            VanillaIds.IdEntry selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedId = selected.getId();
            }
            dialog.close();
        });
        cancelBtn.setOnAction(e -> {
            selectedId = null;
            dialog.close();
        });
        bottomBox.getChildren().addAll(okBtn, cancelBtn);
        root.setBottom(bottomBox);

        // 搜索过滤
        searchField.textProperty().addListener((obs, old, val) -> {
            String lower = val == null ? "" : val.toLowerCase();
            filteredItems.setPredicate(entry -> {
                if (lower.isEmpty()) return true;
                return entry.getId().toLowerCase().contains(lower) ||
                        entry.getChinese().toLowerCase().contains(lower);
            });
        });

        // 双击选中
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                VanillaIds.IdEntry selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selectedId = selected.getId();
                    dialog.close();
                }
            }
        });

        Scene scene = new Scene(root);
        dialog.setScene(scene);
    }

    /**
     * 显示对话框并等待用户选择
     * @return 选中的 ID（如果用户取消则返回空 Optional）
     */
    public Optional<String> showAndWait() {
        selectedId = null;
        dialog.showAndWait();
        return selectedId == null ? Optional.empty() : Optional.of(selectedId);
    }

    /**
     * 异步显示对话框，通过回调返回结果
     */
    public void showAndWait(Consumer<Optional<String>> callback) {
        dialog.showAndWait();
        callback.accept(selectedId == null ? Optional.empty() : Optional.of(selectedId));
    }
}