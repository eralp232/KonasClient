package me.darki.konas.gui.altmanager;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiDirectLogin extends GuiScreen {

    GuiAltManager altManager;
    private GuiTextField nameEdit;
    private GuiPasswordField passwordEdit;
    boolean invalidCredentials;

    public GuiDirectLogin(GuiAltManager altManager) {
        this.altManager = altManager;
    }

    @Override
    public void updateScreen() {
        this.nameEdit.updateCursorCounter();
        this.passwordEdit.updateCursorCounter();
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 18, "Login"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 18, "Cancel"));
        this.nameEdit = new GuiTextField(2, this.fontRenderer, this.width / 2 - 100, 66, 200, 20);
        this.passwordEdit = new GuiPasswordField(3, this.fontRenderer, this.width / 2 - 100, 106, 200, 20);
        this.nameEdit.setFocused(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 1) {
                this.mc.displayGuiScreen(altManager);
            } else if (button.id == 0) {
                if(nameEdit.getText().isEmpty()) return;
                AltSummary summary;
                GuiListAltAccountEntry entry;
                if(this.passwordEdit.getText().isEmpty()) {
                    summary = new AltSummary(this.nameEdit.getText().trim(), this.passwordEdit.getText().trim(), false, false);
                    entry = new GuiListAltAccountEntry(summary);
                    entry.handleCrackedLogin();
                } else {
                    summary = new AltSummary(this.nameEdit.getText().trim(), this.passwordEdit.getText().trim(), true, false);
                    entry = new GuiListAltAccountEntry(summary);
                    if(!entry.tryLoginAccountPremium()) {
                        invalidCredentials = true;
                        return;
                    }
                    entry.handlePremiumLogin();
                }
                mc.displayGuiScreen(altManager);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if(nameEdit.isFocused()) {
            this.nameEdit.textboxKeyTyped(typedChar, keyCode);

            if (keyCode == 28 || keyCode == 156)
            {
                this.actionPerformed(this.buttonList.get(0));
            }
            if(keyCode == 15) {
                this.passwordEdit.setFocused(true);
                this.nameEdit.setFocused(false);
            }
        } else if(passwordEdit.isFocused()) {
            this.passwordEdit.textboxKeyTyped(typedChar, keyCode);

            if (keyCode == 28 || keyCode == 156)
            {
                this.actionPerformed(this.buttonList.get(0));
            }
        }

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameEdit.mouseClicked(mouseX, mouseY, mouseButton);
        this.passwordEdit.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "Direct Login", this.width / 2, 17, 16777215);
        this.drawString(this.fontRenderer, "Username", this.width / 2 - 100, 53, 10526880);
        this.drawString(this.fontRenderer, "Password", this.width / 2 - 100, 94, 10526880);
        if(invalidCredentials)  this.drawCenteredString(this.fontRenderer, TextFormatting.DARK_RED + "Invalid Credentials", this.width / 2, this.height / 4 + 96, 16777215);
        this.nameEdit.drawTextBox();
        this.passwordEdit.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
