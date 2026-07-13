import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Runner {

    static Scanner sc = new Scanner(System.in);
    static List<User> users = new ArrayList<>();

    static {
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword("admin123");

        Customer customer1 = new Customer();
        customer1.setUsername("rohit");
        customer1.setPassword("rohit123");
        // Starting balances
        // customer1.getAccount().deposit(1000);

        Customer customer2 = new Customer();
        customer2.setUsername("mohit");
        customer2.setPassword("mohit123");
        // Starting balances
        // customer2.getAccount().deposit(500);

        Customer customer3 = new Customer();
        customer3.setUsername("shobhit");
        customer3.setPassword("shobhit123");
        // Starting balances
       //  customer3.getAccount().deposit(750);

        users.add(admin);
        users.add(customer1);
        users.add(customer2);
        users.add(customer3);
    }

    public static void main(String[] args) {
        printMessage("Welcome to ABC Digital Bank");

        boolean programRunning = true;

        while (programRunning) {
            String loginResult = login();

            if (loginResult.equals("invalid")) {
                System.out.println("Invalid credentials.");
            } else if (loginResult.equals("admin")) {
                adminDashboard();
            } else {
                customerDashboard(loginResult);
            }

            System.out.print("\nDo you want to return to the login screen? Press y/n: ");
            String response = sc.nextLine();

            if (response.equalsIgnoreCase("n")) {
                programRunning = false;
            }
        }

        System.out.println("Thank you for using ABC Digital Bank.");
        sc.close();
    }

    static String login() {
        System.out.println("\nLogin");
        System.out.println("Enter your username and password separated by a space.");

        String loginInput = sc.nextLine().trim();
        String[] parts = loginInput.split("\\s+");

        if (parts.length != 2) {
            System.out.println("Please enter exactly one username and one password.");
            return "invalid";
        }

        String enteredUsername = parts[0];
        String enteredPassword = parts[1];

        for (User user : users) {
            if (enteredUsername.equals(user.getUsername())
                    && enteredPassword.equals(user.getPassword())) {

                if (user instanceof Admin) {
                    return "admin";
                }

                return user.getUsername();
            }
        }

        return "invalid";
    }

    private static void customerDashboard(String username) {
        Customer customer = findCustomer(username);

        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.println("\nWelcome customer, " + customer.getUsername());

        boolean running = true;

        while (running) {
            System.out.println("\n========== Customer Menu ==========");
            System.out.println("1. View Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    viewAccount(customer);
                    break;

                case "2":
                    deposit(customer);
                    break;

                case "3":
                    withdraw(customer);
                    break;

                case "4":
                    transfer(customer);
                    break;

                case "5":
                    running = false;
                    System.out.println("Logging out...");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void adminDashboard() {
        System.out.println("\nWelcome admin");

        boolean running = true;

        while (running) {
            System.out.println("\n========== Admin Menu ==========");
            System.out.println("1. Create Customer");
            System.out.println("2. View Customers");
            System.out.println("3. Update Customer");
            System.out.println("4. Delete Customer");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    createCustomer();
                    break;

                case "2":
                    viewCustomers();
                    break;

                case "3":
                    updateCustomer();
                    break;

                case "4":
                    deleteCustomer();
                    break;

                case "5":
                    running = false;
                    System.out.println("Logging out...");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static Customer findCustomer(String username) {
        for (User user : users) {
            if (user instanceof Customer
                    && user.getUsername().equalsIgnoreCase(username)) {

                return (Customer) user;
            }
        }

        return null;
    }

    private static void viewAccount(Customer customer) {
        Account account = customer.getAccount();

        System.out.println("\n----- Account Information -----");
        System.out.println("Username: " + customer.getUsername());
        System.out.println("Account type: " + account.getAccountType());
        System.out.println("Account number: " + account.getAccountNumber());
        System.out.printf("Balance: $%.2f%n", account.getBalance());
        System.out.printf(
                "Interest rate: %.2f%%%n",
                account.getInterestRate() * 100
        );
    }

    private static void deposit(Customer customer) {
        System.out.print("Enter deposit amount: $");

        try {
            double amount = Double.parseDouble(sc.nextLine());
            customer.getAccount().deposit(amount);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
        }
    }

    private static void withdraw(Customer customer) {
        System.out.print("Enter withdrawal amount: $");

        try {
            double amount = Double.parseDouble(sc.nextLine());
            customer.getAccount().withdraw(amount);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
        }
    }

    private static void transfer(Customer sender) {
        System.out.print("Enter receiver's username: ");
        String receiverUsername = sc.nextLine();

        Customer receiver = findCustomer(receiverUsername);

        if (receiver == null) {
            System.out.println("Receiver not found.");
            return;
        }

        if (sender == receiver) {
            System.out.println("You cannot transfer money to yourself.");
            return;
        }

        System.out.print("Enter transfer amount: $");

        try {
            double amount = Double.parseDouble(sc.nextLine());

            boolean successful = sender.getAccount().transfer(
                    receiver.getAccount(),
                    amount
            );

            if (successful) {
                System.out.println(
                        "Transfer to " + receiver.getUsername()
                                + " was successful."
                );
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
        }
    }

    private static void createCustomer() {
        System.out.print("Enter new username: ");
        String username = sc.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        if (usernameExists(username)) {
            System.out.println("That username already exists.");
            return;
        }

        System.out.print("Enter new password: ");
        String password = sc.nextLine();

        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        System.out.println("Choose account type:");
        System.out.println("1. Checking");
        System.out.println("2. Savings");
        System.out.print("Choice: ");

        String accountChoice = sc.nextLine();

        Account account;

        if (accountChoice.equals("2")) {
            account = new SavingsAccount();
        } else {
            account = new CheckingAccount();
        }

        Customer customer = new Customer(account);
        customer.setUsername(username);
        customer.setPassword(password);

        users.add(customer);

        System.out.println("Customer created successfully.");
        System.out.println("Account number: " + account.getAccountNumber());
    }

    private static boolean usernameExists(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }

        return false;
    }

    private static void viewCustomers() {
        System.out.println("\n---------- Customers ----------");

        boolean found = false;

        for (User user : users) {
            if (user instanceof Customer) {
                Customer customer = (Customer) user;

                System.out.println(
                        "Username: " + customer.getUsername()
                                + " | Account: "
                                + customer.getAccount().getAccountNumber()
                                + " | Type: "
                                + customer.getAccount().getAccountType()
                                + " | Balance: $"
                                + String.format(
                                "%.2f",
                                customer.getAccount().getBalance()
                        )
                );

                found = true;
            }
        }

        if (!found) {
            System.out.println("No customers found.");
        }
    }

    private static void updateCustomer() {
        System.out.print("Enter customer username: ");
        String username = sc.nextLine();

        Customer customer = findCustomer(username);

        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.print("Enter new password: ");
        String newPassword = sc.nextLine();

        if (newPassword.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        customer.setPassword(newPassword);
        System.out.println("Customer updated successfully.");
    }

    private static void deleteCustomer() {
        System.out.print("Enter username to delete: ");
        String username = sc.nextLine();

        Customer customer = findCustomer(username);

        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        users.remove(customer);
        System.out.println("Customer deleted successfully.");
    }

    static void printMessage(String message) {
        System.out.println(message);
    }
}

class Bank {
    private int id;
    private String name;
    private List<Customer> customers;

    public Bank(int id, String name) {
        this.id = id;
        this.name = name;
        this.customers = new ArrayList<>();
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void removeCustomer(Customer customer) {
        customers.remove(customer);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Customer> getCustomers() {
        return customers;
    }
}

abstract class User {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    abstract String getUserType();
}

class Admin extends User {
    @Override
    String getUserType() {
        return "admin";
    }
}

class Customer extends User {
    private Account account;

    public Customer() {
        this.account = new CheckingAccount();
    }

    public Customer(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    String getUserType() {
        return "customer";
    }
}

abstract class Account implements AccountOperations {
    private static int nextAccountNumber = 1001;

    private final int accountNumber;
    private double balance;

    public Account() {
        this.accountNumber = nextAccountNumber++;
        this.balance = 0.0;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    protected void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Deposit must be greater than zero.");
            return;
        }

        balance += amount;

        System.out.printf(
                "$%.2f deposited successfully.%n",
                amount
        );

        System.out.printf(
                "New balance: $%.2f%n",
                balance
        );
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal must be greater than zero.");
            return false;
        }

        if (amount > balance) {
            System.out.println("Insufficient funds.");
            return false;
        }

        balance -= amount;

        System.out.printf(
                "$%.2f withdrawn successfully.%n",
                amount
        );

        System.out.printf(
                "New balance: $%.2f%n",
                balance
        );

        return true;
    }

    @Override
    public boolean transfer(Account receiver, double amount) {
        if (receiver == null) {
            System.out.println("Receiver account does not exist.");
            return false;
        }

        if (amount <= 0) {
            System.out.println("Transfer amount must be greater than zero.");
            return false;
        }

        if (amount > balance) {
            System.out.println("Insufficient funds.");
            return false;
        }

        balance -= amount;
        receiver.balance += amount;

        System.out.printf(
                "$%.2f transferred successfully.%n",
                amount
        );

        System.out.printf(
                "Your new balance: $%.2f%n",
                balance
        );

        return true;
    }

    public abstract double getInterestRate();

    public abstract String getAccountType();
}

class CheckingAccount extends Account {
    @Override
    public double getInterestRate() {
        return 0.01;
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }
}

class SavingsAccount extends Account {
    @Override
    public double getInterestRate() {
        return 0.02;
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }
}

interface AccountOperations {
    void deposit(double amount);

    boolean withdraw(double amount);

    boolean transfer(Account receiver, double amount);
}