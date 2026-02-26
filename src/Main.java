import model.Bid;
import service.AuctionEngine;
import java.util.Scanner;

public class Main {

    private static final int DEFAULT_RAPID_WINDOW = 20;
    private static final int DEFAULT_RAPID_THRESHOLD = 5;

    private static void clearScreen() {
        System.out.print("\n".repeat(30));
    }

    public static void main(String[] args) {
        AuctionEngine engine = new AuctionEngine();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu(engine);
            int choice = readInt(scanner, "Select option: ");

            switch (choice) {
                case 1:
                    handlePlaceBid(engine, scanner);
                    break;
                case 2:
                    showHighestBid(engine);
                    pause(scanner);
                    break;
                case 3:
                    System.out.println("\nSorted bids (low to high):");
                    engine.printSortedBids();
                    pause(scanner);
                    break;
                case 4:
                    handleRapidCheck(engine, scanner);
                    break;
                case 5:
                    handlePriceCheck(engine, scanner);
                    break;
                case 6:
                    handleCloseAuction(engine, scanner);
                    break;
                case 7:
                    handleHistorySnapshot(engine, scanner);
                    break;
                case 8:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option.");
                    pause(scanner);
                    break;
            }
        }
    }

    private static void printMenu(AuctionEngine engine) {
        clearScreen();
        Bid highest = engine.getHighestBid();
        System.out.println("Current Highest: " + (highest == null ? "None" : highest.getAmount()));
        System.out.println("Total Bids: " + engine.getTotalBids());
        System.out.println("Auction Status: " + (engine.isAuctionOpen() ? "OPEN" : "CLOSED"));
        System.out.println("===================================");
        System.out.println("      ONLINE AUCTION ENGINE");
        System.out.println("===================================");
        System.out.println("1. Place Bid");
        System.out.println("2. Show Highest Bid");
        System.out.println("3. Show All Bids (Sorted)");
        System.out.println("4. Check Rapid Bidding (Bidder)");
        System.out.println("5. Check Price Spike (Amount)");
        System.out.println("6. Close Auction");
        System.out.println("7. Show Historical Snapshot");
        System.out.println("8. Exit");
        System.out.println("===================================");
    }

    private static void handlePlaceBid(AuctionEngine engine, Scanner scanner) {
        System.out.print("Bidder ID: ");
        String bidderId = scanner.nextLine().trim();
        while (bidderId.isEmpty()) {
            System.out.print("Bidder ID cannot be empty. Enter Bidder ID: ");
            bidderId = scanner.nextLine().trim();
        }

        double amount = readDouble(scanner, "Bid amount: ");
        if (amount <= 0) {
            System.out.println("Bid amount must be greater than 0.");
            pause(scanner);
            return;
        }

        engine.placeBid(bidderId, amount);
        pause(scanner);
    }

    private static void showHighestBid(AuctionEngine engine) {
        Bid highest = engine.getHighestBid();
        System.out.println("\nHighest bid:");
        if (highest == null) {
            System.out.println("No bids yet.");
        } else {
            System.out.println(highest);
        }
    }

    private static void handleRapidCheck(AuctionEngine engine, Scanner scanner) {
        System.out.print("Bidder ID: ");
        String bidderId = scanner.nextLine().trim();
        while (bidderId.isEmpty()) {
            System.out.print("Bidder ID cannot be empty. Enter Bidder ID: ");
            bidderId = scanner.nextLine().trim();
        }

        int windowSeconds = readInt(scanner, "Window seconds (default 20): ");
        if (windowSeconds <= 0) {
            windowSeconds = DEFAULT_RAPID_WINDOW;
        }

        int threshold = readInt(scanner, "Threshold (default 5): ");
        if (threshold < 0) {
            threshold = DEFAULT_RAPID_THRESHOLD;
        }

        boolean rapid = engine.isRapidBidding(bidderId, windowSeconds, threshold);
        System.out.println(rapid ? "Rapid bidding detected." : "No rapid bidding detected.");
        pause(scanner);
    }

    private static void handlePriceCheck(AuctionEngine engine, Scanner scanner) {
        double amount = readDouble(scanner, "Bid amount to evaluate: ");
        boolean spike = engine.isPriceSpike(amount);
        System.out.println(spike ? "Price spike detected." : "No price spike detected.");
        pause(scanner);
    }

    private static void handleCloseAuction(AuctionEngine engine, Scanner scanner) {
        if (!engine.isAuctionOpen()) {
            System.out.println("Auction is already closed.");
            pause(scanner);
            return;
        }

        engine.closeAuction();
        Bid winner = engine.getHighestBid();
        if (winner == null) {
            System.out.println("Auction closed. No bids were placed.");
        } else {
            System.out.println("Auction closed.");
            System.out.println("Winner: " + winner.getBidderId() + " with amount " + winner.getAmount());
        }
        pause(scanner);
    }

    private static void handleHistorySnapshot(AuctionEngine engine, Scanner scanner) {
        int latestVersion = engine.getHistoryVersionCount();
        System.out.println("Available versions: 0 to " + latestVersion);
        int version = readInt(scanner, "Enter version to inspect: ");

        if (!engine.isValidHistoryVersion(version)) {
            System.out.println("Invalid version.");
            pause(scanner);
            return;
        }

        Bid highestAtVersion = engine.getHighestBidAtVersion(version);
        System.out.println("\nSnapshot version: " + version);
        System.out.println("Highest at version: " + (highestAtVersion == null ? "None" : highestAtVersion));
        System.out.println("Bids in this version (sorted):");
        for (Bid bid : engine.getBidsAtVersion(version)) {
            System.out.println(bid);
        }
        pause(scanner);
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid amount. Try again.");
            }
        }
    }

    private static void pause(Scanner scanner) {
        System.out.println();
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
}
