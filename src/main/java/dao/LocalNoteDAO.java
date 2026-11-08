package dao;

import model.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LocalNoteDAO implements NoteDAO {
    private final Map<Integer, Note> storage = new HashMap<>();
    private int nextId = 1;

    @Override
    public void saveNote(Note note) {
        note.setId(nextId++);
        storage.put(note.getId(), note);
    }

    @Override
    public void updateNote(Note note) {
        storage.put(note.getId(), note);
    }

    @Override
    public void deleteNote(int id) {
        storage.remove(id);
    }

    @Override
    public Note findNoteByID(int id) {
        return storage.get(id);
    }

    @Override
    public List<Note> listNotes() {
        List<Note> noteList = new ArrayList<>((storage.values()));
        return noteList;
    }
}
