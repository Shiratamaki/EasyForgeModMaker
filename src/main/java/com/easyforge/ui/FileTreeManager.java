package com.easyforge.ui;

import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class FileTreeManager {
    private final TreeView<File> treeView;
    private File rootDirectory;

    public FileTreeManager(TreeView<File> treeView, File rootDirectory) {
        this.treeView = treeView;
        this.rootDirectory = rootDirectory;
        buildTree();
    }

    public void setCurrentProjectPath(File newRoot) {
        this.rootDirectory = newRoot;
        buildTree();
        // 确保 TreeView 可见且根节点展开
        treeView.setVisible(true);
        TreeItem<File> root = treeView.getRoot();
        if (root != null) root.setExpanded(true);
    }

    public void refreshTree() {
        buildTree();
    }

    private void buildTree() {
        if (rootDirectory == null || !rootDirectory.exists()) {
            treeView.setRoot(null);
            return;
        }
        TreeItem<File> rootItem = new TreeItem<>(rootDirectory);
        rootItem.setExpanded(true);   // 根节点默认展开
        addChildren(rootItem, rootDirectory);
        treeView.setRoot(rootItem);
    }

    private void addChildren(TreeItem<File> parentItem, File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            TreeItem<File> childItem = new TreeItem<>(file);
            parentItem.getChildren().add(childItem);
            if (file.isDirectory()) addChildren(childItem, file);
        }
    }

    private File getSelectedFile() {
        TreeItem<File> selected = treeView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getValue();
    }

    public void createNewFile() {
        File parent = getSelectedFile();
        if (parent == null || !parent.isDirectory()) parent = rootDirectory;
        if (parent == null) return;
        final File finalParent = parent;
        TextInputDialog dialog = new TextInputDialog("new_file.txt");
        dialog.setTitle("新建文件");
        dialog.setHeaderText("输入文件名");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            File newFile = new File(finalParent, name);
            try {
                if (newFile.createNewFile()) refreshTree();
            } catch (IOException e) { e.printStackTrace(); }
        });
    }

    public void createNewFolder() {
        File parent = getSelectedFile();
        if (parent == null || !parent.isDirectory()) parent = rootDirectory;
        if (parent == null) return;
        final File finalParent = parent;
        TextInputDialog dialog = new TextInputDialog("新文件夹");
        dialog.setTitle("新建文件夹");
        dialog.setHeaderText("输入文件夹名称");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            File newFolder = new File(finalParent, name);
            if (newFolder.mkdir()) refreshTree();
        });
    }

    public void deleteSelected() {
        File file = getSelectedFile();
        if (file == null) return;
        if (file.isDirectory()) deleteDirectory(file);
        else file.delete();
        refreshTree();
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) deleteDirectory(f);
        }
        dir.delete();
    }

    public void renameSelected() {
        File oldFile = getSelectedFile();
        if (oldFile == null) return;
        TextInputDialog dialog = new TextInputDialog(oldFile.getName());
        dialog.setTitle("重命名");
        dialog.setHeaderText("输入新名称");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            File newFile = new File(oldFile.getParent(), newName);
            if (oldFile.renameTo(newFile)) refreshTree();
        });
    }
}