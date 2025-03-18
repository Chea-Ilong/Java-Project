package Users;

import DataBase.BuildingDML;
import DataBase.RoomDML;
import DataBase.TenantDML;
import Exceptions.LandlordException;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Payment.Bill;
import Payment.BillRecord;
import Properties.Building;
import Properties.Floor;
import Properties.Room;
import java.time.YearMonth;
import java.util.*;

public class Landlord extends User {

    // ====================================================================================================
    // Fields
    // ====================================================================================================
    private List<Tenant> tenants;
    private List<Building> buildings; // Manage multiple buildings
    private BillRecord billRecord; // Add BillRecord reference
    private static int landlordPIN = 1234;
    private int failedPinAttempts;
    private static final int MAX_PIN_ATTEMPTS = 5;

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public Landlord(String name, String IDcard, String contact, List<Tenant> tenants, List<Building> buildings) {
        super(name, IDcard, contact, "Landlord");
        this.failedPinAttempts = 0;
        this.tenants = tenants != null ? tenants : new ArrayList<>();
        this.buildings = buildings != null ? buildings : new ArrayList<>();
        this.billRecord = new BillRecord(); // Initialize BillRecord
    }

    // ====================================================================================================
    // Bill Management Methods
    // ====================================================================================================

    // Create and distribute bills to rooms in a specific building and floor
    public List<Bill> createBillsForFloor(String buildingName, String floorNumber,
                                          double rentAmount,
                                          Map<String, Integer> electricUsageMap,
                                          Map<String, Integer> waterUsageMap) {
        Building building = getBuildingByName(buildingName);
        if (building == null) {
            System.out.println("Building not found: " + buildingName);
            return new ArrayList<>();
        }

        Floor floor = building.getFloorByNumber(floorNumber);
        if (floor == null) {
            System.out.println("Floor not found: " + floorNumber);
            return new ArrayList<>();
        }

        List<Room> rooms = floor.getRooms();
        return billRecord.distributeBills(rooms, buildingName, floorNumber,
                rentAmount, electricUsageMap, waterUsageMap);
    }

    public void refreshBuildingList() {
        BuildingDML buildingDML = new BuildingDML();
        this.buildings = buildingDML.getAllBuildings();
    }
    // View bills for a specific month
    public List<Bill> viewBillsForMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return billRecord.getBillsForMonth(yearMonth);
    }

    // View bills for a specific tenant
    public List<Bill> viewBillsForTenant(String tenantId) {
        return billRecord.getBillHistoryForTenant(tenantId);
    }

    // View unpaid bills
    public List<Bill> viewUnpaidBills() {
        return billRecord.getUnpaidBills();
    }

    // Generate monthly billing report
    public String generateMonthlyBillingReport(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return billRecord.generateMonthlyReport(yearMonth);
    }

    // ====================================================================================================
    // Login & Authentication
    // ====================================================================================================
    public boolean login(Scanner scanner, String username, String password) {
        username = username.trim();
        password = password.trim();

        if (this.name.equals(username) && this.IdCard.equals(password)) {
            System.out.println("Login successful for " + this.name);

            // Handle PIN authentication
            while (failedPinAttempts < MAX_PIN_ATTEMPTS) {
                System.out.print("Enter your 4-digit PIN: ");
                if (scanner.hasNextInt()) {
                    int pin = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline left-over

                    if (pin == landlordPIN) {
                        System.out.println("PIN verified. Login successful as Landlord!");
                        failedPinAttempts = 0;
                        return true;
                    } else {
                        failedPinAttempts++;
                        System.out.println("Incorrect PIN. Attempts left: " + (MAX_PIN_ATTEMPTS - failedPinAttempts));
                        if (failedPinAttempts == MAX_PIN_ATTEMPTS) {
                            System.out.print("Too many failed PIN attempts. Press R to reset your PIN or L to go back to the login page: ");
                            String choice = scanner.nextLine().trim().toUpperCase();
                            if (choice.equals("R")) {
                                resetPIN(scanner);
                            } else if (choice.equals("L")) {
                                return false;
                            }
                        }
                    }
                } else {
                    System.out.println("Invalid input. Please enter a 4-digit PIN.");
                    scanner.next(); // Consume the invalid input
                    failedPinAttempts++;
                }
            }

            System.out.println("Returning to login page.");
            return false;
        }

        return false;
    }

    // ====================================================================================================
    // PIN Reset
    // ====================================================================================================
    public void resetPIN(Scanner scanner) {
        System.out.print("Enter your Landlord ID: ");
        String inputID = scanner.nextLine().trim();
        System.out.print("Enter your Contact Number: ");
        String inputContact = scanner.nextLine().trim();

        if (this.IdCard.equals(inputID) && this.contact.equals(inputContact)) {
            System.out.print("Enter a new 4-digit PIN: ");
            if (scanner.hasNextInt()) {
                int newPin = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                this.landlordPIN = newPin;
                this.failedPinAttempts = 0;
                System.out.println("PIN reset successful. You can now log in with the new PIN.");
            } else {
                System.out.println("Invalid input. PIN reset failed.");
                scanner.next(); // Consume the invalid input
            }
        } else {
            System.out.println("Incorrect Landlord ID or Contact Number. PIN reset failed.");
        }
    }

    // ====================================================================================================
    // CRUD Operations for Room
    // ====================================================================================================
    // Find room by room number across all buildings and floors
    public Room getRoomAcrossAllBuildings(String roomNumber) {
        for (Building building : buildings) {
            for (Floor floor : building.getFloors()) {
                for (Room room : floor.getRooms()) {
                    if (room.getRoomNumber().equals(roomNumber)) {
                        return room; // Room found
                    }
                }
            }
        }
        return null; // Room not found
    }

    // ====================================================================================================
    // CRUD Operations for Floor
    // ====================================================================================================
    public void removeFloorFromBuilding(String buildingName, String floorNumber) {
        Building building = getBuildingByName(buildingName);
        if (building != null) {
            building.removeFloor(floorNumber);
        } else {
            System.out.println("Building " + buildingName + " not found.");
        }
    }

    // ====================================================================================================
    // CRUD Operations for Building
    // ====================================================================================================
    public void addBuilding(Building building) throws LandlordException {
        if (building == null) {
            throw new LandlordException("Cannot add a null building.");
        }

        if (buildingExists(building)) {
            throw new LandlordException("Building already exists: " + building.getBuildingName());
        }

        buildings.add(building);
        System.out.println("Building " + building.getBuildingName() + " has been added.");
    }

    public void removeBuilding(String buildingName) {
        BuildingDML buildingDML = new BuildingDML();
        int buildingId = buildingDML.getBuildingIdByName(buildingName);

        if (buildingId != -1) {
            buildingDML.deleteBuilding(buildingId);
            // Also remove from local collection if needed
            Building building = getBuildingByName(buildingName);
            if (building != null) {
                buildings.remove(building);
            }
            System.out.println("Building " + buildingName + " has been removed.");
        } else {
            System.out.println("Building not found.");
        }
    }

    public void updateBuilding(String oldBuildingName, String newBuildingName, String newAddress) {
        BuildingDML buildingDML = new BuildingDML();
        Building building = buildingDML.getBuildingByName(oldBuildingName);

        if (building != null) {
            // Update the building object
            building.setBuildingName(newBuildingName);
            building.setAddress(newAddress);

            // Get the building ID and update it in the database
            int buildingId = buildingDML.getBuildingIdByName(oldBuildingName);
            if (buildingId != -1) {
                buildingDML.updateBuilding(buildingId, building);
                System.out.println("Building " + oldBuildingName + " has been updated to " + newBuildingName + " at " + newAddress);
            } else {
                System.out.println("Building ID could not be found in database.");
            }
        } else {
            System.out.println("Building not found.");
        }
    }

    private boolean buildingExists(Building newBuilding) {
        BuildingDML buildingDML = new BuildingDML();
        Building existingBuilding = buildingDML.getBuildingByName(newBuilding.getBuildingName());
        return existingBuilding != null;
    }

    // ====================================================================================================
// CRUD Operations for Tenant
// ====================================================================================================
public void addTenant(Tenant tenant) {
    TenantDML tenantDML = new TenantDML();
    if (!tenantDML.tenantExists(tenant.getIdCard())) {
        tenantDML.saveTenant(tenant);
        System.out.println("Tenant added: " + tenant.getName());
    }
}

    public void removeTenant(String tenantID) throws TenantException, RoomException {
        Tenant tenant = getTenantByID(tenantID);
        if (tenant != null) {
            // If tenant has an assigned room, remove them from it
            Room assignedRoom = tenant.getAssignedRoom();
            if (assignedRoom != null) {
                assignedRoom.removeTenant();
                System.out.println(tenant.getName() + " has been removed from Room " + assignedRoom.getRoomNumber());
            }

            tenants.remove(tenant);
            System.out.println("Tenant removed: " + tenant.getName());
        } else {
            throw new TenantException("Tenant not found.");
        }
    }

    public Building getBuildingByName(String buildingName) {
        BuildingDML buildingDML = new BuildingDML();
        return buildingDML.getBuildingByName(buildingName);
    }

    public void addFloorToBuilding(String buildingName, Floor floor) {
        BuildingDML buildingDML = new BuildingDML();
        buildingDML.addFloorToBuilding(buildingName, floor);
    }

    public Tenant getTenantByID(String tenantID) {
        for (Tenant tenant : tenants) {
            if (tenant.getIdCard().equals(tenantID)) {
                return tenant;
            }
        }
        return null; // Tenant not found
    }

    // ====================================================================================================
    // Getter Methods
    // ====================================================================================================
    public List<Tenant> getTenants() {
        return tenants;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public BillRecord getBillRecord() {
        return billRecord;
    }

    // ====================================================================================================
    // Display Information Methods
    // ====================================================================================================
    public void displayAllBuildings() {
        System.out.println("\n===== All Buildings =====");
        if (buildings.isEmpty()) {
            System.out.println("No buildings available.");
            return;
        }

        for (Building building : buildings) {
            System.out.println("Building Name: " + building.getBuildingName() +
                    " | Address: " + building.getAddress());

            List<Floor> floors = building.getFloors();
            if (floors.isEmpty()) {
                System.out.println("  No floors available.");
            } else {
                for (Floor floor : floors) {
                    System.out.println("  Floor: " + floor.getFloorNumber());

                    List<Room> rooms = floor.getRooms();
                    if (rooms.isEmpty()) {
                        System.out.println("    No rooms available.");
                    } else {
                        System.out.println("    Rooms: ");
                        for (Room room : rooms) {
                            System.out.println("      - Room: " + room.getRoomNumber());
                        }
                    }
                }
            }
            System.out.println("---------------------");
        }
    }
    // Display tenant details
    public void displayAllTenants() {
        TenantDML tenantDML = new TenantDML();
        List<Tenant> tenants = tenantDML.getAllTenantsForLandlord();

        System.out.println("===== All Tenants =====");
        for (Tenant tenant : tenants) {
            System.out.println("Name: " + tenant.getName());
            System.out.println("ID: " + tenant.getIdCard());
            System.out.println("Contact: " + tenant.getContact());
            Room room = tenant.getAssignedRoom();
            if (room != null) {
                System.out.println("Room: " + room.getRoomNumber());
            } else {
                System.out.println("Room: Not assigned");
            }
            System.out.println("-----------------------");
        }
    }

    // ====================================================================================================
    // toString Method
    // ====================================================================================================
    @Override
    public String toString() {
        return super.toString() +
                "Landlord{" +
                "tenants=" + tenants +
                ", buildings=" + buildings +
                '}';
    }
}