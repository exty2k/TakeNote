package dao;

import model.Note;
import model.Tag;

import java.sql.SQLException;
import java.util.List;

public interface TagDAO {

    /** Вернуть существующий тег по имени или создать новый, если такого ещё нет. */
    Tag findOrCreateTag(String name) throws SQLException;

    /** Список всех тегов в системе. */
    List<Tag> listAllTags() throws SQLException;

    /** Теги, привязанные к конкретной заметке. */
    List<Tag> getTagsForNote(int noteId) throws SQLException;

    /** Привязать тег к заметке (связь note_id <-> tag_id). */
    void addTagToNote(int noteId, int tagId) throws SQLException;

    /** Отвязать тег от заметки. */
    void removeTagFromNote(int noteId, int tagId) throws SQLException;

    /** Полностью заменить набор тегов у заметки (удалить старые связи, создать новые). */
    void setTagsForNote(int noteId, List<String> tagNames) throws SQLException;

    /** Заметки, помеченные данным тегом. */
    List<Note> findNotesByTagId(int tagId) throws SQLException;
}
