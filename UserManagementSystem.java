import java.io.*;
import java.util.*;


interface FileReadable {
    void readFromFile(String filename);
}

interface FileWriteable {
    void writeToFile(String filename);
}


interface BasicUserActions {
    List<String> viewUserList();
}

interface PowerUserActions extends BasicUserActions {
    boolean addNewUser(String username, String email);
}

interface AdminActions extends PowerUserActions {
    boolean changeUserType(String username, String newType);
    boolean updateSystem(String setting);
}


class User implements FileReadable, FileWriteable {
    String username;
    String email;
    String password;
    String type;

    User(String username, String email, String password, String type) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.type = type;
    }

    @Override
    public void readFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(this.username)) {
                    this.email = parts[1];
                    this.password = parts[2];
                    this.type = parts.length > 3 ? parts[3] : "admin";
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading user data: " + e.getMessage());
        }
    }

    @Override
    public void writeToFile(String filename) {
        try {
            List<String> allLines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            boolean userFound = false;
            
            allLines.add(reader.readLine()); 
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(this.username)) {
                    allLines.add(String.format("%s,%s,%s,%s", username, email, password, type));
                    userFound = true;
                } else {
                    allLines.add(line);
                }
            }
            
            if (!userFound) {
                allLines.add(String.format("%s,%s,%s,%s", username, email, password, type));
            }
            
            reader.close();

            PrintWriter writer = new PrintWriter(new FileWriter(filename));
            for (String userLine : allLines) {
                writer.println(userLine);
            }
            writer.close();
            
        } catch (IOException e) {
            System.out.println("Error writing user data: " + e.getMessage());
        }
    }
}

// Main system class (Singleton)
public class UserManagementSystem {
    private static UserManagementSystem onlyInstance;
    
    private ArrayList<User> normalUsers;
    private ArrayList<User> adminUsers;

    private static final String USERS_FILE = "Users.csv";
    private static final String ADMIN_FILE = "Admin.csv";

    private UserManagementSystem() {
        normalUsers = new ArrayList<>();
        adminUsers = new ArrayList<>();
        loadUsersFromFiles();
    }

    public static UserManagementSystem getInstance() {
        if (onlyInstance == null) {
            onlyInstance = new UserManagementSystem();
        }
        return onlyInstance;
    }

    // Load users from CSV files
    private void loadUsersFromFiles() {
        try {
            // Create files if they don't exist
            createFilesIfNotExist();

            // Load normal users
            BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE));
            String line = reader.readLine(); 
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                User user = new User(parts[0], parts[1], parts[2], parts[3]);
                user.readFromFile(USERS_FILE);
                normalUsers.add(user);
            }
            reader.close();

            // Load admin users
            reader = new BufferedReader(new FileReader(ADMIN_FILE));
            line = reader.readLine(); 
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                User admin = new User(parts[0], parts[1], parts[2], "admin");
                admin.readFromFile(ADMIN_FILE);
                adminUsers.add(admin);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Error reading files: " + e.getMessage());
        }
    }

    private void createFilesIfNotExist() throws IOException {
        File usersFile = new File(USERS_FILE);
        if (!usersFile.exists()) {
            PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE));
            writer.println("Username,Email,Password,Type");
            writer.println("basic_user,basic@test.com,password123,basic");
            writer.println("power_user,power@test.com,password123,power");
            writer.close();
        }

        File adminFile = new File(ADMIN_FILE);
        if (!adminFile.exists()) {
            PrintWriter writer = new PrintWriter(new FileWriter(ADMIN_FILE));
            writer.println("Username,Email,Password");
            writer.println("admin,admin@test.com,admin123");
            writer.close();
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
            System.out.println("System setting updated: " + setting);
            return true;
        }
    }

    // Save users to file
    private void saveUsersToFile() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE));
            writer.println("Username,Email,Password,Type");
            writer.close();

            for (User u : normalUsers) {
                u.writeToFile(USERS_FILE);
            }
        } catch (IOException e) {
            System.out.println("Error saving to file: " + e.getMessage());
        }
    }

    public Object getUserInterface(String username, String password) {
        for (User u : normalUsers) {
            if (u.username.equals(username) && u.password.equals(password)) {
                if (u.type.equals("basic")) {
                    return new BasicUser(u);
                } else if (u.type.equals("power")) {
                    return new PowerUser(u);
                }
            }
        }
        
        for (User u : adminUsers) {
            if (u.username.equals(username) && u.password.equals(password)) {
                return new AdminUser(u);
            }
        }
        return null;
    }

    // Main method to test the system
    public static void main(String[] args) {
        UserManagementSystem system = UserManagementSystem.getInstance();
        System.out.println("Testing User Management System with File Operations:");

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