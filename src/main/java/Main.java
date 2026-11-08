import app.MainApp;
import config.DatabaseConfig;
import controller.Controller;
import dao.*;
import javafx.application.Application;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Main создаёт слой доступа к данным и Controller, затем передаёт их в JavaFX-приложение.
 * Сам Main класс не наследует Application — это позволяет держать сборку/логику запуска
 * отдельно от UI-кода (см. диаграмму компонентов проекта).
 */
public class Main {
    public static void main(String[] args) {
        NoteDAO noteDAO;
        TagDAO tagDAO;
        String statusText;

        try {
            Connection connection = DatabaseConfig.getConnection();
            noteDAO = new NoteDAOImpl(connection);
            tagDAO = new TagDAOImpl(connection);
            statusText = DatabaseConfig.databaseName() + " · подключено";
            System.out.println("Подключение к PostgreSQL установлено.");
        } catch (SQLException e) {
            System.err.println("Не удалось подключиться к PostgreSQL, используется временное хранилище в памяти.");
            System.err.println("Причина: " + e.getMessage());
            LocalNoteDAO localNoteDAO = new LocalNoteDAO();
            noteDAO = localNoteDAO;
            tagDAO = new LocalTagDAO(localNoteDAO);
            statusText = "локальное хранилище · без БД";
        }

        Controller controller = new Controller(noteDAO, tagDAO);
        MainApp.inject(controller, statusText);
        Application.launch(MainApp.class, args);
    }
}
