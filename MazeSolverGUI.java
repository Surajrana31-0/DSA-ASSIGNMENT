import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MazeSolverGUI extends JFrame {
    static final int ROWS = 20, COLS = 20, CELL_SIZE = 25;
    MazePanel mazePanel;
    JButton dfsButton, bfsButton, generateButton;
    Maze maze;

    public MazeSolverGUI() {
        maze = new Maze(ROWS, COLS);

        mazePanel = new MazePanel(maze);
        dfsButton = new JButton("Solve with DFS");
        bfsButton = new JButton("Solve with BFS");
        generateButton = new JButton("Generate New Maze");

        dfsButton.addActionListener(e -> mazePanel.solveMaze("DFS"));
        bfsButton.addActionListener(e -> mazePanel.solveMaze("BFS"));
        generateButton.addActionListener(e -> {
            maze.generateMaze();
            mazePanel.resetSolution();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(dfsButton);
        buttonPanel.add(bfsButton);
        buttonPanel.add(generateButton);

        setLayout(new BorderLayout());
        add(mazePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setTitle("Maze Solver (DFS/BFS)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(COLS * CELL_SIZE + 30, ROWS * CELL_SIZE + 80);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MazeSolverGUI gui = new MazeSolverGUI();
            gui.setVisible(true);
        });
    }
}

// Represents the maze and logic for generating/solving
class Maze {
    int rows, cols;
    Cell[][] grid;
    int startRow = 0, startCol = 0, endRow, endCol;

    static final int[] dRow = {-1,1,0,0}, dCol = {0,0,-1,1}; // N, S, W, E

    public Maze(int rows, int cols) {
        this.rows = rows; this.cols = cols;
        this.endRow = rows-1; this.endCol = cols-1;
        generateMaze();
    }

    // Generate a random maze using recursive backtracking
    public void generateMaze() {
        grid = new Cell[rows][cols];
        for(int r = 0; r < rows; r++)
            for(int c = 0; c < cols; c++)
                grid[r][c] = new Cell(r, c);

        Stack<Cell> stack = new Stack<>();
        Cell start = grid[startRow][startCol];
        start.visited = true;
        stack.push(start);

        Random rand = new Random();
        while(!stack.isEmpty()) {
            Cell curr = stack.peek();
            List<Cell> unvisited = new ArrayList<>();
            for(int d = 0; d < 4; d++) {
                int nr = curr.row + dRow[d], nc = curr.col + dCol[d];
                if(inBounds(nr, nc) && !grid[nr][nc].visited)
                    unvisited.add(grid[nr][nc]);
            }
            if(!unvisited.isEmpty()) {
                Cell next = unvisited.get(rand.nextInt(unvisited.size()));
                removeWallBetween(curr, next);
                next.visited = true;
                stack.push(next);
            }
            else stack.pop();
        }
        // Reset all cell visited for solving
        for(int r = 0; r < rows; r++)
            for(int c = 0; c < cols; c++)
                grid[r][c].visited = false;
    }

    // Remove wall between c1 and c2
    private void removeWallBetween(Cell c1, Cell c2) {
        int dr = c2.row - c1.row, dc = c2.col - c1.col;
        if(dr == 1) { c1.south = false; c2.north = false; }
        if(dr == -1) { c1.north = false; c2.south = false; }
        if(dc == 1) { c1.east = false; c2.west = false; }
        if(dc == -1) { c1.west = false; c2.east = false; }
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // Return path as list of cells (for drawing), or null if not found
    public List<Cell> solveMaze(String method, MazePanel panel) {
        for(int r=0;r<rows;r++)
            for(int c=0;c<cols;c++)
                grid[r][c].visited = false;
        if(method.equals("DFS")) return solveDFS(panel);
        else return solveBFS(panel);
    }

    // DFS using stack, with GUI step animation
    private List<Cell> solveDFS(MazePanel panel) {
        Stack<Cell> stack = new Stack<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Cell start = grid[startRow][startCol], end = grid[endRow][endCol];
        stack.push(start);
        start.visited = true;
        while(!stack.isEmpty()) {
            Cell curr = stack.pop();
            panel.animateStep(curr, Color.ORANGE);
            if(curr.equals(end)) {
                return buildPath(parent, end);
            }
            for(Cell neighbor : getNeighbors(curr)) {
                if(!neighbor.visited) {
                    neighbor.visited = true;
                    parent.put(neighbor, curr);
                    stack.push(neighbor);
                }
            }
        }
        return null;
    }

    // BFS using queue, with GUI step animation
    private List<Cell> solveBFS(MazePanel panel) {
        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Cell start = grid[startRow][startCol], end = grid[endRow][endCol];
        queue.add(start);
        start.visited = true;
        while(!queue.isEmpty()) {
            Cell curr = queue.poll();
            panel.animateStep(curr, Color.CYAN);
            if(curr.equals(end)) {
                return buildPath(parent, end);
            }
            for(Cell neighbor : getNeighbors(curr)) {
                if(!neighbor.visited) {
                    neighbor.visited = true;
                    parent.put(neighbor, curr);
                    queue.add(neighbor);
                }
            }
        }
        return null;
    }

    // Helper to reconstruct the path from end to start
    private List<Cell> buildPath(Map<Cell, Cell> parent, Cell end) {
        List<Cell> path = new ArrayList<>();
        for(Cell at = end; at != null; at = parent.get(at))
            path.add(at);
        Collections.reverse(path);
        return path;
    }

    // Return a list of walkable (neighbor) cells
    private List<Cell> getNeighbors(Cell curr) {
        List<Cell> neighbors = new ArrayList<>();
        if(!curr.north && inBounds(curr.row-1, curr.col)) neighbors.add(grid[curr.row-1][curr.col]);
        if(!curr.south && inBounds(curr.row+1, curr.col)) neighbors.add(grid[curr.row+1][curr.col]);
        if(!curr.west && inBounds(curr.row, curr.col-1)) neighbors.add(grid[curr.row][curr.col-1]);
        if(!curr.east && inBounds(curr.row, curr.col+1)) neighbors.add(grid[curr.row][curr.col+1]);
        return neighbors;
    }
}

// The visualization and animation panel
// ...existing code...
class MazePanel extends JPanel {
    Maze maze;
    List<Cell> solutionPath = new ArrayList<>();
    boolean animating = false;

    MazePanel(Maze maze) {
        this.maze = maze;
        setPreferredSize(new Dimension(MazeSolverGUI.COLS * MazeSolverGUI.CELL_SIZE, MazeSolverGUI.ROWS * MazeSolverGUI.CELL_SIZE));
    }

    void resetSolution() { solutionPath.clear(); repaint(); }

    void solveMaze(String method) {
        if(animating) return;
        solutionPath.clear();
        new Thread(() -> {
            animating = true;
            List<Cell> path = maze.solveMaze(method, this);
            if(path != null) {
                solutionPath.addAll(path);
                repaint();
                JOptionPane.showMessageDialog(this, "Maze solved! ("+method+")\nPath length: "+path.size());
            } else {
                repaint();
                JOptionPane.showMessageDialog(this, "No path found!");
            }
            animating = false;
        }).start();
    }

    void animateStep(Cell cell, Color color) {
        Graphics g = getGraphics();
        int sz = MazeSolverGUI.CELL_SIZE;
        int x = cell.col * sz, y = cell.row * sz;
        try {
            if (g != null) {
                g.setColor(color);
                g.fillRect(x+2, y+2, sz-4, sz-4);
            }
            Thread.sleep(25);
        } catch (Exception ignored) {}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int sz = MazeSolverGUI.CELL_SIZE;
        for (int r = 0; r < maze.rows; r++) {
            for (int c = 0; c < maze.cols; c++) {
                int x = c * sz, y = r * sz;
                Cell cell = maze.grid[r][c];
                g.setColor(Color.WHITE);
                g.fillRect(x, y, sz, sz);
                g.setColor(Color.BLACK);
                if (cell.north) g.drawLine(x, y, x + sz, y);
                if (cell.south) g.drawLine(x, y + sz, x + sz, y + sz);
                if (cell.west)  g.drawLine(x, y, x, y + sz);
                if (cell.east)  g.drawLine(x + sz, y, x + sz, y + sz);
            }
        }
        // Draw path
        if (!solutionPath.isEmpty()) {
            g.setColor(Color.RED);
            for (Cell cell : solutionPath) {
                int x = cell.col * sz, y = cell.row * sz;
                g.fillOval(x + sz/4, y + sz/4, sz/2, sz/2);
            }
        }
        // Start and End highlighting
        g.setColor(Color.GREEN);
        g.fillRect(maze.startCol * sz + 6, maze.startRow * sz + 6, sz - 12, sz - 12);
        g.setColor(Color.MAGENTA);
        g.fillRect(maze.endCol * sz + 6, maze.endRow * sz + 6, sz - 12, sz - 12);
    }
}
// ...existing code...

class Cell {
    int row, col;
    boolean north = true, south = true, west = true, east = true;
    boolean visited = false;

    Cell(int row, int col) { this.row = row; this.col = col; }

    // Needed for hashmap/set and comparisons
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Cell)) return false;
        Cell c = (Cell)o;
        return row==c.row && col==c.col;
    }
    @Override
    public int hashCode() { return Objects.hash(row, col);}
}
