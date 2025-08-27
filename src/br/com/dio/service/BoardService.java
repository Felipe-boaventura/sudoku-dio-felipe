package br.com.dio.service;

import br.com.dio.model.Board;
import br.com.dio.model.GameStatusEnum;
import br.com.dio.model.Hint;
import br.com.dio.model.Space;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BoardService {

    private final static int BOARD_LIMIT = 9;
    private final Board board;
    private int[][] solutionBoard;


    public BoardService(final Map<String, String> gameConfig) {
        this.board = new Board(initBoard(gameConfig));
        this.solutionBoard = new int[BOARD_LIMIT][BOARD_LIMIT];
        loadSolutionBoard("config/easy.config");
    }

    public BoardService(String difficulty) {
        this.solutionBoard = new int[BOARD_LIMIT][BOARD_LIMIT];
        loadSolutionBoard(getConfigFilePath(difficulty));
        this.board = new Board(initBoardFromConfig(difficulty));
    }


    private List<List<Space>> initBoardFromConfig(String difficulty) {
        List<List<Space>> spaces = new ArrayList<>();
        for (int i = 0; i < BOARD_LIMIT; i++) {
            spaces.add(new ArrayList<>());
            for (int j = 0; j < BOARD_LIMIT; j++) {
                spaces.get(i).add(new Space(0, false)); // Inicializa com espaços vazios
            }
        }

        String configPath = getConfigFilePath(difficulty);
        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("[,;]");
                if (parts.length >= 3) {
                    int col = Integer.parseInt(parts[0]);
                    int row = Integer.parseInt(parts[1]);
                    int value = Integer.parseInt(parts[2]);
                    boolean fixed = false;
                    if (parts.length >= 4) {
                        fixed = Boolean.parseBoolean(parts[3]);
                    } else {
                        fixed = (value != 0);
                    }
                    if (col >= 0 && col < BOARD_LIMIT && row >= 0 && row < BOARD_LIMIT) {
                        spaces.get(col).set(row, new Space(value, fixed));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar o tabuleiro inicial da configuração: " + e.getMessage());
        }
        return spaces;
    }

    private String getConfigFilePath(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy":
                return "config/easy.config";
            case "medium":
                return "config/medium.config";
            case "hard":
                return "config/hard.config";
            default:
                throw new IllegalArgumentException("Dificuldade inválida: " + difficulty);
        }
    }

    public List<List<Space>> getSpaces() {
        return board.getSpaces();
    }

    public void reset() {
        board.reset();
    }

    public boolean hasErrors() {
        return board.hasErrors();
    }

    public GameStatusEnum getStatus() {
        return board.getStatus();
    }

    public boolean gameIsFinished() {
        return board.gameIsFinished();
    }

    private void loadSolutionBoard(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("[,;]");
                if (parts.length >= 3) {
                    int col = Integer.parseInt(parts[0]);
                    int row = Integer.parseInt(parts[1]);
                    int value = Integer.parseInt(parts[2]);
                    if (row >= 0 && row < BOARD_LIMIT && col >= 0 && col < BOARD_LIMIT) {
                        solutionBoard[row][col] = value;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar o gabarito: " + e.getMessage());
        }
    }

    private List<List<Space>> initBoard(final Map<String, String> gameConfig) {
        List<List<Space>> spaces = new ArrayList<>();
        for (int i = 0; i < BOARD_LIMIT; i++) {
            spaces.add(new ArrayList<>());
            for (int j = 0; j < BOARD_LIMIT; j++) {
                var positionConfig = gameConfig.get("%s,%s".formatted(i, j));
                var expected = Integer.parseInt(positionConfig.split(",")[0]);
                var fixed = Boolean.parseBoolean(positionConfig.split(",")[1]);
                var currentSpace = new Space(expected, fixed);
                spaces.get(i).add(currentSpace);
            }
        }
        return spaces;
    }

    public Hint getNextHint() {
        List<List<Space>> currentSpaces = board.getSpaces();
        for (int r = 0; r < BOARD_LIMIT; r++) {
            for (int c = 0; c < BOARD_LIMIT; c++) {
                Space space = currentSpaces.get(c).get(r);
                if (space.isEmpty() || (space.getActual() != null && space.getActual() != solutionBoard[r][c])) {
                    return new Hint(r, c, solutionBoard[r][c]);
                }
            }
        }
        return null;
    }
}
