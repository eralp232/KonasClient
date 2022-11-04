package me.darki.konas.gui.reconnect;

import me.darki.konas.mixin.mixins.IGuiDisconnected;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class KonasGuiDisconnected extends GuiScreen
{
    private final String reason;
    private final ITextComponent message;
    private List<String> multilineMessage;
    private final GuiScreen parentScreen;
    private int textHeight;
    private final ServerData lastServer;
    private final Timer timer;
    private final int delay;

    public KonasGuiDisconnected(GuiDisconnected disconnected, ServerData lastServer, int delay)
    {
        this.parentScreen = ((IGuiDisconnected) disconnected).getParentScreen();
        this.reason = ((IGuiDisconnected) disconnected).getReason();
        this.message = ((IGuiDisconnected) disconnected).getMessage();
        this.lastServer = lastServer;
        this.timer = new Timer();
        this.delay = delay;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {}

    @Override
    public void initGui()
    {
        this.buttonList.clear();
        this.multilineMessage = this.fontRenderer.listFormattedStringToWidth(this.message.getFormattedText(), this.width - 50);
        this.textHeight = this.multilineMessage.size() * this.fontRenderer.FONT_HEIGHT;
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + this.fontRenderer.FONT_HEIGHT, this.height - 30), I18n.format("gui.toMenu")));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, (int) Math.min(this.height / 1.85 + this.textHeight / 1.85 + this.fontRenderer.FONT_HEIGHT, this.height + 80), "Reconnect"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {

        switch (button.id) {
            case 0:
                this.mc.displayGuiScreen(this.parentScreen);
                break;
            case 1:
                connectToLastServer();
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.reason, this.width / 2, this.height / 2 - this.textHeight / 2 - this.fontRenderer.FONT_HEIGHT * 2, 11184810);
        int i = this.height / 2 - this.textHeight / 2;

        if (this.multilineMessage != null)
        {
            for (String s : this.multilineMessage)
            {
                this.drawCenteredString(this.fontRenderer, s, this.width / 2, i, 16777215);
                i += this.fontRenderer.FONT_HEIGHT;
            }
        }

        if(timer.hasPassed(delay * 1000)) {
            connectToLastServer();
        }
        float secondsLeft = delay - ((System.currentTimeMillis() - timer.getTime()) / 1000F);
        this.buttonList.get(1).displayString = "Reconnecting: " + Math.round(secondsLeft);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void connectToLastServer() {
        this.mc.displayGuiScreen(new GuiConnecting(this.parentScreen, this.mc, lastServer == null ? mc.getCurrentServerData() : lastServer));
    }

}
