package me.darki.konas.gui.altmanager;

import me.darki.konas.mixin.mixins.IGuiTextField;
import me.darki.konas.util.KonasGlobals;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class GuiAddAccount extends GuiScreen {

    private GuiTextField nameEdit;
    private GuiPasswordField passwordEdit;
    boolean invalidCredentials;
    boolean invalidCredentialsMicrosoft;

    public GuiAddAccount() {
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
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 18, "Add"));
        this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height / 4 + 120 + 18, "Add through Microsoft"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 144 + 18, "Cancel"));
        this.buttonList.add(new GuiButton(5, this.width / 2 - 100, this.height / 4 + 168 + 18, "Login through browser"));
        this.buttonList.get(3).visible = false;
        this.nameEdit = new GuiTextField(2, this.fontRenderer, this.width / 2 - 100, 66, 200, 20);
        ((IGuiTextField) this.nameEdit).setMaxStringLength(320);
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
                this.mc.displayGuiScreen(KonasGlobals.INSTANCE.altManager);
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
                KonasGlobals.INSTANCE.altManager.getSelectionList().addAccount(entry);
                mc.displayGuiScreen(KonasGlobals.INSTANCE.altManager);
            } else if (button.id == 3) {
                if(nameEdit.getText().isEmpty()) return;
                if(this.passwordEdit.getText().isEmpty()) {
                    invalidCredentials = true;
                } else {
                    AltSummary summary = new AltSummary(this.nameEdit.getText().trim(), this.passwordEdit.getText().trim(), true, true);
                    GuiListAltAccountEntry entry = new GuiListAltAccountEntry(summary);
                    if(!entry.tryMicrosoftLogin()) {
                        invalidCredentials = true;
                        invalidCredentialsMicrosoft = true;
                        return;
                    }
                    entry.handlePremiumLogin();
                    KonasGlobals.INSTANCE.altManager.getSelectionList().addAccount(entry);
                    mc.displayGuiScreen(KonasGlobals.INSTANCE.altManager);
                }
            } else if (button.id == 5) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://login.live.com/oauth20_authorize.srf?redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=service::user.auth.xboxlive.com::MBI_SSL&display=touch&response_type=code&locale=en&client_id=00000000402b5328"));
                    } catch (URISyntaxException | IOException ignored) {}
                }
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
        this.drawCenteredString(this.fontRenderer, "Add Account", this.width / 2, 17, 16777215);
        this.drawString(this.fontRenderer, "Username", this.width / 2 - 100, 53, 10526880);
        this.drawString(this.fontRenderer, "Password", this.width / 2 - 100, 94, 10526880);
        if(invalidCredentials)  this.drawCenteredString(this.fontRenderer, TextFormatting.DARK_RED + "Invalid Credentials", this.width / 2, this.height / 4 + 96, 16777215);
        if(invalidCredentialsMicrosoft) this.buttonList.get(3).visible = true;
        this.nameEdit.drawTextBox();
        this.passwordEdit.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
