import java.util.ArrayList;
import java.text.NumberFormat;
import java.util.Locale;

public class Room {

    private String roomType;
    private String roomID;
    private double roomPrice;
    private boolean isOccupied;
    private int waterCounterUsage = 0;
    private int electricCounterUsage = 0;
    private  int currentElectricCounter;
    private  int currentWaterCounter;
    private final double electricRate = 620.00; // per kWh
    private final double waterRate = 2500.00;   // per cubic meter
    private final double KHR_TO_USD_RATE = 4100.00;
        

    public Room(String roomID, String roomTypeInput , boolean isOccupied, int currentElectricCounter, int currentWaterCounter) {
        this.roomID = roomID;
        this.roomType = roomTypeInput .equalsIgnoreCase("SMALL") ? "Small" : roomTypeInput .equalsIgnoreCase("MEDIUM") ? "Medium" : roomTypeInput .equalsIgnoreCase("LARGE") ? "Large" : "UNKNOWN";
        this.isOccupied = isOccupied;
        this.roomPrice = determinePrice();
        this.currentElectricCounter = Math.max(currentElectricCounter, 0);
        this.currentWaterCounter = Math.max(currentWaterCounter, 0);
        // Automatically set price based on size
    }

    //Getter / Setter

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public double getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(double roomPrice) {
        this.roomPrice = roomPrice;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public int getWaterCounterUsage() {
        return waterCounterUsage;
    }

    public void setWaterCounterUsage(int waterCounterUsage) {
        this.waterCounterUsage = waterCounterUsage;
    }

    public int getElectricCounterUsage() {
        return electricCounterUsage;
    }

    public void setElectricCounterUsage(int electricCounterUsage) {
        this.electricCounterUsage = electricCounterUsage;
    }

    public int getCurrentElectricCounter() {
        return currentElectricCounter;
    }

    public void setCurrentElectricCounter(int currentElectricCounter) {
        this.currentElectricCounter = currentElectricCounter;
    }

    public int getCurrentWaterCounter() {
        return currentWaterCounter;
    }

    public void setCurrentWaterCounter(int currentWaterCounter) {
        this.currentWaterCounter = currentWaterCounter;
    }



    private double convertToUSD(double khrPrice) {
        return Math.round(khrPrice / KHR_TO_USD_RATE * 100.0) / 100.0;  // Rounds to two decimal places
    }

    // Set price based on the room size
    private double determinePrice() {
        return switch (this.roomType) {
            case "Small" -> 400000;
            case "Medium" -> 800000;
            case "Large" -> 1200000;
            default -> 0;
        };
    }

    public void markAsOccupied() {
        if (isOccupied) {
            System.out.println("Room " + roomID + " is already occupied.");
        } else {
            isOccupied = true;
            System.out.println("Room " + roomID + " is now occupied.");
        }
    }

    public void markAsVacant() {
        if (!isOccupied) {
            System.out.println("Room " + roomID + " is already vacant.");
        } else {
            isOccupied = false;
            electricCounterUsage = 0;
            waterCounterUsage = 0;
            System.out.println("Room " + roomID + " is now vacant. Utility usage has been reset.");
        }
    }


    public void updateUsage(int newElectricCounter, int newWaterCounter) {
        if (!isOccupied) {
            System.out.println("Error: Cannot update usage for a vacant room.");
            return;
        }

        if (newElectricCounter < this.currentElectricCounter) {
            System.out.println("Error: New electric counter must be bigger than the current electric counter.");
            return;
        }

        if (newWaterCounter < this.currentWaterCounter) {
            System.out.println("Error: New water counter must be bigger than the current water counter.");
            return;
        }

        if (newElectricCounter == this.currentElectricCounter && newWaterCounter == this.currentWaterCounter) {
            System.out.println("No change in usage. No update needed.");
            return;
        }

        this.electricCounterUsage = newElectricCounter - this.currentElectricCounter;
        this.waterCounterUsage = newWaterCounter - this.currentWaterCounter;
        this.currentElectricCounter = newElectricCounter;
        this.currentWaterCounter = newWaterCounter;
    }

    private double calculateElectricPrice() {
        return isOccupied ? electricCounterUsage * electricRate : 0;
    }

    private double calculateWaterPrice() {
        return isOccupied ? waterCounterUsage * waterRate : 0;
    }

    private String formatKHR(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("km", "KH"));
        return formatter.format(price) + " ៛";
    }
    private String formatUSD(double amount) {
        NumberFormat usdFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return usdFormat.format(amount);
    }

    public void displayRoomBilling() {
        if (!isOccupied) {
            System.out.println("Room " + roomID + " is vacant. No billing required.");
            return;
        }

        double electricPrice = electricCounterUsage * electricRate;
        double waterPrice = waterCounterUsage * waterRate;
        double totalPrice = roomPrice + electricPrice + waterPrice;
        double totalPriceUSD = convertToUSD(totalPrice);

        System.out.println("Room ID: " + roomID);
        System.out.println("Room Size: " + roomType);
        System.out.println("Room Price: " + formatKHR(roomPrice) + " (" + formatUSD(convertToUSD(roomPrice)) + ")");
        System.out.println("Water counter: " + (currentWaterCounter - waterCounterUsage) + " -> " + currentWaterCounter);
        System.out.println("Electric counter: " + (currentElectricCounter - electricCounterUsage) + " -> " + currentElectricCounter);
        System.out.println("Water Usage: " + waterCounterUsage + " m³");
        System.out.println("Electric Usage: " + electricCounterUsage + " kWh");
        System.out.println("Water Price: " + formatKHR(waterPrice) + " (" + formatUSD(convertToUSD(waterPrice)) + ")");
        System.out.println("Electric Price: " + formatKHR(electricPrice) + " (" + formatUSD(convertToUSD(electricPrice)) + ")");
        System.out.println("Total Expense: " + formatKHR(totalPrice) + " (" + formatUSD(totalPriceUSD) + ")");
        System.out.println();
    }

    public void displayRoomInfo() {
        System.out.println("Room ID: " + roomID);
        System.out.println("Room Price: " + roomPrice);
        System.out.println("Room Status: " + (isOccupied ? "Occupied" : "Available"));
    }


    @Override
    public String toString() {
        return "Room ID: " + roomID + "\n" +
                "Room Size: " + roomType + "\n" +
                "Price: " + formatKHR(roomPrice) + " (" + formatUSD(convertToUSD(roomPrice)) + ")\n" +
                "Status: " + (isOccupied ? "Occupied" : "Vacant") + "\n" +
                "Current Water Counter: " + currentWaterCounter + " m³\n" +
                "Current Electric Counter: " + currentElectricCounter + " kWh\n";
    }
}
