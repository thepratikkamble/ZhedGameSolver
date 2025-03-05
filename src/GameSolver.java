import java.util.*;

public class GameSolver {

    // Four possible move directions.
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

    // A helper class for board cell positions.
    static class Cell {
        int row, col;
        Cell(int row, int col) { this.row = row; this.col = col; }
    }

    // Board dimensions.
    static final int ROWS = 8, COLS = 8;

    // Global cache for forward search.
    static Set<String> visitedStates = new HashSet<>();

    /**
     * Main solver: try to solve the board using a combination of forward search and
     * the backward (terminal-tower) heuristic.
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
     * Recursive forward search that now first attempts a backward resolution.
     */
    private static boolean search(int[][] board, int goalRow, int goalCol, List<Move> moves) {
        // Base: goal already activated.
        if (board[goalRow][goalCol] == -1) {
            return true;
        }

        // Try the backward approach first.
        List<Move> backwardSolution = solveBackward(board, goalRow, goalCol);
        if (backwardSolution != null) {
            moves.addAll(backwardSolution);
            return true;
        }

        // Use memoization to avoid re-exploring the same board state.
        String stateKey = boardToString(board);
        if (visitedStates.contains(stateKey)) return false;
        visitedStates.add(stateKey);

        // Standard forward search: try every tower and every move.
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (board[i][j] > 0) {
                    for (Direction d : Direction.values()) {
                        int[][] newBoard = copyBoard(board);
                        if (simulateMove(newBoard, i, j, d)) {
                            moves.add(new Move(i, j, d));
                            if (search(newBoard, goalRow, goalCol, moves)) {
                                return true;
                            }
                            moves.remove(moves.size() - 1);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Backward method: if the goal cell is not activated, look for a terminal tower in the
     * same row or column. Then treat every box between that tower and the goal as a subgoal.
     *
     * This method is heuristic: it returns a (possibly empty) list of moves that, if applied,
     * would (in theory) activate the goal.
     *
     * If no candidate tower yields a backward solution, returns null.
     */
    private static List<Move> solveBackward(int[][] board, int goalRow, int goalCol) {
        // If goal already activated, return an empty solution.
        if (board[goalRow][goalCol] == -1) return new ArrayList<>();

        // Look for towers (tiles) in the same row or column as the goal.
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (board[i][j] > 0) {
                    Direction d = null;
                    if (i == goalRow) {
                        d = (j < goalCol) ? Direction.RIGHT : (j > goalCol) ? Direction.LEFT : null;
                    }
                    if (j == goalCol) {
                        d = (i < goalRow) ? Direction.DOWN : (i > goalRow) ? Direction.UP : d;
                    }
                    if (d != null) {
                        // Determine the straight-line path from tower to goal.
                        List<Cell> path = new ArrayList<>();
                        int r = i, c = j;
                        while (true) {
                            r += deltaRow(d);
                            c += deltaCol(d);
                            if (r < 0 || r >= ROWS || c < 0 || c >= COLS) break;
                            path.add(new Cell(r, c));
                            if (r == goalRow && c == goalCol) break;
                        }
                        // For this tower, count the number of cells that are not yet activated along the path.
                        int needed = 0;
                        for (Cell cell : path) {
                            if (board[cell.row][cell.col] != -1) needed++;
                        }
                        // For the terminal tower, its tile value must equal the number of new activations it would do.
                        if (needed == board[i][j] && contains(path, goalRow, goalCol)) {
                            // Try to solve each subgoal (i.e. every cell in the path that is not activated) recursively.
                            int[][] boardCopy = copyBoard(board);
                            // Remove the tower (simulate using it) by marking it as activated.
                            boardCopy[i][j] = -1;
                            List<Move> subMoves = new ArrayList<>();
                            boolean allSolved = true;
                            // Process each cell along the path in order.
                            for (Cell cell : path) {
                                if (boardCopy[cell.row][cell.col] != -1) {
                                    List<Move> sol = solveBackward(boardCopy, cell.row, cell.col);
                                    if (sol == null) {
                                        allSolved = false;
                                        break;
                                    } else {
                                        // Apply the moves for the subgoal to our board copy.
                                        for (Move m : sol) {
                                            // Simulate the move on boardCopy.
                                            simulateMove(boardCopy, m.row, m.col, m.dir);
                                        }
                                        subMoves.addAll(sol);
                                    }
                                }
                            }
                            if (allSolved && boardCopy[goalRow][goalCol] == -1) {
                                // Finally, add the terminal tower move.
                                subMoves.add(new Move(i, j, d));
                                return subMoves;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    // Helper: returns true if the list of cells contains a cell at (goalRow, goalCol).
    private static boolean contains(List<Cell> cells, int goalRow, int goalCol) {
        for (Cell cell : cells) {
            if (cell.row == goalRow && cell.col == goalCol) return true;
        }
        return false;
    }

    // Helper: Return delta for row given a direction.
    private static int deltaRow(Direction d) {
        switch (d) {
            case UP: return -1;
            case DOWN: return 1;
            default: return 0;
        }
    }

    // Helper: Return delta for col given a direction.
    private static int deltaCol(Direction d) {
        switch (d) {
            case LEFT: return -1;
            case RIGHT: return 1;
            default: return 0;
        }
    }

    /**
     * Simulate a move (forward) according to the rules:
     * Activate the tile at (row, col), then travel in the given direction,
     * skipping activated cells, until exactly [tile value] new cells are activated.
     * Returns true if the move is valid.
     */
    private static boolean simulateMove(int[][] board, int row, int col, Direction dir) {
        int value = board[row][col];
        if (value <= 0) return false;
        board[row][col] = -1;
        int count = 0;
        int r = row, c = col;
        while (count < value) {
            int nr = r + deltaRow(dir);
            int nc = c + deltaCol(dir);
            if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) return false;
            if (board[nr][nc] != -1) {
                board[nr][nc] = -1;
                count++;
            }
            r = nr; c = nc;
        }
        return true;
    }

    // Helper: deep-copy the board.
    private static int[][] copyBoard(int[][] board) {
        int[][] newBoard = new int[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            newBoard[i] = Arrays.copyOf(board[i], COLS);
        }
        return newBoard;
    }

    // Helper: convert board to a string for memoization.
    private static String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                sb.append(board[i][j]).append(",");
            }
        }
        return sb.toString();
    }

    // Helper: run a test.
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
        // Test 1:
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
