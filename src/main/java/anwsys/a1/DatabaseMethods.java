package anwsys.a1;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.sql.*;

@Log4j2
@UtilityClass
public class DatabaseMethods {

    //Methode zum Verbinden der Datenbank
    public static Connection connectingDB(String path) {
        log.debug("DatabaseMethods.connectingDB() aufgerufen, path = {}", path);

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(path); //Datatype Connection aus gegebenen Path holen und in conn speichern
            if (conn != null) {
                log.info("Conncting with database was successfull");
            }
            return conn;
        } catch (SQLException e) {
            log.error("Eine Verbindung zur Datenbank konnte mit path ='{}' nicht erreicht werden", path, e);
            System.out.println("Error: Connecting to the database failed! You will be redirected to the menu");
            return null; //null zurückgeben, wenn keine Connection gespeichert werden konnte
        }
    }

    //Tabelle in Datenbank erstellen, falls keine vorhanden ist (Erste Ausführung)
    public static void creatingTable(Connection conn) {
        log.debug("DatabaseMethods.creatingTable() aufgerufen");

        //SQL Statement als String speichern (Tabelle und Spalten erstellen)
        String create_table = "CREATE TABLE IF NOT EXISTS ToDo(id INTEGER PRIMARY KEY AUTOINCREMENT, checked INTEGER NOT NULL, taskPriority TEXT, taskName TEXT NOT NULL, DueDate TEXT, ProjectTag TEXT, ContextTag TEXT, SpecialTag TEXT);";

        try {
            Statement create = conn.createStatement(); //Statement erstellen
            create.executeUpdate(create_table); //create_table auf Connection ausführen (SQL String)
            log.info("Tabelle ToDo wurde erfolgreich erstellt oder war vorhanden");
        } catch (SQLException e) {
            log.error("Tabelle konnte nicht erstellt werden", e);
            System.out.println("Error: Creating table failed! You will be redirected to the menu");
        }
    }

    //Methode zum Einfügen von Werten in die Tabelle (add Methode)
    public static void insertValues(Connection conn, int checked, String name, String prio, String due, String project, String context, String special) {
        log.debug("DatabaseMethods.insertValues() aufgerufen für name = {}", name);

        try {
            //Erneut SQL-Befehl als String speichern
            String insert_value = "INSERT INTO ToDo(checked, taskName, taskPriority, DueDate, ProjectTag, ContextTag, SpecialTag) VALUES (?,?,?,?,?,?,?);";

            if (name == null || name.isEmpty()) {
                log.error("Der Name der Task ist leer, insert abgebrochen");
                throw new IllegalArgumentException("Name of the task is not allowed to be empty!");
            }

            PreparedStatement pstm = conn.prepareStatement(insert_value); //PreparedStatement erstellen, da Platzhalter ? befüllt werden müssen (Schutz vor Injection)
            //Alle Parameter des Funktionskopfs in die Platzhalter befüllen
            pstm.setInt(1, checked); //Parameter
            pstm.setString(2, name);
            pstm.setString(3, prio);
            pstm.setString(4, due);
            pstm.setString(5, project);
            pstm.setString(6, context);
            pstm.setString(7, special);

            pstm.executeUpdate(); //PreparedStatement ausführen
            log.info("Aufgabe {} erfolgreich eingefügt", name);

        } catch (SQLException e) {
            log.error("Einfügen der Werte erfolglos, Parameter: checked='{}'. name='{}', prio='{}', due='{}', project='{}', context='{}', special='{}'",
                    checked, name, prio, due, project, context, special, e);
            System.out.println("Error: Inserting value failed! You will be redirected to the menu");
        }
    }

    //Methode zum Lesen aller Datensätze der Tabelle
    public static void readValue(Connection conn) {
        log.debug("DatabaseMethods.readValue() aufgerufen");

        //SQL Query als String speichern (Alle Daten bzw. Spalten der Tabelle ausgeben)
        String queryall = "SELECT * FROM ToDo";

        try {
            Statement querystm = conn.createStatement(); //Statement mit SQL-String erstellen
            ResultSet rs = querystm.executeQuery(queryall); //Resultset = Objekt, welches einen Datensatz / "Zeile" speichert mit jenen Feldern

            //Tabellenkopf als formatierten String speichern (schöner für die Optik im Terminal)
            String format = "%-5s | %-5s | %-50s | %-10s | %-10s | %-15s | %-15s | %-15s%n"; //Jeweils Platzhalter definieren (linksbündig)
            System.out.printf(format, "ID", "Done", "Task", "Priority", "Due", "Project", "Context", "Individual"); //Platzhalter mit Spaltennamen befüllen

            //Trennlinie mit exakter Anzahl der Spaltenbreiten
            System.out.println("------+-------+----------------------------------------------------+------------+------------+-----------------+-----------------+-----------------");

            //Schleife, die jede rs-Instanz ausliest in lokalen Variablen speichert und am Ende in der Terminaltabelle printed
            while (rs.next()) {
                int id = rs.getInt("id");
                int bool = rs.getInt("checked");
                String taskName = rs.getString("taskName");
                String prio = rs.getString("taskPriority");
                String due = rs.getString("DueDate");
                String project = rs.getString("ProjectTag");
                String context = rs.getString("ContextTag");
                String special = rs.getString("SpecialTag");

                //Kurze Logik, um erledigte Tasks nicht mit 1 und 0 darzustellen, sondern mit einem String [ ] oder [X]
                String checked;
                if (bool == 1) {
                    checked = " [X] ";
                } else {
                    checked = " [ ] ";
                }

                //Printing
                System.out.printf("%-5d | %-5s | %-50s | %-10s | %-10s | %-15s | %-15s | %-15s%n",
                        id, checked, taskName, prio, due, project, context, special);
            }

        } catch (SQLException e) {
            log.error("Ausgabe aller Aufgaben failed", e);
            System.out.println("Error: Reading all values failed! You will be redirected to the menu");
        }
    }

    //Methode zum Auslesen nur offener Aufgaben
    //Beinhaltet nahezu die gleiche Logik wie readValue(), nur dass WHERE im SQL-String spezifiziert wird
    public static void readOpen(Connection conn) {
        log.debug("DatabaseMethods.readOpen() aufgerufen");

        String queryOpen = "SELECT * FROM ToDo WHERE (checked = 0)"; //Nur alle Datensätze ausgeben mit 0 in checked
        try {
            Statement openstm = conn.createStatement();
            ResultSet rs = openstm.executeQuery(queryOpen);

            //Alles Folgende identisch mit readValue(), oben erklärt
            String format = "%-5s | %-5s | %-50s | %-10s | %-10s | %-15s | %-15s | %-15s%n";
            System.out.printf(format, "ID", "Done", "Task", "Priority", "Due", "Project", "Context", "Individual");

            System.out.println("------+-------+----------------------------------------------------+------------+------------+-----------------+-----------------+-----------------");

            while (rs.next()) {
                int id = rs.getInt("id");
                int bool = rs.getInt("checked");
                String taskName = rs.getString("taskName");
                String prio = rs.getString("taskPriority");
                String due = rs.getString("DueDate");
                String project = rs.getString("ProjectTag");
                String context = rs.getString("ContextTag");
                String special = rs.getString("SpecialTag");

                String checked = "";
                if (bool == 1) {
                    checked = " [X] ";
                } else {
                    checked = " [ ] ";
                }

                System.out.printf("%-5d | %-5s | %-50s | %-10s | %-10s | %-15s | %-15s | %-15s%n",
                        id, checked, taskName, prio, due, project, context, special);
            }

        } catch (SQLException e) {
            log.error("Ausgabe offener Aufgaben nicht failed", e);
            System.out.println("Error: Reading all open tasks failed! You will be redirected to the menu");
        }
    }

    //Methode zum Durchsuchen der Tabelle nach exaktem Keyword
    public static void searchTable(Connection conn, String searchColumn, String value) {
        log.debug("DatabaseMethods.searchTable() aufgerufen, Spalte={}, Wert={}", searchColumn, value);

        String queryall; //SQL String erstellen, aber zwei Logiken implementieren:

        //Wenn alle Spalten durchsucht werden sollen
        if (searchColumn.equals("*")) {
            //Wichtig ist hier, dass LIKE statt = genutzt wird, damit zum Beispiel bei der Eingabe "Ler" auch die Aufgabe "Lernen" gefunden wird
            queryall = "SELECT * FROM ToDo WHERE taskName LIKE ? " + "OR taskPriority LIKE ? " + "OR DueDate LIKE ? " + "OR ProjectTag LIKE ? " + "OR ContextTag LIKE ? " + "OR SpecialTag LIKE ?";
        } else {
            queryall = "SELECT * FROM ToDo WHERE " + searchColumn + " LIKE ?"; //Wenn eine spezifische Spalte durchsucht werden soll (übergeben von Methods.search)
        }

        try {
            PreparedStatement pstm = conn.prepareStatement(queryall); //Prepared Statement erstellen (wie vorher)

            if (!searchColumn.equals("*")) {
                pstm.setString(1, "%" + value + "%"); //Wenn eine spezifische Spalte durchsucht wird (value übergeben von Methods.search für jeweilige Spalte)
            } else {
                pstm.setString(1, "%" + value + "%");
                pstm.setString(2, "%" + value + "%");
                pstm.setString(3, "%" + value + "%");
                pstm.setString(4, "%" + value + "%");
                pstm.setString(5, "%" + value + "%");
                pstm.setString(6, "%" + value + "%");
            }

            //Erneute Logik im Folgenden wie aus readValue()
            ResultSet rs = pstm.executeQuery();

            String format = "%-5s | %-5s | %-50s | %-10s | %-10s | %-15s | %-15s | %-15s%n";

            System.out.printf(format, "ID", "Done", "Task", "Priority", "Due", "Project", "Context", "Individual");
            System.out.println("------+-------+----------------------------------------------------+------------+------------+-----------------+-----------------+-----------------");

            while (rs.next()) {

                int id = rs.getInt("id");
                int bool = rs.getInt("checked");
                String taskName = rs.getString("taskName");
                String prio = rs.getString("taskPriority");
                String due = rs.getString("DueDate");
                String project = rs.getString("ProjectTag");
                String context = rs.getString("ContextTag");
                String special = rs.getString("SpecialTag");

                String checked = "";
                if (bool == 1) {
                    checked = " [X] ";
                } else {
                    checked = " [ ] ";
                }

                System.out.printf(format, String.valueOf(id), checked, taskName, prio, due, project, context, special);
            }

        } catch (SQLException e) {
            log.error("Suche erfolglos, searchColumn = {}, value = {}", searchColumn, value, e);
            System.out.println("Error: Searching the table failed! You will be redirected to the menu");
        }
    }

    //Methode zum abändern einzelner Werte für done-Methode bzw. optional weiteren Methoden
    public static void updateTable(Connection conn, String targetColumn, String searchColumn, String filterColumn, String filter) {
        log.debug("DatabaseMethods.updateTable() aufgerufen. Setze '{}' = '{}' wo '{}' = '{}'", targetColumn, searchColumn, filterColumn, filter);

        String updateQuery = "UPDATE ToDo SET " + targetColumn + " = ? WHERE " + filterColumn + " = ?"; //Update-Query mit Spaltenparameter und Zeilenparameter (Bei done -> checked-Spalte updaten wo übergebene ID = ID in Spalte übereinstimmt)

        //Wieder Preparedstatement erstellen und ausführen auf Platzhalter
        try {
            PreparedStatement pstm = conn.prepareStatement(updateQuery);

            pstm.setString(1, searchColumn);
            pstm.setString(2, filter);

            pstm.executeUpdate();
            log.info("Update erfolgreich");

        } catch (SQLException e) {
            log.error("Update fehlgeschlagen. Konnte Spalte '{}' nicht auf Wert '{}' (Filter: {}={}) setzen.",
                    targetColumn, searchColumn, filterColumn, filter, e);
            System.out.println("Error: Updating table failed! You will be redirected to the menu");
        }
    }

    //Methode zum suchen von mehreren Aufgaben und abändern der gefundenen Aufgaben (Näher beschrieben in Methods.java)
    public static void severalUpdatesTable(Connection conn, String targetColumn, String newValue, String searchColumn, String searchValue) {
        log.debug("DatabaseMethods.severalUpdatesTable() aufgerufen. Zielspalte: '{}', Neuer Wert: '{}', Suchspalte: '{}', Suchwert: '{}'",
                targetColumn, newValue, searchColumn, searchValue);

        String updateQuery; //SQL Query als String

        if (searchColumn.equals("*")) { // Wenn alle Spalten durchsucht werden
            //zielSpalte updaten, wo Task like einer der searchValue entspricht
            updateQuery = "UPDATE ToDo SET " + targetColumn + " = ? WHERE taskName LIKE ? OR taskPriority LIKE ? OR DueDate LIKE ? OR ProjectTag LIKE ? OR ContextTag LIKE ? OR SpecialTag LIKE ?";
        } else { // Wenn nur eine bestimmte Spalte durchsucht worden ist
            //Zielspalte updaten mit bereits gewissen searchColumn
            updateQuery = "UPDATE ToDo SET " + targetColumn + " = ? WHERE " + searchColumn + " LIKE ?";
        }

        try {
            //Prepared Statement mit neuem / geupdated Wert in den ersten ?
            PreparedStatement pstm = conn.prepareStatement(updateQuery);
            pstm.setString(1, newValue);

            if (!searchColumn.equals("*")) { //Erst der Fall, wenn eine gewisse Spalte beabsichtig ist
                pstm.setString(2, "%" + searchValue + "%"); //Gezielte Spalte in searchValue
            } else {
                // Einer der Tabellenspalten entspricht mit searchedValue
                pstm.setString(2, "%" + searchValue + "%");
                pstm.setString(3, "%" + searchValue + "%");
                pstm.setString(4, "%" + searchValue + "%");
                pstm.setString(5, "%" + searchValue + "%");
                pstm.setString(6, "%" + searchValue + "%");
                pstm.setString(7, "%" + searchValue + "%");
            }

            pstm.executeUpdate(); //Prepared Statement ausführen

        } catch (SQLException e) {
            log.error("Fehler beim SeveralUpdate. Ziel: '{}', Wert: '{}'", targetColumn, newValue, e);
            System.out.println("Error: Searching and updating the table failed! You will be redirected to the menu");
        }
    }
}