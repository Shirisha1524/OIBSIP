package reservation;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PNRSearchForm extends JFrame {

    private JTextField pnrF;
    private JButton searchBtn;
    private JTextArea ticketDetails;

    public PNRSearchForm() {
        setTitle("Search Ticket by PNR");
        setSize(500, 400);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Enter PNR:"));
        pnrF = new JTextField(15); top.add(pnrF);
        searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchTicket());
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        ticketDetails = new JTextArea();
        ticketDetails.setEditable(false);
        add(new JScrollPane(ticketDetails), BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void searchTicket() {
        String pnr = pnrF.getText().trim();
        if(pnr.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter PNR"); return; }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM reservations WHERE pnr=?");
            pst.setString(1, pnr);
            ResultSet rs = pst.executeQuery();

            if(rs.next()){
                String details = "PNR: "+rs.getString("pnr")+"\n"+
                        "Name: "+rs.getString("name")+"\n"+
                        "Train: "+rs.getString("train_name")+"\n"+
                        "Class: "+rs.getString("class_type")+"\n"+
                        "Journey: "+rs.getString("source")+" -> "+rs.getString("destination")+"\n"+
                        "Date: "+rs.getDate("journey_date")+"\n"+
                        "Status: "+rs.getString("status");
                ticketDetails.setText(details);
            } else {
                JOptionPane.showMessageDialog(this,"PNR not found");
                ticketDetails.setText("");
            }

        } catch(Exception e){ e.printStackTrace(); }
    }
}
