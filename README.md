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

# 2. Secure Bank PIN Solution
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


# Weather Anomaly Detection Solution
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



# Pattern Sequence Derivation Solution
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


# Secure Transmission Solution
## Compilation
`javac SecureTransmission.java`
## Execution
`java SecureTransmission`
## Output
Query 1: true
Query 2: false
Query 3: true
Query 4: false

