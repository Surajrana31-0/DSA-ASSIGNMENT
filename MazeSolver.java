import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Maze Solver Application
 * 
 * This program implements a maze solving application with visualization capabilities.
 * It features:
 * - Random maze generation using recursive backtracking
 * - Graph representation of the maze (adjacency list)
 * - DFS (Depth-First Search) using a stack
 * - BFS (Breadth-First Search) using a queue
 * - Interactive GUI with visualization of solving process
 * - Statistics tracking for algorithm performance
 * 
 * Approach:
 * 1. Maze Generation: Uses recursive backtracking to generate a solvable maze
 * 2. Graph Representation: Each cell is a node, connected to adjacent walkable cells
 * 3. Solving Algorithms:
 *    - DFS: Explores paths deeply using a stack (LIFO)
 *    - BFS: Explores all neighbors at current depth before moving deeper (FIFO)
 * 4. Visualization: Animates the solving process with color-coded cells
 * 
 * Time Complexity:
 * - Maze Generation: O(n^2) where n is grid size (each cell processed once)
 * - Graph Construction: O(n^2) (each cell and its neighbors processed)
 * - DFS/BFS: O(V + E) where V is vertices, E is edges (worst-case explores entire maze)
 * 
 * Space Complexity: O(n^2) to store grid and graph
 */
public class MazeSolver extends JFrame {
    // Constants for maze configuration
    private static final int SIZE = 31; // Maze dimensions (must be odd for proper generation)
    private static final int CELL_SIZE = 20; // Size of each cell in pixels
    private static final Color WALL_COLOR = new Color(30, 30, 70); // Dark blue walls
    private static final Color PATH_COLOR = new Color(240, 248, 255); // Light blue paths
    private static final Color START_COLOR = new Color(50, 205, 50); // Green start point
    private static final Color END_COLOR = new Color(220, 20, 60); // Red end point
    private static final Color EXPLORED_COLOR = new Color(135, 206, 250); // Light blue for explored cells
    private static final Color SOLUTION_COLOR = new Color(255, 215, 0); // Gold solution path
    private static final Color CURRENT_COLOR = new Color(255, 140, 0); // Orange for current cell
    
    // GUI components
    private JPanel mazePanel; // Main panel for maze display
    private JButton generateButton, dfsButton, bfsButton, resetButton;
    private JButton setStartButton, setEndButton;
    private JLabel statusLabel, statsLabel; // Information displays
    
    // Maze data structures
    private Cell[][] grid; // 2D array representing the maze
    private Point start, end; // Start and end positions
    private boolean solving = false; // Flag for solving in progress
    private boolean settingStart = false; // Flag for setting start point
    private boolean settingEnd = false; // Flag for setting end point
    
    // Graph representation of the maze
    private Map<Point, List<Point>> graph; // Adjacency list: Key = cell, Value = neighbors
    
    // Algorithm statistics
    private int dfsSteps, bfsSteps; // Steps taken by each algorithm
    private long dfsTime, bfsTime; // Execution time in milliseconds
    private int pathLength; // Length of solution path

    /**
     * Constructor: Sets up the GUI and initializes the maze
     */
    public MazeSolver() {
        // Configure main window
        setTitle("Advanced Maze Solver");
        setSize(SIZE * CELL_SIZE + 300, SIZE * CELL_SIZE + 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));
        
        // Initialize maze panel with grid layout
        mazePanel = new JPanel(new GridLayout(SIZE, SIZE));
        mazePanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        mazePanel.setBackground(new Color(220, 220, 220));
        grid = new Cell[SIZE][SIZE];
        graph = new HashMap<>();
        
        // Create grid cells
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = new Cell(row, col);
                grid[row][col].setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                grid[row][col].setBorder(BorderFactory.createLineBorder(new Color(180, 180, 220), 1));
                grid[row][col].addMouseListener(new CellMouseListener());
                mazePanel.add(grid[row][col]);
            }
        }
        
        // Create control panel with buttons and status displays
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setBackground(new Color(240, 248, 255));
        
        // Button panel configuration
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));
        
        // Create styled buttons
        generateButton = createStyledButton("Generate New Maze", new Color(70, 130, 180));
        dfsButton = createStyledButton("Solve with DFS", new Color(46, 139, 87));
        bfsButton = createStyledButton("Solve with BFS", new Color(178, 34, 34));
        setStartButton = createStyledButton("Set Start Point", new Color(50, 205, 50));
        setEndButton = createStyledButton("Set End Point", new Color(220, 20, 60));
        resetButton = createStyledButton("Reset Solution", new Color(148, 0, 211));
        
        // Add action listeners to buttons
        generateButton.addActionListener(e -> generateMaze());
        dfsButton.addActionListener(e -> solveMaze(true));
        bfsButton.addActionListener(e -> solveMaze(false));
        resetButton.addActionListener(e -> resetSolution());
        setStartButton.addActionListener(e -> settingStart = true);
        setEndButton.addActionListener(e -> settingEnd = true);
        
        // Add buttons to panel
        buttonPanel.add(generateButton);
        buttonPanel.add(dfsButton);
        buttonPanel.add(bfsButton);
        buttonPanel.add(setStartButton);
        buttonPanel.add(setEndButton);
        buttonPanel.add(resetButton);
        
        // Status panel configuration
        JPanel statusPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        statusPanel.setBackground(new Color(240, 248, 255));
        
        // Create status labels
        statusLabel = new JLabel("Click 'Generate New Maze' to start", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(new Color(25, 25, 112));
        
        statsLabel = new JLabel("DFS Steps: 0 | BFS Steps: 0 | Path Length: 0", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statsLabel.setForeground(new Color(47, 79, 79));
        
        // Add components to panels
        statusPanel.add(statusLabel);
        statusPanel.add(statsLabel);
        controlPanel.add(buttonPanel);
        controlPanel.add(statusPanel);
        
        // Add main components to frame
        add(mazePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        // Generate initial maze
        generateMaze();
    }

    /**
     * Creates a styled button with consistent appearance
     * 
     * @param text Button text
     * @param bgColor Background color
     * @return Styled JButton
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        return button;
    }

    /**
     * Generates a new random maze
     * 
     * Algorithm: Recursive Backtracking
     * 1. Start with grid full of walls
     * 2. Pick initial cell, mark as path
     * 3. Randomly select adjacent unvisited cell (2 steps away)
     * 4. Remove wall between current cell and chosen cell
     * 5. Recursively process chosen cell
     * 6. Ensure end point is reachable
     */
    private void generateMaze() {
        if (solving) return; // Don't generate during solving
        
        // Reset all cells to walls
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col].setType(CellType.WALL);
            }
        }
        
        // Generate maze paths
        generateMazeRecursive(1, 1);
        
        // Set start and end positions
        start = new Point(1, 1);
        end = new Point(SIZE - 2, SIZE - 2);
        
        // Update GUI for start/end
        grid[start.x][start.y].setType(CellType.START);
        grid[end.x][end.y].setType(CellType.END);
        
        // Build graph representation
        buildGraph();
        
        // Reset setting flags
        settingStart = false;
        settingEnd = false;
        statusLabel.setText("Maze generated. Select an algorithm to solve.");
        updateStats();
    }

    /**
     * Builds graph representation of the maze
     * 
     * Graph Structure: Adjacency List
     * - Key: Cell coordinate (Point)
     * - Value: List of neighboring walkable cells
     */
    private void buildGraph() {
        graph.clear(); // Reset graph
        
        // Process each cell in the grid
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                // Only consider walkable cells (non-walls)
                if (grid[row][col].getType() != CellType.WALL) {
                    Point current = new Point(row, col);
                    List<Point> neighbors = new ArrayList<>();
                    
                    // Possible movement directions: up, down, left, right
                    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                    
                    // Check each direction for walkable neighbors
                    for (int[] dir : directions) {
                        int newRow = row + dir[0];
                        int newCol = col + dir[1];
                        
                        // Validate position and check if neighbor is walkable
                        if (newRow >= 0 && newRow < SIZE && newCol >= 0 && newCol < SIZE && 
                            grid[newRow][newCol].getType() != CellType.WALL) {
                            neighbors.add(new Point(newRow, newCol));
                        }
                    }
                    
                    // Add current cell and its neighbors to graph
                    graph.put(current, neighbors);
                }
            }
        }
    }

    /**
     * Recursive method for maze generation
     * 
     * @param row Current row position
     * @param col Current column position
     */
    private void generateMazeRecursive(int row, int col) {
        // Mark current cell as path
        grid[row][col].setType(CellType.PATH);
        
        // Directions: up, right, down, left (each moves 2 cells)
        int[][] directions = {{-2, 0}, {0, 2}, {2, 0}, {0, -2}};
        
        // Randomize direction order
        Collections.shuffle(Arrays.asList(directions), new Random());
        
        // Process each direction
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            // Validate new position and check if unvisited (wall)
            if (newRow > 0 && newRow < SIZE - 1 && newCol > 0 && newCol < SIZE - 1 && 
                grid[newRow][newCol].getType() == CellType.WALL) {
                
                // Remove wall between current and new cell
                grid[row + dir[0]/2][col + dir[1]/2].setType(CellType.PATH);
                
                // Recursively process new cell
                generateMazeRecursive(newRow, newCol);
            }
        }
        
        // Ensure end point is reachable
        if (grid[SIZE - 2][SIZE - 2].getType() == CellType.WALL) {
            grid[SIZE - 2][SIZE - 2].setType(CellType.PATH);
            grid[SIZE - 3][SIZE - 2].setType(CellType.PATH);
            grid[SIZE - 2][SIZE - 3].setType(CellType.PATH);
        }
    }

    /**
     * Resets the solution visualization
     * 
     * Clears explored cells and solution path while preserving
     * the current maze structure and start/end points
     */
    private void resetSolution() {
        if (solving) return; // Don't reset during solving
        
        // Process each cell
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                // Reset solution-related cells
                if (grid[row][col].getType() == CellType.EXPLORED || 
                    grid[row][col].getType() == CellType.SOLUTION ||
                    grid[row][col].getType() == CellType.CURRENT) {
                    
                    // Restore start/end or set to path
                    if (row == start.x && col == start.y) {
                        grid[row][col].setType(CellType.START);
                    } else if (row == end.x && col == end.y) {
                        grid[row][col].setType(CellType.END);
                    } else {
                        grid[row][col].setType(CellType.PATH);
                    }
                }
            }
        }
        
        // Update status and statistics
        statusLabel.setText("Solution cleared. Ready to solve again.");
        pathLength = 0;
        updateStats();
    }

    /**
     * Solves the maze using either DFS or BFS
     * 
     * @param useDFS True for DFS, False for BFS
     */
    private void solveMaze(boolean useDFS) {
        if (solving) return; // Don't start new solution during solving
        
        // Clear previous solution
        resetSolution();
        
        // Rebuild graph in case of manual edits
        buildGraph(); 
        
        // Set solving flag and update status
        solving = true;
        statusLabel.setText("Solving with " + (useDFS ? "Depth-First Search..." : "Breadth-First Search..."));
        
        // Initialize statistics
        if (useDFS) {
            dfsSteps = 0;
            dfsTime = System.currentTimeMillis();
        } else {
            bfsSteps = 0;
            bfsTime = System.currentTimeMillis();
        }
        
        // Start solving in background thread to keep GUI responsive
        new Thread(() -> {
            // Solve with selected algorithm
            boolean solved = useDFS ? solveDFS() : solveBFS();
            
            // Record execution time
            if (useDFS) {
                dfsTime = System.currentTimeMillis() - dfsTime;
            } else {
                bfsTime = System.currentTimeMillis() - bfsTime;
            }
            
            // Update GUI after solving
            SwingUtilities.invokeLater(() -> {
                if (solved) {
                    statusLabel.setText("Solution found! " + (useDFS ? "DFS" : "BFS") + 
                                       " took " + (useDFS ? dfsSteps : bfsSteps) + " steps and " +
                                       (useDFS ? dfsTime : bfsTime) + "ms");
                } else {
                    statusLabel.setText("No solution exists.");
                }
                
                // Reset solving flag and update statistics
                solving = false;
                updateStats();
            });
        }).start();
    }

    /**
     * Solves the maze using Depth-First Search (DFS)
     * 
     * Algorithm:
     * 1. Use a stack for LIFO processing
     * 2. Track visited cells and parent-child relationships
     * 3. Explore neighbors in random order
     * 4. Visualize exploration and solution path
     * 
     * @return True if solution found, False otherwise
     */
    private boolean solveDFS() {
        // Data structures for DFS
        Stack<Point> stack = new Stack<>(); // Cells to explore
        Map<Point, Point> parentMap = new HashMap<>(); // Parent-child relationships
        Set<Point> visited = new HashSet<>(); // Track visited cells
        
        // Start from beginning
        stack.push(start);
        visited.add(start);
        
        // Process stack until empty
        while (!stack.isEmpty()) {
            Point current = stack.pop();
            dfsSteps++; // Increment step counter
            
            // Visualize current cell (unless start/end)
            if (!current.equals(start) && !current.equals(end)) {
                updateCell(current, CellType.CURRENT, 5);
            }
            
            // Check for solution
            if (current.equals(end)) {
                showSolution(parentMap);
                return true;
            }
            
            // Get neighbors from graph
            List<Point> neighbors = graph.getOrDefault(current, Collections.emptyList());
            
            // Process each neighbor
            for (Point neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    // Mark neighbor as visited
                    visited.add(neighbor);
                    
                    // Record parent for path reconstruction
                    parentMap.put(neighbor, current);
                    
                    // Add to exploration stack
                    stack.push(neighbor);
                    
                    // Visualize explored cell (unless end point)
                    if (!neighbor.equals(end)) {
                        updateCell(neighbor, CellType.EXPLORED, 5);
                    }
                }
            }
            
            // Reset current cell to explored after processing
            if (!current.equals(start) && !current.equals(end)) {
                updateCell(current, CellType.EXPLORED, 0);
            }
        }
        
        // No solution found
        return false;
    }

    /**
     * Solves the maze using Breadth-First Search (BFS)
     * 
     * Algorithm:
     * 1. Use a queue for FIFO processing
     * 2. Track visited cells and parent-child relationships
     * 3. Explore all neighbors at current level before moving deeper
     * 4. Visualize exploration and solution path
     * 
     * @return True if solution found, False otherwise
     */
    private boolean solveBFS() {
        // Data structures for BFS
        Queue<Point> queue = new LinkedList<>(); // Cells to explore
        Map<Point, Point> parentMap = new HashMap<>(); // Parent-child relationships
        Set<Point> visited = new HashSet<>(); // Track visited cells
        
        // Start from beginning
        queue.add(start);
        visited.add(start);
        
        // Process queue until empty
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            bfsSteps++; // Increment step counter
            
            // Visualize current cell (unless start/end)
            if (!current.equals(start) && !current.equals(end)) {
                updateCell(current, CellType.CURRENT, 5);
            }
            
            // Check for solution
            if (current.equals(end)) {
                showSolution(parentMap);
                return true;
            }
            
            // Get neighbors from graph
            List<Point> neighbors = graph.getOrDefault(current, Collections.emptyList());
            
            // Process each neighbor
            for (Point neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    // Mark neighbor as visited
                    visited.add(neighbor);
                    
                    // Record parent for path reconstruction
                    parentMap.put(neighbor, current);
                    
                    // Add to exploration queue
                    queue.add(neighbor);
                    
                    // Visualize explored cell (unless end point)
                    if (!neighbor.equals(end)) {
                        updateCell(neighbor, CellType.EXPLORED, 5);
                    }
                }
            }
            
            // Reset current cell to explored after processing
            if (!current.equals(start) && !current.equals(end)) {
                updateCell(current, CellType.EXPLORED, 0);
            }
        }
        
        // No solution found
        return false;
    }

    /**
     * Visualizes the solution path
     * 
     * @param parentMap Map containing parent-child relationships
     */
    private void showSolution(Map<Point, Point> parentMap) {
        // Trace back from end to start
        List<Point> path = new ArrayList<>();
        Point current = end;
        pathLength = 0; // Reset path length
        
        // Backtrack from end to start using parent map
        while (current != null && !current.equals(start)) {
            path.add(current);
            current = parentMap.get(current);
            pathLength++; // Count path segments
        }
        
        // Highlight solution path in reverse order (from start to end)
        for (int i = path.size() - 1; i >= 0; i--) {
            updateCell(path.get(i), CellType.SOLUTION, 30);
        }
    }

    /**
     * Updates statistics display
     */
    private void updateStats() {
        statsLabel.setText(String.format("DFS Steps: %d | BFS Steps: %d | Path Length: %d", 
                                        dfsSteps, bfsSteps, pathLength));
    }

    /**
     * Updates a cell in the GUI with a small delay for visualization
     * 
     * @param p Cell position to update
     * @param type New cell type
     * @param delay Delay in milliseconds
     */
    private void updateCell(Point p, CellType type, int delay) {
        // Add delay for visualization effect
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        // Update GUI on EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            // Don't override start/end points
            if (!p.equals(start) && !p.equals(end)) {
                grid[p.x][p.y].setType(type);
            }
        });
    }

    /**
     * Mouse listener for cell interaction
     */
    private class CellMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (solving) return; // Ignore clicks during solving
            
            // Get clicked cell
            Cell cell = (Cell) e.getSource();
            int row = cell.row;
            int col = cell.col;
            
            // Set start point mode
            if (settingStart) {
                if (cell.getType() != CellType.WALL) {
                    // Clear previous start
                    if (start != null) {
                        grid[start.x][start.y].setType(CellType.PATH);
                    }
                    
                    // Set new start
                    start = new Point(row, col);
                    cell.setType(CellType.START);
                    settingStart = false;
                    statusLabel.setText("Start point set. You can set end point or solve the maze.");
                }
            } 
            // Set end point mode
            else if (settingEnd) {
                if (cell.getType() != CellType.WALL && !cell.getType().equals(CellType.START)) {
                    // Clear previous end
                    if (end != null) {
                        grid[end.x][end.y].setType(CellType.PATH);
                    }
                    
                    // Set new end
                    end = new Point(row, col);
                    cell.setType(CellType.END);
                    settingEnd = false;
                    statusLabel.setText("End point set. Ready to solve the maze.");
                }
            }
            // Edit mode: Toggle between wall and path
            else {
                // Toggle wall/path
                if (cell.getType() == CellType.WALL) {
                    cell.setType(CellType.PATH);
                } else if (cell.getType() == CellType.PATH) {
                    cell.setType(CellType.WALL);
                }
                
                // Rebuild graph after modification
                buildGraph();
            }
        }
    }

    /**
     * Custom cell component for maze visualization
     */
    private class Cell extends JPanel {
        private CellType type; // Current cell state
        private final int row, col; // Grid position
        
        /**
         * Cell constructor
         * 
         * @param row Row position
         * @param col Column position
         */
        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
            this.type = CellType.WALL; // Default to wall
        }
        
        // Accessor for cell type
        public CellType getType() {
            return type;
        }
        
        /**
         * Sets cell type and triggers repaint
         * 
         * @param type New cell type
         */
        public void setType(CellType type) {
            this.type = type;
            repaint(); // Update visual appearance
        }
        
        /**
         * Custom painting for cell
         * 
         * @param g Graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Set color based on cell type
            switch (type) {
                case WALL:
                    g.setColor(WALL_COLOR);
                    break;
                case PATH:
                    g.setColor(PATH_COLOR);
                    break;
                case START:
                    g.setColor(START_COLOR);
                    break;
                case END:
                    g.setColor(END_COLOR);
                    break;
                case EXPLORED:
                    g.setColor(EXPLORED_COLOR);
                    break;
                case SOLUTION:
                    g.setColor(SOLUTION_COLOR);
                    break;
                case CURRENT:
                    g.setColor(CURRENT_COLOR);
                    break;
            }
            
            // Fill cell with selected color
            g.fillRect(0, 0, getWidth(), getHeight());
            
            // Add text indicator for start/end cells
            if (type == CellType.START || type == CellType.END) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                String text = (type == CellType.START) ? "S" : "E";
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(text, x, y);
            }
        }
    }

    /**
     * Enumeration of possible cell types
     */
    private enum CellType {
        WALL,       // Non-walkable barrier
        PATH,       // Walkable area
        START,      // Starting position
        END,        // Target position
        EXPLORED,   // Area searched by algorithm
        SOLUTION,   // Final solution path
        CURRENT     // Current cell being processed
    }

    /**
     * Main entry point for the application
     * 
     * @param args Command-line arguments (unused)
     */
    public static void main(String[] args) {
        // Start GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MazeSolver mazeSolver = new MazeSolver();
            mazeSolver.setVisible(true);
            mazeSolver.setLocationRelativeTo(null); // Center window
        });
    }
}