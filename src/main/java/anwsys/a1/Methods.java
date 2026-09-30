package anwsys.a1;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.util.*;

//Code für die Implementierung von Methoden aus den Anforderungen der Projektanforderungen
// (sortiert nach numerischer Abfolge der Anforderungen auf dem Arbeitsblatt)
@Log4j2
@UtilityClass
public class Methods {

    //Anforderung 1: alle To-dos ausgeben
    public static void listAll(Connection conn) {
        log.debug("Methods.listAll() aufgerufen");
        System.out.println("Listing all tasks:");
        System.out.println(" ");
        //Aufruf der Datenbank-Methode readValue mit definierter Connection in Main-function
        DatabaseMethods.readValue(conn);
        System.out.println(" ");
    }

    //Anforderung 2: nur offe To-dos ausgeben
    public static void listOpen(Connection conn) {
        log.debug("Methods.listOpen() aufgerufen");
        System.out.println("Unchecked Tasks: \n");
        DatabaseMethods.readOpen(conn);
    }

    //Anforderung 3: nach To-dos suchen (im Text oder in den Attributen des To-dos) und diese ausgeben
    public static void search(Connection conn) {
        log.debug("Methods.search() aufgerufen");
        Scanner scan = new Scanner(System.in);

        //Alle Tasks ausgeben
        DatabaseMethods.readValue(conn);

        System.out.println("Decide where you want to search");
        System.out.println("-----------------");
        System.out.println("1 - All columns");
        System.out.println("2 - ID");
        System.out.println("3 - Name");
        System.out.println("4 - Priority");
        System.out.println("5 - Due");
        System.out.println("6 - Project");
        System.out.println("7 - Context");
        System.out.println("8 - Individual");
        System.out.println("-----------------");
        System.out.println("Enter Number: ");
        int searchColumn = Integer.parseInt(scan.nextLine());
        System.out.println("Enter keyword to search: ");
        String value = scan.nextLine();

        //switch-case-Fall um zunächst richtige Spalte weiterzuverarbeiten und dann den Wert als Filter in dieser zu suchen
        switch (searchColumn) {
            case 1 -> {
                DatabaseMethods.searchTable(conn, "*", value);
            }
            case 2 -> {
                DatabaseMethods.searchTable(conn, "id", value);
            }
            case 3 -> {
                DatabaseMethods.searchTable(conn, "taskName", value);
            }
            case 4 -> {
                DatabaseMethods.searchTable(conn, "taskPriority", value);
            }
            case 5 -> {
                DatabaseMethods.searchTable(conn, "DueDate", value);
            }
            case 6 -> {
                DatabaseMethods.searchTable(conn, "ProjectTag", value);
            }
            case 7 -> {
                DatabaseMethods.searchTable(conn, "ContextTag", value);
            }
            case 8 -> {
                DatabaseMethods.searchTable(conn, "SpecialTag", value);
            }
        }
    }

    //Anforderung 4: den Status von einem To-do setzen (z. B. auf ”fertig“)
    public static void done(Connection conn) {
        log.debug("Methods.done() aufgerufen");
        System.out.println("Which task is done?");
        DatabaseMethods.readOpen(conn);

        Scanner scan = new Scanner(System.in);
        System.out.println("Enter Task-ID of done task");
        int doneID = Integer.parseInt(scan.nextLine());

        DatabaseMethods.updateTable(conn, "checked", "1", "id", String.valueOf(doneID));

    }

    //Anforderung 5: nach To-dos suchen und allen gefundenen To-dos einen Status geben
    // (z. B. alle To-dos mit dem Attribut ”AS“ den Status ”dringend“ geben)
    //Logisch gesehen Mischung aus DatabaseMethods.searchTable() und DatabaseMethods.updateTable(), daher viel recycled code
    public static void searchAndUpdate(Connection conn) {
        log.debug("Methods.searchAndUpdate() aufgerufen");
        Scanner scan = new Scanner(System.in); //Scanner erstellen für Userinput

        //1. Schritt -> Aufgaben zum ändern lokalisieren
        System.out.println("Where should be searched?: ");
        System.out.println("1 - All columns");
        System.out.println("2 - ID");
        System.out.println("3 - Name");
        System.out.println("4 - Priority");
        System.out.println("5 - Due");
        System.out.println("6 - Project");
        System.out.println("7 - Context");
        System.out.println("8 - Individual");
        System.out.print("Enter number: ");
        int searchColInput = Integer.parseInt(scan.nextLine()); //Userinput (int) in searchColInput speichern

        System.out.print("Which keyword should be searched? (e.g. Doing laundry) ");
        String searchValue = scan.nextLine(); //Userinput String in searchValue speichern

        //Kurz Hilfsfunktion unten aufrufen, um aus dem int-input den richtigen String für weiteren Methodeaufruf mappen
        String searchColumn = getColumnName(searchColInput);

        //2. Schritt -> Bei allen gefundenen Aufgaben einheitlich einen Spaltenwert mit input ändern
        System.out.println("Which column should be updated?:");
        System.out.println("3 - Name"); //Beginnend ab 3, damit getColumnName noch funktioniert
        System.out.println("4 - Priority");
        System.out.println("5 - Due");
        System.out.println("6 - Project");
        System.out.println("7 - Context");
        System.out.println("8 - Individual");
        System.out.print("With what should the value be replaced? Enter replacement: ");
        int targetColInput = Integer.parseInt(scan.nextLine());

        System.out.print("Which value should be inserted? (e.g. urgent) ");
        String newValue = scan.nextLine();

        String targetColumn = getColumnName(targetColInput); //Wieder Hilfsmethode für String-mapping

        //Alle Variaben als Parameter in die Database Methode befüllen und ausführen
        DatabaseMethods.severalUpdatesTable(conn, targetColumn, newValue, searchColumn, searchValue);
    }

    // Hilfsmethode, um den Integer aus dem Menü auf den echten Datenbank-Spaltennamen zu mappen
    private static String getColumnName(int input) {
        return switch (input) {
            case 1 -> "*";
            case 2 -> "id";
            case 3 -> "taskName";
            case 4 -> "taskPriority";
            case 5 -> "DueDate";
            case 6 -> "ProjectTag";
            case 7 -> "ContextTag";
            case 8 -> "SpecialTag";
            default -> "taskName";
        };
    }

    //Anforderung 6: ein neues To-do erstellen
    public static void add(Connection conn) {
        log.debug("Methods.add() aufgerufen");

        Scanner scan = new Scanner(System.in);
        System.out.println("Add the name of the task:");
        String name = scan.nextLine();

        Task task = new Task(0, name);

        //Hardcoded Abfrage von einzelne Bestandteile der Task
        System.out.println("Add priority?: (Press Enter to skip)");
        String prio = scan.nextLine();
        task.setPriority(prio);

        System.out.println("Add a due date?: (Press Enter to skip)");
        String due = scan.nextLine();
        task.setDue_date(due);

        System.out.println("Add a project tag?: (Press Enter to skip)");
        String project = scan.nextLine();
        task.setProject_tag(project);

        System.out.println("Add a context tag?: (Press Enter to skip)");
        String context = scan.nextLine();
        task.setContext_tag(context);

        System.out.println("Add an individual tag? (Press Enter to skip)");
        String individual = scan.nextLine();
        task.setSpeacial_tag(individual);

        //Aufruf der Datenbank-Funktion zum Einfügen von neuen Werten, dabei werden umständlich die Objektattribute übergeben um überhaupt die Task Klasse zu nutzen
        DatabaseMethods.insertValues(conn, task.getChecked(), task.getTask(), task.getPriority(), task.getDue_date(), task.getProject_tag(), task.getContext_tag(), task.getSpeacial_tag());
    }

    // Ab hier folgen weitere Hilfsfunktionen um das Programm vollständiger zu gestalten
    // Menü mit allen Optionen auf Terminal ausgeben
    public static void menu() {
        System.out.println("1 - add");
        System.out.println("2 - list all");
        System.out.println("3 - list all open tasks");
        System.out.println("4 - search");
        System.out.println("5 - mark as done");
        System.out.println("6 - search and update");
        System.out.println("7 - help");
        System.out.println("8 - exit");
    }

    //Ausgabe des Menues mit weiterer Beschreibung
    public static void help() {
        System.out.println("An Overview of possible commands and short description: ");
        System.out.println("1 - add - adding a task");
        System.out.println("2 - list all - lists all tasks without filter");
        System.out.println("3 - list all open tasks - lists all unchecked tasks");
        System.out.println("4 - search - search for tasks in one or all columns");
        System.out.println("5 - mark as done - marking a task as done (checked)");
        System.out.println("6 - search and update - change a column for all matching tasks");
        System.out.println("7 - help - shows this overview");
        System.out.println("8 - exit - exiting the program, files still saved");
    }
}