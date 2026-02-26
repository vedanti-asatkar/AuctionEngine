# AuctionEngine

Console-based online auction simulator in Java, focused on data-structure-driven operations:
- fast highest-bid lookup
- sorted bid listing
- rapid bidding detection
- price spike detection
- historical snapshot/version queries

## Tech Stack

- Java (standard library only)
- No external dependencies

## Features

- Place bids with validation:
  - auction must be open
  - amount must be positive
  - new bid must be strictly greater than current highest
- Show current highest bid
- Show all bids sorted by amount (ascending)
- Detect rapid bidding per bidder in a recent time window
- Detect price spikes using a multiplier threshold
- Close auction and announce winner
- Query historical snapshots by version

## Data Structures Used

1. `MaxHeap` (`src/structures/MaxHeap.java`)
- Purpose: instant access to highest bid
- Key operation: root (`getMax`) stores current maximum

2. `RedBlackTree` (`src/structures/RedBlackTree.java`)
- Purpose: maintain bids in sorted order
- Key operation: inorder traversal prints bids low to high

3. `PersistentBidTree` (`src/structures/PersistentBidTree.java`)
- Purpose: immutable versioned history of bids
- Key operation: each new bid creates a new root/version
- Supports:
  - highest bid at any version
  - full sorted bid list at any version

4. `HashMap<String, List<Long>>` in `FraudDetector` (`src/service/FraudDetector.java`)
- Purpose: track bidder activity timestamps
- Key operation: remove outdated timestamps and compare recent count to threshold

## Detection Logic

- Rapid bidding:
  - Input: `bidderId`, `windowSeconds`, `threshold`
  - Rule: rapid bidding if bids in last window are `> threshold`

- Price spike:
  - Constant: `PRICE_SPIKE_MULTIPLIER = 1.7`
  - Rule: spike if `newBidAmount > currentHighest * 1.7`

## Project Structure

```text
src/
  Main.java
  model/
    Bid.java
  service/
    AuctionEngine.java
    FraudDetector.java
  structures/
    MaxHeap.java
    RedBlackTree.java
    PersistentBidTree.java
```

## How To Run

### Option 1: VS Code

1. Open project in VS Code.
2. Ensure Java extension pack is installed.
3. Run `Main.java`.

### Option 2: Command Line

From project root:

```bash
javac -d out src/Main.java src/model/*.java src/service/*.java src/structures/*.java
java -cp out Main
```

On Windows PowerShell (if wildcard expansion is limited), use:

```powershell
$files = Get-ChildItem -Recurse -Path src -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $files
java -cp out Main
```

## Menu Overview

1. Place Bid
2. Show Highest Bid
3. Show All Bids (Sorted)
4. Check Rapid Bidding (Bidder)
5. Check Price Spike (Amount)
6. Close Auction
7. Show Historical Snapshot
8. Exit

## Notes

- Version `0` in history is the empty initial snapshot.
- Bid timestamps use system time (`System.currentTimeMillis()`).
- Compiled outputs may appear in `out/` or `bin/` depending on IDE settings.
