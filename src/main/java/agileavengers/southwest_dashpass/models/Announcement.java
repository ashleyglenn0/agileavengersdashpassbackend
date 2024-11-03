package agileavengers.southwest_dashpass.models;

import java.time.LocalDate;

public class Announcement {
    private String message;
    private String type;  // e.g., "Important", "Holiday", "General"
    private LocalDate date;

    // Constructor
    public Announcement(String message, String type, LocalDate date) {
        this.message = message;
        this.type = type;
        this.date = date;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }
}

