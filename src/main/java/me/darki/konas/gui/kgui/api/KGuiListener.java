package me.darki.konas.gui.kgui.api;

public interface KGuiListener {
    void onRender(int mouseX, int mouseY, float partialTicks);
    void onRenderPost(int mouseX, int mouseY, float partialTicks);

    boolean mouseClicked(int mouseX, int mouseY, int mouseButton);
    boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick);

    void mouseReleased(int mouseX, int mouseY, int state);

    void keyTyped(char typedChar, int keyCode);

    void onResize(float parentX, float parentY);
}
