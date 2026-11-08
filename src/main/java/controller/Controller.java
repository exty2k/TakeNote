package controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import dao.NoteDAO;
import dao.TagDAO;
import model.Note;
import model.Tag;

public class Controller {
    private final NoteDAO noteDAO;
    private final TagDAO tagDAO;

    public Controller(NoteDAO noteDAO, TagDAO tagDAO) {
        this.noteDAO = noteDAO;
        this.tagDAO = tagDAO;
    }

    public void createNote(String title, String content) throws SQLException {
        createNote(title, content, List.of());
    }

    public Note createNote(String title, String content, List<String> tagNames) throws SQLException {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Заголовок не может быть пустым");
        }
        Note note = new Note(title, content);
        noteDAO.saveNote(note);
        tagDAO.setTagsForNote(note.getId(), tagNames);
        note.setTags(tagDAO.getTagsForNote(note.getId()));
        return note;
    }

    public Note getNote(int id) throws SQLException {
        Note note = noteDAO.findNoteByID(id);
        if (note == null) {
            throw new NoSuchElementException("Заметка " + id + " не найдена.");
        }
        note.setTags(tagDAO.getTagsForNote(id));
        return note;
    }

    public void updateNote(int id, String newTitle, String newContent) throws SQLException {
        updateNote(id, newTitle, newContent, null);
    }

    public void updateNote(int id, String newTitle, String newContent, List<String> tagNames) throws SQLException {
        Note note = getNote(id);
        note.setTitle(newTitle);
        note.setContent(newContent);
        noteDAO.updateNote(note);
        if (tagNames != null) {
            tagDAO.setTagsForNote(id, tagNames);
        }
    }

    public void deleteNote(int id) throws SQLException {
        getNote(id);
        noteDAO.deleteNote(id);
    }

    public void checkNote(int id) throws SQLException {
        Note note = getNote(id);
        System.out.println(note.fullInfo());
    }

    public List<Note> listOfNotes() throws SQLException {
        List<Note> notes = noteDAO.listNotes();
        for (Note note : notes) {
            note.setTags(tagDAO.getTagsForNote(note.getId()));
        }
        return notes;
    }

    public List<Tag> listAllTags() throws SQLException {
        return tagDAO.listAllTags();
    }

    public List<Note> listNotesByTag(int tagId) throws SQLException {
        List<Note> notes = tagDAO.findNotesByTagId(tagId);
        for (Note note : notes) {
            note.setTags(tagDAO.getTagsForNote(note.getId()));
        }
        return notes;
    }

    /** Разбирает строку вида "работа, важное, идеи" в список имён тегов. */
    public static List<String> parseTagNames(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String part : rawTags.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
