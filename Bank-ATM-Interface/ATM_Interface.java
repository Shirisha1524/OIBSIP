package Projects;

import java.util.*;
import java.text.SimpleDateFormat;

class Account {
    private String accountNumber;
    private String accountHolderName;
    private int pin;
    private double balance;
    private List<String> transactionHistory;
    private static int accountCounter = 1000;

    public Account(String accountHolderName, int pin, double initialDeposit) {
        this.accountNumber = "ACC" + (++accountCounter);
        this.accountHolderName = accountHolderName;
        this.pin = pin;
        this.balance = initialDeposit;
        this.transactionHistory = new ArrayList<>();
        addTransaction("Account created with initial deposit: $" + initialDeposit);
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String name) { this.accountHolderName = name; }
    public double getBalance() { return balance; }
    
    public boolean validatePin(int inputPin) {
        return this.pin == inputPin;
    }
    
    public boolean changePin(int oldPin, int newPin) {
        if (this.pin == oldPin) {
            this.pin = newPin;
            addTransaction("PIN changed successfully");
            return true;
        }
        return false;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Invalid withdrawal amount!");
            return false;
        }
        
        if (balance >= amount) {
            balance -= amount;
            addTransaction("Withdrawal: -$" + String.format("%.2f", amount));
            return true;
        }
        return false;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            addTransaction("Deposit: +$" + String.format("%.2f", amount));
        }
    }

    public boolean transfer(Account receiver, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid transfer amount!");
            return false;
        }
        
        if (this.balance >= amount) {
            this.balance -= amount;
            receiver.balance += amount;
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            this.addTransaction("Transfer to " + receiver.getAccountNumber() + ": -$" + String.format("%.2f", amount));
            receiver.addTransaction("Transfer from " + this.getAccountNumber() + ": +$" + String.format("%.2f", amount));
            
            return true;
        }
        return false;
    }

    private void addTransaction(String description) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        transactionHistory.add(timestamp + " - " + description + " | Balance: $" + String.format("%.2f", balance));
        
        // Keep only last 20 transactions
        if (transactionHistory.size() > 20) {
            transactionHistory.remove(0);
        }
    }

    public void printTransactionHistory() {
        System.out.println("\n=== Transaction History for Account: " + accountNumber + " ===");
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (String transaction : transactionHistory) {
                System.out.println(transaction);
            }
        }
        System.out.println("=".repeat(50));
    }
    
    public void printMiniStatement() {
        System.out.println("\n=== Mini Statement ===");
        System.out.println("Account Holder: " + accountHolderName);
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Current Balance: $" + String.format("%.2f", balance));
        System.out.println("\nRecent Transactions (Last 5):");
        
        int start = Math.max(0, transactionHistory.size() - 5);
        for (int i = start; i < transactionHistory.size(); i++) {
            System.out.println(transactionHistory.get(i));
        }
        System.out.println("=".repeat(50));
    }
}

public class ATM_Interface {
    private static Map<String, Account> accounts = new HashMap<>();
    private static Account currentAccount = null;
    private static Scanner sc = new Scanner(System.in);
    private static int loginAttempts = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public static void main(String[] args) {
        initializeSampleAccounts();
        
        while (true) {
            if (currentAccount == null) {
                showMainMenu();
            } else {
                showATMMenu();
            }
        }
    }

    private static void initializeSampleAccounts() {
        // Create some sample accounts
        accounts.put("ACC1001", new Account("John Doe", 1234, 5000.00));
        accounts.put("ACC1002", new Account("Jane Smith", 5678, 10000.00));
        accounts.put("ACC1003", new Account("Bob Johnson", 9999, 2500.00));
    }

    private static void showMainMenu() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║       WELCOME TO BANK ATM SYSTEM     ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║ 1. Login to Account                  ║");
        System.out.println("║ 2. Create New Account                ║");
        System.out.println("║ 3. Admin Login                       ║");
        System.out.println("║ 4. Exit                              ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("Choose option: ");
        
        try {
            int choice = sc.nextInt();
            sc.nextLine(); 
            
            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    createAccount();
                    break;
                case 3:
                    adminLogin();
                    break;
                case 4:
                    System.out.println("\nThank you for using our ATM. Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Please enter a valid number!");
            sc.nextLine();
        }
    }

    private static void login() {
        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            System.out.println("\n⚠ Too many failed attempts! Account locked.");
            System.out.println("Please contact bank administrator.");
            return;
        }

        System.out.print("\nEnter Account Number: ");
        String accNumber = sc.nextLine();
        
        System.out.print("Enter PIN: ");
        int pin = sc.nextInt();
        sc.nextLine();
        Account account = accounts.get(accNumber);
        
        if (account != null && account.validatePin(pin)) {
            currentAccount = account;
            loginAttempts = 0;
            System.out.println("\n✅ Login successful! Welcome, " + account.getAccountHolderName());
        } else {
            loginAttempts++;
            System.out.println("\n❌ Invalid credentials! Attempts left: " + (MAX_LOGIN_ATTEMPTS - loginAttempts));
        }
    }

    private static void createAccount() {
        System.out.println("\n=== CREATE NEW ACCOUNT ===");
        
        System.out.print("Enter Full Name: ");
        String name = sc.nextLine();
        
        int pin;
        while (true) {
            try {
                System.out.print("Set 4-digit PIN: ");
                pin = sc.nextInt();
                sc.nextLine(); // Consume newline
                
                if (pin < 1000 || pin > 9999) {
                    System.out.println("PIN must be 4 digits!");
                    continue;
                }
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter 4-digit number.");
                sc.nextLine();
            }
        }
        
        double initialDeposit;
        while (true) {
            try {
                System.out.print("Initial Deposit Amount ($): ");
                initialDeposit = sc.nextDouble();
                sc.nextLine(); 
                
                if (initialDeposit < 100) {
                    System.out.println("Minimum initial deposit is $100");
                    continue;
                }
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid amount!");
                sc.nextLine();
            }
        }
        
        Account newAccount = new Account(name, pin, initialDeposit);
        accounts.put(newAccount.getAccountNumber(), newAccount);
        
        System.out.println("\n✅ Account created successfully!");
        System.out.println("Account Number: " + newAccount.getAccountNumber());
        System.out.println("Account Holder: " + name);
        System.out.println("Initial Balance: $" + String.format("%.2f", initialDeposit));
        System.out.println("\n⚠ Please note your account number for future login.");
    }

    private static void adminLogin() {
        System.out.print("\nEnter Admin Username: ");
        String username = sc.nextLine();
        
        System.out.print("Enter Admin Password: ");
        String password = sc.nextLine();
        
        if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
            showAdminMenu();
        } else {
            System.out.println("❌ Invalid admin credentials!");
        }
    }

    private static void showAdminMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║           ADMIN DASHBOARD            ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║ 1. View All Accounts                 ║");
            System.out.println("║ 2. View Account Details              ║");
            System.out.println("║ 3. Reset User PIN                    ║");
            System.out.println("║ 4. Back to Main Menu                 ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Choose option: ");
            
            int choice = sc.nextInt();
            sc.nextLine();
            
            switch (choice) {
                case 1:
                    viewAllAccounts();
                    break;
                case 2:
                    viewAccountDetails();
                    break;
                case 3:
                    resetUserPin();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void viewAllAccounts() {
        System.out.println("\n=== ALL ACCOUNTS ===");
        System.out.printf("%-15s %-20s %-15s\n", "Account No.", "Account Holder", "Balance");
        System.out.println("-".repeat(50));
        
        for (Account acc : accounts.values()) {
            System.out.printf("%-15s %-20s $%-14.2f\n", 
                acc.getAccountNumber(), 
                acc.getAccountHolderName(), 
                acc.getBalance());
        }
    }

    private static void viewAccountDetails() {
        System.out.print("\nEnter Account Number to view: ");
        String accNum = sc.nextLine();
        
        Account acc = accounts.get(accNum);
        if (acc != null) {
            acc.printMiniStatement();
            acc.printTransactionHistory();
        } else {
            System.out.println("Account not found!");
        }
    }

    private static void resetUserPin() {
        System.out.print("\nEnter Account Number: ");
        String accNum = sc.nextLine();
        
        Account acc = accounts.get(accNum);
        if (acc != null) {
            System.out.print("Enter new 4-digit PIN: ");
            int newPin = sc.nextInt();
            sc.nextLine();
            System.out.println("PIN reset successful for account: " + accNum);
        } else {
            System.out.println("Account not found!");
        }
    }

    private static void showATMMenu() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║        ATM OPERATIONS MENU           ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║ 1. Withdraw                          ║");
        System.out.println("║ 2. Deposit                           ║");
        System.out.println("║ 3. Check Balance                     ║");
        System.out.println("║ 4. Transfer Funds                    ║");
        System.out.println("║ 5. Mini Statement                    ║");
        System.out.println("║ 6. Transaction History               ║");
        System.out.println("║ 7. Change PIN                        ║");
        System.out.println("║ 8. Logout                            ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("Choose operation: ");
        
        try {
            int choice = sc.nextInt();
            sc.nextLine(); // Consume newline
            
            switch (choice) {
                case 1:
                    withdrawMoney();
                    break;
                case 2:
                    depositMoney();
                    break;
                case 3:
                    checkBalance();
                    break;
                case 4:
                    transferFunds();
                    break;
                case 5:
                    currentAccount.printMiniStatement();
                    break;
                case 6:
                    currentAccount.printTransactionHistory();
                    break;
                case 7:
                    changePin();
                    break;
                case 8:
                    System.out.println("\nLogging out... Thank you!");
                    currentAccount = null;
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        } catch (InputMismatchException e) {
            System.out.println("Please enter a valid number!");
            sc.nextLine();
        }
    }

    private static void withdrawMoney() {
        System.out.print("\nEnter amount to withdraw: $");
        try {
            double amount = sc.nextDouble();
            sc.nextLine();
            
            if (currentAccount.withdraw(amount)) {
                System.out.println("\n✅ Transaction successful!");
                System.out.println("Please collect your cash: $" + String.format("%.2f", amount));
                System.out.println("Remaining balance: $" + String.format("%.2f", currentAccount.getBalance()));
            } else {
                System.out.println("\n❌ Transaction failed! Insufficient balance.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid amount!");
            sc.nextLine();
        }
    }

    private static void depositMoney() {
        System.out.print("\nEnter amount to deposit: $");
        try {
            double amount = sc.nextDouble();
            sc.nextLine();
            
            if (amount > 0) {
                currentAccount.deposit(amount);
                System.out.println("\n✅ Deposit successful!");
                System.out.println("New balance: $" + String.format("%.2f", currentAccount.getBalance()));
            } else {
                System.out.println("Invalid deposit amount!");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid amount!");
            sc.nextLine();
        }
    }

    private static void checkBalance() {
        System.out.println("\n=== ACCOUNT BALANCE ===");
        System.out.println("Account Holder: " + currentAccount.getAccountHolderName());
        System.out.println("Account Number: " + currentAccount.getAccountNumber());
        System.out.println("Available Balance: $" + String.format("%.2f", currentAccount.getBalance()));
        System.out.println("=".repeat(30));
    }

    private static void transferFunds() {
        System.out.print("\nEnter recipient's Account Number: ");
        String recipientAccNum = sc.nextLine();
        
        Account recipient = accounts.get(recipientAccNum);
        if (recipient == null) {
            System.out.println("❌ Recipient account not found!");
            return;
        }
        
        if (recipient.getAccountNumber().equals(currentAccount.getAccountNumber())) {
            System.out.println("❌ Cannot transfer to your own account!");
            return;
        }
        
        System.out.println("Recipient: " + recipient.getAccountHolderName());
        System.out.print("Enter transfer amount: $");
        
        try {
            double amount = sc.nextDouble();
            sc.nextLine();
            
            System.out.print("Confirm transfer? (Y/N): ");
            String confirm = sc.nextLine();
            
            if (confirm.equalsIgnoreCase("Y")) {
                if (currentAccount.transfer(recipient, amount)) {
                    System.out.println("\n✅ Transfer successful!");
                    System.out.println("Transferred: $" + String.format("%.2f", amount));
                    System.out.println("To: " + recipient.getAccountHolderName());
                    System.out.println("Your new balance: $" + String.format("%.2f", currentAccount.getBalance()));
                } else {
                    System.out.println("\n❌ Transfer failed! Insufficient balance.");
                }
            } else {
                System.out.println("Transfer cancelled.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid amount!");
            sc.nextLine();
        }
    }

    private static void changePin() {
        System.out.print("\nEnter current PIN: ");
        int currentPin = sc.nextInt();
        sc.nextLine();
        
        System.out.print("Enter new 4-digit PIN: ");
        int newPin = sc.nextInt();
        sc.nextLine();
        
        System.out.print("Confirm new PIN: ");
        int confirmPin = sc.nextInt();
        sc.nextLine();
        
        if (newPin != confirmPin) {
            System.out.println("❌ New PINs don't match!");
            return;
        }
        
        if (newPin < 1000 || newPin > 9999) {
            System.out.println("❌ PIN must be 4 digits!");
            return;
        }
        
        if (currentAccount.changePin(currentPin, newPin)) {
            System.out.println("✅ PIN changed successfully!");
        } else {
            System.out.println("❌ Current PIN is incorrect!");
        }
    }
}