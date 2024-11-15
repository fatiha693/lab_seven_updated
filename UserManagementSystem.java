import java.io.*;
import java.util.*;

// Simple interfaces for different user types (ISP)
interface BasicUserActions {
    List<String> viewUserList();  // Simplified to just return usernames
}

interface PowerUserActions extends BasicUserActions {
    boolean addNewUser(String username, String email);
}

interface AdminActions extends PowerUserActions {
    boolean changeUserType(String username, String newType);
    boolean updateSystem(String setting);
}

// Simple User class
class User {
    String username;
    String email;
    String password;
    String type;  // "basic", "power", or "admin"

    User(String username, String email, String password, String type) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.type = type;
    }
}

// Main system class using Singleton pattern
public class UserManagementSystem {
    // Singleton instance
    private static UserManagementSystem onlyInstance;
    
    // Lists to store users
    private ArrayList<User> normalUsers;
    private ArrayList<User> adminUsers;
    
    // File names
    private static final String USERS_FILE = "Users.csv";
    private static final String ADMIN_FILE = "Admin.csv";

    // Private constructor (Singleton pattern)
    private UserManagementSystem() {
        normalUsers = new ArrayList<>();
        adminUsers = new ArrayList<>();
        loadUsersFromFiles();
    }

    // Get the single instance (Singleton pattern)
    public static UserManagementSystem getInstance() {
        if (onlyInstance == null) {
            onlyInstance = new UserManagementSystem();
        }
        return onlyInstance;
    }

    // Load users from CSV files
    private void loadUsersFromFiles() {
        try {
            // Load normal users
            BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE));
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                normalUsers.add(new User(parts[0], parts[1], parts[2], parts[3]));
            }
            reader.close();

            // Load admin users
            reader = new BufferedReader(new FileReader(ADMIN_FILE));
            line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                adminUsers.add(new User(parts[0], parts[1], parts[2], "admin"));
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Error reading files: " + e.getMessage());
        }
    }

    // Different user type implementations
    class BasicUser implements BasicUserActions {
        private User user;

        BasicUser(User user) {
            this.user = user;
        }

        public List<String> viewUserList() {
            List<String> usernames = new ArrayList<>();
            for (User u : normalUsers) {
                usernames.add(u.username);
            }
            return usernames;
        }
    }

    class PowerUser implements PowerUserActions {
        private User user;

        PowerUser(User user) {
            this.user = user;
        }

        public List<String> viewUserList() {
            List<String> usernames = new ArrayList<>();
            for (User u : normalUsers) {
                usernames.add(u.username);
            }
            return usernames;
        }

        public boolean addNewUser(String username, String email) {
            // Check if username already exists
            for (User u : normalUsers) {
                if (u.username.equals(username)) {
                    return false;
                }
            }
            normalUsers.add(new User(username, email, "default123", "basic"));
            saveUsersToFile();
            return true;
        }
    }

    class AdminUser implements AdminActions {
        private User user;

        AdminUser(User user) {
            this.user = user;
        }

        public List<String> viewUserList() {
            List<String> usernames = new ArrayList<>();
            for (User u : normalUsers) {
                usernames.add(u.username);
            }
            return usernames;
        }

        public boolean addNewUser(String username, String email) {
            // Check if username already exists
            for (User u : normalUsers) {
                if (u.username.equals(username)) {
                    return false;
                }
            }
            normalUsers.add(new User(username, email, "default123", "basic"));
            saveUsersToFile();
            return true;
        }

        public boolean changeUserType(String username, String newType) {
            for (User u : normalUsers) {
                if (u.username.equals(username)) {
                    u.type = newType;
                    saveUsersToFile();
                    return true;
                }
            }
            return false;
        }

        public boolean updateSystem(String setting) {
            // Simple implementation
            System.out.println("System setting updated: " + setting);
            return true;
        }
    }

    // Save users to file
    private void saveUsersToFile() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE));
            writer.println("Username,Email,Password,Type");
            for (User u : normalUsers) {
                writer.println(u.username + "," + u.email + "," + u.password + "," + u.type);
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error saving to file: " + e.getMessage());
        }
    }

    // Get appropriate user interface based on user type
    public Object getUserInterface(String username, String password) {
        // Check normal users
        for (User u : normalUsers) {
            if (u.username.equals(username) && u.password.equals(password)) {
                if (u.type.equals("basic")) {
                    return new BasicUser(u);
                } else if (u.type.equals("power")) {
                    return new PowerUser(u);
                }
            }
        }
        
        // Check admin users
        for (User u : adminUsers) {
            if (u.username.equals(username) && u.password.equals(password)) {
                return new AdminUser(u);
            }
        }
        return null;
    }

    // Main method to test the system
    public static void main(String[] args) {
        // Get the system instance
        UserManagementSystem system = UserManagementSystem.getInstance();

        // Test with different user types
        System.out.println("Testing User Management System:");

        // Test basic user
        Object userInterface = system.getUserInterface("basic_user", "password123");
        if (userInterface instanceof BasicUserActions) {
            BasicUserActions basicUser = (BasicUserActions) userInterface;
            System.out.println("Basic user viewing list: " + basicUser.viewUserList());
        }

        // Test power user
        userInterface = system.getUserInterface("power_user", "password123");
        if (userInterface instanceof PowerUserActions) {
            PowerUserActions powerUser = (PowerUserActions) userInterface;
            System.out.println("Power user adding new user: " + 
                powerUser.addNewUser("newuser", "new@email.com"));
        }

        // Test admin user
        userInterface = system.getUserInterface("admin", "admin123");
        if (userInterface instanceof AdminActions) {
            AdminActions adminUser = (AdminActions) userInterface;
            System.out.println("Admin changing user type: " + 
                adminUser.changeUserType("newuser", "power"));
        }
    }
}