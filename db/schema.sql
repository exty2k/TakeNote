CREATE TABLE IF NOT EXISTS notes (
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    content     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS tags (
    tag_id      SERIAL PRIMARY KEY,
    tag_name    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS note_tag (
    note_id     INTEGER NOT NULL REFERENCES notes(id) ON DELETE CASCADE,
    tag_id      INTEGER NOT NULL REFERENCES tags(tag_id) ON DELETE CASCADE,
    PRIMARY KEY (note_id, tag_id)
);
