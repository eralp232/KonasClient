package me.darki.konas.setting;

public final class Bind implements IBind {

    private int keyCode;

    public Bind(int keyCode) {
        this.keyCode = keyCode;
    }

    @Override
    public int getKeyCode() {
        return keyCode;
    }

    @Override
    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

}
