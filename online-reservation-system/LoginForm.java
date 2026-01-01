package reservation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame implements ActionListener {

    private JTextField userF;
    private JPasswordField passF;
    private JButton loginBtn;

    public LoginForm() {
        setTitle("Login");
        setSize(300,200);
        setLayout(new GridLayout(3,2,5,5));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(new JLabel("Username:"));
        userF = new JTextField(); add(userF);

        add(new JLabel("Password:"));
        passF = new JPasswordField(); add(passF);

        loginBtn = new JButton("Login");
        loginBtn.addActionListener(this);
        add(new JLabel());
        add(loginBtn);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String user = userF.getText().trim();
        String pass = new String(passF.getPassword());

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement(
                "SELECT role FROM users WHERE username=? AND password=?"
            );
            pst.setString(1, user);
            pst.setString(2, pass);
            ResultSet rs = pst.executeQuery();
            if(rs.next()) {
                String role = rs.getString("role");
                new MainMenu(user, role);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,"Invalid username or password");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LoginForm();
    }
}
