package me.darki.konas.gui.clickgui.frame;


public abstract class Frame {
    private final String name;

    private float posX;
    private float posY;

    private float prevPosX;
    private float prevPosY;

    private float width;
    private float height;

    private boolean dragging;
    private boolean extended;

    public Frame(String name, float posX, float posY, float width, float height) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
    }

    public void initialize() {
    }

    public void onMove(float posX, float posY) {
    }

    public void onRender(int mouseX, int mouseY, float partialTicks) {
    }

    public void onKeyTyped(char character, int keyCode) {
    }

    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    }

    public String getName() {
        return name;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public float getPrevPosX() {
        return prevPosX;
    }

    public void setPrevPosX(float prevPosX) {
        this.prevPosX = prevPosX;
    }

    public float getPrevPosY() {
        return prevPosY;
    }

    public void setPrevPosY(float prevPosY) {
        this.prevPosY = prevPosY;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }


    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public static boolean mouseWithinBounds(int mouseX, int mouseY, double x, double y, double width, double height) {
        return (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height);
    }
}
