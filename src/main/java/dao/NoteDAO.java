package dao;

import model.Note;

import java.sql.SQLException;
import java.util.List;

public interface NoteDAO {
    public void saveNote(Note note) throws SQLException;

    public void updateNote(Note note) throws SQLException;

    public void deleteNote(int id) throws SQLException;

    public Note findNoteByID(int id) throws SQLException;

    public List<Note> listNotes() throws SQLException;
}
