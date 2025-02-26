import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ZhedSolverUI extends JFrame {
    private static final int SIZE = 8; // Board is 8x8


    private JTextField[][] boardFields;
    private JTextField goalIField;
    private JTextField goalJField;
    private JTextArea resultArea;
    private JButton solveButton;
    private JButton clearButton;

    public ZhedSolverUI() {
        super("Zhed Solver UI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel for the board
        JPanel boardPanel = new JPanel(new GridLayout(SIZE, SIZE, 2, 2));
        boardFields = new JTextField[SIZE][SIZE];

        // Create text fields for each cell in the board
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boardFields[i][j] = new JTextField("0", 2);
                boardFields[i][j].setHorizontalAlignment(JTextField.CENTER);
                boardPanel.add(boardFields[i][j]);
            }
        }

       
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        controlPanel.add(new JLabel("Goal I:"));
        goalIField = new JTextField("1", 2);
        controlPanel.add(goalIField);

        controlPanel.add(new JLabel("Goal J:"));
        goalJField = new JTextField("2", 2);
        controlPanel.add(goalJField);

        solveButton = new JButton("Solve");
        controlPanel.add(solveButton);


        clearButton = new JButton("Clear");
        controlPanel.add(clearButton);


        resultArea = new JTextArea(12, 40);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);


        add(boardPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);


        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solvePuzzle();
            }
        });


        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllFields();
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void solvePuzzle() {
        try {
            // Parse the board from text fields
            int[][] board = new int[SIZE][SIZE];
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    board[i][j] = Integer.parseInt(boardFields[i][j].getText().trim());
                }
            }

            // Parse the goal coordinates
            int goalI = Integer.parseInt(goalIField.getText().trim());
            int goalJ = Integer.parseInt(goalJField.getText().trim());


            ZhedSolver solver = new ZhedSolver(goalI, goalJ);
            List<String> solution = solver.solve(board);

            // Display results
            resultArea.setText("");
            if (solution != null) {
                resultArea.append("Solution found!\n");
                for (String step : solution) {
                    resultArea.append(step + "\n");
                }
            } else {
                resultArea.append("No solution found.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please make sure all fields contain valid integers.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private void clearAllFields() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boardFields[i][j].setText("0");
            }
        }
        goalIField.setText("0");
        goalJField.setText("0");
        resultArea.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ZhedSolverUI());
    }
}
