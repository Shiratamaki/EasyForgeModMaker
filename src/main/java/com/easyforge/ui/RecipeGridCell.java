package com.easyforge.ui;

import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class RecipeGridCell extends StackPane {
    private TextField textField;
    private Rectangle background;
    private int row, col;

    public RecipeGridCell(int row, int col, int number) {
        this.row = row;
        this.col = col;
        setPrefSize(140, 80);
        background = new Rectangle(140, 80);
        background.setFill(Color.LIGHTGRAY);
        background.setStroke(Color.GRAY);
        textField = new TextField();
        textField.setPromptText("物品ID");
        textField.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
        textField.setPrefWidth(130);
        VBox vbox = new VBox(2);
        Text numberText = new Text(String.valueOf(number));
        numberText.setStyle("-fx-font-size: 10px; -fx-fill: gray;");
        vbox.getChildren().addAll(textField, numberText);
        getChildren().addAll(background, vbox);
        // 拖拽支持
        setOnDragOver(e -> {
            if (e.getDragboard().hasString()) e.acceptTransferModes(TransferMode.COPY);
            e.consume();
        });
        setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasString()) {
                textField.setText(db.getString());
                e.setDropCompleted(true);
            }
            e.consume();
        });
        setOnDragDetected(e -> {
            String txt = textField.getText();
            if (txt != null && !txt.isEmpty()) {
                Dragboard db = startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putString(txt);
                db.setContent(content);
                e.consume();
            }
        });
    }

    public String getItemId() {
        String txt = textField.getText();
        return (txt == null || txt.trim().isEmpty()) ? null : txt.trim();
    }

    public void setItemId(String id) {
        textField.setText(id);
    }
}