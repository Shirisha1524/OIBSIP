package Projects;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

// Main class
public class OnlineExamSystem {
    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;
    private static final String USER_FILE = "users.txt";
    private static final String QUESTIONS_FILE = "questions.txt";
    private static final String RESULTS_FILE = "results.txt";
    private static final int EXAM_DURATION = 60; 

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println("   ONLINE EXAMINATION SYSTEM");
        System.out.println("====================================");
        
        // Create sample data files
        createSampleData();
        
        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private static void showLoginMenu() {
        System.out.println("\n--- Login Menu ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose option: ");
        
        // Handle non-integer input
        if (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number (1, 2, or 3)");
            scanner.nextLine(); // Clear invalid input
            return;
        }
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            case 3:
                System.out.println("Thank you for using the system. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice! Please select 1, 2, or 3.");
        }
    }

    private static void showMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("Welcome, " + currentUser.username + "!");
        System.out.println("1. Take Exam");
        System.out.println("2. Update Profile");
        System.out.println("3. Change Password");
        System.out.println("4. View Previous Results");
        System.out.println("5. Logout");
        System.out.print("Choose option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                startExam();
                break;
            case 2:
                updateProfile();
                break;
            case 3:
                changePassword();
                break;
            case 4:
                viewResults();
                break;
            case 5:
                logout();
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        currentUser = authenticate(username, password);
        if (currentUser == null) {
            System.out.println("Invalid credentials!");
            // Debug: Show available users
            System.out.println("\nAvailable users in system:");
            List<String> users = readFile(USER_FILE);
            for (String user : users) {
                System.out.println(user.split(",")[0]);
            }
        } else {
            System.out.println("Login successful!");
        }
    }

    private static void register() {
        System.out.println("\n--- User Registration ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        // Check if username exists
        if (userExists(username)) {
            System.out.println("Username already exists!");
            return;
        }
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine();
        
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();
        
        User newUser = new User(username, password, fullName, email, phone, "student");
        saveUser(newUser);
        
        System.out.println("Registration successful! You can now login.");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
    }

    private static void startExam() {
        System.out.println("\n--- Starting Exam ---");
        System.out.println("You have " + EXAM_DURATION + " seconds to complete the exam.");
        System.out.println("The exam will auto-submit when time expires.");
        System.out.println("Type 'S' to submit manually before time ends.");
        System.out.print("Press Enter to start...");
        scanner.nextLine();
        
        // Load questions
        List<Question> questions = loadQuestions();
        if (questions.isEmpty()) {
            System.out.println("No questions available!");
            return;
        }
        
        // Create exam session
        ExamSession session = new ExamSession(currentUser);
        session.startTime = System.currentTimeMillis();
        
        // Start timer thread
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> timer = executor.schedule(() -> {
            System.out.println("\n\nTime's up! Auto-submitting exam...");
            submitExam(session, questions);
        }, EXAM_DURATION, TimeUnit.SECONDS);
        
        // Display questions
        for (Question question : questions) {
            if (session.submitted) break;
            
            question.displayQuestion();
            System.out.print("Your answer (A/B/C/D or S to submit): ");
            String answer = scanner.nextLine().toUpperCase();
            
            if (answer.equals("S")) {
                timer.cancel(false);
                submitExam(session, questions);
                break;
            }
            
            if (answer.length() == 1 && "ABCD".contains(answer)) {
                session.answers.put(question.id, answer.charAt(0));
            } else {
                System.out.println("Invalid answer! Skipping question.");
            }
        }
        
        // If not submitted yet, submit now
        if (!session.submitted) {
            timer.cancel(false);
            submitExam(session, questions);
        }
        
        executor.shutdown();
    }

    private static void updateProfile() {
        System.out.println("\n--- Update Profile ---");
        
        System.out.print("Full Name (" + currentUser.fullName + "): ");
        String fullName = scanner.nextLine();
        if (!fullName.isEmpty()) {
            currentUser.fullName = fullName;
        }
        
        System.out.print("Email (" + currentUser.email + "): ");
        String email = scanner.nextLine();
        if (!email.isEmpty()) {
            currentUser.email = email;
        }
        
        System.out.print("Phone (" + currentUser.phone + "): ");
        String phone = scanner.nextLine();
        if (!phone.isEmpty()) {
            currentUser.phone = phone;
        }
        
        updateUserInFile();
        System.out.println("Profile updated successfully!");
    }

    private static void changePassword() {
        System.out.println("\n--- Change Password ---");
        
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();
        
        if (!currentPassword.equals(currentUser.password)) {
            System.out.println("Current password is incorrect!");
            return;
        }
        
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match!");
            return;
        }
        
        if (newPassword.length() < 6) {
            System.out.println("Password must be at least 6 characters long!");
            return;
        }
        
        currentUser.password = newPassword;
        updateUserInFile();
        System.out.println("Password changed successfully!");
    }

    private static void viewResults() {
        System.out.println("\n--- Exam Results ---");
        List<String> results = readFile(RESULTS_FILE);
        
        boolean found = false;
        for (String result : results) {
            String[] parts = result.split(",");
            if (parts[0].equals(currentUser.username)) {
                System.out.println("Exam Date: " + parts[1]);
                System.out.println("Score: " + parts[2] + "/" + parts[3]);
                System.out.println("Time Taken: " + parts[4] + " seconds");
                System.out.println("----------------------");
                found = true;
            }
        }
        
        if (!found) {
            System.out.println("No exam results found!");
        }
    }

    private static void logout() {
        System.out.println("Logging out...");
        currentUser = null;
    }

    private static void submitExam(ExamSession session, List<Question> questions) {
        session.endTime = System.currentTimeMillis();
        
        // Calculate score
        int score = 0;
        int totalMarks = 0;
        
        for (Question question : questions) {
            totalMarks += question.marks;
            Character answer = session.answers.get(question.id);
            if (answer != null && answer == question.correctAnswer) {
                score += question.marks;
            }
        }
        
        session.score = score;
        session.submitted = true;
        
        // Display result
        System.out.println("\n--- Exam Results ---");
        System.out.println("Total Questions: " + questions.size());
        System.out.println("Questions Attempted: " + session.answers.size());
        System.out.println("Score: " + score + "/" + totalMarks);
        System.out.println("Percentage: " + (score * 100 / totalMarks) + "%");
        System.out.println("Time Taken: " + ((session.endTime - session.startTime) / 1000) + " seconds");
        
        // Save result
        saveResult(session, totalMarks);
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    // Helper methods
    private static User authenticate(String username, String password) {
        List<String> userData = readFile(USER_FILE);
        
        for (String line : userData) {
            String[] parts = line.split(",");
            if (parts.length >= 2 && parts[0].equals(username) && parts[1].equals(password)) {
                return new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
            }
        }
        return null;
    }

    private static boolean userExists(String username) {
        List<String> userData = readFile(USER_FILE);
        for (String line : userData) {
            String[] parts = line.split(",");
            if (parts.length > 0 && parts[0].equals(username)) {
                return true;
            }
        }
        return false;
    }

    private static void saveUser(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            writer.write(user.toString());
            writer.newLine();
            writer.flush(); // Force write to disk immediately
        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }

    private static void updateUserInFile() {
        List<String> users = readFile(USER_FILE);
        List<String> updatedUsers = new ArrayList<>();
        
        for (String userLine : users) {
            String[] parts = userLine.split(",");
            if (parts.length >= 1 && parts[0].equals(currentUser.username)) {
                updatedUsers.add(currentUser.toString());
            } else {
                updatedUsers.add(userLine);
            }
        }
        
        writeFile(USER_FILE, updatedUsers);
    }

    private static List<Question> loadQuestions() {
        List<Question> questions = new ArrayList<>();
        List<String> questionData = readFile(QUESTIONS_FILE);
        
        int id = 1;
        for (String line : questionData) {
            String[] parts = line.split("\\|");
            if (parts.length >= 7) {
                questions.add(new Question(
                    id++,
                    parts[0],
                    parts[1],
                    parts[2],
                    parts[3],
                    parts[4],
                    parts[5].charAt(0),
                    Integer.parseInt(parts[6])
                ));
            }
        }
        return questions;
    }

    private static void saveResult(ExamSession session, int totalMarks) {
        String result = session.user.username + "," +
                       new Date() + "," +
                       session.score + "," +
                       totalMarks + "," +
                       ((session.endTime - session.startTime) / 1000);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESULTS_FILE, true))) {
            writer.write(result);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving result: " + e.getMessage());
        }
    }

    private static void createSampleData() {
        // Create users file if not exists
        File userFile = new File(USER_FILE);
        if (!userFile.exists() || userFile.length() == 0) {
            try {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
                    writer.write("admin,admin123,Administrator,admin@examsystem.com,1234567890,admin");
                    writer.newLine();
                    writer.write("student1,pass123,John Doe,john@example.com,9876543210,student");
                    writer.newLine();
                    writer.write("student2,pass456,Jane Smith,jane@example.com,9123456780,student");
                    writer.newLine();
                }
                System.out.println("Sample users created.");
            } catch (IOException e) {
                System.out.println("Error creating user file: " + e.getMessage());
            }
        }

        // Create questions file if not exists
        File questionFile = new File(QUESTIONS_FILE);
        if (!questionFile.exists() || questionFile.length() == 0) {
            try {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(QUESTIONS_FILE))) {
                    writer.write("What is the capital of France?|Paris|London|Berlin|Madrid|A|5");
                    writer.newLine();
                    writer.write("Which programming language is this exam written in?|Python|Java|C++|JavaScript|B|5");
                    writer.newLine();
                    writer.write("What does OOP stand for?|Object-Oriented Programming|Object-Oriented Process|Object-Oriented Protocol|Object-Oriented Principle|A|10");
                    writer.newLine();
                    writer.write("Which data structure uses LIFO?|Queue|Stack|Array|Linked List|B|10");
                    writer.newLine();
                    writer.write("What is the default value of int in Java?|0|1|null|undefined|A|5");
                }
                System.out.println("Sample questions created.");
            } catch (IOException e) {
                System.out.println("Error creating questions file: " + e.getMessage());
            }
        }

        // Create results file if not exists
        File resultFile = new File(RESULTS_FILE);
        if (!resultFile.exists()) {
            try {
                resultFile.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating results file: " + e.getMessage());
            }
        }
    }

    private static List<String> readFile(String filename) {
        List<String> lines = new ArrayList<>();
        File file = new File(filename);
        
        if (!file.exists()) {
            // If file doesn't exist, create it
            try {
                file.createNewFile();
                return lines;
            } catch (IOException e) {
                System.out.println("Error creating file: " + filename);
                return lines;
            }
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + filename);
        }
        return lines;
    }

    private static void writeFile(String filename, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }
}

// Model Classes
class User {
    String username;
    String password;
    String fullName;
    String email;
    String phone;
    String role;
    
    public User(String username, String password, String fullName, String email, String phone, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }
    
    @Override
    public String toString() {
        return username + "," + password + "," + fullName + "," + email + "," + phone + "," + role;
    }
}

class Question {
    int id;
    String questionText;
    String optionA;
    String optionB;
    String optionC;
    String optionD;
    char correctAnswer;
    int marks;
    
    public Question(int id, String questionText, String optionA, String optionB, 
                   String optionC, String optionD, char correctAnswer, int marks) {
        this.id = id;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.marks = marks;
    }
    
    public void displayQuestion() {
        System.out.println("\nQ" + id + ": " + questionText);
        System.out.println("A) " + optionA);
        System.out.println("B) " + optionB);
        System.out.println("C) " + optionC);
        System.out.println("D) " + optionD);
        System.out.println("Marks: " + marks);
    }
}

class ExamSession {
    User user;
    Map<Integer, Character> answers;
    int score;
    long startTime;
    long endTime;
    boolean submitted;
    
    public ExamSession(User user) {
        this.user = user;
        this.answers = new HashMap<>();
        this.score = 0;
        this.submitted = false;
    }
}