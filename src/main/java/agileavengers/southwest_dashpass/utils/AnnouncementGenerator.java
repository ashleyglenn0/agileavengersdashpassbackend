package agileavengers.southwest_dashpass.utils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import agileavengers.southwest_dashpass.models.Announcement;
import org.springframework.stereotype.Component;

@Component
public class AnnouncementGenerator {
    private static final String[] MESSAGES = {
            "Company-wide meeting this Friday at 3 PM.",
            "Holiday schedule will be announced next week.",
            "New COVID-19 safety protocols in place.",
            "Employee training session scheduled for Monday.",
            "Year-end reviews coming up next month.",
            "Office will be closed for Thanksgiving holiday."
    };

    private static final String[] TYPES = {"Important", "Holiday", "General"};

    // Method to generate random announcements
    public static List<Announcement> generateRandomAnnouncements(int count) {
        List<Announcement> announcements = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String message = MESSAGES[random.nextInt(MESSAGES.length)];
            String type = TYPES[random.nextInt(TYPES.length)];
            LocalDate date = LocalDate.now().plusDays(random.nextInt(10)); // Random date within the next 10 days
            announcements.add(new Announcement(message, type, date));
        }

        return announcements;
    }
}
