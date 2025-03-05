import java.util.*;

public class ZhedSolver {

    // Directions a tile can be activated.
    enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    // A Move holds the starting position and chosen direction.
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

    // The board dimensions (8x8)
    static final int ROWS = 8, COLS = 8;

    /**
     * Attempts to solve the puzzle starting from a given board configuration.
     * @param board an 8x8 int[][] board (0 = empty, >0 = tile value, -1 = activated cell)
     * @param goalRow the goal cell row index
     * @param goalCol the goal cell column index
     * @return a list of Moves that solve the puzzle (or null if no solution is found)
     */
    public static List<Move> solve(int[][] board, int goalRow, int goalCol) {
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
        // Iterate over all cells.
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                // Only consider cells with a tile (value > 0).
                if (board[i][j] > 0) {
                    // Try each direction.
                    for (Direction d : Direction.values()) {
                        int[][] newBoard = copyBoard(board);
                        if (simulateMove(newBoard, i, j, d)) {
                            moves.add(new Move(i, j, d));
                            if (search(newBoard, goalRow, goalCol, moves)) {
                                return true;
                            }
                            // Backtrack if not successful.
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
     * 2. Then travel in the direction given.
     *    - If a cell is not activated (i.e. not -1), set it to -1 and count it.
     *    - If the cell is already -1, skip it (do not count it).
     *    - Continue until you have activated exactly 'value' new cells.
     *    - If the board boundary is reached before activating 'value' cells, the move is invalid.
     *
     * @return true if the move is valid (i.e. exactly 'value' cells activated), false otherwise.
     */
    private static boolean simulateMove(int[][] board, int row, int col, Direction dir) {
        int value = board[row][col];
        if (value <= 0) return false; // No valid tile here.

        // Activate the tile itself.
        board[row][col] = -1;
        int count = 0; // Count of new cells activated.
        int r = row, c = col;

        int dr = 0, dc = 0;
        switch (dir) {
            case UP:    dr = -1; break;
            case DOWN:  dr = 1;  break;
            case LEFT:  dc = -1; break;
            case RIGHT: dc = 1;  break;
        }

        // Move until we've activated 'value' non-activated cells.
        while (count < value) {
            int nr = r + dr;
            int nc = c + dc;
            // If the boundary is reached before meeting the required activations, the move is invalid.
            if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) {
                return false;
            }
            // If the cell is not yet activated, activate it and count it.
            if (board[nr][nc] != -1) {
                board[nr][nc] = -1;
                count++;
            }
            // Move to the next cell regardless.
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

    // Main method for testing the solver with a sample input.
    public static void main(String[] args) {
        // Test 1: Sample board configuration.
//        int[][] initial = {
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 1, 1, 0, 0},
//                {0, 1, 0, 0, 0, 0, 0, 0},  // Goal is at (3,6)
//                {0, 0, 0, 1, 1, 0, 0, 0},
//                {0, 0, 1, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0}
//        };
//        int goalRow = 3, goalCol = 6;

//        int[][] initial = {
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 3, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 2, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 3, 0, 0},
//                {0, 0, 0, 2, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0}
//        };
//        int goalRow = 1;
//        int goalCol = 2;

        int[][] initial = {
                {0, 1, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 1, 0, 1, 0, 0},
                {0, 0, 0, 0, 1, 0, 1, 0},
                {0, 0, 0, 1, 0, 1, 0, 0},
                {0, 0, 1, 0, 1, 0, 2, 0},
                {0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1},
                {0, 0, 0, 0, 0, 0, 0, 0}
        };
        int goalRow = 0, goalCol = 7;
        List<Move> solution = solve(initial, goalRow, goalCol);

        if (solution != null) {
            System.out.println("Solution found:");
            for (Move m : solution) {
                System.out.println(m);
            }
        } else {
            System.out.println("No solution found.");
        }
    }
}
