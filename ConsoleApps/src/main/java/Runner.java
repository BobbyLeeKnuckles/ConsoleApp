import java.util.*;

//Admin credentials: admin, admin123
//User credentials: .....................

public class Runner {
    static Scanner sc = new Scanner(System.in);

    static List<User> users = new ArrayList<>();

    static{
        User admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword("admin123");

        User customer1 = new Customer();
        customer1.setUsername("rohit");
        customer1.setPassword("rohit123");

        User customer2 = new Customer();
        customer2.setUsername("mohit");
        customer2.setPassword("mohit123");

        User customer3 = new Customer();
        customer3.setUsername("shobhit");
        customer3.setPassword("shobhit123");

        users.add(admin);
        users.add(customer1); users.add(customer2); users.add(customer3);
    }


    public static void main(String[] args) {
        printMessage("Welcome to ABC Digital Bank");


        boolean flag = true;
        while(flag){
            String loginResult = login();

            if(loginResult.equals("invalid")){
                //exception-handling
                System.out.println("Invalid Credentials");
            }else if (loginResult.equals("admin")){
                adminDashboard(loginResult);
            }else{
                customerDashboard(loginResult);
            }

            System.out.println("Do you want to continue? Press y/n");
            String mainLoopUserResponse = sc.nextLine();
            if(mainLoopUserResponse.equalsIgnoreCase("n")){
                flag = false;
            }
        }

    }

    private static void customerDashboard(String username) {
        System.out.println("Welcome customer, " + username);
        //todo: using switch-case present customer options'
        //view his account or accounts, withdraw, transfer, deposit

    }

    private static void adminDashboard(String loginResult) {
        System.out.println("Welcome admin");
        //todo: using switch-case present admin options
        //CRUD for customers, accounts etc
    }

    static String login(){
        String loginType = "invalid";
        System.out.println("Please enter your username and password separated by a space");//rohit rohit123
        //validation
        String enteredUsernamePassword = sc.nextLine();
        String[] parts =enteredUsernamePassword.split(" ");
        String enteredUsername = parts[0];
        String entetedPassword = parts[1];

        if(enteredUsername.equals("admin") && entetedPassword.equals("admin123")){
            loginType = "admin";
        }else{
            for(int i=0; i<users.size(); i++){
                User user = users.get(i);
                if(enteredUsername.equals(user.getUsername()) && entetedPassword.equals(user.getPassword())){
                    loginType = user.getUsername();
                    break;
                }
            }
        }

        return loginType;
    }

    static void printMessage(String message){
        System.out.println(message);
    }
}

class Bank{
    private int id;
    private String name;
    private List<Customer> customers = new ArrayList<>();

    public Bank(int id, String name, List<Customer> customers) {
        this.id = id;
        this.name = name;
        this.customers = customers;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}

abstract class User{
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

class Admin extends User{
    String getUserType() {
        return "admin";
    }
}

class Customer extends User{
    String getUserType() {
        return "customer";
    }
}



abstract class Account{
  //id,
    // balance
}

class CheckingAccount extends Account implements AccountOperations{
    @Override
    public void deposit() {

    }

    @Override
    public void withdraw() {

    }

    @Override
    public void transfer() {

    }
    //getinterestRate()// 1%
}

class SavingsAccount extends Account implements AccountOperations{
    @Override
    public void deposit() {

    }

    @Override
    public void withdraw() {

    }

    @Override
    public void transfer() {

    }
    //getinterestRate()// 2%
}

interface AccountOperations{
    void deposit();
    void withdraw();
    void transfer();
}

