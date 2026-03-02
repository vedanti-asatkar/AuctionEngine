# AuctionEngine

AuctionEngine is a Java console application that simulates an online auction system with efficient data-structure-backed operations.

## Highlights

- Real-time highest bid tracking
- Sorted bid listing
- Rapid bidding detection per bidder
- Price spike detection
- Historical snapshots of bidding state

## Tech Stack

- Java (standard library only)
- No external dependencies

## Core Features

1. Place Bid
- Accepts bidder ID and amount
- Validates:
  - auction is open
  - amount is positive
  - amount is strictly greater than current highest bid
- Runs fraud checks (rapid bidding and price spike)

2. Show Highest Bid
- Displays current highest bid instantly

3. Show All Bids (Sorted)
- Prints all bids from lowest to highest amount

4. Show Recent Alerts
- Displays recent automatic fraud alerts generated during bid placement

5. Close Auction
- Stops new bids and displays winner

6. Show Historical Snapshot
- Lets you inspect any version of the auction history

## Data Structures and Why They Are Used

1. `MaxHeap` (`src/structures/MaxHeap.java`)
- Maintains highest bid at heap root for `O(1)` max lookup

2. `RedBlackTree` (`src/structures/RedBlackTree.java`)
- Maintains bids in balanced sorted structure
- Inorder traversal outputs bids in ascending order

3. `PersistentBidTree` (`src/structures/PersistentBidTree.java`)
- Creates immutable versions after each bid
- Enables historical queries without modifying past states

4. `HashMap<String, List<Long>>` in `FraudDetector` (`src/service/FraudDetector.java`)
- Stores bid timestamps per bidder
- Supports sliding-window rapid bidding detection

## Fraud Detection Rules

- Rapid bidding:
  - Automatic on each accepted bid
  - Default: `3 bids` in `20 seconds` by the same bidder
  - Condition: bids in window `>= threshold`

- Price spike:
  - Constant: `PRICE_SPIKE_MULTIPLIER = 1.7`
  - Condition: `newBidAmount > currentHighest * 1.7`

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

## Run Locally

### VS Code

1. Open the project.
2. Install Java extensions if prompted.
3. Run `src/Main.java`.

### PowerShell / CLI

From project root:

```powershell
$files = Get-ChildItem -Recurse -Path src -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $files
java -cp out Main
```

## Create GitHub Repository (Recommended)

From project root:

```powershell
git init
git add .
git commit -m "Initial commit: AuctionEngine"
git branch -M main
git remote add origin https://github.com/<your-username>/AuctionEngine.git
git push -u origin main
```

## Notes

- History version `0` is an empty initial snapshot.
- Bid timestamps are stored with `System.currentTimeMillis()`.
- Build outputs may appear in `out/` or `bin/` depending on IDE configuration.
