# TakeNote

Учебное приложение для заметок: JavaFX + PostgreSQL (JDBC), архитектура Controller → DAO → Model.

## Что нужно, чтобы запустить

1. **JDK 21+**
2. **PostgreSQL**, запущенный локально. Выполнить `db/schema.sql` в базе `takenote_db`
   (или создать такую БД и применить скрипт):
   ```
   psql -U postgres -c "CREATE DATABASE takenote_db;"
   psql -U postgres -d takenote_db -f db/schema.sql
   ```
   Пользователь/пароль/URL заданы в `src/main/java/config/DatabaseConfig.java`
   (`takenote_user` / `mypassword` / `jdbc:postgresql://localhost:5432/takenote_db`) —
   поправь под свою локальную БД, если у тебя другие данные.
3. Если PostgreSQL недоступен — приложение всё равно запустится, но будет работать
   на временном хранилище в памяти (`LocalNoteDAO` / `LocalTagDAO`), об этом скажет
   надпись внизу левой панели.

## Запуск

```
mvn clean javafx:run
```

Если у тебя в pom.xml версия JavaFX (`javafx.version`) не подходит под установленный
JDK — поменяй её на актуальную LTS-версию с https://openjfx.io.

## Структура проекта

```
src/main/java/
  Main.java            — создаёт DAO/Controller и запускает JavaFX-приложение
  app/MainApp.java     — точка входа JavaFX (Application), загружает FXML
  config/              — DatabaseConfig (подключение к Postgres)
  model/               — Note, Tag — простые модели без бизнес-логики
  dao/                 — NoteDAO/TagDAO (интерфейсы) + JDBC- и in-memory реализации
  controller/          — Controller — бизнес-логика поверх DAO
  ui/                  — код самого JavaFX-интерфейса (не бизнес-логика)
    MainViewController.java — вся логика окна (список, поиск, редактирование)
    MarkdownLite.java        — упрощённый рендер текста заметки (**жирный**, `код`, - списки)

src/main/resources/
  fxml/MainView.fxml   — разметка интерфейса
  css/style.css        — стили

db/schema.sql          — SQL-схема (notes, tags, note_tag)
```

## Что изменено по сравнению с исходным вариантом

- Добавлены теги: модель `Tag`, интерфейс `TagDAO` + `TagDAOImpl` (Postgres) и
  `LocalTagDAO` (in-memory), таблицы `tags` и `note_tag` в БД.
- `Controller` теперь работает и с `NoteDAO`, и с `TagDAO`
  (конструктор поменялся: `new Controller(noteDAO, tagDAO)`).
- Добавлен JavaFX-интерфейс: список заметок слева, теги как «папки»
  библиотеки, панель чтения/редактирования справа.
- Исправлен `Main.java`: раньше метод `main` был объявлен без `public` и без
  `String[] args`, из-за чего JVM не находила точку входа и приложение не
  запускалось ни в каком виде.
- В `LocalNoteDAO` добавлена автогенерация id — раньше все заметки, созданные
  без явного id, получали id = 0 и затирали друг друга при сохранении.

## Несостыковки в исходных диаграммах (для отчёта по проекту)

- ER-диаграммы описывали также таблицы `User` и связь один-ко-многим с
  заметками — в текущей реализации логина/пользователей нет, в интерфейсе и
  БД реализованы только заметки и теги. Если для проекта нужна ещё и
  авторизация — это отдельная задача (таблица `users`, `user_id` в `notes`,
  экран логина).
- Диаграмма классов и диаграмма компонентов соответствовали коду точно,
  кроме отсутствия `Tag`/`TagDAO`, которых до этого не было ни на диаграмме,
  ни в коде.
