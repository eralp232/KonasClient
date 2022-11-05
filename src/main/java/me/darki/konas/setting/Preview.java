package me.darki.konas.setting;

public class Preview {
    private final boolean esp;
    private final boolean chams;

    public Preview(boolean esp, boolean chams) {
        this.esp = esp;
        this.chams = chams;
    }

    public boolean isEsp() {
        return esp;
    }

    public boolean isChams() {
        return chams;
    }
}
