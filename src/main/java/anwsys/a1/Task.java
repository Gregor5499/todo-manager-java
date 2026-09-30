package anwsys.a1;

import lombok.*;

//Erstellung eines Task-Objekts -> Getter, Setter und Konstruktoren durch Lonbok generiert (@Data}
@Data
public class Task {
    @NonNull
    private int checked; //Attribut checked muss immer 0 oder 1 sein -> null logisch falsch
    private String priority;
    @NonNull
    private String task; //Mindestinfo einer Aufgabe ist zumindest der Name, alles andere optional
    private String due_date;
    private String project_tag;
    private String context_tag;
    private String speacial_tag;
}