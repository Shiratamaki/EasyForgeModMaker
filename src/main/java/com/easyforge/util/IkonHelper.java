package com.easyforge.util;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

public class IkonHelper {
    public static Button createIconButton(String text, Ikon ikon) {
        Button btn = new Button(text);
        btn.setGraphic(new FontIcon(ikon));
        btn.getStyleClass().add("icon-button");
        return btn;
    }

    public static Label createIconLabel(Ikon ikon, String text) {
        Label label = new Label(text);
        label.setGraphic(new FontIcon(ikon));
        label.getStyleClass().add("icon-label");
        return label;
    }
}