package dao;

import model.Note;
import model.Tag;

import java.util.*;

public class LocalTagDAO implements TagDAO {
    private final Map<Integer, Tag> tags = new HashMap<>();
    // noteId -> множество id тегов
    private final Map<Integer, Set<Integer>> noteTagLinks = new HashMap<>();
    private final LocalNoteDAO noteDAO;
    private int nextTagId = 1;

    public LocalTagDAO(LocalNoteDAO noteDAO) {
        this.noteDAO = noteDAO;
    }

    @Override
    public Tag findOrCreateTag(String name) {
        String normalized = name.trim();
        for (Tag tag : tags.values()) {
            if (tag.getName().equalsIgnoreCase(normalized)) {
                return tag;
            }
        }
        Tag tag = new Tag(nextTagId++, normalized);
        tags.put(tag.getId(), tag);
        return tag;
    }

    @Override
    public List<Tag> listAllTags() {
        List<Tag> result = new ArrayList<>(tags.values());
        result.sort(Comparator.comparing(Tag::getName));
        return result;
    }

    @Override
    public List<Tag> getTagsForNote(int noteId) {
        Set<Integer> ids = noteTagLinks.getOrDefault(noteId, Collections.emptySet());
        List<Tag> result = new ArrayList<>();
        for (int id : ids) {
            if (tags.containsKey(id)) {
                result.add(tags.get(id));
            }
        }
        result.sort(Comparator.comparing(Tag::getName));
        return result;
    }

    @Override
    public void addTagToNote(int noteId, int tagId) {
        noteTagLinks.computeIfAbsent(noteId, k -> new HashSet<>()).add(tagId);
    }

    @Override
    public void removeTagFromNote(int noteId, int tagId) {
        Set<Integer> ids = noteTagLinks.get(noteId);
        if (ids != null) {
            ids.remove(tagId);
        }
    }

    @Override
    public void setTagsForNote(int noteId, List<String> tagNames) {
        noteTagLinks.remove(noteId);
        if (tagNames == null) {
            return;
        }
        for (String rawName : tagNames) {
            String name = rawName.trim();
            if (name.isEmpty()) continue;
            Tag tag = findOrCreateTag(name);
            addTagToNote(noteId, tag.getId());
        }
    }

    @Override
    public List<Note> findNotesByTagId(int tagId) {
        List<Note> result = new ArrayList<>();
        for (Note note : noteDAO.listNotes()) {
            Set<Integer> ids = noteTagLinks.getOrDefault(note.getId(), Collections.emptySet());
            if (ids.contains(tagId)) {
                result.add(note);
            }
        }
        return result;
    }
}
