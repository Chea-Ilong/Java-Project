package Users;

import Exceptions.RoomException;
import Exceptions.TenantException;
import Payment.UtilityUsage;
import Properties.Room;
import Payment.RentPayment;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Tenant extends User {

    // ============================ Constants =================================
    private static final double KHR_TO_USD_RATE = 4100.0; // Example exchange rate

    // ============================ Fields ====================================
    private Room assignedRoom;
    private final List<RentPayment> rentPaymentHistory;
    private boolean rentPaid;
    private double balanceDue;

    // ============================ Constructor =================================
    public Tenant(String name, String IdCard, String contact) {
        super(name, IdCard, contact, "Tenant");
        this.rentPaymentHistory = new ArrayList<>();
        this.rentPaid = false;
        this.balanceDue = 0.0;
    }

    // ============================ Methods for Utility Usage =====================

    // Method to view utility usage for a specific date
    public void viewUtilityUsage(Landlord landlord, LocalDate date) {
        UtilityUsage usage = landlord.getUtilityUsageForRoom(assignedRoom, date);
        if (usage != null) {
            System.out.println(name + "'s utility usage for " + date + ": " + usage.toString());
        } else {
            System.out.println("No utility data available for " + date);
        }
    }

    // ============================ Room Assignment =============================

    // Assign a room to the tenant
    public void assignRoom(Room room) throws RoomException, TenantException {
        if (this.assignedRoom != null) {
            throw new TenantException("Error: Tenant is already assigned to Room " + assignedRoom.getRoomNumber() + ".");
        }

        if (room.isOccupied()) {
            throw new RoomException("Room " + room.getRoomNumber() + " is already occupied.");
        }

        this.assignedRoom = room;
        room.assignTenant(this);
        this.balanceDue = room.getRent();

        System.out.println(name + " has been assigned to Room " + room.getRoomNumber() +
                " with rent: " + formatKHR(room.getRent()) + " (" + formatUSD(convertToUSD(room.getRent())) + ")");
    }

    // ============================ Rent Payment ================================

    // Pay Rent
    public void payRent(Scanner scanner) throws TenantException {
        if (assignedRoom == null) {
            throw new TenantException("Error: No room assigned to pay rent for.");
        }

        System.out.print("Enter the amount to pay for rent: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        if (amount > balanceDue) {
            System.out.println("Error: Payment amount cannot be greater than the rent balance.");
            return;
        }

        if (amount == balanceDue) {
            rentPaid = true;
            balanceDue = 0.0;
            RentPayment rentPayment = new RentPayment(IdCard, amount, true, false);
            rentPaymentHistory.add(rentPayment);

            System.out.println(name + " has fully paid rent: " + formatKHR(amount) + " (" + formatUSD(convertToUSD(amount)) + ")");
        } else {
            balanceDue -= amount;
            RentPayment rentPayment = new RentPayment(IdCard, amount, true, false);
            rentPaymentHistory.add(rentPayment);

            System.out.println(name + " has partially paid rent. Remaining balance: " + formatKHR(balanceDue) +
                    " (" + formatUSD(convertToUSD(balanceDue)) + ")");
        }
    }

    // ============================ Utility Payment ============================

    // Pay Utilities
    public void payUtilities(double amount) throws TenantException {
        if (assignedRoom == null) {
            throw new TenantException("Error: No room assigned to pay utilities for.");
        }

        UtilityUsage usage = assignedRoom.getUtilityUsage();
        if (usage == null) {
            throw new TenantException("Error: Utility usage data is not available. Please contact the landlord.");
        }

        double totalUtilityCost = calculateTotalUtilityCost(usage);
        System.out.println("Total Utility Cost: " + formatKHR(totalUtilityCost) + " (" + formatUSD(convertToUSD(totalUtilityCost)) + ")");

        if (amount > totalUtilityCost) {
            System.out.println("Error: Payment amount cannot be greater than the utility balance.");
            return;
        }

        if (amount == totalUtilityCost) {
            RentPayment rentPayment = new RentPayment(IdCard, amount, false, true);
            rentPaymentHistory.add(rentPayment);

            System.out.println(name + " has fully paid utilities: " + formatKHR(amount) + " (" + formatUSD(convertToUSD(amount)) + ") on " + rentPayment.getPaymentDate());
        } else {
            System.out.println("Error: Partial payments for utilities are not allowed.");
        }
    }
    public double calculateTotalUtilityCost(UtilityUsage usage) {
        return (usage.getElectricUsage() * Room.getElectricRate()) + (usage.getWaterUsage() * Room.getWaterRate());
    }
    // Display Payment History
    public void displayPaymentHistory() {
        System.out.println(name + "'s Payment History:");
        for (RentPayment rentPayment : rentPaymentHistory) {
            System.out.println(rentPayment.toString());
        }
    }
    // ============================ Vacating Room =============================

    // Vacate Room
    public void vacateRoom() throws TenantException {
        if (assignedRoom == null) {
            throw new TenantException("Error: Tenant is not assigned to any room.");
        }

        System.out.println(name + " is vacating Room " + assignedRoom.getRoomNumber());
        assignedRoom.removeTenant();
        this.assignedRoom = null;
        this.balanceDue = 0.0;
        this.rentPaid = false;
    }

    // ============================ Getter and Helper Methods =================
    public Room getAssignedRoom() {
        return assignedRoom;
    }

    // Convert KHR to USD
    private double convertToUSD(double amount) {
        return amount / KHR_TO_USD_RATE;
    }

    // Format amount in KHR
    private String formatKHR(double amount) {
        return String.format("%.0f KHR", amount);
    }

    // Format amount in USD
    private String formatUSD(double amount) {
        return String.format("%.2f USD", amount);
    }

    // ============================ Override toString ==========================
    @Override
    public String toString() {
        return super.toString() +
                "Tenant{" +
                "assignedRoom=" + (assignedRoom != null ? assignedRoom.getRoomNumber() : "None") +
                ", balanceDue=" + formatKHR(balanceDue) +
                ", rentPaid=" + rentPaid +
                '}';
    }
}
