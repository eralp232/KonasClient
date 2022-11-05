package me.darki.konas.gui.altmanager;

import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.LoginUtils;
import me.darki.konas.util.network.APIUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashSet;

public class GuiAltManager extends GuiScreen {
    protected GuiScreen prevScreen;
    protected String title = "Select Account";
    private GuiButton deleteButton;
    private GuiButton loginButton;
    public LinkedHashSet<GuiListAltAccountEntry> loadedEntries = new LinkedHashSet<>();
    public GuiListAltAccounts selectionList;
    private String[] responses = null;

    public GuiAltManager(GuiScreen screenIn) {
        prevScreen = screenIn;
    }

    public void setPrevScreen(GuiScreen prevScreen) {
        this.prevScreen = prevScreen;
    }

    public boolean validSession = false;

    public void initGui() {
        mc = Minecraft.getMinecraft();
        if (selectionList == null) {
            selectionList = new GuiListAltAccounts(this, mc, width, height, 32 + 20, height - 64, 36);
            loadedEntries.forEach(selectionList::addAccount);
        }
        selectionList.getEntries().forEach(e -> {
            if (e.altSummary.isLoggedIn()) {
                if (!mc.getSession().getProfile().getName().equals(e.altSummary.getName())) {
                    e.altSummary.setLoggedIn(false);
                }
            }
        });
        selectionList.setDimensions(width, height, 32 + 20, height - 64);
        selectionList.right = (int) (width - width * 0.025);
        selectionList.left = (int) (width * 0.025);
        postInit();
    }

    public void initializeGui() {
        validSession = LoginUtils.sessionValid();
        responses = APIUtils.getServerStates();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        selectionList.handleMouseInput();
    }

    public void postInit() {
        // Cancel Button
        addButton(new GuiButton(0, width / 2 + 82, height - 28, 72, 20, "Cancel"));
        // Add Account Button enabled when no selection
        addButton(addButton(new GuiButton(1, width / 2 - 154, height - 52, 150, 20, "Add Account")));
        // Delete Button
        deleteButton = addButton(new GuiButton(2, width / 2 - 154, height - 28, 150, 20, "Delete Account"));
        // Login Button
        loginButton = addButton(new GuiButton(3, width / 2 + 4, height - 52, 150, 20, "Login"));
        // Direct Login Button
        addButton(new GuiButton(5, width / 2 + 4, height - 28, 72, 20, "Direct"));
        loginButton.enabled = false;
        deleteButton.enabled = false;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            GuiListAltAccountEntry guiListAltAccountEntry = selectionList.getSelectedAccount();
            // Cancel
            if (button.id == 0) {
                mc.displayGuiScreen(prevScreen);
            }
            // Add Account
            else if (button.id == 1) {
                mc.displayGuiScreen(new GuiAddAccount());
            }
            // Delete
            else if (button.id == 2) {
                if (guiListAltAccountEntry != null) {
                    guiListAltAccountEntry.deleteAccount();
                }
            }
            // Login
            else if (button.id == 3) {
                if (guiListAltAccountEntry != null) {
                    LoginThread loginThread = new LoginThread();
                    loginThread.start();
                    //KonasGlobals.INSTANCE.altManager.selectionList.getSelectedAccount().loginAccount();
                }
            }
            // Direct Login
            else if (button.id == 5) {
                mc.displayGuiScreen(new GuiDirectLogin(this));
            }
        }
    }

    private class LoginThread extends Thread {
        public void run() {
            KonasGlobals.INSTANCE.altManager.selectionList.getSelectedAccount().loginAccount();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        selectionList.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRenderer, title, width / 2, 20, 16777215);
        if (responses != null && responses.length != 0) {
            drawString(mc.fontRenderer, "Account Server: " + APIUtils.parseResponse(responses[0]).getSecond() + APIUtils.parseResponse(responses[0]).getFirst(), 2, 2, Color.WHITE.hashCode());
            drawString(mc.fontRenderer, "Auth Server: " + APIUtils.parseResponse(responses[1]).getSecond() + APIUtils.parseResponse(responses[1]).getFirst(), 2, mc.fontRenderer.FONT_HEIGHT + 2, Color.WHITE.hashCode());
            drawString(mc.fontRenderer, "Session Server: " + APIUtils.parseResponse(responses[2]).getSecond() + APIUtils.parseResponse(responses[2]).getFirst(), 2, mc.fontRenderer.FONT_HEIGHT * 2 + 2, Color.WHITE.hashCode());
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        selectionList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        selectionList.mouseReleased(mouseX, mouseY, state);
    }

    public void selectAccount(@Nullable GuiListAltAccountEntry entry) {
        boolean flag = entry != null;
        loginButton.enabled = flag;
        deleteButton.enabled = flag;
    }

    public GuiListAltAccounts getSelectionList() {
        return selectionList;
    }

}
