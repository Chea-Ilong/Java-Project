import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Tenant extends User {
    private boolean isBillPaid;
    private LocalDate lastPaymentDate;
    private LocalDate leaseStartDate;
    private LocalDate leaseEndDate;
    private Room assignedRoom;
    private Floor assignedFloor;

    private static List<Tenant> tenantList = new ArrayList<>(); // Store all tenants

    // Constructor
    public Tenant(String username, String phoneNumber, String tenantIDCard) {
        super(username, tenantIDCard, phoneNumber, "Tenant"); // Use tenantIDCard as password
        this.isBillPaid = false;
        tenantList.add(this); // Add to tenant list
    }

    // View tenant details
    public void viewDetails() {
        System.out.println("Tenant Information:");
        System.out.println(this);
        if (assignedRoom != null && assignedFloor != null) {
            System.out.println("Assigned Floor: " + assignedFloor.getFloorNumber());
            System.out.println("Assigned Room: " + assignedRoom.getRoomID());
        } else {
            System.out.println("No assigned room or floor.");
        }
    }

    // Pay rent if not paid
    public void payRent(double amount) {
        if (isBillPaid) {
            System.out.println("Rent is already paid.");
            return;
        }
        this.isBillPaid = true; // Mark rent as paid
        this.lastPaymentDate = LocalDate.now(); // Set payment date
        System.out.println(username + " has paid rent on " + lastPaymentDate);
    }

    @Override
    public String toString() {
        return super.toString() +
                ", isBillPaid=" + isBillPaid +
                ", lastPaymentDate=" + lastPaymentDate +
                ", leaseStartDate=" + leaseStartDate +
                ", leaseEndDate=" + leaseEndDate +
                ", assignedRoom=" + (assignedRoom != null ? assignedRoom.getRoomID() : "No room assigned") +
                ", assignedFloor=" + (assignedFloor != null ? assignedFloor.getFloorNumber() : "No floor assigned") +
                '}';
    }


    // Static method to get tenant list (needed for login and displaying tenants)
    public static List<Tenant> getTenantList() {
        return tenantList;
    }

    // Static method to set the tenant list (needed for updates)
    public static void setTenantList(List<Tenant> tenantList) {
        Tenant.tenantList = tenantList;
    }

    public String getUsername() {
        return username;
    }

    public void setAssignedRoom(Room assignedRoom) {
        this.assignedRoom = assignedRoom;
    }

    public Room getAssignedRoom() {
        return assignedRoom;
    }

    public Floor getAssignedFloor() {
        return assignedFloor;
    }

    public Object getPassword() {
        return password;
    }

    public boolean isBillPaid() {
        return isBillPaid;
    }
}
