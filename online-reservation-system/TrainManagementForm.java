package reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TrainManagementForm extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField noF, nameF, seatsF;
    private JButton addBtn, updateBtn, deleteBtn;

    public TrainManagementForm() {
        setTitle("Train Management");
        setSize(600,400);
        setLayout(new BorderLayout());

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"Train No","Train Name","Total Seats","Available Seats"});
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout());
        noF = new JTextField(6); nameF = new JTextField(10); seatsF = new JTextField(5);
        addBtn = new JButton("Add"); updateBtn = new JButton("Update"); deleteBtn = new JButton("Delete");

        form.add(new JLabel("No:")); form.add(noF);
        form.add(new JLabel("Name:")); form.add(nameF);
        form.add(new JLabel("Seats:")); form.add(seatsF);
        form.add(addBtn); form.add(updateBtn); form.add(deleteBtn);

        add(form, BorderLayout.SOUTH);

        loadTrains();

        addBtn.addActionListener(e -> addTrain());
        updateBtn.addActionListener(e -> updateTrain());
        deleteBtn.addActionListener(e -> deleteTrain());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadTrains() {
        try (Connection con = DBConnection.getConnection()) {
            model.setRowCount(0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM trains");
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("train_no"),
                        rs.getString("train_name"),
                        rs.getInt("total_seats"),
                        rs.getInt("available_seats")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addTrain() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("INSERT INTO trains(train_no,train_name,total_seats,available_seats) VALUES(?,?,?,?)");
            pst.setString(1,noF.getText());
            pst.setString(2,nameF.getText());
            pst.setInt(3,Integer.parseInt(seatsF.getText()));
            pst.setInt(4,Integer.parseInt(seatsF.getText()));
            pst.executeUpdate();
            loadTrains();
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void updateTrain() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("UPDATE trains SET train_name=?, total_seats=?, available_seats=? WHERE train_no=?");
            pst.setString(1,nameF.getText());
            pst.setInt(2,Integer.parseInt(seatsF.getText()));
            pst.setInt(3,Integer.parseInt(seatsF.getText()));
            pst.setString(4,noF.getText());
            pst.executeUpdate();
            loadTrains();
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void deleteTrain() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("DELETE FROM trains WHERE train_no=?");
            pst.setString(1,noF.getText());
            pst.executeUpdate();
            loadTrains();
        } catch(Exception e){ e.printStackTrace(); }
    }
}
