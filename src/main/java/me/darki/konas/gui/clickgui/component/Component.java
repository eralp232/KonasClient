package me.darki.konas.gui.clickgui.component;

public abstract class Component {
    private final String name;

    // For simplicity, components have their parent's posXY, and an offset
    private float parentX;
    private float parentY;

    private float prevPosX;
    private float prevPosY;

    private float offsetX;
    private float offsetY;

    private float absoluteX;
    private float absoluteY;

    private final float width;
    private final float height;

    private boolean dragging;
    private boolean extended;

    public Component(String name, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        this.name = name;
        this.parentX = parentX;
        this.parentY = parentY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.absoluteX = parentX + offsetX;
        this.absoluteY = parentY + offsetY;
        this.width = width;
        this.height = height;
    }

    public void initialize() {
    }

    public void onMove(float parentX, float parentY) {
        setParentX(parentX);
        setParentY(parentY);
        setAbsoluteX(getParentX() + getOffsetX());
        setAbsoluteY(getParentY() + getOffsetY());
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

    public float getParentX() {
        return parentX;
    }

    public void setParentX(float parentX) {
        this.parentX = parentX;
    }

    public float getParentY() {
        return parentY;
    }

    public void setParentY(float parentY) {
        this.parentY = parentY;
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

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public float getAbsoluteX() {
        return absoluteX;
    }

    public void setAbsoluteX(float absoluteX) {
        this.absoluteX = absoluteX;
    }

    public float getAbsoluteY() {
        return absoluteY;
    }

    public void setAbsoluteY(float absoluteY) {
        this.absoluteY = absoluteY;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
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
