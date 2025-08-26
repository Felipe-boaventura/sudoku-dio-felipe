package br.com.dio.service;

import br.com.dio.model.Board;
import br.com.dio.model.GameStatusEnum;
import br.com.dio.model.Hint;
import br.com.dio.model.Space;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BoardService {

    private final static int BOARD_LIMIT = 9;

    private final Board board;


    public BoardService(final Map<String, String> gameConfig) {
        this.board = new Board(initBoard(gameConfig));
    }

    public List<List<Space>> getSpaces(){
        return board.getSpaces();
    }

    public void reset(){
        board.reset();
    }

    public boolean hasErrors(){
        return board.hasErrors();
    }

    public GameStatusEnum getStatus(){
        return board.getStatus();
    }

    public boolean gameIsFinished(){
        return board.gameIsFinished();
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
    private List<Integer> getValidNumbersForSpace(List<List<Space>> spaces, int row, int col) {
        Set<Integer> used = new HashSet<>();

        // Verifica linha
        for (int c = 0; c < BoardService.BOARD_LIMIT; c++) {
            int val = spaces.get(c).get(row).getActual();
            if (val != 0) used.add(val);
        }

        // Verifica coluna
        for (int r = 0; r < BoardService.BOARD_LIMIT; r++) {
            int val = spaces.get(col).get(r).getActual();
            if (val != 0) used.add(val);
        }

        // Verifica setor 3x3
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                int val = spaces.get(c).get(r).getActual();
                if (val != 0) used.add(val);
            }
        }

        // Retorna os números válidos
        return IntStream.rangeClosed(1, 9)
                .filter(n -> !used.contains(n))
                .boxed()
                .collect(Collectors.toList());
    }

    public Hint getHint() {
        List<List<Space>> spaces = getSpaces();

        for (int row = 0; row < BOARD_LIMIT; row++) {
            for (int col = 0; col < BOARD_LIMIT; col++) {
                Space space = spaces.get(col).get(row);
                if (space.isEmpty()) {
                    List<Integer> validNumbers = getValidNumbersForSpace(spaces, row, col);
                    if (!validNumbers.isEmpty()) {
                        return new Hint(row, col, validNumbers.get(0));
                    }
                }
            }
        }
        return null;
    }
}
