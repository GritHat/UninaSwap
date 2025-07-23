package com.uninaswap.client.util;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;


public class TextUtils {

    public static void setMaxLength(TextInputControl control, int maxLength) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.length() <= maxLength ? change : null;
        };
        control.setTextFormatter(new TextFormatter<>(filter));
    }
}