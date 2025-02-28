import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZhedSolver {
    private final int goalI, goalJ;
    private static final int[][] DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // up, down, left, right
    private static final String[] DIR_NAMES = {"up", "down", "left", "right"};
    private Set<Long> visited;
    private volatile List<String> solution; // Shared solution across threads
    private AtomicBoolean solved; // Flag to stop threads when solution is found

    public ZhedSolver(int goalI, int goalJ) {
        this.goalI = goalI;
        this.goalJ = goalJ;
        this.visited = Collections.synchronizedSet(new HashSet<>()); // Thread-safe
        this.solution = null;
        this.solved = new AtomicBoolean(false);
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
        List<String> sequence = Collections.synchronizedList(new ArrayList<>());
        System.out.println("Initial Board:");
        printBoard(initialBoard);
        visited.clear();

        // Multithreading setup
        int threadCount = Runtime.getRuntime().availableProcessors(); // Use available cores
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        // Submit a task for each initial tower-direction pair
        for (int t = 0; t < towers.size(); t++) {
            Tower tower = towers.get(t);
            for (int dir = 0; dir < 4; dir++) {
                if (!isWithinBounds(tower.i, tower.j, dir, tower.k)) continue;
                int[][] newBoard = cloneBoard(initialBoard);
                activateTower(newBoard, tower, dir);
                List<Tower> newRemaining = new ArrayList<>(towers);
                newRemaining.remove(t);
                List<String> threadSequence = new ArrayList<>();
                threadSequence.add("Activate (" + tower.i + "," + tower.j + ") " + DIR_NAMES[dir]);
                futures.add(executor.submit(() -> {
                    if (!solved.get()) {
                        dfs(newBoard, newRemaining, threadSequence);
                    }
                }));
            }
        }

        // Wait for any thread to finish or all to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return solution != null ? new ArrayList<>(solution) : null;
    }

    private void dfs(int[][] board, List<Tower> remaining, List<String> sequence) {
        if (solved.get()) return; // Stop if solution found

        long hash = boardToHash(board);
        if (visited.contains(hash)) return;
        visited.add(hash);

        if (remaining.isEmpty()) {
            if (board[goalI][goalJ] == -1) {
                synchronized (this) {
                    if (!solved.get()) {
                        solution = new ArrayList<>(sequence); // Store solution
                        solved.set(true);
                    }
                }
            }
            return;
        }

        for (int t = 0; t < remaining.size() && !solved.get(); t++) {
            Tower tower = remaining.get(t);
            if (board[tower.i][tower.j] > 0) {
                for (int dir = 0; dir < 4; dir++) {
                    if (!isWithinBounds(tower.i, tower.j, dir, tower.k)) continue;
                    int[][] newBoard = cloneBoard(board);
                    activateTower(newBoard, tower, dir);
                    List<Tower> newRemaining = new ArrayList<>(remaining);
                    newRemaining.remove(t);
                    String move = "Activate (" + tower.i + "," + tower.j + ") " + DIR_NAMES[dir];
                    List<String> newSequence = new ArrayList<>(sequence);
                    newSequence.add(move);
                    dfs(newBoard, newRemaining, newSequence);
                }
            }
        }
    }

    private boolean isWithinBounds(int i, int j, int dir, int k) {
        int minI = 0, maxI = 6, minJ = 1, maxJ = 7;
        int di = DIRS[dir][0], dj = DIRS[dir][1];
        int ni = i + di, nj = j + dj;
        int steps = k;
        while (steps > 0 && ni >= 0 && ni < 8 && nj >= 0 && nj < 8) {
            if (ni >= minI && ni <= maxI && nj >= minJ && nj <= maxJ) {
                return true;
            }
            ni += di;
            nj += dj;
            steps--;
        }
        return false;
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
                hash = (hash << 1) | (board[i][j] == -1 ? 1 : 0);
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