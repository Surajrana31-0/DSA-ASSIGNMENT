1. Tech Startup Capital Optimization

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