package com.easyforge.ui;

import com.easyforge.util.I18n;
import com.easyforge.util.UpdateDownloader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.function.Consumer;

public class ProgressDialog {
    private final Stage stage;
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private Button cancelButton;
    private volatile boolean cancelled = false;

    public ProgressDialog(String title, String initialMessage) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(title);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> {
            if (cancelButton != null) cancelButton.fire();
            e.consume();
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);

        statusLabel = new Label(initialMessage);
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);

        cancelButton = new Button(I18n.get("cancel"));
        cancelButton.setOnAction(e -> {
            cancelled = true;
            stage.close();
        });

        root.getChildren().addAll(statusLabel, progressBar, cancelButton);
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void updateProgress(double progress, String message) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            if (message != null) statusLabel.setText(message);
        });
    }

    public void setOnCancel(Runnable onCancel) {
        cancelButton.setOnAction(e -> {
            cancelled = true;
            if (onCancel != null) onCancel.run();
            stage.close();
        });
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void show() {
        stage.show();
    }

    public void close() {
        Platform.runLater(() -> stage.close());
    }

    /**
     * 显示一个简单的进度对话框，用于自动更新下载
     * @param downloadUrl 下载链接
     * @param onComplete 下载完成回调，参数为下载的文件
     * @param onError 错误回调
     */
    public static void showDownloadDialog(String downloadUrl, Consumer<File> onComplete, Consumer<String> onError) {
        ProgressDialog dialog = new ProgressDialog(I18n.get("update.downloading"), I18n.get("update.downloadingMsg"));
        dialog.show();

        UpdateDownloader.downloadAndInstall(downloadUrl,
                progress -> dialog.updateProgress(progress, String.format(I18n.get("update.downloadingProgress"), (int)(progress * 100))),
                file -> {
                    dialog.close();
                    onComplete.accept(file);
                },
                error -> {
                    dialog.close();
                    onError.accept(error);
                });
    }
}