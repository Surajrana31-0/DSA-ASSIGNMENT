import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MazeSolver extends JFrame {
    // Changed to odd size for proper maze generation
    private static final int SIZE = 31;
    private static final int CELL_SIZE = 20;  // Slightly smaller for better fit
    private static final Color WALL_COLOR = Color.BLACK;
    private static final Color PATH_COLOR = Color.WHITE;
    private static final Color START_COLOR = Color.GREEN;
    private static final Color END_COLOR = Color.RED;
    private static final Color EXPLORED_COLOR = new Color(135, 206, 250);
    private static final Color SOLUTION_COLOR = Color.YELLOW;
    
    private JPanel mazePanel;
    private JButton generateButton, dfsButton, bfsButton;
    private JLabel statusLabel;
    private Cell[][] grid;
    private Point start, end;
    private boolean solving = false;
    
    public MazeSolver() {
        setTitle("Maze Solver");
        setSize(SIZE * CELL_SIZE + 50, SIZE * CELL_SIZE + 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Initialize components
        mazePanel = new JPanel(new GridLayout(SIZE, SIZE));
        mazePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        grid = new Cell[SIZE][SIZE];
        
        // Initialize grid
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = new Cell();
                grid[row][col].setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                grid[row][col].setBorder(BorderFactory.createLineBorder(Color.GRAY));
                grid[row][col].addMouseListener(new CellMouseListener());
                mazePanel.add(grid[row][col]);
            }
        }
        
        // Control panel
        JPanel controlPanel = new JPanel();
        generateButton = new JButton("Generate New Maze");
        dfsButton = new JButton("Solve with DFS");
        bfsButton = new JButton("Solve with BFS");
        statusLabel = new JLabel("Click 'Generate New Maze' to start");
        
        generateButton.addActionListener(e -> generateMaze());
        dfsButton.addActionListener(e -> solveMaze(true));
        bfsButton.addActionListener(e -> solveMaze(false));
        
        controlPanel.add(generateButton);
        controlPanel.add(dfsButton);
        controlPanel.add(bfsButton);
        controlPanel.add(statusLabel);
        
        // Add components to frame
        add(mazePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        // Initial maze generation
        generateMaze();
    }
    
    private void generateMaze() {
        if (solving) return;
        
        // Reset grid
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col].setType(CellType.WALL);
            }
        }
        
        // Generate maze using recursive backtracking
        generateMazeRecursive(1, 1);
        
        // Set valid start and end points (odd coordinates)
        start = new Point(1, 1);
        end = new Point(SIZE - 2, SIZE - 2);
        
        grid[start.x][start.y].setType(CellType.START);
        grid[end.x][end.y].setType(CellType.END);
        
        statusLabel.setText("Maze generated. Select an algorithm to solve.");
    }
    
    private void generateMazeRecursive(int row, int col) {
        grid[row][col].setType(CellType.PATH);
        
        // Define directions: up, right, down, left
        int[][] directions = {{-2, 0}, {0, 2}, {2, 0}, {0, -2}};
        Collections.shuffle(Arrays.asList(directions), new Random());
        
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            // Ensure valid path connections to boundaries
            if (newRow >= 1 && newRow < SIZE - 1 && newCol >= 1 && newCol < SIZE - 1 && 
                grid[newRow][newCol].getType() == CellType.WALL) {
                
                // Connect current and new cell
                grid[row + dir[0]/2][col + dir[1]/2].setType(CellType.PATH);
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
    
    private void solveMaze(boolean useDFS) {
        if (solving) return;
        
        // Reset previous solution
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col].getType() == CellType.EXPLORED || 
                    grid[row][col].getType() == CellType.SOLUTION) {
                    grid[row][col].setType(CellType.PATH);
                }
            }
        }
        
        // Restore start and end points
        grid[start.x][start.y].setType(CellType.START);
        grid[end.x][end.y].setType(CellType.END);
        
        solving = true;
        statusLabel.setText("Solving with " + (useDFS ? "DFS..." : "BFS..."));
        
        // Run in background thread
        new Thread(() -> {
            boolean solved = useDFS ? solveDFS() : solveBFS();
            
            SwingUtilities.invokeLater(() -> {
                if (solved) {
                    statusLabel.setText("Solution found! Path length: " + getPathLength());
                } else {
                    statusLabel.setText("No solution exists.");
                }
                solving = false;
            });
        }).start();
    }
    
    private boolean solveDFS() {
        Stack<Point> stack = new Stack<>();
        Map<Point, Point> parentMap = new HashMap<>();
        Set<Point> visited = new HashSet<>();
        
        stack.push(start);
        visited.add(start);
        
        while (!stack.isEmpty()) {
            Point current = stack.pop();
            
            // Found solution
            if (current.equals(end)) {
                showSolution(parentMap);
                return true;
            }
            
            // Explore neighbors
            for (Point neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    stack.push(neighbor);
                    
                    // Update GUI
                    updateCell(neighbor, CellType.EXPLORED, 20);
                }
            }
        }
        
        return false;
    }
    
    private boolean solveBFS() {
        Queue<Point> queue = new LinkedList<>();
        Map<Point, Point> parentMap = new HashMap<>();
        Set<Point> visited = new HashSet<>();
        
        queue.add(start);
        visited.add(start);
        
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            
            // Found solution
            if (current.equals(end)) {
                showSolution(parentMap);
                return true;
            }
            
            // Explore neighbors
            for (Point neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                    
                    // Update GUI
                    updateCell(neighbor, CellType.EXPLORED, 10);
                }
            }
        }
        
        return false;
    }
    
    private List<Point> getNeighbors(Point p) {
        List<Point> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            int newRow = p.x + dir[0];
            int newCol = p.y + dir[1];
            
            if (newRow >= 0 && newRow < SIZE && newCol >= 0 && newCol < SIZE && 
                grid[newRow][newCol].getType() != CellType.WALL) {
                neighbors.add(new Point(newRow, newCol));
            }
        }
        
        return neighbors;
    }
    
    private void showSolution(Map<Point, Point> parentMap) {
        // Trace back from end to start
        List<Point> path = new ArrayList<>();
        Point current = end;
        
        while (current != null && !current.equals(start)) {
            path.add(current);
            current = parentMap.get(current);
        }
        
        // Highlight solution path in reverse order
        for (int i = path.size() - 1; i >= 0; i--) {
            updateCell(path.get(i), CellType.SOLUTION, 30);
        }
    }
    
    private int getPathLength() {
        int length = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col].getType() == CellType.SOLUTION) {
                    length++;
                }
            }
        }
        return length + 1; // Include start point
    }
    
    private void updateCell(Point p, CellType type, int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            if (!p.equals(start) && !p.equals(end)) {
                grid[p.x][p.y].setType(type);
            }
        });
    }
    
    private class CellMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (solving) return;
            
            Cell cell = (Cell) e.getSource();
            if (cell.getType() == CellType.WALL) {
                cell.setType(CellType.PATH);
            } else if (cell.getType() == CellType.PATH) {
                cell.setType(CellType.WALL);
            }
        }
    }
    
    private class Cell extends JPanel {
        private CellType type;
        
        public Cell() {
            this.type = CellType.WALL;
        }
        
        public CellType getType() {
            return type;
        }
        
        public void setType(CellType type) {
            this.type = type;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
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
            }
            
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    
    private enum CellType {
        WALL, PATH, START, END, EXPLORED, SOLUTION
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MazeSolver mazeSolver = new MazeSolver();
            mazeSolver.setVisible(true);
        });
    }
}