package reservation;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu(String username, String role) {
        setTitle("Main Menu");
        setSize(700,550);
        setLayout(new GridLayout(5,1,10,10));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton bookBtn = new JButton("Book Ticket");
        bookBtn.addActionListener(e -> new ReservationForm(username));
        add(bookBtn);

        JButton cancelBtn = new JButton("Cancel Ticket");
        cancelBtn.addActionListener(e -> new CancellationForm());
        add(cancelBtn);

        JButton historyBtn = new JButton("Booking History");
        historyBtn.addActionListener(e -> new BookingHistoryForm(username));
        add(historyBtn);

        JButton searchBtn = new JButton("Search PNR");
        searchBtn.addActionListener(e -> new PNRSearchForm());
        add(searchBtn);

        if(role.equals("admin")){
            JButton adminBtn = new JButton("Admin Dashboard");
            adminBtn.addActionListener(e -> new AdminDashboard());
            add(adminBtn);

            JButton trainBtn = new JButton("Train Management");
            trainBtn.addActionListener(e -> new TrainManagementForm());
            add(trainBtn);
        }

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
