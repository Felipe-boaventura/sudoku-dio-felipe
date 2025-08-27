package br.com.dio.ui.custom.screen;

import br.com.dio.model.Space;
import br.com.dio.service.BoardService;
import br.com.dio.service.NotifierService;
import br.com.dio.ui.custom.button.*;
import br.com.dio.ui.custom.frame.MainFrame;
import br.com.dio.ui.custom.input.NumberText;
import br.com.dio.ui.custom.panel.MainPanel;
import br.com.dio.ui.custom.panel.SudokuSector;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static br.com.dio.service.EventEnum.CLEAR_SPACE;
import static javax.swing.JOptionPane.*;

public class MainScreen {

    private final static Dimension dimension = new Dimension(600, 600);

    private BoardService boardService;
    private final NotifierService notifierService;

    private JButton tipButton;
    private JButton checkGameStatusButton;
    private JButton finishGameButton;
    private JButton resetButton;
    private JButton difficultyButton;

    private JPanel mainPanel;
    private JPanel boardPanel;
    private JFrame mainFrame;

    public MainScreen(final Map<String, String> gameConfig) {
        this.boardService = new BoardService(gameConfig);
        this.notifierService = new NotifierService();
    }

    public MainScreen() {
        this.boardService = new BoardService("easy");
        this.notifierService = new NotifierService();
    }

    public void buildMainScreen() {
        mainPanel = new MainPanel(dimension);
        mainPanel.setLayout(new BorderLayout());

        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3));
        mainPanel.add(boardPanel, BorderLayout.CENTER);

        addSudokuSectorsToPanel(boardPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Layout para os botões

        addResetButton(buttonPanel);
        addCheckGameStatusButton(buttonPanel);
        addFinishGameButton(buttonPanel);
        addTipButton(buttonPanel);
        addDifficultyButton(buttonPanel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainFrame = new MainFrame(dimension, mainPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void addSudokuSectorsToPanel(JPanel targetPanel) {
        targetPanel.removeAll();


        for (int r = 0; r < 9; r += 3) {
            int endRow = r + 2;
            for (int c = 0; c < 9; c += 3) {
                int endCol = c + 2;
                var spaces = getSpacesFromSector(boardService.getSpaces(), c, endCol, r, endRow);
                JPanel sector = generateSection(spaces);
                targetPanel.add(sector);
            }
        }
        targetPanel.revalidate();
        targetPanel.repaint();
    }

    private List<Space> getSpacesFromSector(final List<List<Space>> spaces,
                                            final int initCol, final int endCol,
                                            final int initRow, final int endRow) {
        List<Space> spaceSector = new ArrayList<>();
        for (int r = initRow; r <= endRow; r++) {
            for (int c = initCol; c <= endCol; c++) {
                spaceSector.add(spaces.get(c).get(r));
            }
        }
        return spaceSector;
    }

    private JPanel generateSection(final List<Space> spaces) {
        List<NumberText> fields = new ArrayList<>(spaces.stream().map(NumberText::new).toList());
        fields.forEach(t -> notifierService.subscribe(CLEAR_SPACE, t));
        return new SudokuSector(fields);
    }

    private void addFinishGameButton(final JPanel panel) {
        finishGameButton = new FinishGameButton(e -> {
            if (boardService.gameIsFinished()) {
                showMessageDialog(null, "Parabéns você concluiu o jogo");
                resetButton.setEnabled(false);
                checkGameStatusButton.setEnabled(false);
                finishGameButton.setEnabled(false);
                tipButton.setEnabled(false);
                difficultyButton.setEnabled(false);
            } else {
                var message = "Seu jogo tem alguma inconsistência, ajuste e tente novamente";
                showMessageDialog(null, message);
            }
        });
        panel.add(finishGameButton);
    }

    private void addCheckGameStatusButton(final JPanel panel) {
        checkGameStatusButton = new CheckGameStatusButton(e -> {
            var hasErrors = boardService.hasErrors();
            var gameStatus = boardService.getStatus();
            var message = switch (gameStatus) {
                case NON_STARTED -> "O jogo não foi iniciado";
                case INCOMPLETE -> "O jogo está incompleto";
                case COMPLETE -> "O jogo está completo";
            };
            message += hasErrors ? " e contém erros" : " e não contém erros";
            showMessageDialog(null, message);
        });
        panel.add(this.checkGameStatusButton);
    }

    private void addResetButton(final JPanel panel) {
        resetButton = new ResetButton(e -> {
            var dialogResult = showConfirmDialog(
                    null,
                    "Deseja realmente reiniciar o jogo?",
                    "Limpar o jogo",
                    YES_NO_OPTION,
                    QUESTION_MESSAGE
            );
            if (dialogResult == 0) {
                boardService.reset();
                notifierService.notify(CLEAR_SPACE);
                resetButton.setEnabled(true);
                checkGameStatusButton.setEnabled(true);
                finishGameButton.setEnabled(true);
                tipButton.setEnabled(true);
                difficultyButton.setEnabled(true);
            }
        });
        panel.add(resetButton);
    }

    private void addTipButton(final JPanel panel) {
        tipButton = new TipButton(e -> {
            var dica = boardService.getNextHint();
            if (dica != null) {
                showMessageDialog(null, "Dica: " + dica);
            } else {
                showMessageDialog(null, "Não há dicas disponíveis no momento. O jogo pode estar completo ou sem erros visíveis.");
                tipButton.setEnabled(false);
            }
        });
        panel.add(tipButton);
    }

    private void addDifficultyButton(final JPanel panel) {
        difficultyButton = new DifficultyButton(e -> {
            String[] difficulties = {"fácil", "médio", "difícil"};
            String selectedDifficulty = (String) JOptionPane.showInputDialog(
                    mainFrame,
                    "Selecione a dificuldade:",
                    "Dificuldade do Sudoku",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    difficulties,
                    difficulties[0]
            );
            if (selectedDifficulty != null && !selectedDifficulty.isEmpty()) {
                this.boardService = new BoardService(selectedDifficulty.equals("fácil") ? "easy" :
                        selectedDifficulty.equals("médio") ? "medium" : "hard");
                addSudokuSectorsToPanel(boardPanel);
                resetButton.setEnabled(true);
                checkGameStatusButton.setEnabled(true);
                finishGameButton.setEnabled(true);
                tipButton.setEnabled(true);
                difficultyButton.setEnabled(true);
                mainFrame.revalidate();
                mainFrame.repaint();
            }
        });
        panel.add(difficultyButton);
    }
}
