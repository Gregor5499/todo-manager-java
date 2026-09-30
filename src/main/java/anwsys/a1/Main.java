package anwsys.a1;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.*;
import java.sql.*;

@Log4j2
public class Main {

    public static void main(String[] args) throws IOException {

        boolean running = true;
        Scanner scan = new Scanner(System.in); //Scannerinsatnz für Userinput später bei cmd
        String path = "jdbc:sqlite:ToDo.db"; //Dateiort der Datenbank

        Connection conn1 = DatabaseMethods.connectingDB(path); //Methode zum Verbinden der DB aufrufen
        DatabaseMethods.creatingTable(conn1); //Methode zum Erstellen einer Tabelle falls nicht vorhanden aufrufen

        while (running == true) { //Programm läuft mit while solange nicht manuell verlassen wird

            log.info("Menülogik erreicht");
            //Interface des Menüs im Terminal
            System.out.println("%%%%%%%%%%%%%%%%%%%%%% Menu %%%%%%%%%%%%%%%%%%%%%%");
            Methods.menu(); //Menü printen
            System.out.println("--------------------------------------------------");
            System.out.println("Enter number of desired operation: ");
            String userInput = scan.nextLine();

            try {
                int cmd = Integer.parseInt(userInput); //Userinput mit Befehls-"Zahl"
                // Switch-case für Auswahl der Operation eingegeben durch den User / Anforderungen aus Assignment
                switch (cmd) {
                    case 1 -> {
                        Methods.add(conn1);
                    }
                    case 2 -> {
                        Methods.listAll(conn1);
                    }
                    case 3 -> {
                        Methods.listOpen(conn1);
                    }
                    case 4 -> {
                        Methods.search(conn1);
                    }
                    case 5 -> {
                        Methods.done(conn1);
                    }
                    case 6 -> {
                        Methods.searchAndUpdate(conn1);
                    }
                    case 7 -> {
                        Methods.help();
                    }
                    case 8 -> {
                        running = false;
                    } //Boolean wird auf false gesetzt -> while Schleife bricht ab -> Programmende
                    default -> {
                        System.out.println("Unknown command! Type in 'help' for further instructions");
                    }
                }
            } catch (RuntimeException e) {
                System.out.println("The entered command does not exist. Did you enter a whole number for the operation?");
                log.error("Der Input des Users (userInput: {}) war falsch", userInput, e);
            }
        }
    }
}