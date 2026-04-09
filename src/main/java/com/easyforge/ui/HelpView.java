package com.easyforge.ui;

import com.easyforge.util.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class HelpView {

    private final Stage stage;
    private final WebView webView;
    private final WebEngine webEngine;

    public HelpView() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(I18n.get("help.title"));
        stage.setWidth(950);
        stage.setHeight(750);
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        BorderPane root = new BorderPane();

        // 顶部工具栏
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(12, 15, 12, 15));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Button homeBtn = createToolButton(I18n.get("help.home"), "🏠");
        homeBtn.setOnAction(e -> loadPage("index"));

        Button gettingStartedBtn = createToolButton(I18n.get("help.gettingStarted"), "🚀");
        gettingStartedBtn.setOnAction(e -> loadPage("getting_started"));

        Button editorsBtn = createToolButton(I18n.get("help.editors"), "📖");
        editorsBtn.setOnAction(e -> loadPage("editors"));

        Button faqBtn = createToolButton(I18n.get("help.faq"), "❓");
        faqBtn.setOnAction(e -> loadPage("faq"));

        Button shortcutsBtn = createToolButton(I18n.get("help.shortcuts"), "⌨️");
        shortcutsBtn.setOnAction(e -> loadPage("shortcuts"));

        toolbar.getChildren().addAll(homeBtn, gettingStartedBtn, editorsBtn, faqBtn, shortcutsBtn);

        // 中间 WebView
        webView = new WebView();
        webEngine = webView.getEngine();
        // 设置用户样式表（修正方法）
        String cssUrl = getClass().getResource("/css/help.css") != null ?
                getClass().getResource("/css/help.css").toExternalForm() : null;
        if (cssUrl != null) {
            webEngine.setUserStyleSheetLocation(cssUrl);
        }

        root.setTop(toolbar);
        root.setCenter(webView);

        // 底部状态栏
        Label statusLabel = new Label(I18n.get("help.status"));
        statusLabel.setPadding(new Insets(6, 12, 6, 12));
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        root.setBottom(statusLabel);

        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    private Button createToolButton(String text, String emoji) {
        Button btn = new Button(emoji + " " + text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3446c; -fx-font-weight: bold; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #ffe4e9; -fx-text-fill: #b3446c; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 15px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3446c; -fx-font-weight: bold; -fx-cursor: hand;"));
        return btn;
    }

    private void loadPage(String pageName) {
        String html = loadHtmlFromResource("/help/" + pageName + ".html");
        if (html != null) {
            webEngine.loadContent(html);
        } else {
            webEngine.loadContent("<html><body style='font-family: sans-serif; padding: 20px;'><h1>Error 404</h1><p>Page not found: " + pageName + "</p></body></html>");
        }
    }

    private String loadHtmlFromResource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Help resource not found: " + path);
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void show() {
        loadPage("index");
        stage.showAndWait();
    }
}