package reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JFrame {

    private JTable trainTable, reservationTable;
    private DefaultTableModel trainModel, reservationModel;
    private JTextField noF, nameF, seatsF, acF, sleeperF, searchPNRF;
    private JButton addBtn, updateBtn, deleteBtn, searchBtn, historyBtn;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(1050,700);
        setLayout(null);

        // ------------------- Train Table -------------------
        trainModel = new DefaultTableModel();
        trainModel.setColumnIdentifiers(new String[]{"Train No","Train Name","Total Seats","Available Seats","AC Seats","Sleeper Seats"});
        trainTable = new JTable(trainModel);
        JScrollPane trainScroll = new JScrollPane(trainTable);
        trainScroll.setBounds(10,10,1000,200);
        add(trainScroll);
        loadTrains();

        // ------------------- Train Management Panel -------------------
        JPanel trainPanel = new JPanel(new FlowLayout());
        trainPanel.setBounds(10,220,1000,50);
        noF = new JTextField(6); nameF = new JTextField(10);
        seatsF = new JTextField(5); acF = new JTextField(5); sleeperF = new JTextField(5);
        addBtn = new JButton("Add"); updateBtn = new JButton("Update"); deleteBtn = new JButton("Delete");

        trainPanel.add(new JLabel("No:")); trainPanel.add(noF);
        trainPanel.add(new JLabel("Name:")); trainPanel.add(nameF);
        trainPanel.add(new JLabel("Total:")); trainPanel.add(seatsF);
        trainPanel.add(new JLabel("AC:")); trainPanel.add(acF);
        trainPanel.add(new JLabel("Sleeper:")); trainPanel.add(sleeperF);
        trainPanel.add(addBtn); trainPanel.add(updateBtn); trainPanel.add(deleteBtn);
        add(trainPanel);

        addBtn.addActionListener(e -> addTrain());
        updateBtn.addActionListener(e -> updateTrain());
        deleteBtn.addActionListener(e -> deleteTrain());

        trainTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = trainTable.getSelectedRow();
                if(row >= 0){
                    noF.setText(trainTable.getValueAt(row,0).toString());
                    nameF.setText(trainTable.getValueAt(row,1).toString());
                    seatsF.setText(trainTable.getValueAt(row,2).toString());
                    acF.setText(trainTable.getValueAt(row,4).toString());
                    sleeperF.setText(trainTable.getValueAt(row,5).toString());
                }
            }
        });

        // ------------------- Reservation Table -------------------
        reservationModel = new DefaultTableModel();
        reservationModel.setColumnIdentifiers(new String[]{"PNR","Name","Train","Class","From","To","Date","Status","Booked By"});
        reservationTable = new JTable(reservationModel);
        JScrollPane resScroll = new JScrollPane(reservationTable);
        resScroll.setBounds(10,280,1000,300);
        add(resScroll);
        loadReservations();

        // ------------------- PNR Search Panel -------------------
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.setBounds(10,590,1000,40);
        searchPNRF = new JTextField(10);
        searchBtn = new JButton("Search PNR");
        historyBtn = new JButton("View Booking History");
        searchPanel.add(new JLabel("PNR:")); searchPanel.add(searchPNRF);
        searchPanel.add(searchBtn); searchPanel.add(historyBtn);
        add(searchPanel);

        searchBtn.addActionListener(e -> searchPNR());
        historyBtn.addActionListener(e -> viewBookingHistory());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ------------------- Load Data -------------------
    private void loadTrains() {
        try(Connection con = DBConnection.getConnection()) {
            trainModel.setRowCount(0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM trains");
            while(rs.next()) {
                trainModel.addRow(new Object[]{
                        rs.getString("train_no"),
                        rs.getString("train_name"),
                        rs.getInt("total_seats"),
                        rs.getInt("available_seats"),
                        rs.getInt("available_ac"),
                        rs.getInt("available_sleeper")
                });
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void loadReservations() {
        try(Connection con = DBConnection.getConnection()) {
            reservationModel.setRowCount(0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM reservations");
            while(rs.next()) {
                reservationModel.addRow(new Object[]{
                        rs.getString("pnr"),
                        rs.getString("name"),
                        rs.getString("train_name"),
                        rs.getString("class_type"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getDate("journey_date"),
                        rs.getString("status"),
                        rs.getString("created_by")
                });
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    // ------------------- Train Actions -------------------
    private void addTrain() {
        try(Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO trains(train_no,train_name,total_seats,available_seats,available_ac,available_sleeper) VALUES(?,?,?,?,?,?)"
            );
            pst.setString(1,noF.getText()); pst.setString(2,nameF.getText());
            pst.setInt(3,Integer.parseInt(seatsF.getText())); pst.setInt(4,Integer.parseInt(seatsF.getText()));
            pst.setInt(5,Integer.parseInt(acF.getText())); pst.setInt(6,Integer.parseInt(sleeperF.getText()));
            pst.executeUpdate(); loadTrains();
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void updateTrain() {
        try(Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE trains SET train_name=?, total_seats=?, available_seats=?, available_ac=?, available_sleeper=? WHERE train_no=?"
            );
            pst.setString(1,nameF.getText());
            pst.setInt(2,Integer.parseInt(seatsF.getText()));
            pst.setInt(3,Integer.parseInt(seatsF.getText()));
            pst.setInt(4,Integer.parseInt(acF.getText()));
            pst.setInt(5,Integer.parseInt(sleeperF.getText()));
            pst.setString(6,noF.getText());
            pst.executeUpdate(); loadTrains();
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void deleteTrain() {
        try(Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("DELETE FROM trains WHERE train_no=?");
            pst.setString(1,noF.getText());
            pst.executeUpdate(); loadTrains();
        } catch(Exception e){ e.printStackTrace(); }
    }

    // ------------------- PNR Search -------------------
    private void searchPNR() {
        String pnr = searchPNRF.getText().trim();
        if(pnr.isEmpty()){ JOptionPane.showMessageDialog(this,"Enter PNR"); return; }
        try(Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM reservations WHERE pnr=?");
            pst.setString(1,pnr);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                JOptionPane.showMessageDialog(this,
                        "PNR: "+rs.getString("pnr")+"\n"+
                        "Name: "+rs.getString("name")+"\n"+
                        "Train: "+rs.getString("train_name")+"\n"+
                        "Class: "+rs.getString("class_type")+"\n"+
                        "From: "+rs.getString("source")+"\n"+
                        "To: "+rs.getString("destination")+"\n"+
                        "Date: "+rs.getDate("journey_date")+"\n"+
                        "Status: "+rs.getString("status")+"\n"+
                        "Booked By: "+rs.getString("created_by")
                );
            } else {
                JOptionPane.showMessageDialog(this,"PNR not found");
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    // ------------------- Booking History -------------------
    private void viewBookingHistory() {
        try(Connection con = DBConnection.getConnection()) {
            JTable historyTable = new JTable();
            DefaultTableModel historyModel = new DefaultTableModel();
            historyModel.setColumnIdentifiers(new String[]{"PNR","Name","Train","Class","From","To","Date","Status","Booked By"});
            historyTable.setModel(historyModel);

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM reservations ORDER BY booking_time DESC");
            while(rs.next()) {
                historyModel.addRow(new Object[]{
                        rs.getString("pnr"),
                        rs.getString("name"),
                        rs.getString("train_name"),
                        rs.getString("class_type"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getDate("journey_date"),
                        rs.getString("status"),
                        rs.getString("created_by")
                });
            }

            JScrollPane scroll = new JScrollPane(historyTable);
            scroll.setPreferredSize(new Dimension(900,400));
            JOptionPane.showMessageDialog(this, scroll, "Booking History", JOptionPane.INFORMATION_MESSAGE);

        } catch(Exception e){ e.printStackTrace(); }
    }
}
