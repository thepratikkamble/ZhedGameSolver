import java.util.*;
import java.util.List;
import java.util.Arrays;

public class ZhedSolver {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT;
        public int dr() {
            switch (this) {
                case UP: return -1;
                case DOWN: return 1;
                default: return 0;
            }
        }
        public int dc() {
            switch (this) {
                case LEFT: return -1;
                case RIGHT: return 1;
                default: return 0;
            }
        }
    }

    public static class Move {
        public int row, col;
        public Direction dir;
        public Move(int row, int col, Direction dir) {
            this.row = row;
            this.col = col;
            this.dir = dir;
        }
        @Override
        public String toString() {
            return "Activate (" + row + "," + col + ") " + dir.toString().toLowerCase();
        }
    }

    static class Cell {
        int row, col;
        Cell(int row, int col) { this.row = row; this.col = col; }
    }

    public static final int ROWS = 8, COLS = 8;
    private static Set<String> visitedStates = new HashSet<>();

    // Static goal coordinates so that simulateMove can check them.
    public static int goalRow, goalCol;

    public static List<Move> solve(int[][] board, int gRow, int gCol) {
        goalRow = gRow;
        goalCol = gCol;
        visitedStates.clear();
        List<Move> moves = new ArrayList<>();
        if (search(board, goalRow, goalCol, moves)) {
            return moves;
        }
        return null;
    }

    private static boolean search(int[][] board, int goalRow, int goalCol, List<Move> moves) {
        if (board[goalRow][goalCol] == -1) return true;
        List<Move> backwardSolution = solveBackward(board, goalRow, goalCol);
        if (backwardSolution != null) {
            moves.addAll(backwardSolution);
            return true;
        }
        String stateKey = boardToString(board);
        if (visitedStates.contains(stateKey)) return false;
        visitedStates.add(stateKey);
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

    private static List<Move> solveBackward(int[][] board, int goalRow, int goalCol) {
        if (board[goalRow][goalCol] == -1) return new ArrayList<>();
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
                        List<Cell> path = new ArrayList<>();
                        int r = i, c = j;
                        while (true) {
                            r += d.dr();
                            c += d.dc();
                            if (r < 0 || r >= ROWS || c < 0 || c >= COLS) break;
                            path.add(new Cell(r, c));
                            if (r == goalRow && c == goalCol) break;
                        }
                        int needed = 0;
                        for (Cell cell : path) {
                            if (board[cell.row][cell.col] != -1) needed++;
                        }
                        if (needed == board[i][j] && contains(path, goalRow, goalCol)) {
                            int[][] boardCopy = copyBoard(board);
                            boardCopy[i][j] = -1;
                            List<Move> subMoves = new ArrayList<>();
                            boolean allSolved = true;
                            for (Cell cell : path) {
                                if (boardCopy[cell.row][cell.col] != -1) {
                                    List<Move> sol = solveBackward(boardCopy, cell.row, cell.col);
                                    if (sol == null) {
                                        allSolved = false;
                                        break;
                                    } else {
                                        for (Move m : sol) {
                                            simulateMove(boardCopy, m.row, m.col, m.dir);
                                        }
                                        subMoves.addAll(sol);
                                    }
                                }
                            }
                            if (allSolved && boardCopy[goalRow][goalCol] == -1) {
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

    private static boolean contains(List<Cell> cells, int goalRow, int goalCol) {
        for (Cell cell : cells) {
            if (cell.row == goalRow && cell.col == goalCol) return true;
        }
        return false;
    }

    public static boolean simulateMove(int[][] board, int row, int col, Direction dir) {
        int value = board[row][col];
        if (value <= 0) return false;
        board[row][col] = -1;
        int count = 0;
        int r = row, c = col;
        while (count < value) {
            int nr = r + dir.dr();
            int nc = c + dir.dc();
            // Instead of immediately returning false when out-of-bound,
            // if the current cell is the goal cell, consider the move valid.
            if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) {
                if (r == goalRow && c == goalCol) {
                    return true;
                } else {
                    return false;
                }
            }
            if (board[nr][nc] != -1) {
                board[nr][nc] = -1;
                count++;
            }
            r = nr;
            c = nc;
        }
        return true;
    }

    public static int[][] copyBoard(int[][] board) {
        int[][] newBoard = new int[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            newBoard[i] = Arrays.copyOf(board[i], COLS);
        }
        return newBoard;
    }

    public static String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                sb.append(board[i][j]).append(",");
            }
        }
        return sb.toString();
    }
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
        int[][] initial1 = {
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,1,1,0,0},
                {0,1,0,0,0,0,0,0},
                {0,0,0,1,1,0,0,0},
                {0,0,1,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0}
        };
        int goalRow1 = 3, goalCol1 = 6;
        runTest(1, initial1, goalRow1, goalCol1);

        int[][] initial2 = {
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,3,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,2,0,0,0,0,0},
                {0,0,0,0,0,3,0,0},
                {0,0,0,2,0,0,0,0},
                {0,0,0,0,0,0,0,0}
        };
        int goalRow2 = 1, goalCol2 = 2;
        runTest(2, initial2, goalRow2, goalCol2);

        int[][] initial3 = {
                {0,0,3,0,0,0,0,0},
                {0,2,0,3,0,0,0,0},
                {0,0,0,0,2,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,3,0},
                {0,0,0,1,0,0,1,0},
                {0,0,0,0,0,1,0,0},
                {0,0,0,0,0,0,0,0}
        };
        int goalRow3 = 5, goalCol3 = 0;
        runTest(3, initial3, goalRow3, goalCol3);
    }
}
