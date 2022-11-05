package me.darki.konas.command.chunks;

import me.darki.konas.command.SyntaxChunk;
import org.lwjgl.input.Keyboard;

public class KeyChunk extends SyntaxChunk {
    public KeyChunk(String name) {
        super(name);
    }

    @Override
    public String predict(String currentArg) {
        for (int i = 0; i < 84; i++) {
            try {
                if (Keyboard.getKeyName(i).toLowerCase().startsWith(currentArg.toLowerCase())) {
                    return Keyboard.getKeyName(i);
                }
            } catch (NullPointerException oops) {
                oops.printStackTrace();
            }
        }
        return currentArg;
    }
}
