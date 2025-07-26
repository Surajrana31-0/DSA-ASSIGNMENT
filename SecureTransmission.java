import java.util.*;

/**
 * SecureTransmission
 * ------------------
 * This program simulates a secure network of communication links.
 *
 * Each link between two nodes (computers/routers) has a **signal strength value**.
 * A transmission from a sender to a receiver is possible only if there exists
 * a path where **all links on the path have strength < maxStrength** (a given threshold).
 *
 * It answers queries like:
 *   → Can we send a message from sender to receiver securely under the maxStrength constraint?
 */
public class SecureTransmission {

    private int n;              // number of nodes in the network
    private int[][] minEdge;    // adjacency matrix storing the *minimum* link strength between nodes

    /**
     * Constructor to build the network graph.
     *
     * @param n      Number of nodes in the network
     * @param links  Array of {nodeA, nodeB, strength} edges
     */
    public SecureTransmission(int n, int[][] links) {
        this.n = n;

        // Initialize all edges with "infinity" (no direct connection yet)
        minEdge = new int[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(minEdge[i], Integer.MAX_VALUE);
        }

        // Add given bidirectional links and keep the *minimum* strength
        // (If multiple links exist, keep the smallest one)
        for (int[] link : links) {
            int a = link[0];
            int b = link[1];
            int s = link[2];

            // Store the minimum signal strength for that edge
            if (s < minEdge[a][b]) {
                minEdge[a][b] = s;
                minEdge[b][a] = s; // because it's bidirectional
            }
        }
    }

    /**
     * Checks if we can transmit a message from `sender` to `receiver`
     * using only links with strength < maxStrength.
     *
     * @param sender       Starting node
     * @param receiver     Destination node
     * @param maxStrength  Maximum allowed signal strength
     * @return true if a valid path exists, false otherwise
     */
    public boolean canTransmit(int sender, int receiver, int maxStrength) {

        // Keep track of visited nodes to avoid loops
        boolean[] visited = new boolean[n];

        // BFS (Breadth-First Search) queue for traversal
        Queue<Integer> queue = new LinkedList<>();

        // Start from sender node
        visited[sender] = true;
        queue.add(sender);

        // BFS traversal
        while (!queue.isEmpty()) {
            int u = queue.poll(); // current node

            // If we reached the receiver, return true
            if (u == receiver) return true;

            // Explore all possible neighbors
            for (int v = 0; v < n; v++) {
                // Condition:
                //   - There is a valid link (strength < maxStrength)
                //   - Neighbor not yet visited
                if (minEdge[u][v] < maxStrength && !visited[v]) {
                    visited[v] = true;
                    queue.add(v);
                }
            }
        }

        // If BFS finishes without reaching receiver → not possible
        return false;
    }

    public static void main(String[] args) {

        /**
         * Network description:
         *
         * 0 --4-- 2 --1-- 3
         *       \       /
         *        \-3-- 1
         *
         * 4 --5-- 5   (separate component)
         *
         * Each link: {nodeA, nodeB, strength}
         */
        int[][] links = {
            {0, 2, 4}, 
            {2, 3, 1}, 
            {2, 1, 3}, 
            {4, 5, 5},
            {3, 0, 2}  // Extra edge connecting 3 and 0
        };

        SecureTransmission st = new SecureTransmission(6, links);

        /**
         * Query 1:
         *   Can we transmit from node 2 → 3 with maxStrength=2?
         *   Path: 2 → 3 has strength 1 (<2) → YES
         */
        System.out.println("Query 1: " + st.canTransmit(2, 3, 2));  // true

        /**
         * Query 2:
         *   Can we transmit from 1 → 3 with maxStrength=3?
         *   Possible path: 1 → 2 (3) → 3 (1)
         *   BUT 1→2 has strength == 3, NOT <3 → NOT allowed → NO
         */
        System.out.println("Query 2: " + st.canTransmit(1, 3, 3));  // false

        /**
         * Query 3:
         *   Can we transmit from 2 → 0 with maxStrength=3?
         *   Path: 2 → 3 (1) → 0 (2), all <3 → YES
         */
        System.out.println("Query 3: " + st.canTransmit(2, 0, 3));  // true

        /**
         * Query 4:
         *   Can we transmit from 0 → 5 with maxStrength=6?
         *   Nodes 4 & 5 are a disconnected component
         *   → NO path → NO
         */
        System.out.println("Query 4: " + st.canTransmit(0, 5, 6));  // false
    }
}
