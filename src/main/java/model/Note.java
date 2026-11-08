package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Note {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private int id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private List<Tag> tags = new ArrayList<>();

    public Note(int id, String title, String content, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = (tags != null) ? tags : new ArrayList<>();
    }

    public String getTagsAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(tags.get(i).getName());
        }
        return sb.toString();
    }

    public String fullInfo() {
        return "(" + id + ") " + title + "\n"
                + content + "\n"
                + "Дата создания: " + createdAt.format(DATE_FORMAT)
                + (tags.isEmpty() ? "" : "\nТеги: " + getTagsAsString());
    }

    public String shortInfo() {
        return "(" + id + ") " + title;
    }
}
