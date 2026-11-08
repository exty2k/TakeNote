package dao;

import model.Note;
import model.Tag;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TagDAOImpl implements TagDAO {
    private final Connection connection;

    public TagDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Tag findOrCreateTag(String name) throws SQLException {
        String normalized = name.trim();

        String selectSql = "SELECT tag_id, tag_name FROM tags WHERE tag_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
            ps.setString(1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Tag(rs.getInt("tag_id"), rs.getString("tag_name"));
                }
            }
        }

        String insertSql = "INSERT INTO tags (tag_name) VALUES (?) RETURNING tag_id";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setString(1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Tag(rs.getInt("tag_id"), normalized);
            }
        }
    }

    @Override
    public List<Tag> listAllTags() throws SQLException {
        String sql = "SELECT tag_id, tag_name FROM tags ORDER BY tag_name";
        List<Tag> tags = new ArrayList<>();

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                tags.add(new Tag(rs.getInt("tag_id"), rs.getString("tag_name")));
            }
        }

        return tags;
    }

    @Override
    public List<Tag> getTagsForNote(int noteId) throws SQLException {
        String sql = "SELECT t.tag_id, t.tag_name " +
                "FROM tags t " +
                "JOIN note_tag nt ON nt.tag_id = t.tag_id " +
                "WHERE nt.note_id = ? " +
                "ORDER BY t.tag_name";

        List<Tag> tags = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tags.add(new Tag(rs.getInt("tag_id"), rs.getString("tag_name")));
                }
            }
        }

        return tags;
    }

    @Override
    public void addTagToNote(int noteId, int tagId) throws SQLException {
        String sql = "INSERT INTO note_tag (note_id, tag_id) VALUES (?, ?) " +
                "ON CONFLICT (note_id, tag_id) DO NOTHING";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            ps.setInt(2, tagId);
            ps.executeUpdate();
        }
    }

    @Override
    public void removeTagFromNote(int noteId, int tagId) throws SQLException {
        String sql = "DELETE FROM note_tag WHERE note_id = ? AND tag_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            ps.setInt(2, tagId);
            ps.executeUpdate();
        }
    }

    @Override
    public void setTagsForNote(int noteId, List<String> tagNames) throws SQLException {
        String clearSql = "DELETE FROM note_tag WHERE note_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(clearSql)) {
            ps.setInt(1, noteId);
            ps.executeUpdate();
        }

        if (tagNames == null) {
            return;
        }

        for (String rawName : tagNames) {
            String name = rawName.trim();
            if (name.isEmpty()) {
                continue;
            }
            Tag tag = findOrCreateTag(name);
            addTagToNote(noteId, tag.getId());
        }
    }

    @Override
    public List<Note> findNotesByTagId(int tagId) throws SQLException {
        String sql = "SELECT n.id, n.title, n.content, n.created_at " +
                "FROM notes n " +
                "JOIN note_tag nt ON nt.note_id = n.id " +
                "WHERE nt.tag_id = ? " +
                "ORDER BY n.created_at DESC";

        List<Note> notes = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tagId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    notes.add(new Note(rs.getInt("id"), rs.getString("title"), rs.getString("content"), createdAt));
                }
            }
        }

        return notes;
    }
}
