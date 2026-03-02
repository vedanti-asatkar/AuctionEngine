import model.Bid;
import service.AuctionEngine;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

public class AuctionEngineSwingApp {

    private final AuctionEngine engine;
    private final JLabel highestLabel;
    private final JLabel totalBidsLabel;
    private final JLabel statusLabel;
    private final JTextField bidderIdField;
    private final JTextField amountField;
    private final JTextArea outputArea;

    public AuctionEngineSwingApp() {
        this.engine = new AuctionEngine();
        this.highestLabel = new JLabel();
        this.totalBidsLabel = new JLabel();
        this.statusLabel = new JLabel();
        this.bidderIdField = new JTextField();
        this.amountField = new JTextField();
        this.outputArea = new JTextArea(18, 60);
    }

    private void createAndShow() {
        JFrame frame = new JFrame("AuctionEngine - Swing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        JPanel topPanel = new JPanel(new GridLayout(2, 3, 8, 8));
        topPanel.setBorder(BorderFactory.createTitledBorder("Auction Status"));
        topPanel.add(new JLabel("Current Highest:"));
        topPanel.add(new JLabel("Total Bids:"));
        topPanel.add(new JLabel("Auction State:"));
        topPanel.add(highestLabel);
        topPanel.add(totalBidsLabel);
        topPanel.add(statusLabel);

        JPanel bidPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        bidPanel.setBorder(BorderFactory.createTitledBorder("Place Bid"));
        bidPanel.add(new JLabel("Bidder ID"));
        bidPanel.add(bidderIdField);
        bidPanel.add(new JLabel("Amount"));
        bidPanel.add(amountField);

        JButton placeBidButton = new JButton("Place Bid");
        JButton showHighestButton = new JButton("Show Highest");
        JButton showSortedButton = new JButton("Show Sorted Bids");
        JButton showAlertsButton = new JButton("Show Alerts");
        JButton snapshotButton = new JButton("Show Snapshot");
        JButton closeAuctionButton = new JButton("Close Auction");
        JButton demoSimulatorButton = new JButton("Demo Simulator");

        bidPanel.add(placeBidButton);
        bidPanel.add(showHighestButton);
        bidPanel.add(showSortedButton);
        bidPanel.add(showAlertsButton);
        bidPanel.add(snapshotButton);
        bidPanel.add(closeAuctionButton);
        bidPanel.add(demoSimulatorButton);

        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Output"));

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(bidPanel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.SOUTH);

        placeBidButton.addActionListener(e -> placeBid());
        showHighestButton.addActionListener(e -> showHighest());
        showSortedButton.addActionListener(e -> showSortedBids());
        showAlertsButton.addActionListener(e -> showAlerts());
        snapshotButton.addActionListener(e -> showSnapshot());
        closeAuctionButton.addActionListener(e -> closeAuction());
        demoSimulatorButton.addActionListener(e -> openDemoSimulator());

        refreshStatus();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void placeBid() {
        String bidderId = bidderIdField.getText().trim();
        String amountText = amountField.getText().trim();

        if (bidderId.isEmpty()) {
            showError("Bidder ID cannot be empty.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            showError("Amount must be a number.");
            return;
        }

        if (amount <= 0) {
            showError("Amount must be greater than 0.");
            return;
        }

        int beforeCount = engine.getTotalBids();
        engine.placeBid(bidderId, amount);
        int afterCount = engine.getTotalBids();

        if (afterCount > beforeCount) {
            appendOutput("Placed bid: bidder=" + bidderId + ", amount=" + amount);
            bidderIdField.setText("");
            amountField.setText("");
            showLatestAlertsIfAny();
        } else {
            appendOutput("Bid rejected by engine rules.");
        }

        refreshStatus();
    }

    private void showHighest() {
        Bid highest = engine.getHighestBid();
        appendOutput(highest == null ? "No bids yet." : "Highest: " + highest);
    }

    private void showSortedBids() {
        int latestVersion = engine.getHistoryVersionCount();
        List<Bid> bids = engine.getBidsAtVersion(latestVersion);
        appendOutput("Sorted bids at latest version " + latestVersion + ":");
        if (bids.isEmpty()) {
            appendOutput("No bids yet.");
            return;
        }
        for (Bid bid : bids) {
            appendOutput("  " + bid);
        }
    }

    private void showAlerts() {
        List<String> alerts = engine.getRecentAlerts();
        appendOutput("Recent alerts:");
        if (alerts.isEmpty()) {
            appendOutput("No alerts.");
            return;
        }
        for (String alert : alerts) {
            appendOutput("  " + alert);
        }
    }

    private void showSnapshot() {
        String input = JOptionPane.showInputDialog(null, "Enter version (0 to " + engine.getHistoryVersionCount() + "):");
        if (input == null) {
            return;
        }

        int version;
        try {
            version = Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            showError("Version must be an integer.");
            return;
        }

        if (!engine.isValidHistoryVersion(version)) {
            showError("Invalid version.");
            return;
        }

        Bid highest = engine.getHighestBidAtVersion(version);
        appendOutput("Snapshot version " + version + ":");
        appendOutput("Highest: " + (highest == null ? "None" : highest));
        List<Bid> bids = engine.getBidsAtVersion(version);
        if (bids.isEmpty()) {
            appendOutput("No bids in this version.");
            return;
        }
        for (Bid bid : bids) {
            appendOutput("  " + bid);
        }
    }

    private void closeAuction() {
        if (!engine.isAuctionOpen()) {
            appendOutput("Auction is already closed.");
            return;
        }
        engine.closeAuction();
        Bid winner = engine.getHighestBid();
        appendOutput(winner == null
                ? "Auction closed. No bids were placed."
                : "Auction closed. Winner: " + winner.getBidderId() + " with amount " + winner.getAmount());
        refreshStatus();
    }

    private void openDemoSimulator() {
        JComboBox<String> scenarioBox = new JComboBox<>(new String[]{
                "Rapid Bidding",
                "Price Spike",
                "Rapid + Price Alerts"
        });
        JTextField bidCountField = new JTextField("12");
        JTextField delayMsField = new JTextField("80");

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Scenario"));
        panel.add(scenarioBox);
        panel.add(new JLabel("Bid count"));
        panel.add(bidCountField);
        panel.add(new JLabel("Delay per bid (ms)"));
        panel.add(delayMsField);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Demo Simulator",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        int bidCount;
        int delayMs;
        try {
            bidCount = Integer.parseInt(bidCountField.getText().trim());
            delayMs = Integer.parseInt(delayMsField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Bid count and delay must be integers.");
            return;
        }

        if (bidCount <= 0 || delayMs < 0) {
            showError("Use bid count > 0 and delay >= 0.");
            return;
        }

        String scenario = (String) scenarioBox.getSelectedItem();
        runScenarioInBackground(scenario, bidCount, delayMs);
    }

    private void runScenarioInBackground(String scenario, int bidCount, int delayMs) {
        Thread worker = new Thread(() -> runScenario(scenario, bidCount, delayMs), "demo-simulator");
        worker.setDaemon(true);
        worker.start();
    }

    private void runScenario(String scenario, int bidCount, int delayMs) {
        if (!engine.isAuctionOpen()) {
            appendOutputOnEdt("Cannot run demo: auction is closed.");
            return;
        }

        appendOutputOnEdt("Running demo scenario: " + scenario);
        double nextAmount = getNextAmount();

        if ("Rapid Bidding".equals(scenario)) {
            int count = Math.max(bidCount, 3);
            nextAmount = placeBids(count, 1, "rapid_user_", nextAmount, delayMs);
        } else if ("Price Spike".equals(scenario)) {
            if (engine.getHighestBid() == null) {
                placeSingleBid("price_base", nextAmount);
                nextAmount += 1.0;
                sleepQuietly(delayMs);
            }
            Bid highest = engine.getHighestBid();
            if (highest != null) {
                double spikeAmount = Math.floor(highest.getAmount() * 1.8) + 1.0;
                placeSingleBid("price_user", spikeAmount);
            }
        } else if ("Rapid + Price Alerts".equals(scenario)) {
            nextAmount = placeBids(Math.max(bidCount, 3), 1, "rapid_user_", nextAmount, delayMs);
            Bid highest = engine.getHighestBid();
            if (highest != null) {
                double spikeAmount = Math.floor(highest.getAmount() * 1.8) + 1.0;
                placeSingleBid("price_user", spikeAmount);
            }
        }

        SwingUtilities.invokeLater(() -> {
            refreshStatus();
            appendOutput("Demo complete.");
            showAlerts();
        });
    }

    private double placeBids(int count, int bidderPool, String bidderPrefix, double startAmount, int delayMs) {
        double amount = startAmount;
        for (int i = 0; i < count; i++) {
            String bidderId = bidderPrefix + ((i % bidderPool) + 1);
            placeSingleBid(bidderId, amount);
            amount += 1.0;
            sleepQuietly(delayMs);
        }
        return amount;
    }

    private void placeSingleBid(String bidderId, double amount) {
        int before = engine.getTotalBids();
        engine.placeBid(bidderId, amount);
        int after = engine.getTotalBids();
        if (after > before) {
            appendOutputOnEdt("Placed bid: bidder=" + bidderId + ", amount=" + amount);
        } else {
            appendOutputOnEdt("Bid rejected during demo: bidder=" + bidderId + ", amount=" + amount);
        }
    }

    private void sleepQuietly(int delayMs) {
        if (delayMs == 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void refreshStatus() {
        Bid highest = engine.getHighestBid();
        highestLabel.setText(highest == null ? "None" : String.valueOf(highest.getAmount()));
        totalBidsLabel.setText(String.valueOf(engine.getTotalBids()));
        statusLabel.setText(engine.isAuctionOpen() ? "OPEN" : "CLOSED");
    }

    private void showLatestAlertsIfAny() {
        List<String> alerts = engine.getRecentAlerts();
        if (alerts.isEmpty()) {
            return;
        }
        appendOutput("Latest alert: " + alerts.get(alerts.size() - 1));
    }

    private void appendOutput(String line) {
        outputArea.append(line + "\n");
    }

    private void appendOutputOnEdt(String line) {
        SwingUtilities.invokeLater(() -> appendOutput(line));
    }

    private double getNextAmount() {
        Bid highest = engine.getHighestBid();
        if (highest == null) {
            return 100.0;
        }
        return highest.getAmount() + 1.0;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuctionEngineSwingApp().createAndShow());
    }
}
