import java.util.*;

/**
 * TreasureHunt Game Simulation
 * ----------------------------
 * This simulates a "cat-and-mouse" style game on a graph.
 *
 * Rules:
 * 1. Mouse starts at node 1, Cat starts at node 2.
 * 2. Mouse moves first (turn = 0), then Cat moves (turn = 1), alternating turns.
 * 3. The game ends when:
 *    - Mouse reaches node 0 → Mouse wins (return 1)
 *    - Cat catches the Mouse (same position) → Cat wins (return 2)
 *    - If a state repeats (cycle), it’s a draw → return 0
 *
 * Approach:
 *  - We represent a **state** as (cat_position, mouse_position, turn)
 *  - DFS with memoization + cycle detection to determine the outcome
 */
public class TreasureHunt {

    private int[][] graph;          // adjacency list for the graph
    private int[][][] memo;         // memo[cat][mouse][turn] stores game result
                                    // -1 = unknown, 0 = draw, 1 = mouse wins, 2 = cat wins

    /**
     * Simulates the game on the given graph.
     * @param graph adjacency list of the graph
     * @return 0 = draw, 1 = mouse wins, 2 = cat wins
     */
    public int simulateGame(int[][] graph) {
        this.graph = graph;
        int n = graph.length;

        // Initialize memo table with -1 (unknown)
        memo = new int[n][n][2];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Arrays.fill(memo[i][j], -1);
            }
        }

        // Start DFS from initial positions:
        // Cat at 2, Mouse at 1, and turn=0 (mouse moves first)
        return dfs(2, 1, 0, new boolean[n][n][2]);
    }

    /**
     * DFS function to determine the game outcome from a given state.
     *
     * @param cat    current cat position
     * @param mouse  current mouse position
     * @param turn   whose turn? 0=mouse, 1=cat
     * @param inStack tracks currently visited states to detect cycles
     * @return outcome of the game (0 = draw, 1 = mouse wins, 2 = cat wins)
     */
    private int dfs(int cat, int mouse, int turn, boolean[][][] inStack) {

        // Base cases:
        if (mouse == 0) return 1;   // Mouse reached node 0 → mouse wins
        if (cat == mouse) return 2; // Cat caught the mouse → cat wins

        // If we already computed this state, return cached result
        if (memo[cat][mouse][turn] != -1) return memo[cat][mouse][turn];

        // If we revisit the exact same state → it's a draw (cycle)
        if (inStack[cat][mouse][turn]) return 0;

        // Mark this state as in recursion stack (cycle detection)
        inStack[cat][mouse][turn] = true;
        int res;

        if (turn == 0) { // Mouse's turn
            res = 2; // Assume worst: cat wins unless we find a better move
            for (int next : graph[mouse]) {
                int outcome = dfs(cat, next, 1, inStack); // next mouse move → cat's turn
                if (outcome == 1) {
                    // If mouse can find a winning move, take it
                    res = 1;
                    break;
                } else if (outcome == 0) {
                    // If at least one move leads to a draw, remember it
                    res = 0;
                }
            }
        } else { // Cat's turn
            res = 1; // Assume mouse wins unless we find a better move
            for (int next : graph[cat]) {
                if (next == 0) continue; // Cat cannot move into the hole (node 0)
                int outcome = dfs(next, mouse, 0, inStack); // next cat move → mouse's turn
                if (outcome == 2) {
                    // If cat can force a win, take it
                    res = 2;
                    break;
                } else if (outcome == 0) {
                    // If at least one move leads to a draw, remember it
                    res = 0;
                }
            }
        }

        // Unmark recursion stack
        inStack[cat][mouse][turn] = false;

        // Memoize result before returning
        memo[cat][mouse][turn] = res;
        return res;
    }

    public static void main(String[] args) {

        // ==========================
        // TEST CASE 1 (Original)
        // ==========================
        int[][] graph1 = {
            {2, 5},    // Node 0 connections
            {3},       // Node 1
            {0, 4, 5}, // Node 2
            {1, 4, 5}, // Node 3
            {2, 3},    // Node 4
            {0, 2, 3}  // Node 5
        };
        TreasureHunt game1 = new TreasureHunt();
        int result1 = game1.simulateGame(graph1);
        System.out.println("Game 1 Outcome: " + result1); // Expected: 0 (draw)

        // ==========================
        // TEST CASE 2 (Mouse wins easily)
        // ==========================
        int[][] graph2 = {
            {1},     // Node 0 connects to Node 1
            {0, 2},  // Node 1 connects to hole (0)
            {1, 3},  // Node 2 connects to Node 1
            {2}      // Node 3 connects to Node 2
        };
        TreasureHunt game2 = new TreasureHunt();
        int result2 = game2.simulateGame(graph2);
        System.out.println("Game 2 Outcome: " + result2); // Expected: 1 (mouse wins)

        // ==========================
        // TEST CASE 3 (Cat wins)
        // ==========================
        int[][] graph3 = {
            {1},     // Node 0 connects to Node 1
            {0, 2},  // Node 1 connects to hole
            {1, 3},  // Node 2 connects to Node 1
            {2, 4},  // Node 3 connects to 2 & 4
            {3}      // Node 4 only connects back to Node 3
        };
        TreasureHunt game3 = new TreasureHunt();
        int result3 = game3.simulateGame(graph3);
        System.out.println("Game 3 Outcome: " + result3); // Expected: 2 (cat wins)
    }
}
