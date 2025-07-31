# 1. Tech Startup Capital Optimization

## Problem Description
A tech startup, AlgoStart, needs to maximize its revenue before acquisition by selecting up to `k` distinct projects. Each project has an investment requirement and a revenue gain. The startup begins with initial capital `c` and can only select projects where the investment ≤ current capital. Upon completing a project, the revenue is added to the capital, which can be reinvested in subsequent projects.

**Inputs:**
- `k`: Maximum number of projects (integer)
- `c`: Initial capital (integer)
- `investments[]`: Minimum investment required for each project (integer array)
- `revenues[]`: Revenue gain from completing each project (integer array)

**Output:**  
Maximum possible capital after completing up to `k` projects (integer)

**Constraints:**
- Can only select projects where investment ≤ current capital
- At most `k` projects can be selected
- Capital grows cumulatively after each project

## Solution Approach
The solution uses a greedy algorithm with optimal project selection:
1. **Sort Projects** by investment requirement (ascending)
2. **Max-Heap** tracks highest-revenue affordable projects
3. **Iterative Selection:**
   - Add all newly affordable projects to heap
   - Select highest-revenue project
   - Reinvest capital
   - Repeat up to `k` times

**Time Complexity:** O(n log n + k log n)  
**Space Complexity:** O(n)


### Steps:
1. **Compile:**
   ```bash
   javac TechStartupCapitalOptimizer.java

# 1B. Secure Bank PIN Solution
## Compilation
`javac SecureBankPIN.java`
## Execution
`java SecureBankPIN`
## Output
Example 1: 3  
Example 2: 2  
Example 3: 0  
Case 4: 3  
Case 5: 2  
Case 6: 3


#2 Weather Anomaly Detection Solution
## Compilation
`javac WeatherAnomalyDetection.java`
## Execution
`java WeatherAnomalyDetection`
## Output
Example 1: 3
Example 2: 4
Case 3: 1
Case 4: 0
Case 5: 6
Case 6: 6
Case 7: 3
Case 8: 3

# Cryptarithmetic Puzzle Solution
## Compilation
`javac CryptarithmeticSolver.java`
## Execution
`java CryptarithmeticSolver`
## Output
CODE + BUG = DEBUG: false
SEND + MORE = MONEY: true
A + A = B: true
AA + BB = CCD: false
SIX + SEVEN = TWELVE: false



#3 Pattern Sequence Derivation Solution
## Compilation
`javac PatternDerivation.java`
## Execution
`java PatternDerivation`
## Output
Example: 6
Case 1: 5
Case 2: 3000
Case 3: 10
Case 4: 4

# Magical Words Power Combination Solution
## Compilation
`javac MagicalWords.java`
## Execution
`java MagicalWords`
## Output
Example 1: 5
Example 2: 35
Case 3: 1
Case 4: 0
Case 5: 3
Case 6: 0


# 4 Secure Transmission Solution
## Compilation
`javac SecureTransmission.java`
## Execution
`java SecureTransmission`
## Output
Query 1: true
Query 2: false
Query 3: true
Query 4: false

# Treasure Hunt


# 5 A Maze Solver 

Online Ticket Booking System – Concurrency Control (GUI)
Overview
This project implements a simulated online ticket booking system with a graphical user interface (GUI) that demonstrates concurrency control using optimistic and pessimistic locking mechanisms. It simulates multiple users booking seats concurrently while preventing race conditions, using threads, locks, and a queue for incoming booking requests.
The system visually displays:

A seating chart showing available, locked (processing), and booked seats.
A queue view for pending booking requests.
Detailed logs and booking history.
Controls to select concurrency control mode, add bookings, process requests, simulate multiple users, and cancel or reset bookings.

This project tackles key concurrency problems such as race conditions, deadlock prevention, and thread-safe GUI updates using Java Swing components and concurrency APIs.
Features
Concurrency Control Mechanisms

Optimistic Locking: Check seat availability, attempt booking with a version check, retry on conflicts.
Pessimistic Locking: Acquire seat lock before booking, ensuring exclusive access during booking.
Booking Request Queue: Thread-safe queue managing incoming booking requests.
Multiple User Simulation: Generate booking requests concurrently from multiple simulated users.

Real-Time GUI

Seating chart with color-coded buttons reflecting seat status.
Live updating pending booking requests and booking history.
Booking logs and statistics for successful and failed bookings.

User Controls

Buttons to add booking requests, process bookings, simulate concurrent users, cancel requests, pause/resume processing, reset system, and login/logout users.
Radio buttons to select between optimistic and pessimistic locking.

Concurrency Safety

Thread pooling for booking processing.
Proper synchronization and locking strategies.
Safe updates to GUI components using Event Dispatch Thread.

Project Structure



File
Description



BookingPanel.java
Main GUI panel with seat chart, queue, and controls.


BookingProcessor.java
Manages booking processing in threads using chosen locking.


BookingRequest.java
Represents a booking request by a user for a specific seat.


SeatManager.java
Manages seat states and locking mechanisms.


SeatStatus.java
Enum defining seat states (AVAILABLE, BOOKED, LOCKED).


Main.java
Launches the application window with BookingPanel.


Prerequisites

Java Development Kit (JDK) 8 or higher installed and the java and javac commands available in your system PATH.
A terminal or command prompt to compile and run Java programs.

Compilation Instructions

Place all source files (BookingPanel.java, BookingProcessor.java, BookingRequest.java, SeatManager.java, SeatStatus.java, and Main.java) in a directory named Question5 (the package folder).
Open a terminal/command prompt, navigate to the folder containing the Question5 directory.
Compile all Java source files at once with:javac Question5/*.java

This will generate corresponding .class files inside the Question5 directory.

Running the Application
Run the compiled program by executing:
java Question5.Main

This will open the GUI window for the Online Ticket Booking System.
Usage Guide
Initial Setup

Upon launch, a login dialog prompts for your username. Enter any non-empty username to start.

Main View Components

Seating Chart (Center):
Displays all seats as buttons, color-coded:
Green: Available
Red: Booked
Yellow/Orange: Processing/Locked




Pending Booking Requests (Right, “Pending Queue” Tab):
Shows all booking requests waiting to be processed.


Booking History (Right, “Booking History” Tab):
Shows past booking results (success or failure).


Booking Log (Bottom):
Shows logs of booking attempts, successes, failures, and system messages.


Statistics (Below Seating Chart / Log):
Displays counts of total bookings attempted, successes, failures, retries, cancellations, and queue size.



User Controls (Top Panel)

Locking Mode:
Choose between Optimistic Locking or Pessimistic Locking to test different concurrency control mechanisms.


Add Random Booking Requests:
Adds multiple random booking requests as your logged-in user.


Process Bookings:
Starts processing booking requests concurrently with the selected locking mode.


Pause / Resume:
Pause or resume booking processing threads.


Cancel Selected Request:
Cancel a selected booking request from the pending queue.


Reset Seats:
Resets the seat statuses to available, clears all requests, logs, and history.


Simulate Users:
Launches multiple concurrent simulated users generating booking requests randomly (simulates concurrency).


Login:
Change the current logged-in user (required before making bookings).



Notes and Tips

The system supports simultaneous booking requests processed by a thread pool.
Optimistic locking uses seat status versioning to detect conflicts.
Pessimistic locking acquires seat-specific locks, preventing simultaneous booking.
GUI updates are thread-safe to avoid race conditions or inconsistent visuals.
You can observe booking conflicts, retries, and failures in the log and stats area.
Cancel requests intelligently to avoid stale or invalid bookings.
Use the "Simulate Users" button to stress-test concurrency controls.

Troubleshooting

Compilation Errors?
Confirm you ran javac Question5/*.java from the parent directory, that Java version is 8+, and all files are in the Question5 package folder.


GUI Not Showing / Errors on Launch?
Check Java Swing compatibility with your platform; ensure no other program is using port bindings or resources.


Threading or Locking Issues?
Make sure you use the buttons as intended (pause before reset, etc.) and avoid manually terminating the program to allow graceful thread shutdown.



