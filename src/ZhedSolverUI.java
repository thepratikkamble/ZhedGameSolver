import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ZhedSolverUI extends JFrame {

    private JComboBox<String> testSelector;
    private JButton solveButton;
    private JButton clearButton;
    private JTextArea outputArea;
    private JTextArea boardArea;
    private JPanel customPanel;
    private JTextArea customBoardInput;
    private JTextField goalRowField;
    private JTextField goalColField;

    public ZhedSolverUI() {
        setTitle("Zhed Puzzle Solver");
        setSize(700, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        testSelector = new JComboBox<>(new String[] { "Test 1", "Test 2", "Test 3", "Custom" });
        solveButton = new JButton("Solve");
        clearButton = new JButton("Clear Board");
        topPanel.add(testSelector);
        topPanel.add(solveButton);
        topPanel.add(clearButton);
        add(topPanel, BorderLayout.NORTH);

        customPanel = new JPanel(new BorderLayout());
        customBoardInput = new JTextArea(8, 40);
        customBoardInput.setText(
                "0 0 0 0 0 0 0 0\n" +
                        "0 0 0 0 0 0 0 0\n" +
                        "0 0 0 0 0 0 0 0\n" +
                        "0 0 0 0 0 0 0 0\n" +
                        "0 0 0 0 0 0 0 0\n" +
                        "0 0 0 0 0 0 0 0\n" +
                        "0 0 0 0 0 0 0 0\n" +
                        "0 0 0 0 0 0 0 0\n"
        );
        customBoardInput.setBorder(BorderFactory.createTitledBorder("Custom Board (8 lines, 8 numbers each separated by spaces)"));
        JPanel goalPanel = new JPanel(new FlowLayout());
        goalPanel.add(new JLabel("Goal Row (0-index):"));
        goalRowField = new JTextField(3);
        goalPanel.add(goalRowField);
        goalPanel.add(new JLabel("Goal Col (0-index):"));
        goalColField = new JTextField(3);
        goalPanel.add(goalColField);
        customPanel.add(customBoardInput, BorderLayout.CENTER);
        customPanel.add(goalPanel, BorderLayout.SOUTH);
        customPanel.setVisible(false);
        add(customPanel, BorderLayout.WEST);

        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Solution Moves"));
        add(outputScroll, BorderLayout.CENTER);

        boardArea = new JTextArea(8, 40);
        boardArea.setEditable(false);
        JScrollPane boardScroll = new JScrollPane(boardArea);
        boardScroll.setBorder(BorderFactory.createTitledBorder("Final Board"));
        add(boardScroll, BorderLayout.SOUTH);

        testSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selection = (String) testSelector.getSelectedItem();
                customPanel.setVisible(selection.equals("Custom"));
            }
        });

        solveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runSelectedTest();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearCustomBoard();
            }
        });
    }

    private void clearCustomBoard() {
        StringBuilder zeros = new StringBuilder();
        for (int i = 0; i < ZhedSolver.ROWS; i++) {
            for (int j = 0; j < ZhedSolver.COLS; j++) {
                zeros.append("0");
                if (j < ZhedSolver.COLS - 1) zeros.append(" ");
            }
            if (i < ZhedSolver.ROWS - 1) zeros.append("\n");
        }
        customBoardInput.setText(zeros.toString());
        goalRowField.setText("");
        goalColField.setText("");
    }

    private Object[] getTestData(String testName) {
        if (testName.equals("Test 1")) {
            int[][] board = {
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,1,1,0,0},
                    {0,1,0,0,0,0,0,0},
                    {0,0,0,1,1,0,0,0},
                    {0,0,1,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0}
            };
            return new Object[] { board, 3, 6 };
        } else if (testName.equals("Test 2")) {
            int[][] board = {
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,3,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,2,0,0,0,0,0},
                    {0,0,0,0,0,3,0,0},
                    {0,0,0,2,0,0,0,0},
                    {0,0,0,0,0,0,0,0}
            };
            return new Object[] { board, 1, 2 };
        } else if (testName.equals("Test 3")) {
            int[][] board = {
                    {0,0,3,0,0,0,0,0},
                    {0,2,0,3,0,0,0,0},
                    {0,0,0,0,2,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,3,0},
                    {0,0,0,1,0,0,1,0},
                    {0,0,0,0,0,1,0,0},
                    {0,0,0,0,0,0,0,0}
            };
            return new Object[] { board, 5, 0 };
        } else if (testName.equals("Custom")) {
            try {
                int[][] board = new int[ZhedSolver.ROWS][ZhedSolver.COLS];
                String[] lines = customBoardInput.getText().split("\\n");
                if (lines.length != ZhedSolver.ROWS) {
                    JOptionPane.showMessageDialog(this, "Please enter exactly " + ZhedSolver.ROWS + " lines for the board.");
                    return null;
                }
                for (int i = 0; i < ZhedSolver.ROWS; i++) {
                    String[] tokens = lines[i].trim().split("\\s+");
                    if (tokens.length != ZhedSolver.COLS) {
                        JOptionPane.showMessageDialog(this, "Line " + (i+1) + " must contain exactly " + ZhedSolver.COLS + " numbers.");
                        return null;
                    }
                    for (int j = 0; j < ZhedSolver.COLS; j++) {
                        board[i][j] = Integer.parseInt(tokens[j]);
                    }
                }
                int goalRow = Integer.parseInt(goalRowField.getText().trim());
                int goalCol = Integer.parseInt(goalColField.getText().trim());
                return new Object[] { board, goalRow, goalCol };
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format in custom input.");
                return null;
            }
        }
        return null;
    }

    private void runSelectedTest() {
        String selectedTest = (String) testSelector.getSelectedItem();
        Object[] data = getTestData(selectedTest);
        if (data == null) return;
        int[][] board = (int[][]) data[0];
        int goalRow = (Integer) data[1];
        int goalCol = (Integer) data[2];

        List<ZhedSolver.Move> solution = ZhedSolver.solve(board, goalRow, goalCol);
        outputArea.setText("");
        boardArea.setText("");
        if (solution != null) {
            outputArea.append("Solution (" + solution.size() + " moves):\n");
            for (ZhedSolver.Move m : solution) {
                outputArea.append(m.toString() + "\n");
            }
        } else {
            outputArea.append("No solution found.");
        }
        int[][] finalBoard = copyBoard(board);
        if (solution != null) {
            for (ZhedSolver.Move m : solution) {
                simulateMove(finalBoard, m.row, m.col, m.dir);
            }
        }
        boardArea.setText(renderBoard(finalBoard));
    }

    private String renderBoard(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ZhedSolver.ROWS; i++) {
            for (int j = 0; j < ZhedSolver.COLS; j++) {
                int cell = board[i][j];
                if (cell == -1) sb.append(" X ");
                else sb.append(String.format(" %d ", cell));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // --- UI Helper Methods (mirroring solver helper methods) ---
    private static int deltaRow(ZhedSolver.Direction d) {
        return d.dr();
    }
    private static int deltaCol(ZhedSolver.Direction d) {
        return d.dc();
    }
    private static boolean simulateMove(int[][] board, int row, int col, ZhedSolver.Direction dir) {
        int value = board[row][col];
        if (value <= 0) return false;
        board[row][col] = -1;
        int count = 0;
        int r = row, c = col;
        while (count < value) {
            int nr = r + dir.dr();
            int nc = c + dir.dc();
            if (nr < 0 || nr >= ZhedSolver.ROWS || nc < 0 || nc >= ZhedSolver.COLS) {
                if (r == ZhedSolver.goalRow && c == ZhedSolver.goalCol) return true;
                else return false;
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
    private static int[][] copyBoard(int[][] board) {
        int[][] newBoard = new int[ZhedSolver.ROWS][ZhedSolver.COLS];
        for (int i = 0; i < ZhedSolver.ROWS; i++) {
            newBoard[i] = Arrays.copyOf(board[i], ZhedSolver.COLS);
        }
        return newBoard;
    }
    private static String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ZhedSolver.ROWS; i++) {
            for (int j = 0; j < ZhedSolver.COLS; j++) {
                sb.append(board[i][j]).append(",");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ZhedSolverUI ui = new ZhedSolverUI();
            ui.setVisible(true);
        });
    }
}