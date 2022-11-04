package me.darki.konas.command;

public class SyntaxChunk {
    private final String name;

    public SyntaxChunk(String name) {
        this.name = name;
    }

    public String predict(String currentArg) {
        return currentArg;
    }

    public String getName() {
        return name;
    }
}
