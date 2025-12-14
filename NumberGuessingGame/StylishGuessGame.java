package Projects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class StylishGuessGame extends JFrame implements ActionListener {

    private int randomNumber;
    private int attempts;
    private int maxAttempts = 10;
    private int score = 0;
    private int totalScore = 0;

    private JLabel messageLabel, attemptsLabel, scoreLabel;
    private JTextField guessField;
    private JButton guessButton, restartButton;

    public StylishGuessGame() {
        setTitle("🎮 Stylish Number Guessing Game");
        setSize(450, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {}

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(72, 61, 139),  
                        0, getHeight(), new Color(123, 104, 238)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setLayout(null);

        messageLabel = new JLabel("Guess a number between 1 and 100");
        messageLabel.setBounds(40, 20, 380, 30);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        guessField = new JTextField();
        guessField.setBounds(150, 80, 150, 35);
        guessField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        guessField.setHorizontalAlignment(JTextField.CENTER);
        
        guessButton = new JButton("Guess");
        styleButton(guessButton);
        guessButton.setBounds(170, 130, 110, 40);
        guessButton.addActionListener(this);

        attemptsLabel = new JLabel("Attempts Left: " + maxAttempts);
        attemptsLabel.setBounds(40, 190, 180, 30);
        attemptsLabel.setForeground(Color.WHITE);
        attemptsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(270, 190, 150, 30);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        restartButton = new JButton("Restart");
        styleButton(restartButton);
        restartButton.setBounds(170, 240, 110, 40);
        restartButton.addActionListener(e -> restartGame());

        panel.add(messageLabel);
        panel.add(guessField);
        panel.add(guessButton);
        panel.add(attemptsLabel);
        panel.add(scoreLabel);
        panel.add(restartButton);

        add(panel);
        startNewRound();
    }

    private void styleButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setBackground(new Color(255, 255, 255));
        btn.setForeground(new Color(72, 61, 139));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void startNewRound() {
        randomNumber = new Random().nextInt(100) + 1;
        attempts = 0;
        score = 0;

        messageLabel.setText("Guess a number between 1 and 100");
        attemptsLabel.setText("Attempts Left: " + maxAttempts);
        scoreLabel.setText("Score: " + totalScore);
        guessField.setText("");
        guessField.setEditable(true);
        guessButton.setEnabled(true);
    }

    private void restartGame() {
        totalScore = 0;
        startNewRound();
    }

    public void actionPerformed(ActionEvent e) {
        String input = guessField.getText();

        try {
            int userGuess = Integer.parseInt(input);

            attempts++;
            attemptsLabel.setText("Attempts Left: " + (maxAttempts - attempts));

            if (userGuess == randomNumber) {
                score = (maxAttempts - attempts + 1) * 10;
                totalScore += score;
                messageLabel.setText("\u221A  Correct! Number was " + randomNumber);
                scoreLabel.setText("Score: " + totalScore);

                guessButton.setEnabled(false);
                guessField.setEditable(false);
            } else if (userGuess > randomNumber) {
                messageLabel.setText("\u2193 Too High! Try Lower.");
            } else {
                messageLabel.setText("\u2191 Too Low! Try Higher.");
            }

            if (attempts >= maxAttempts && userGuess != randomNumber) {
                messageLabel.setText("\u2718 Out of attempts! Number was " + randomNumber);
                guessButton.setEnabled(false);
                guessField.setEditable(false);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StylishGuessGame().setVisible(true));
    }
}
