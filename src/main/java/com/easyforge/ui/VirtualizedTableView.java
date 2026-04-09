package com.easyforge.ui;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * 虚拟化表格包装类，实际使用 TableView 本身就支持虚拟化，
 * 但为了设置固定行高和批量更新，提供此辅助类。
 * 使用 setFixedCellSize 启用虚拟化。
 */
public class VirtualizedTableView<S> extends TableView<S> {
    public VirtualizedTableView() {
        // 启用虚拟化
        setFixedCellSize(30);
        // 批量更新时关闭重绘
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void setItemsBulk(ObservableList<S> items) {
        setItems(items);
        // 强制刷新
        refresh();
    }
}