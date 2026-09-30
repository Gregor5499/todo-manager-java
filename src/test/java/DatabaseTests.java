import anwsys.a1.DatabaseMethods;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTests {

    //Vorbereitung der Tests

    private Connection testConnection;

    //Vor jeder Ausführng wird eine temporäre Datenbank in den RAM geladen, die später wieder gelöscht wird
    @BeforeEach
    void createTestDB() {
        testConnection = DatabaseMethods.connectingDB("jdbc:sqlite::memory:");
        assertNotNull(testConnection, "temp db exists"); //Prüfen ob connection != null ist
        DatabaseMethods.creatingTable(testConnection); //Gleiche Tabellenstruktur wie in Produktionscode
    }

    //Testing failing Verbindungsaufbau-Methode (Coverage)
    @Test
    void connectingDBFailTest() {
        Connection conn = DatabaseMethods.connectingDB("jdbc:invalid:database:path");
        assertNull(conn, "Sollte Exception auslösen und null zurückgeben");
    }

    //Nach jeder Test Auführung muss die temporäre Datenbank wieder aus dem RAM gelöscht werden, sonst Memory-Leaks
    @AfterEach
    void closeTestDB() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close(); //Schließen wenn nicht null und nicht bereits geschlossen ist
        }
    }

    //Unit Tests

    //Normaler Test mit gängigen Daten der insertValues() Methode
    @Test
    void insertValuesTest() throws SQLException {
        //Werte einfügen
        DatabaseMethods.insertValues(testConnection, 0, "Tests erledigen", "A", "26.06.2026", "Assignments", "Uni", "Dringend");

        //Checken ob die Task erfolgreich in die testDB gespeichert wurde
        String checkTestInsert = "SELECT * FROM ToDo WHERE taskName = ?";

        PreparedStatement pstm = testConnection.prepareStatement(checkTestInsert);
        pstm.setString(1, "Tests erledigen");
        ResultSet rsTest = pstm.executeQuery();

        assertTrue(rsTest.next(), "Eintrag vorhanden");
        assertEquals(0, rsTest.getInt("checked"));
        assertEquals("Tests erledigen", rsTest.getString("taskName"));
        assertEquals("A", rsTest.getString("taskPriority"));
        assertEquals("26.06.2026", rsTest.getString("DueDate"));
        assertEquals("Assignments", rsTest.getString("ProjectTag"));
        assertEquals("Uni", rsTest.getString("ContextTag"));
        assertEquals("Dringend", rsTest.getString("SpecialTag"));
    }

    //Normaler Daten-Test für readValue()
    @Test
    void readValueTest() throws SQLException {
        //Erst Daten einfügen um auslesen zu können
        DatabaseMethods.insertValues(testConnection, 0, "Tests erledigen", "A", "26.06.2026", "Assignments", "Uni", "Dringend");

        //Bei Ausgaben kann man nur schauen, ob die Methode keine Exception wirft
        //Optional könnte man es als Liste formatieren und die Ergebnisse vergleichen, aber umständlich und in meinem Programm nicht genutzt
        assertDoesNotThrow(() -> {
            DatabaseMethods.readValue(testConnection);
        }, "readValues löst keine Exception aus");

    }

    //Normaler Daten-Test für readOpen() -> Selbe Logik wie readValueTest()
    @Test
    void readOpenTest() throws SQLException {
        DatabaseMethods.insertValues(testConnection, 0, "Tests erledigen", "A", "26.06.2026", "Assignments", "Uni", "Dringend");
        DatabaseMethods.insertValues(testConnection, 1, "Tests erledigen", "A", "26.06.2026", "Assignments", "Uni", "Dringend");

        assertDoesNotThrow(() -> {
            DatabaseMethods.readOpen(testConnection);
        }, "readOpen löst keine Exception aus");
    }

    //Normaler Daten-Test für searchTable(), Spezifische Spalte wird durchsucht -> Selbe Logik wie readValueTest()
    @Test
    void searchTableTestSpecific() throws SQLException {
        DatabaseMethods.insertValues(testConnection, 0, "Tests erledigen", "A", "26.06.2026", "Assignments", "Uni", "Dringend");

        assertDoesNotThrow(() -> {
            DatabaseMethods.searchTable(testConnection, "taskPriority", "A");
        }, "searchTable löst keine Exception aus");
    }

    //Normaler Daten-Test für searchTable(), Alle Spalte werden durchsucht -> Selbe Logik wie readValueTest()
    @Test
    void searchTableTestAll() throws SQLException {
        DatabaseMethods.insertValues(testConnection, 0, "Tests erledigen", "A", "26.06.2026", "Assignments", "Uni", "Dringend");
        DatabaseMethods.insertValues(testConnection, 0, "adgffda", "A", "26.06.2026", "agffd", "afg", "Dringend");
        DatabaseMethods.insertValues(testConnection, 0, "adg", "A", "26.06.2026", "agag", "Uni", "Assignments");


        assertDoesNotThrow(() -> {
            DatabaseMethods.searchTable(testConnection, "*", "Assign");
        }, "searchTable löst keine Exception aus");
    }

    //Normaler Daten-Test für updateTable()
    @Test
    void updateTableTest() throws SQLException {
        //Aufgabe einfügen
        DatabaseMethods.insertValues(testConnection, 0, "Tests erledigen", "A", "26.06.2026", "Assignments", "Uni", "Dringend");
        //Aufgabe updaten -> checked von 0 auf 1
        DatabaseMethods.updateTable(testConnection, "checked", "1", "id", "1");

        //SQL Query -> Ist wirklich checked auf 1 nun?
        String checkQuery = "SELECT checked FROM ToDo WHERE id = ?";

        try (PreparedStatement pstmt = testConnection.prepareStatement(checkQuery)) {

            pstmt.setString(1, "1");

            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Es wurde kein Datensatz mit der ID 1 gefunden.");
                String actualValue = rs.getString("checked"); //checked isolieren
                assertEquals("1", actualValue, "Der Wert der 'checked'-Spalte wurde korrekt aktualisiert."); //Vergleich 1==1?
            }
        }
    }

    @Test
    void severalUpdatesTest() throws SQLException {
        //Mehrere Datensätze einfügen
        DatabaseMethods.insertValues(testConnection, 1, "Tests erledigen", "A", "26.06.2026", "Assignments", "Uni", "Dringend");
        DatabaseMethods.insertValues(testConnection, 2, "Vorlesung AnwSys", "B", "27.06.2026", "AnwSys", "Uni", "The Web vorbereiten");
        DatabaseMethods.insertValues(testConnection, 3, "Einkaufen", "C", "28.06.2026", "Haushalt", "Privat", "Normal");

        //
        DatabaseMethods.severalUpdatesTable(testConnection, "SpecialTag", "Wichtig", "ContextTag", "Uni");

        String checkQuery = "SELECT ID, SpecialTag FROM ToDo";

        try (Statement stmt = testConnection.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {

            assertTrue(rs.next(), "To-do 1 sollte existieren");
            assertEquals(1, rs.getInt("ID"));
            assertEquals("Wichtig", rs.getString("SpecialTag"), "SpecialTag von To-do 1 sollte auf 'Wichtig' geupdatet worden sein.");

            assertTrue(rs.next(), "To-do 2 sollte existieren");
            assertEquals(2, rs.getInt("ID"));
            assertEquals("Wichtig", rs.getString("SpecialTag"), "SpecialTag von To-do 2 sollte auf 'Wichtig' geupdatet worden sein.");

            assertTrue(rs.next(), "To-do 3 sollte existieren");
            assertEquals(3, rs.getInt("ID"));
            assertEquals("Normal", rs.getString("SpecialTag"), "SpecialTag von To-do 3 sollte unverändert 'Normal' sein.");

            assertFalse(rs.next(), "Es sollten genau 3 Einträge vorhanden sein.");
        }
    }

    @Test
        //Zusatz: Test für Asterisk (*) in severalUpdatesTable
    void severalUpdatesTestAllColumns() throws SQLException {
        DatabaseMethods.insertValues(testConnection, 1, "Tests erledigen", "A", "26.06.2026", "Assignments", "Uni", "Dringend");
        DatabaseMethods.severalUpdatesTable(testConnection, "taskPriority", "Z", "*", "Tests");

        String checkQuery = "SELECT taskPriority FROM ToDo WHERE id = 1";
        try (Statement stmt = testConnection.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            assertTrue(rs.next());
            assertEquals("Z", rs.getString("taskPriority"), "Sollte durch die Wildcard-Suche gefunden und geupdatet werden");
        }
    }

    //Für eine höhere Coverage-Rate werden die catch-Blöcke der einzelnen Methoden nochmal getestet
    //Die Logik ist dabei die Selbe: Die Datenbankverbnidung wird geschlossen bevor die Methode aufgerufen wird

    @Test
    void creatingTableTestSQLException() throws SQLException {
        testConnection.close();
        assertDoesNotThrow(() -> DatabaseMethods.creatingTable(testConnection), "Sollte Exception fangen und ausgeben");
    }

    @Test
    void insertValuesTestSQLException() throws SQLException {
        testConnection.close();
        assertDoesNotThrow(() -> DatabaseMethods.insertValues(testConnection, 0, "Test", "", "", "", "", ""), "Sollte Exception fangen und ausgeben");
    }

    @Test
    void readValueTestSQLException() throws SQLException {
        testConnection.close();
        assertDoesNotThrow(() -> DatabaseMethods.readValue(testConnection), "Sollte Exception fangen und ausgeben");
    }

    @Test
    void readOpenTestSQLException() throws SQLException {
        testConnection.close();
        assertDoesNotThrow(() -> DatabaseMethods.readOpen(testConnection), "Sollte Exception fangen und ausgeben");
    }

    @Test
    void SearchTableTestSQLException() throws SQLException {
        testConnection.close();
        assertDoesNotThrow(() -> DatabaseMethods.searchTable(testConnection, "taskName", "Test"), "Sollte Exception fangen und ausgeben");
    }

    @Test
    void updateTableTestSQLException() throws SQLException {
        testConnection.close();
        assertDoesNotThrow(() -> DatabaseMethods.updateTable(testConnection, "checked", "1", "id", "1"), "Sollte Exception fangen und ausgeben");
    }

    @Test
    void severalUpdatesTableTestSQLException() throws SQLException {
        testConnection.close();
        assertDoesNotThrow(() -> DatabaseMethods.severalUpdatesTable(testConnection, "checked", "1", "taskName", "Test"), "Sollte Exception fangen und ausgeben");
    }


    //Grenzfälle

    @Test
        //Grenzfall: Wenn Name null ist muss eine Exception geworfen werden
    void insertValuesTestNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            DatabaseMethods.insertValues(testConnection, 0, null, "", "", "", "", "");
        },"Sollte IllegalArgumentException werfen weil Name null ist");
    }

    @Test
        //Grenzfall: Wenn Name leer ist muss eine Exception geworfen werden
    void insertValuesTestEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            DatabaseMethods.insertValues(testConnection, 0, "", "", "", "", "", "");
        }, "Sollte IllegalArgumentException werfen weil Name leer ist");
    }
}