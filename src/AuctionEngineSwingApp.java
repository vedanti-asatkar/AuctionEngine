import model.Auction;
import model.Bid;
import service.AuctionEngine;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuctionEngineSwingApp {

    private static final DateTimeFormatter LIST_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, HH:mm").withZone(ZoneId.systemDefault());

    private final AuctionEngine engine;
    private final JLabel highestLabel;
    private final JLabel totalBidsLabel;
    private final JLabel statusLabel;
    private final JLabel selectedAuctionLabel;
    private final JTextField bidderIdField;
    private final JTextField amountField;
    private final JTextArea outputArea;
    private final DefaultListModel<String> activeAuctionListModel;
    private final JList<String> activeAuctionList;
    private final NumberFormat currencyFormat;
    private String currentAuctionId;

    public AuctionEngineSwingApp() {
        this.engine = new AuctionEngine();
        this.highestLabel = new JLabel();
        this.totalBidsLabel = new JLabel();
        this.statusLabel = new JLabel();
        this.selectedAuctionLabel = new JLabel("None");
        this.bidderIdField = new JTextField();
        this.amountField = new JTextField();
        this.outputArea = new JTextArea(18, 60);
        this.activeAuctionListModel = new DefaultListModel<>();
        this.activeAuctionList = new JList<>(activeAuctionListModel);
        this.currencyFormat = NumberFormat.getNumberInstance();
        this.currencyFormat.setMinimumFractionDigits(2);
        this.currencyFormat.setMaximumFractionDigits(2);
        createDefaultAuctionIfEmpty();
    }

    private void createAndShow() {
        applyLookAndFeel();

        JFrame frame = new JFrame("AuctionEngine - Swing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(12, 12));

        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topPanel = createStatusPanel();
        JPanel sidebarPanel = createSidebarPanel();
        JPanel centerPanel = createCenterPanel();
        JScrollPane scrollPane = createOutputPane();

        JButton placeBidButton = new JButton("Place Bid");
        JButton showHighestButton = new JButton("Show Highest");
        JButton showSortedButton = new JButton("Show Sorted Bids");
        JButton rangeQueryButton = new JButton("Range Query");
        JButton showAlertsButton = new JButton("Show Alerts");
        JButton snapshotButton = new JButton("Show Snapshot");
        JButton createAuctionButton = new JButton("Create Auction");
        JButton closeAuctionButton = new JButton("Close Auction");
        JButton demoSimulatorButton = new JButton("Demo Simulator");

        JPanel quickBidPanel = createQuickBidPanel();
        JPanel actionsPanel = createActionsPanel(
                placeBidButton,
                showHighestButton,
                showSortedButton,
                rangeQueryButton,
                showAlertsButton,
                snapshotButton,
                createAuctionButton,
                closeAuctionButton,
                demoSimulatorButton
        );

        centerPanel.add(quickBidPanel, BorderLayout.NORTH);
        centerPanel.add(actionsPanel, BorderLayout.CENTER);

        activeAuctionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activeAuctionList.setCellRenderer(new AuctionListRenderer());

        JSplitPane upperSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, centerPanel);
        upperSplit.setResizeWeight(0.30);
        upperSplit.setBorder(null);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperSplit, scrollPane);
        mainSplit.setResizeWeight(0.63);
        mainSplit.setBorder(null);

        rootPanel.add(topPanel, BorderLayout.NORTH);
        rootPanel.add(mainSplit, BorderLayout.CENTER);
        frame.add(rootPanel, BorderLayout.CENTER);

        placeBidButton.addActionListener(e -> placeBid());
        showHighestButton.addActionListener(e -> showHighest());
        showSortedButton.addActionListener(e -> showSortedBids());
        rangeQueryButton.addActionListener(e -> showRangeQuery());
        showAlertsButton.addActionListener(e -> showAlerts());
        snapshotButton.addActionListener(e -> showSnapshot());
        createAuctionButton.addActionListener(e -> createAuction());
        closeAuctionButton.addActionListener(e -> closeAuction());
        demoSimulatorButton.addActionListener(e -> openDemoSimulator());
        activeAuctionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedValue = activeAuctionList.getSelectedValue();
                if (selectedValue != null) {
                    currentAuctionId = parseAuctionId(selectedValue);
                    refreshStatus();
                }
            }
        });

        refreshActiveAuctions();
        refreshStatus();
        frame.pack();
        frame.setMinimumSize(new Dimension(1100, 720));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 10));
        panel.add(createMetricCard("Selected Auction", selectedAuctionLabel));
        panel.add(createMetricCard("Current Highest", highestLabel));
        panel.add(createMetricCard("Total Bids", totalBidsLabel));
        panel.add(createMetricCard("Auction State", statusLabel));
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 220)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));

        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 18f));
        valueLabel.setForeground(new Color(28, 45, 72));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Active Auctions"),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel helperLabel = new JLabel("<html>Select one auction. All bids and queries use the current selection.</html>");
        helperLabel.setForeground(new Color(90, 90, 90));

        panel.add(helperLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(activeAuctionList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Auction Controls"),
                new EmptyBorder(8, 8, 8, 8)
        ));
        return panel;
    }

    private JPanel createQuickBidPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Quick Bid"),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        fieldsPanel.add(new JLabel("Bidder ID"));
        fieldsPanel.add(bidderIdField);
        fieldsPanel.add(new JLabel("Amount"));
        fieldsPanel.add(amountField);

        JLabel hintLabel = new JLabel("Tip: a bid must be higher than the current highest bid.");
        hintLabel.setForeground(new Color(90, 90, 90));

        panel.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(hintLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createActionsPanel(JButton... buttons) {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Operations"),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JPanel grid = new JPanel(new GridLayout(3, 3, 8, 8));
        for (JButton button : buttons) {
            button.setFocusPainted(false);
            grid.add(button);
        }

        JPanel notesPanel = new JPanel();
        notesPanel.setLayout(new BoxLayout(notesPanel, BoxLayout.Y_AXIS));
        notesPanel.add(new JSeparator());
        notesPanel.add(new JLabel(" "));
        notesPanel.add(createMutedLabel("Use Create Auction to add a new timed auction."));
        notesPanel.add(createMutedLabel("Show Snapshot reads from the persistent history."));
        notesPanel.add(createMutedLabel("Demo Simulator generates fraud scenarios automatically."));

        wrapper.add(grid, BorderLayout.CENTER);
        wrapper.add(notesPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JScrollPane createOutputPane() {
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Output"));
        return scrollPane;
    }

    private JLabel createMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(90, 90, 90));
        return label;
    }

    private void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private void placeBid() {
        if (!ensureAuctionSelected()) {
            return;
        }

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

        int beforeCount = engine.getTotalBids(currentAuctionId);
        engine.placeBid(currentAuctionId, bidderId, amount);
        int afterCount = engine.getTotalBids(currentAuctionId);

        if (afterCount > beforeCount) {
            appendOutput("Placed bid in " + currentAuctionId + ": bidder=" + bidderId + ", amount=" + amount);
            bidderIdField.setText("");
            amountField.setText("");
            showLatestAlertsIfAny();
        } else {
            appendOutput("Bid rejected by engine rules.");
        }

        refreshStatus();
    }

    private void showHighest() {
        if (!ensureAuctionSelected()) {
            return;
        }
        Bid highest = engine.getHighestBid(currentAuctionId);
        appendOutput(highest == null ? "No bids yet." : "Highest: " + formatBidForDisplay(highest));
    }

    private void showSortedBids() {
        if (!ensureAuctionSelected()) {
            return;
        }
        int latestVersion = engine.getHistoryVersionCount(currentAuctionId);
        List<Bid> bids = engine.getBidsAtVersion(currentAuctionId, latestVersion);
        appendOutput("Sorted bids for " + currentAuctionId + " at latest version " + latestVersion + ":");
        if (bids.isEmpty()) {
            appendOutput("No bids yet.");
            return;
        }
        for (Bid bid : bids) {
            appendOutput("  " + formatBidForDisplay(bid));
        }
    }

    private void showRangeQuery() {
        if (!ensureAuctionSelected()) {
            return;
        }

        JTextField minField = new JTextField();
        JTextField maxField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Min Amount"));
        panel.add(minField);
        panel.add(new JLabel("Max Amount"));
        panel.add(maxField);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Amount Range Query",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        double minAmount;
        double maxAmount;
        try {
            minAmount = Double.parseDouble(minField.getText().trim());
            maxAmount = Double.parseDouble(maxField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Min/Max amounts must be valid numbers.");
            return;
        }

        if (minAmount > maxAmount) {
            showError("Min amount must be less than or equal to max amount.");
            return;
        }

        List<Bid> bidsInRange = engine.getBidsInAmountRange(currentAuctionId, minAmount, maxAmount);
        appendOutput("Bids in range [" + minAmount + ", " + maxAmount + "] for " + currentAuctionId + ":");
        if (bidsInRange.isEmpty()) {
            appendOutput("No bids found in this range.");
            return;
        }

        for (Bid bid : bidsInRange) {
            appendOutput("  " + formatBidForDisplay(bid));
        }
    }

    private void showAlerts() {
        if (!ensureAuctionSelected()) {
            return;
        }
        List<String> alerts = engine.getRecentAlerts(currentAuctionId);
        appendOutput("Recent alerts for " + currentAuctionId + ":");
        if (alerts.isEmpty()) {
            appendOutput("No alerts.");
            return;
        }
        for (String alert : alerts) {
            appendOutput("  " + alert);
        }
    }

    private void showSnapshot() {
        if (!ensureAuctionSelected()) {
            return;
        }

        String input = JOptionPane.showInputDialog(
                null,
                "Enter version (0 to " + engine.getHistoryVersionCount(currentAuctionId) + "):"
        );
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

        if (!engine.isValidHistoryVersion(currentAuctionId, version)) {
            showError("Invalid version.");
            return;
        }

        Bid highest = engine.getHighestBidAtVersion(currentAuctionId, version);
        appendOutput("Snapshot for " + currentAuctionId + ", version " + version + ":");
        appendOutput("Highest: " + (highest == null ? "None" : formatBidForDisplay(highest)));
        List<Bid> bids = engine.getBidsAtVersion(currentAuctionId, version);
        if (bids.isEmpty()) {
            appendOutput("No bids in this version.");
            return;
        }
        for (Bid bid : bids) {
            appendOutput("  " + formatBidForDisplay(bid));
        }
    }

    private void closeAuction() {
        if (!ensureAuctionSelected()) {
            return;
        }

        if (!engine.isAuctionOpen(currentAuctionId)) {
            appendOutput("Auction is already closed.");
            return;
        }

        engine.closeAuction(currentAuctionId);
        Bid winner = engine.getHighestBid(currentAuctionId);
        appendOutput(winner == null
                ? "Auction " + currentAuctionId + " closed. No bids were placed."
                : "Auction " + currentAuctionId + " closed. Winner: " + winner.getBidderId() + " with amount " + winner.getAmount());
        refreshActiveAuctions();
        refreshStatus();
    }

    private void openDemoSimulator() {
        if (!ensureAuctionSelected()) {
            return;
        }

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
        if (!engine.isAuctionOpen(currentAuctionId)) {
            appendOutputOnEdt("Cannot run demo: auction is closed.");
            return;
        }

        appendOutputOnEdt("Running demo scenario for " + currentAuctionId + ": " + scenario);
        double nextAmount = getNextAmount();

        if ("Rapid Bidding".equals(scenario)) {
            int count = Math.max(bidCount, 3);
            nextAmount = placeBids(count, 1, "rapid_user_", nextAmount, delayMs);
        } else if ("Price Spike".equals(scenario)) {
            if (engine.getHighestBid(currentAuctionId) == null) {
                placeSingleBid("price_base", nextAmount);
                nextAmount += 1.0;
                sleepQuietly(delayMs);
            }
            Bid highest = engine.getHighestBid(currentAuctionId);
            if (highest != null) {
                double spikeAmount = Math.floor(highest.getAmount() * 1.8) + 1.0;
                placeSingleBid("price_user", spikeAmount);
            }
        } else if ("Rapid + Price Alerts".equals(scenario)) {
            nextAmount = placeBids(Math.max(bidCount, 3), 1, "rapid_user_", nextAmount, delayMs);
            Bid highest = engine.getHighestBid(currentAuctionId);
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
        int before = engine.getTotalBids(currentAuctionId);
        engine.placeBid(currentAuctionId, bidderId, amount);
        int after = engine.getTotalBids(currentAuctionId);
        if (after > before) {
            appendOutputOnEdt("Placed bid in " + currentAuctionId + ": bidder=" + bidderId + ", amount=" + amount);
        } else {
            appendOutputOnEdt("Bid rejected during demo in " + currentAuctionId + ": bidder=" + bidderId + ", amount=" + amount);
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
        selectedAuctionLabel.setText(currentAuctionId == null ? "None" : currentAuctionId);
        if (currentAuctionId == null) {
            highestLabel.setText("None");
            totalBidsLabel.setText("0");
            statusLabel.setText("NO AUCTION SELECTED");
            statusLabel.setForeground(new Color(120, 40, 40));
            return;
        }

        Bid highest = engine.getHighestBid(currentAuctionId);
        highestLabel.setText(highest == null ? "None" : formatAmount(highest.getAmount()));
        totalBidsLabel.setText(String.valueOf(engine.getTotalBids(currentAuctionId)));
        boolean open = engine.isAuctionOpen(currentAuctionId);
        statusLabel.setText(open ? "OPEN" : "CLOSED");
        statusLabel.setForeground(open ? new Color(28, 128, 76) : new Color(160, 48, 48));
    }

    private void showLatestAlertsIfAny() {
        List<String> alerts = currentAuctionId == null
                ? List.of()
                : engine.getRecentAlerts(currentAuctionId);
        if (alerts.isEmpty()) {
            return;
        }
        appendOutput("Latest alert: " + alerts.get(alerts.size() - 1));
    }

    private void appendOutput(String line) {
        outputArea.append(line + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void appendOutputOnEdt(String line) {
        SwingUtilities.invokeLater(() -> appendOutput(line));
    }

    private double getNextAmount() {
        if (currentAuctionId == null) {
            return 100.0;
        }
        Bid highest = engine.getHighestBid(currentAuctionId);
        if (highest == null) {
            return 100.0;
        }
        return highest.getAmount() + 1.0;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    private String formatBidForDisplay(Bid bid) {
        return "Bidder: " + bid.getBidderId() + ", Amount: " + formatAmount(bid.getAmount());
    }

    private void createAuction() {
        JTextField auctionIdField = new JTextField();
        JTextField itemNameField = new JTextField();
        JTextField startField = new JTextField(String.valueOf(System.currentTimeMillis()));
        JTextField endField = new JTextField(String.valueOf(System.currentTimeMillis() + 300000));

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Auction ID"));
        panel.add(auctionIdField);
        panel.add(new JLabel("Item Name"));
        panel.add(itemNameField);
        panel.add(new JLabel("Start Time (epoch millis)"));
        panel.add(startField);
        panel.add(new JLabel("End Time (epoch millis)"));
        panel.add(endField);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Create Auction",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        long start;
        long end;
        try {
            start = Long.parseLong(startField.getText().trim());
            end = Long.parseLong(endField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Start and end time must be valid epoch milliseconds.");
            return;
        }

        try {
            engine.createAuction(
                    auctionIdField.getText().trim(),
                    itemNameField.getText().trim(),
                    start,
                    end
            );
            appendOutput("Created auction: " + auctionIdField.getText().trim());
            refreshActiveAuctions();
            currentAuctionId = auctionIdField.getText().trim();
            activeAuctionList.setSelectedValue(formatAuctionForList(engine.getAuction(currentAuctionId)), true);
            refreshStatus();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshActiveAuctions() {
        List<Auction> activeAuctions = engine.getActiveAuctions();
        activeAuctionListModel.clear();
        for (Auction auction : activeAuctions) {
            activeAuctionListModel.addElement(formatAuctionForList(auction));
        }

        if (currentAuctionId != null && engine.isAuctionActive(currentAuctionId)) {
            activeAuctionList.setSelectedValue(formatAuctionForList(engine.getAuction(currentAuctionId)), true);
            return;
        }

        if (!activeAuctions.isEmpty()) {
            currentAuctionId = activeAuctions.get(0).getAuctionId();
            activeAuctionList.setSelectedIndex(0);
        } else if (!engine.getAllAuctions().isEmpty()) {
            currentAuctionId = engine.getAllAuctions().get(0).getAuctionId();
        } else {
            currentAuctionId = null;
        }
    }

    private boolean ensureAuctionSelected() {
        if (currentAuctionId == null) {
            showError("Select or create an auction first.");
            return false;
        }
        return true;
    }

    private String formatAuctionForList(Auction auction) {
        return auction.getAuctionId() + " | " + auction.getItemName()
                + " | " + auction.getStartTimeMillis() + " - " + auction.getEndTimeMillis();
    }

    private String formatAmount(double amount) {
        return currencyFormat.format(amount);
    }

    private String formatMillis(long timeMillis) {
        return LIST_TIME_FORMATTER.format(Instant.ofEpochMilli(timeMillis));
    }

    private String parseAuctionId(String listValue) {
        int separatorIndex = listValue.indexOf(" | ");
        return separatorIndex < 0 ? listValue : listValue.substring(0, separatorIndex);
    }

    private void createDefaultAuctionIfEmpty() {
        long now = System.currentTimeMillis();
        engine.createAuction("AUCT-1", "Demo Item", now - 60000, now + 3600000);
        currentAuctionId = "AUCT-1";
    }

    private final class AuctionListRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String auctionId = parseAuctionId(String.valueOf(value));
            Auction auction = engine.getAuction(auctionId);
            if (auction == null) {
                return label;
            }

            label.setBorder(new EmptyBorder(8, 10, 8, 10));
            label.setVerticalAlignment(JLabel.TOP);
            label.setText("<html><b>" + auction.getAuctionId() + "</b> - " + auction.getItemName()
                    + "<br><span style='color:#666666;'>"
                    + formatMillis(auction.getStartTimeMillis()) + " to " + formatMillis(auction.getEndTimeMillis())
                    + "</span></html>");
            return label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuctionEngineSwingApp().createAndShow());
    }
}
