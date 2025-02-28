import java.util.*;

public class ZhedSolver {
    private final int goalI, goalJ;
    private static final int[][] DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // up, down, left, right
    private static final String[] DIR_NAMES = {"up", "down", "left", "right"};
    private Set<Long> visited;

    public ZhedSolver(int goalI, int goalJ) {
        this.goalI = goalI;
        this.goalJ = goalJ;
        this.visited = new HashSet<>();
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

        towers.sort((t1, t2) -> {
            int d1 = Math.abs(t1.i - goalI) + Math.abs(t1.j - goalJ);
            int d2 = Math.abs(t2.i - goalI) + Math.abs(t2.j - goalJ);
            return Integer.compare(d2, d1);
        });
        List<String> sequence = new ArrayList<>();
        System.out.println("Initial Board:");
        printBoard(initialBoard);
        visited.clear(); // Reset visited set for each solve
        return dfs(initialBoard, towers, sequence) ? sequence : null;
    }

    private boolean dfs(int[][] board, List<Tower> remaining, List<String> sequence) {
        long hash = boardToHash(board);
        if (visited.contains(hash)) return false;
        visited.add(hash);

        if (remaining.isEmpty()) return board[goalI][goalJ] == -1;

        for (int t = 0; t < remaining.size(); t++) {
            Tower tower = remaining.get(t);
            if (board[tower.i][tower.j] > 0) {
                for (int dir = 0; dir < 4; dir++) {

                    if (!isWithinBounds(tower.i, tower.j, dir, tower.k)) continue;

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

    private boolean isWithinBounds(int i, int j, int dir, int k) {

        int minI = 0, maxI = 6, minJ = 1, maxJ = 7; // Example box for your input
        int di = DIRS[dir][0], dj = DIRS[dir][1];
        int ni = i + di, nj = j + dj;
        int steps = k; // Number of tiles the tower can fill
        while (steps > 0 && ni >= 0 && ni < 8 && nj >= 0 && nj < 8) {
            if (ni >= minI && ni <= maxI && nj >= minJ && nj <= maxJ) {
                return true; // At least one tile falls within bounds
            }
            ni += di;
            nj += dj;
            steps--;
        }
        return false; // All tiles fall outside bounds
    }

    private void activateTower(int[][] board, Tower tower, int dir) {
        int i = tower.i, j = tower.j, k = tower.k;
        board[i][j] = -1;
        int di = DIRS[dir][0], dj = DIRS[dir][1];
        int fillsLeft = k;
        int ni = i + di, nj = j + dj;
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

    private long boardToHash(int[][] board) {
        long hash = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                hash = (hash << 1) | (board[i][j] == -1 ? 1 : 0); // 1 for filled, 0 otherwise
            }
        }
        return hash;
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
                {0, 1, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 1, 0, 1, 0, 0},
                {0, 0, 0, 0, 1, 0, 1, 0},
                {0, 0, 0, 1, 0, 1, 0, 0},
                {0, 0, 1, 0, 1, 0, 2, 0},
                {0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1},
                {0, 0, 0, 0, 0, 0, 0, 0}
        };
        int goalI = 0, goalJ = 7;

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