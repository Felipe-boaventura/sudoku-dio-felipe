package br.com.dio.model;

public record Hint (int row, int col, int value){
    @Override
    public String toString(){
        return "Linha " + (row + 1) + ", Coluna " + (col + 1) + " → Sugestão: " + value;

    }
}
