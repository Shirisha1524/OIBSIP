package reservation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;
import javax.swing.SpinnerDateModel;

public class ReservationForm extends JFrame implements ActionListener {

    private JTextField nameF, fromF, toF;
    private JComboBox<String> trainNoCombo, classCombo;
    private JSpinner dateSpinner;
    private JButton bookBtn;
    private JTextField trainNameF;
    private String createdBy;

    public ReservationForm(String createdBy) {
        this.createdBy = createdBy;
        setTitle("Book Ticket");
        setSize(450, 380);
        setLayout(new GridLayout(9,2,8,8));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(new JLabel("Passenger Name:"));
        nameF = new JTextField(); add(nameF);

        add(new JLabel("Train Number:"));
        trainNoCombo = new JComboBox<>(); add(trainNoCombo);

        add(new JLabel("Train Name:"));
        trainNameF = new JTextField(); trainNameF.setEditable(false); add(trainNameF);

        add(new JLabel("Class Type:"));
        classCombo = new JComboBox<>(new String[]{"AC","Sleeper"}); add(classCombo);

        add(new JLabel("Journey Date:"));
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner,"yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        add(dateSpinner);

        add(new JLabel("From:"));
        fromF = new JTextField(); add(fromF);

        add(new JLabel("To:"));
        toF = new JTextField(); add(toF);

        bookBtn = new JButton("Book Ticket");
        bookBtn.addActionListener(this);
        add(new JLabel()); add(bookBtn);

        loadTrainNumbers();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadTrainNumbers() {
        try(Connection con = DBConnection.getConnection()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT train_no, train_name FROM trains");
            while(rs.next()) {
                trainNoCombo.addItem(rs.getString("train_no")+" - "+rs.getString("train_name"));
            }
        } catch(Exception ex){ ex.printStackTrace(); }

        trainNoCombo.addActionListener(e -> {
            String sel = (String)trainNoCombo.getSelectedItem();
            if(sel != null) trainNameF.setText(sel.split(" - ")[1]);
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(Utils.isEmpty(nameF.getText()) || Utils.isEmpty(fromF.getText()) || Utils.isEmpty(toF.getText())) {
            JOptionPane.showMessageDialog(this,"Please fill all fields");
            return;
        }

        try(Connection con = DBConnection.getConnection()) {
            String selected = (String)trainNoCombo.getSelectedItem();
            if(selected == null) { JOptionPane.showMessageDialog(this,"Select a train"); return; }
            String trainNo = selected.split(" - ")[0];
            String trainName = selected.split(" - ")[1];
            String classType = (String)classCombo.getSelectedItem();

            con.setAutoCommit(false);

            // Check class-specific seats
            String seatCol = classType.equals("AC") ? "available_ac" : "available_sleeper";
            PreparedStatement seatCheck = con.prepareStatement("SELECT "+seatCol+" FROM trains WHERE train_no=?");
            seatCheck.setString(1, trainNo);
            ResultSet rsSeat = seatCheck.executeQuery();
            if(rsSeat.next() && rsSeat.getInt(seatCol) <= 0){
                JOptionPane.showMessageDialog(this,"No "+classType+" seats available!");
                con.rollback();
                return;
            }

            String pnr = Utils.generatePNR(con);
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO reservations(pnr,name,train_no,train_name,class_type,journey_date,source,destination,created_by) VALUES(?,?,?,?,?,?,?,?,?)"
            );
            pst.setString(1, pnr); pst.setString(2,nameF.getText().trim());
            pst.setString(3, trainNo); pst.setString(4,trainName);
            pst.setString(5, classType);
            java.util.Date date = (java.util.Date)dateSpinner.getValue();
            pst.setDate(6,new java.sql.Date(date.getTime()));
            pst.setString(7, fromF.getText().trim());
            pst.setString(8, toF.getText().trim());
            pst.setString(9, createdBy);
            pst.executeUpdate();

            // Reduce class-specific seats
            PreparedStatement updateSeat = con.prepareStatement("UPDATE trains SET "+seatCol+"="+seatCol+"-1, available_seats=available_seats-1 WHERE train_no=?");
            updateSeat.setString(1, trainNo);
            updateSeat.executeUpdate();

            con.commit();
            JOptionPane.showMessageDialog(this,"Ticket Booked! PNR: "+pnr);
            dispose();

        } catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(this,"Booking failed: "+ex.getMessage()); }
    }
}
