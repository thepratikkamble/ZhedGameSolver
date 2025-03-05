import java.util.*;

public class GameSolver {

    // The four possible move directions.
    enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    // A Move holds the starting position and the chosen direction.
    static class Move {
        int row, col;
        Direction dir;

        Move(int row, int col, Direction dir) {
            this.row = row;
            this.col = col;
            this.dir = dir;
        }

        @Override
        public String toString() {
            return "Activate (" + row + "," + col + ") " + dir.toString().toLowerCase();
        }
    }

    // Board dimensions.
    static final int ROWS = 8, COLS = 8;

    // Global cache for memoization: store visited board states.
    static Set<String> visitedStates = new HashSet<>();

    /**
     * Attempts to solve the puzzle starting from the given board configuration.
     * @param board An 8x8 board (0 = empty, >0 = tile value, -1 = activated cell)
     * @param goalRow The goal cell row index.
     * @param goalCol The goal cell column index.
     * @return A list of Moves that solve the puzzle (or null if no solution is found).
     */
    public static List<Move> solve(int[][] board, int goalRow, int goalCol) {
        visitedStates.clear();
        List<Move> moves = new ArrayList<>();
        if (search(board, goalRow, goalCol, moves)) {
            return moves;
        }
        return null;
    }

    /**
     * Recursive backtracking search over board moves.
     */
    private static boolean search(int[][] board, int goalRow, int goalCol, List<Move> moves) {
        // Base case: if the goal cell is activated (-1), the puzzle is solved.
        if (board[goalRow][goalCol] == -1) {
            return true;
        }

        // Use memoization to avoid revisiting states.
        String stateKey = boardToString(board);
        if (visitedStates.contains(stateKey)) {
            return false;
        }
        visitedStates.add(stateKey);

        // Try moves from every tile with a positive value.
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (board[i][j] > 0) {
                    // Create a list of directions to try.
                    List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
                    // Prioritize a direction that is toward the goal if aligned.
                    if (i == goalRow) {
                        if (j < goalCol) {
                            directions.remove(Direction.RIGHT);
                            directions.add(0, Direction.RIGHT);
                        } else {
                            directions.remove(Direction.LEFT);
                            directions.add(0, Direction.LEFT);
                        }
                    }
                    if (j == goalCol) {
                        if (i < goalRow) {
                            directions.remove(Direction.DOWN);
                            directions.add(0, Direction.DOWN);
                        } else {
                            directions.remove(Direction.UP);
                            directions.add(0, Direction.UP);
                        }
                    }

                    for (Direction d : directions) {
                        int[][] newBoard = copyBoard(board);
                        if (simulateMove(newBoard, i, j, d)) {
                            moves.add(new Move(i, j, d));
                            if (search(newBoard, goalRow, goalCol, moves)) {
                                return true;
                            }
                            // Backtrack.
                            moves.remove(moves.size() - 1);
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Simulates activating the tile at (row, col) in the given direction.
     *
     * Rules:
     * 1. Set the tile at (row, col) to -1.
     * 2. Then travel in the given direction:
     *    - As you travel, if you encounter a cell not activated (-1), set it to -1 and count it.
     *    - If the cell is already activated, skip it (do not count it).
     *    - Continue until exactly 'value' new cells are activated.
     *    - If you hit the board’s boundary before meeting the required activations, the move is invalid.
     *
     * @return true if the move is valid (i.e. exactly 'value' new cells activated), false otherwise.
     */
    private static boolean simulateMove(int[][] board, int row, int col, Direction dir) {
        int value = board[row][col];
        if (value <= 0) return false;

        // Activate the tile itself.
        board[row][col] = -1;
        int count = 0;  // Count of new cells activated.
        int r = row, c = col;

        int dr = 0, dc = 0;
        switch (dir) {
            case UP:    dr = -1; break;
            case DOWN:  dr = 1;  break;
            case LEFT:  dc = -1; break;
            case RIGHT: dc = 1;  break;
        }

        // Travel in the chosen direction.
        while (count < value) {
            int nr = r + dr;
            int nc = c + dc;
            // If the boundary is reached before activating the required number of cells, move is invalid.
            if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) {
                return false;
            }
            // If cell is not yet activated, activate it and count it.
            if (board[nr][nc] != -1) {
                board[nr][nc] = -1;
                count++;
            }
            // Always move to the next cell.
            r = nr;
            c = nc;
        }
        return true;
    }

    // Helper method to deep-copy the board.
    private static int[][] copyBoard(int[][] board) {
        int[][] newBoard = new int[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            newBoard[i] = Arrays.copyOf(board[i], COLS);
        }
        return newBoard;
    }

    // Helper method to convert the board to a string (for caching).
    private static String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                sb.append(board[i][j]).append(",");
            }
        }
        return sb.toString();
    }

    // Run a test case.
    private static void runTest(int testNumber, int[][] initial, int goalRow, int goalCol) {
        System.out.println("Test " + testNumber + ":");
        List<Move> solution = solve(initial, goalRow, goalCol);
        if (solution != null) {
            System.out.println("Solution found (" + solution.size() + " moves):");
            for (Move m : solution) {
                System.out.println(m);
            }
        } else {
            System.out.println("No solution found.");
        }
        System.out.println("------");
    }

    public static void main(String[] args) {
        // Test 1: Sample board configuration.
        int[][] initial1 = {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 1, 1, 0, 0},
                {0, 1, 0, 0, 0, 0, 0, 0},  // Goal is at (3,6)
                {0, 0, 0, 1, 1, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0}
        };
        int goalRow1 = 3, goalCol1 = 6;
        runTest(1, initial1, goalRow1, goalCol1);

        // Test 2:
        int[][] initial2 = {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 3, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 2, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 3, 0, 0},
                {0, 0, 0, 2, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0}
        };
        int goalRow2 = 1, goalCol2 = 2;
        runTest(2, initial2, goalRow2, goalCol2);

        // Test 3:
        int[][] initial3 = {
                {0, 1, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 1, 0, 1, 0, 0},
                {0, 0, 0, 0, 1, 0, 1, 0},
                {0, 0, 0, 1, 0, 1, 0, 0},
                {0, 0, 1, 0, 1, 0, 2, 0},
                {0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1},
                {0, 0, 0, 0, 0, 0, 0, 0}
        };
        int goalRow3 = 0, goalCol3 = 7;
        runTest(3, initial3, goalRow3, goalCol3);
    }
}
