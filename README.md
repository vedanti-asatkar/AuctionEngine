# AuctionEngine

AuctionEngine is a Java auction simulator that demonstrates how multiple data structures can work together in a bidding system. The project currently includes both a console application and a Swing desktop UI built on the same backend engine.

## Current Features

- Place bids with validation
- Track the current highest bid in real time
- View all accepted bids in sorted order
- Detect rapid bidding activity
- Detect suspicious price spikes
- Store recent fraud alerts
- Inspect historical snapshots of auction state
- Search bids by amount range from the Swing UI
- Run built-in demo scenarios from the Swing UI

## Entry Points

- `Main`
  - Console-based auction app
- `AuctionEngineSwingApp`
  - Swing desktop application with buttons for bidding, alerts, snapshots, range queries, and demo simulation

## Data Structures Used

- `MaxHeap`
  - Maintains fast access to the highest bid
- `RedBlackTree`
  - Keeps bids sorted for ordered traversal
- `PersistentBidTree`
  - Stores versioned snapshots of bidding history
- `BPlusTree`
  - Supports amount-based lookup and range queries
- `HashMap<String, List<Long>>` in `FraudDetector`
  - Tracks bidder activity timestamps for rapid-bidding detection

## Fraud Detection Rules

- Rapid bidding
  - Triggered when the same bidder places at least `3` bids within `20` seconds
- Price spike
  - Triggered when a new bid is greater than `1.7x` the current highest bid

Alerts are stored in memory and the engine keeps up to `50` recent alerts.

## Project Structure

```text
src/
  AuctionEngineSwingApp.java
  Main.java
  model/
    Bid.java
  service/
    AuctionEngine.java
    FraudDetector.java
  structures/
    BPlusTree.java
    MaxHeap.java
    PersistentBidTree.java
    RedBlackTree.java
```

## Requirements

- JDK 8 or newer
- No external dependencies

Check Java:

```powershell
java -version
javac -version
```

## How To Build

From the project root:

```powershell
$files = Get-ChildItem -Recurse -Path src -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $files
```

This compiles all source files into the `out/` directory.

## How To Run

### Run the Console App

```powershell
java -cp out Main
```

Console menu options currently include:

- Place Bid
- Show Highest Bid
- Show All Bids (Sorted)
- Show Recent Alerts
- Close Auction
- Show Historical Snapshot

### Run the Swing App

```powershell
java -cp out AuctionEngineSwingApp
```

The Swing app currently lets you:

- Place bids
- View highest bid and total bid count
- Show sorted bids
- Run amount range queries
- View recent alerts
- Inspect history snapshots
- Close the auction
- Run demo scenarios for suspicious bidding behavior

## Typical Workflow

1. Compile the project.
2. Start either `Main` or `AuctionEngineSwingApp`.
3. Place bids with increasing amounts.
4. Check alerts and snapshots as bids are added.
5. Close the auction to lock the winner.

## Implementation Notes

- Bids must be greater than the current highest bid.
- Closed auctions reject new bids.
- Snapshot version `0` is the empty initial state.
- Bid timestamps are generated with `System.currentTimeMillis()`.
- `Bid.toString()` prints both a readable timestamp and raw milliseconds.

## Output Directories

- `out/`
  - Recommended manual compile output
- `bin/`
  - May be used by some IDE setups

## Running In VS Code

If you use the Java extensions in VS Code:

1. Open the project folder.
2. Let VS Code detect the Java sources.
3. Run either `src/Main.java` or `src/AuctionEngineSwingApp.java`.

## Known Scope

- Data is in-memory only and is not persisted to disk.
- There is no networking, database, or authentication layer.
- Fraud alerts are heuristic and intended for demonstration purposes.
