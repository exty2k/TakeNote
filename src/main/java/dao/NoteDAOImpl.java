package dao;

import model.Note;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoteDAOImpl implements NoteDAO {
    private Connection connection;

    public NoteDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void saveNote(Note note) throws SQLException {
        String sql = "INSERT INTO notes (title, content, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, note.getTitle());
            ps.setString(2, note.getContent());
            ps.setTimestamp(3, Timestamp.valueOf(note.getCreatedAt()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    note.setId(rs.getInt(1));
                }
            }
        };
    }

    @Override
    public void updateNote(Note note) throws SQLException {
        String sql = "UPDATE notes SET title = ?, content = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement("UPDATE notes SET title = ?, content = ? WHERE id = ?")) {
            ps.setString(1, note.getTitle());
            ps.setString(2, note.getContent());
            ps.setInt(3, note.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void deleteNote(int id) throws SQLException {
        String sql = "DELETE FROM notes WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            ps.executeUpdate();
        }
    }

    @Override
    public Note findNoteByID(int id) throws SQLException {
        String sql = "SELECT id, title, content, created_at FROM notes WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNote(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<Note> listNotes() throws SQLException {
        String sql = "SELECT id, title, content, created_at FROM notes";
        List<Note> notes = new ArrayList<>();

        try (Statement ps = connection.createStatement();
             ResultSet rs = ps.executeQuery(sql)) {

            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        };

        return notes;
    }

    private Note mapResultSetToNote(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String content = rs.getString("content");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

        return new Note(id, title, content, createdAt);
    }
}
