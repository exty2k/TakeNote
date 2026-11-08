package ui;

import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Очень простой markdown-рендерер для панели чтения заметки.
 * Поддерживает: # заголовок, - пункт списка, **жирный**, `код`.
 * Это не полноценный парсер markdown, а лёгкая имитация для красивого отображения.
 */
public final class MarkdownLite {

    private MarkdownLite() {
    }

    public static TextFlow render(String rawContent) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("content-flow");

        if (rawContent == null || rawContent.isEmpty()) {
            Text placeholder = new Text("Пусто. Нажмите, чтобы начать писать...");
            placeholder.getStyleClass().add("content-placeholder");
            flow.getChildren().add(placeholder);
            return flow;
        }

        String[] lines = rawContent.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.startsWith("# ")) {
                Text heading = new Text(line.substring(2));
                heading.getStyleClass().add("content-heading");
                flow.getChildren().add(heading);
            } else if (line.startsWith("- ")) {
                flow.getChildren().addAll(inlineNodes("•  " + line.substring(2)));
            } else {
                flow.getChildren().addAll(inlineNodes(line));
            }

            if (i < lines.length - 1) {
                Text br = new Text("\n");
                flow.getChildren().add(br);
            }
        }

        return flow;
    }

    /** Разбирает строку на обычный текст, **жирный** и `код`. */
    private static java.util.List<javafx.scene.Node> inlineNodes(String line) {
        java.util.List<javafx.scene.Node> nodes = new java.util.ArrayList<>();
        int i = 0;
        StringBuilder plain = new StringBuilder();

        while (i < line.length()) {
            if (line.startsWith("**", i)) {
                int end = line.indexOf("**", i + 2);
                if (end != -1) {
                    flushPlain(plain, nodes);
                    Text bold = new Text(line.substring(i + 2, end));
                    bold.setStyle("-fx-font-weight: bold;");
                    bold.getStyleClass().add("content-text");
                    nodes.add(bold);
                    i = end + 2;
                    continue;
                }
            } else if (line.charAt(i) == '`') {
                int end = line.indexOf('`', i + 1);
                if (end != -1) {
                    flushPlain(plain, nodes);
                    Label code = new Label(line.substring(i + 1, end));
                    code.getStyleClass().add("inline-code");
                    nodes.add(code);
                    i = end + 1;
                    continue;
                }
            }
            plain.append(line.charAt(i));
            i++;
        }
        flushPlain(plain, nodes);
        return nodes;
    }

    private static void flushPlain(StringBuilder plain, java.util.List<javafx.scene.Node> nodes) {
        if (plain.length() > 0) {
            Text text = new Text(plain.toString());
            text.getStyleClass().add("content-text");
            nodes.add(text);
            plain.setLength(0);
        }
    }
}
