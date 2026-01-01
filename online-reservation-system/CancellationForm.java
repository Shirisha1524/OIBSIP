package reservation;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CancellationForm extends JFrame {

    private JTextField pnrF;
    private JButton fetchBtn, cancelBtn;
    private JTextArea ticketDetails;

    public CancellationForm() {
        setTitle("Cancel Ticket");
        setSize(500,400);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Enter PNR:"));
        pnrF = new JTextField(15); top.add(pnrF);
        fetchBtn = new JButton("Fetch Ticket"); fetchBtn.addActionListener(e -> fetchTicket());
        top.add(fetchBtn);
        add(top,BorderLayout.NORTH);

        ticketDetails = new JTextArea(); ticketDetails.setEditable(false);
        add(new JScrollPane(ticketDetails), BorderLayout.CENTER);

        cancelBtn = new JButton("Cancel Ticket"); cancelBtn.addActionListener(e -> cancelTicket());
        add(cancelBtn,BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void fetchTicket() {
        String pnr = pnrF.getText().trim();
        if(pnr.isEmpty()){ JOptionPane.showMessageDialog(this,"Enter PNR"); return; }

        try(Connection con = DBConnection.getConnection()) {
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

    private void cancelTicket() {
        String pnr = pnrF.getText().trim();
        if(pnr.isEmpty()) return;

        try(Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            PreparedStatement pst1 = con.prepareStatement("SELECT train_no, class_type, status FROM reservations WHERE pnr=?");
            pst1.setString(1, pnr);
            ResultSet rs = pst1.executeQuery();
            if(rs.next()){
                String trainNo = rs.getString("train_no");
                String classType = rs.getString("class_type");
                String status = rs.getString("status");

                if(status.equals("Cancelled")){
                    JOptionPane.showMessageDialog(this,"Ticket is already cancelled");
                    return;
                }

                // Update reservation status
                PreparedStatement pst2 = con.prepareStatement("UPDATE reservations SET status='Cancelled' WHERE pnr=?");
                pst2.setString(1, pnr); pst2.executeUpdate();

                // Increase class-specific seat
                String seatCol = classType.equals("AC") ? "available_ac" : "available_sleeper";
                PreparedStatement pst3 = con.prepareStatement("UPDATE trains SET "+seatCol+"="+seatCol+"+1, available_seats=available_seats+1 WHERE train_no=?");
                pst3.setString(1, trainNo); pst3.executeUpdate();

                con.commit();
                JOptionPane.showMessageDialog(this,"Ticket Cancelled Successfully");
                ticketDetails.setText("");
            } else {
                JOptionPane.showMessageDialog(this,"PNR not found");
            }
        } catch(Exception e){ e.printStackTrace(); }
    }
}
