package reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class BookingHistoryForm extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private String username;

    public BookingHistoryForm(String username) {
        this.username = username;
        setTitle("Booking History");
        setSize(750,600);

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"PNR","Train","Class","From","To","Date","Status"});
        table = new JTable(model);
        add(new JScrollPane(table));

        loadHistory();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadHistory() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM reservations WHERE created_by=?");
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            model.setRowCount(0);
            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getString("pnr"),
                        rs.getString("train_name"),
                        rs.getString("class_type"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getDate("journey_date"),
                        rs.getString("status")
                });
            }
        } catch(Exception e){ e.printStackTrace(); }
    }
}

