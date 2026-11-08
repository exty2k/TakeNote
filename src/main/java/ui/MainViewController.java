package ui;

import controller.Controller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import model.Note;
import model.Tag;

public class MainViewController {

    // ---------- sidebar ----------
    @FXML private TextField sidebarSearchField;
    @FXML private ListView<LibraryItem> libraryListView;
    @FXML private Label footerStatusLabel;

    // ---------- notes list ----------
    @FXML private Label listHeaderTitle;
    @FXML private Button newNoteButton;
    @FXML private TextField listSearchField;
    @FXML private ListView<Note> notesListView;

    // ---------- detail pane ----------
    @FXML private VBox emptyStatePane;
    @FXML private VBox detailPane;
    @FXML private Label breadcrumbLabel;
    @FXML private TextField titleField;
    @FXML private Label metaLabel;
    @FXML private Label tagsLabel;
    @FXML private javafx.scene.layout.StackPane contentStack;
    @FXML private ScrollPane contentPreviewScroll;
    @FXML private VBox contentPreviewHolder;
    @FXML private TextArea contentEditor;
    @FXML private HBox formattingToolbar;
    @FXML private Button boldButton;
    @FXML private Button codeButton;
    @FXML private Button bulletButton;
    @FXML private Button headingButton;

    private Controller controller;

    private final ObservableList<LibraryItem> libraryItems = FXCollections.observableArrayList();
    private final ObservableList<Note> displayedNotes = FXCollections.observableArrayList();

    private Note selectedNote;
    private Integer currentTagId = null; // null означает пункт "Все заметки"
    private boolean suppressFieldEvents = false;

    public void init(Controller controller, String statusText) {
        this.controller = controller;
        footerStatusLabel.setText(statusText);

        setupLibraryList();
        setupNotesList();
        setupSearchFields();
        setupDetailPane();
        setupFormattingToolbar();
        setupTagsLabel();
        setupNewNoteButton();

        refreshLibrary();
    }

    // ==================================================================
    // Настройка списков
    // ==================================================================

    private void setupLibraryList() {
        libraryListView.setItems(libraryItems);
        libraryListView.setCellFactory(lv -> new ListCell<>() {
            private final Label nameLabel = new Label();
            private final Label countLabel = new Label();
            private final Region spacer = new Region();
            private final HBox box = new HBox(6, nameLabel, spacer, countLabel);
            {
                HBox.setHgrow(spacer, Priority.ALWAYS);
                nameLabel.getStyleClass().add("library-item-name");
                countLabel.getStyleClass().add("library-item-count");
            }

            @Override
            protected void updateItem(LibraryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(item.name);
                    countLabel.setText(String.valueOf(item.count));
                    setGraphic(box);
                }
            }
        });

        libraryListView.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> {
            if (item == null) return;
            currentTagId = item.tagId;
            listHeaderTitle.setText(item.name);
            refreshNotesList();
        });
    }

    private void setupNotesList() {
        notesListView.setItems(displayedNotes);
        notesListView.setCellFactory(lv -> new ListCell<>() {
            private final Label title = new Label();
            private final Label date = new Label();
            private final Label preview = new Label();
            private final Region spacer = new Region();
            private final HBox topRow = new HBox(8, title, spacer, date);
            private final VBox box = new VBox(3, topRow, preview);
            {
                HBox.setHgrow(spacer, Priority.ALWAYS);
                title.getStyleClass().add("note-cell-title");
                date.getStyleClass().add("note-cell-date");
                preview.getStyleClass().add("note-cell-preview");
                title.setMinWidth(0);
                preview.setMinWidth(0);
                title.setMaxWidth(Double.MAX_VALUE);
                preview.setMaxWidth(Double.MAX_VALUE);
                box.maxWidthProperty().bind(lv.widthProperty().subtract(24));
                addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, evt -> {
                    if (evt.getButton() == javafx.scene.input.MouseButton.SECONDARY && getItem() != null) {
                        getListView().getSelectionModel().select(getItem());
                    }
                });
            }

            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setGraphic(null);
                    setContextMenu(null);
                } else {
                    title.setText(note.getTitle() == null || note.getTitle().isBlank() ? "Без названия" : note.getTitle());
                    date.setText(relativeDateShort(note.getCreatedAt()));
                    preview.setText(snippet(note.getContent()));
                    setGraphic(box);

                    MenuItem delete = new MenuItem("Удалить заметку");
                    delete.setOnAction(e -> confirmAndDeleteNote(note));
                    setContextMenu(new ContextMenu(delete));
                }
            }
        });

        notesListView.getSelectionModel().selectedItemProperty().addListener((obs, old, note) -> {
            if (note != null) {
                loadNoteIntoDetail(note.getId());
            } else {
                showEmptyState();
            }
        });
    }

    private void setupSearchFields() {
        listSearchField.textProperty().addListener((obs, old, val) -> refreshNotesList());

        sidebarSearchField.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.isBlank()) {
                currentTagId = null;
                if (!libraryItems.isEmpty()) {
                    libraryListView.getSelectionModel().select(0);
                }
                listSearchField.setText(val);
            } else {
                refreshNotesList();
            }
        });
    }

    private void setupNewNoteButton() {
        newNoteButton.setOnAction(e -> {
            try {
                List<String> tagNames = currentTagId != null
                        ? List.of(libraryListView.getSelectionModel().getSelectedItem().name)
                        : List.of();
                Note created = controller.createNote("Новая заметка", "", tagNames);
                refreshLibrary();
                refreshNotesList();
                selectNoteById(created.getId());
                Platform.runLater(() -> {
                    titleField.requestFocus();
                    titleField.selectAll();
                });
            } catch (SQLException ex) {
                showError("Не удалось создать заметку", ex);
            }
        });
    }

    private void setupTagsLabel() {
        tagsLabel.setOnMouseClicked(e -> {
            if (selectedNote == null) return;
            TextInputDialog dialog = new TextInputDialog(selectedNote.getTagsAsString());
            dialog.setTitle("Теги заметки");
            dialog.setHeaderText(null);
            dialog.setContentText("Теги через запятую:");
            dialog.showAndWait().ifPresent(raw -> {
                try {
                    List<String> tagNames = Controller.parseTagNames(raw);
                    controller.updateNote(selectedNote.getId(), selectedNote.getTitle(), selectedNote.getContent(), tagNames);
                    refreshLibrary();
                    refreshNotesList();
                    loadNoteIntoDetail(selectedNote.getId());
                } catch (SQLException ex) {
                    showError("Не удалось обновить теги", ex);
                }
            });
        });
    }

    private void confirmAndDeleteNote(Note note) {
        if (note == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить заметку «" + note.getTitle() + "»?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.showAndWait().filter(bt -> bt == ButtonType.OK).ifPresent(bt -> {
            try {
                controller.deleteNote(note.getId());
                if (selectedNote != null && selectedNote.getId() == note.getId()) {
                    selectedNote = null;
                }
                refreshLibrary();
                refreshNotesList();
            } catch (SQLException ex) {
                showError("Не удалось удалить заметку", ex);
            }
        });
    }

    private void setupDetailPane() {
        contentPreviewScroll.setOnMouseClicked(e -> enterEditMode());

        titleField.focusedProperty().addListener((obs, was, is) -> {
            if (!is && selectedNote != null && !suppressFieldEvents) {
                saveCurrentNote();
            }
        });

        contentEditor.focusedProperty().addListener((obs, was, is) -> {
            if (!is && selectedNote != null && !suppressFieldEvents) {
                saveCurrentNote();
                switchToPreviewMode();
            }
        });
    }

    // ==================================================================
    // Обновление данных
    // ==================================================================

    private void refreshLibrary() {
        try {
            List<Tag> tags = controller.listAllTags();
            List<Note> all = controller.listOfNotes();

            libraryItems.clear();
            libraryItems.add(new LibraryItem(null, "Все заметки", all.size()));
            for (Tag tag : tags) {
                int count = (int) all.stream()
                        .filter(n -> n.getTags().stream().anyMatch(t -> t.getId() == tag.getId()))
                        .count();
                libraryItems.add(new LibraryItem(tag.getId(), tag.getName(), count));
            }

            int indexToSelect = 0;
            for (int i = 0; i < libraryItems.size(); i++) {
                if (Objects.equals(libraryItems.get(i).tagId, currentTagId)) {
                    indexToSelect = i;
                    break;
                }
            }
            libraryListView.getSelectionModel().select(indexToSelect);
        } catch (SQLException ex) {
            showError("Не удалось загрузить список тегов", ex);
        }
    }

    private void refreshNotesList() {
        try {
            List<Note> source = currentTagId == null
                    ? controller.listOfNotes()
                    : controller.listNotesByTag(currentTagId);

            String filter = listSearchField.getText();
            if (filter != null && !filter.isBlank()) {
                String q = filter.toLowerCase(Locale.ROOT);
                source = source.stream()
                        .filter(n -> (n.getTitle() != null && n.getTitle().toLowerCase(Locale.ROOT).contains(q))
                                || (n.getContent() != null && n.getContent().toLowerCase(Locale.ROOT).contains(q)))
                        .collect(Collectors.toList());
            }

            source.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            Integer previouslySelectedId = selectedNote != null ? selectedNote.getId() : null;
            displayedNotes.setAll(source);

            if (previouslySelectedId != null) {
                selectNoteById(previouslySelectedId);
            } else if (!displayedNotes.isEmpty()) {
                notesListView.getSelectionModel().select(0);
            } else {
                showEmptyState();
            }
        } catch (SQLException ex) {
            showError("Не удалось загрузить заметки", ex);
        }
    }

    private void selectNoteById(int id) {
        for (Note n : displayedNotes) {
            if (n.getId() == id) {
                notesListView.getSelectionModel().select(n);
                notesListView.scrollTo(n);
                return;
            }
        }
        if (!displayedNotes.isEmpty()) {
            notesListView.getSelectionModel().select(0);
        } else {
            showEmptyState();
        }
    }

    private void loadNoteIntoDetail(int id) {
        try {
            Note note = controller.getNote(id);
            selectedNote = note;

            emptyStatePane.setVisible(false);
            detailPane.setVisible(true);

            breadcrumbLabel.setText(breadcrumbFor());

            suppressFieldEvents = true;
            titleField.setText(note.getTitle());
            contentEditor.setText(note.getContent());
            suppressFieldEvents = false;

            metaLabel.setText("Создано " + formattedDateFull(note.getCreatedAt()));
            tagsLabel.setText(note.getTags().isEmpty() ? "+ добавить теги" : "🏷 " + note.getTagsAsString());

            renderPreview(note.getContent());
            switchToPreviewMode();
        } catch (Exception ex) {
            showError("Не удалось открыть заметку", ex);
        }
    }

    private void showEmptyState() {
        selectedNote = null;
        detailPane.setVisible(false);
        emptyStatePane.setVisible(true);
    }

    private String breadcrumbFor() {
        if (currentTagId != null) {
            LibraryItem selected = libraryListView.getSelectionModel().getSelectedItem();
            String tagName = selected != null ? selected.name : "";
            return tagName + " / Все заметки";
        }
        return "Все заметки";
    }

    // ==================================================================
    // Режим чтение / редактирование
    // ==================================================================

    private void renderPreview(String content) {
        contentPreviewHolder.getChildren().setAll(MarkdownLite.render(content));
    }

    private void enterEditMode() {
        if (selectedNote == null) return;
        contentPreviewScroll.setVisible(false);
        contentPreviewScroll.setMouseTransparent(true);
        contentEditor.setVisible(true);
        contentEditor.setMouseTransparent(false);
        formattingToolbar.setVisible(true);
        formattingToolbar.setManaged(true);
        contentEditor.requestFocus();
        contentEditor.positionCaret(contentEditor.getText() == null ? 0 : contentEditor.getText().length());
    }

    private void switchToPreviewMode() {
        contentEditor.setVisible(false);
        contentEditor.setMouseTransparent(true);
        contentPreviewScroll.setVisible(true);
        contentPreviewScroll.setMouseTransparent(false);
        formattingToolbar.setVisible(false);
        formattingToolbar.setManaged(false);
    }

    private void setupFormattingToolbar() {
        boldButton.setOnAction(e -> wrapSelection("**", "**"));
        codeButton.setOnAction(e -> wrapSelection("`", "`"));
        bulletButton.setOnAction(e -> prefixLines("- "));
        headingButton.setOnAction(e -> prefixLines("# "));
    }

    /** Оборачивает выделенный текст маркерами (например **...** для жирного). Без выделения — просто вставляет маркеры и ставит курсор между ними. */
    private void wrapSelection(String prefix, String suffix) {
        IndexRange range = contentEditor.getSelection();
        if (range.getLength() > 0) {
            String selected = contentEditor.getSelectedText();
            contentEditor.replaceSelection(prefix + selected + suffix);
            contentEditor.selectRange(range.getStart() + prefix.length(), range.getStart() + prefix.length() + selected.length());
        } else {
            int caret = contentEditor.getCaretPosition();
            contentEditor.insertText(caret, prefix + suffix);
            contentEditor.positionCaret(caret + prefix.length());
        }
        contentEditor.requestFocus();
    }

    /** Добавляет префикс (например "- " или "# ") в начало каждой строки, задетой текущим выделением/курсором. */
    private void prefixLines(String prefix) {
        String text = contentEditor.getText();
        if (text == null) text = "";
        IndexRange range = contentEditor.getSelection();

        int lineStart = text.lastIndexOf('\n', Math.max(0, range.getStart() - 1)) + 1;
        int searchFrom = Math.max(range.getEnd() - 1, lineStart);
        int nextNewline = text.indexOf('\n', Math.min(searchFrom, text.length()));
        int lineEnd = nextNewline == -1 ? text.length() : nextNewline;

        String segment = text.substring(lineStart, lineEnd);
        String[] lines = segment.split("\n", -1);
        StringBuilder rebuilt = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) rebuilt.append("\n");
            rebuilt.append(lines[i].startsWith(prefix) ? lines[i] : prefix + lines[i]);
        }

        contentEditor.selectRange(lineStart, lineEnd);
        contentEditor.replaceSelection(rebuilt.toString());
        contentEditor.requestFocus();
    }

    private void saveCurrentNote() {
        if (selectedNote == null) return;

        String title = titleField.getText();
        if (title == null || title.isBlank()) {
            title = "Без названия";
        }
        String content = contentEditor.getText();
        if (content == null) content = "";

        if (title.equals(selectedNote.getTitle()) && content.equals(selectedNote.getContent())) {
            return; // ничего не поменялось
        }

        try {
            controller.updateNote(selectedNote.getId(), title, content);
            selectedNote.setTitle(title);
            selectedNote.setContent(content);
            renderPreview(content);
            refreshNotesList();
        } catch (SQLException ex) {
            showError("Не удалось сохранить заметку", ex);
        }
    }

    // ==================================================================
    // Вспомогательные методы
    // ==================================================================

    private String snippet(String content) {
        if (content == null || content.isBlank()) return "Нет текста";
        String flat = content.replace("\n", " ").trim();
        return flat.length() > 60 ? flat.substring(0, 60) + "..." : flat;
    }

    private String relativeDateShort(LocalDateTime dt) {
        LocalDate d = dt.toLocalDate();
        LocalDate today = LocalDate.now();
        if (d.equals(today)) return "Сегодня";
        if (d.equals(today.minusDays(1))) return "Вчера";
        return d.format(DateTimeFormatter.ofPattern("d MMM"));
    }

    private String formattedDateFull(LocalDateTime dt) {
        LocalDate d = dt.toLocalDate();
        LocalDate today = LocalDate.now();
        DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm");
        if (d.equals(today)) return "сегодня в " + dt.format(time);
        if (d.equals(today.minusDays(1))) return "вчера в " + dt.format(time);
        return dt.format(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm"));
    }

    private void showError(String headline, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(headline);
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
    }

    /** Строка боковой панели: "Все заметки" (tagId == null) либо конкретный тег. */
    private static class LibraryItem {
        final Integer tagId;
        final String name;
        final int count;

        LibraryItem(Integer tagId, String name, int count) {
            this.tagId = tagId;
            this.name = name;
            this.count = count;
        }
    }
}
