import java.util.*;

public class ZhedSolver {
    private final int goalI, goalJ;
    private static final int[][] DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // up, down, left, right
    private static final String[] DIR_NAMES = {"up", "down", "left", "right"};

    public ZhedSolver(int goalI, int goalJ) {
        this.goalI = goalI;
        this.goalJ = goalJ;
    }

    public List<String> solve(int[][] initialBoard) {
        List<Tower> towers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (initialBoard[i][j] > 0) {
                    towers.add(new Tower(i, j, initialBoard[i][j]));
                }
            }
        }
        List<String> sequence = new ArrayList<>();
        System.out.println("Initial Board:");
        printBoard(initialBoard);
        return dfs(initialBoard, towers, sequence) ? sequence : null;
    }

    private boolean dfs(int[][] board, List<Tower> remaining, List<String> sequence) {
        if (remaining.isEmpty()) {
            return board[goalI][goalJ] == -1;
        }
        for (int t = 0; t < remaining.size(); t++) {
            Tower tower = remaining.get(t);
            if (board[tower.i][tower.j] > 0) {
                for (int dir = 0; dir < 4; dir++) {
                    int[][] newBoard = cloneBoard(board);
                    activateTower(newBoard, tower, dir);
                    List<Tower> newRemaining = new ArrayList<>(remaining);
                    newRemaining.remove(t);
                    String move = "Activate (" + tower.i + "," + tower.j + ") " + DIR_NAMES[dir];
                    sequence.add(move);
                    System.out.println("\nAfter " + move + ":");
                    printBoard(newBoard);
                    if (dfs(newBoard, newRemaining, sequence)) {
                        return true;
                    }
                    sequence.remove(sequence.size() - 1);
                }
            }
        }
        return false;
    }

    private void activateTower(int[][] board, Tower tower, int dir) {
        int i = tower.i;
        int j = tower.j;
        int k = tower.k;
        board[i][j] = -1;

        int di = DIRS[dir][0];
        int dj = DIRS[dir][1];
        int fillsLeft = k;
        int ni = i + di;
        int nj = j + dj;

        while (fillsLeft > 0 && ni >= 0 && ni < 8 && nj >= 0 && nj < 8) {
            if (board[ni][nj] == -1) {
                ni += di;
                nj += dj;
                continue;
            }
            board[ni][nj] = -1;
            fillsLeft--;
            ni += di;
            nj += dj;
        }
    }

    private int[][] cloneBoard(int[][] board) {
        int[][] newBoard = new int[8][8];
        for (int i = 0; i < 8; i++) {
            newBoard[i] = board[i].clone();
        }
        return newBoard;
    }


    private void printBoard(int[][] board) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.print(String.format("%2d ", board[i][j]));
            }
            System.out.println();
        }
    }

    private static class Tower {
        int i, j, k;
        Tower(int i, int j, int k) {
            this.i = i;
            this.j = j;
            this.k = k;
        }
    }

    public static void main(String[] args) {
        int[][] initial = {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 1, 1, 0, 0},
                {0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 1, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0}
        };

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
//
//        int goalI = 1;
//        int goalJ = 2;

        int goalI = 3;
        int goalJ = 6;

        ZhedSolver solver = new ZhedSolver(goalI, goalJ);
        List<String> solution = solver.solve(initial);

        if (solution != null) {
            System.out.println("\nFinal Solution Sequence:");
            for (String step : solution) {
                System.out.println(step);
            }
        } else {
            System.out.println("No solution found.");
        }
    }
}