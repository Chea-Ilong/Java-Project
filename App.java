public class App {
    public static void main(String[] args) {

        Room room = new Room("A101", "large", false);
        Room room3 = new Room("A102", "small", false);
        Room room43 = new Room("A103", "medium", false);

// Display room info
        room.displayRoomInfo();   // No need to wrap in System.out.println()
        room3.displayRoomInfo();
        room43.displayRoomInfo();


//        Tenant tenant1 = new Tenant("John Doe", "555-1234", 101, true);
//        tenant1.signUp();
//
//        // Attempt to log in with correct credentials
//        Tenant tenant2 = new Tenant("John Doe", 101);  // Same name and ID card as tenant1
//        tenant2.login();  // Login successful
//
//        // Attempt to log in with incorrect credentials
//        Tenant tenant3 = new Tenant("Jane Smith", 102);  // Different name and ID card
//        tenant3.login();  // Login failed
    }
}