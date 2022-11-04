package me.darki.konas.setting;

public class SubBind implements IBind {
    private int keyCode;

    public SubBind(int keyCode) {
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
