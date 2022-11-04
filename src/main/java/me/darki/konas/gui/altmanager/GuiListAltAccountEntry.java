package me.darki.konas.gui.altmanager;

import me.darki.konas.mixin.mixins.IMinecraft;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.LoginUtils;
import me.darki.konas.util.network.APIUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class GuiListAltAccountEntry implements GuiListExtended.IGuiListEntry {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation ICON_MISSING = new ResourceLocation("konas/textures/steve.png");
    private final Minecraft mc;
    private final GuiAltManager altManagerScreen;
    public final AltSummary altSummary;
    private DynamicTexture icon;
    private ResourceLocation location;
    private long lastClickTime;
    private BufferedImage face = null;

    public GuiListAltAccountEntry(AltSummary altSummaryIn) {
        this.altManagerScreen = KonasGlobals.INSTANCE.altManager;
        this.altSummary = altSummaryIn;
        this.mc = Minecraft.getMinecraft();
        if(!altSummary.isCracked()) {
            new Thread(this::loadFace).start();
        }
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        String s = this.altSummary.isLoggedIn() ? TextFormatting.GREEN + this.altSummary.getName() : this.altSummary.getName();
        String s1 = this.altSummary.getEmail();
        String s2 = this.altSummary.isCracked() ? TextFormatting.RED + "Cracked" : TextFormatting.GREEN + "Premium";


        this.mc.fontRenderer.drawString(s, x + 32 + 3, y + 1, 16777215);
        if (!this.altSummary.isCracked()) {
            this.mc.fontRenderer.drawString(s1, x + 32 + 3, y + this.mc.fontRenderer.FONT_HEIGHT + 2, 8421504);
            this.mc.fontRenderer.drawString(s2, x + 32 + 3, y + this.mc.fontRenderer.FONT_HEIGHT + this.mc.fontRenderer.FONT_HEIGHT + 3, 8421504);
        } else {
            this.mc.fontRenderer.drawString(s2, x + 32 + 3, y + this.mc.fontRenderer.FONT_HEIGHT + 3, 8421504);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if(face != null) {
            if (this.icon == null) {
                this.icon = new DynamicTexture(face);
                this.location = this.mc.getTextureManager().getDynamicTextureLocation(altSummary.getUuid(), this.icon);
                this.mc.getTextureManager().loadTexture(location, this.icon);
                face.getRGB(0, 0, face.getWidth(), face.getHeight(), this.icon.getTextureData(), 0, face.getWidth());
                this.icon.updateDynamicTexture();
            }
        } else if(!altSummary.isCracked()) {
            new Thread(this::loadFace).start();
        }
        this.mc.getTextureManager().bindTexture(this.icon != null ? this.location : ICON_MISSING);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        GlStateManager.disableBlend();
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        KonasGlobals.INSTANCE.altManager.getSelectionList().selectAccount(slotIndex);

        if (relativeX <= 32 && relativeX < 32) {
            this.loginAccount();
            return true;
        } else if (Minecraft.getSystemTime() - this.lastClickTime < 250L) {
            this.loginAccount();
            return true;
        } else {
            this.lastClickTime = Minecraft.getSystemTime();
            return false;
        }
    }

    public void deleteAccount() {
        this.mc.displayGuiScreen(new GuiYesNo((result, id) -> {
            if (result) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiScreenWorking());
                KonasGlobals.INSTANCE.altManager.getSelectionList().deleteAccount(this);
            }
            GuiListAltAccountEntry.this.mc.displayGuiScreen(GuiListAltAccountEntry.this.altManagerScreen);
        }, "Are you sure you want to delete '" + this.altSummary.getName() + "'?", "This process cannot be reverted.", "Delete", "Cancel", 0));
    }

    public boolean tryLoginAccountPremium() {
        return LoginUtils.login(altSummary, altSummary.getEmail(), altSummary.getPassword());
    }

    public boolean tryMicrosoftLogin() {
        return LoginUtils.loginMicrosoft(altSummary, altSummary.getEmail(), altSummary.getPassword());
    }

    public void loginAccount() {
        if (altSummary.isLoggedIn()) return;
        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));

        boolean premiumLogin;

        File file = new File(mc.gameDir + File.separator + "exploit.txt");

        if(altSummary.isMicrosoft()) {
            ((IMinecraft) Minecraft.getMinecraft()).setSession(new Session(altSummary.getName(), altSummary.getUuid(), altSummary.getToken(), "mojang"));
            if(!LoginUtils.sessionValid()) {
                premiumLogin = LoginUtils.loginMicrosoft(altSummary, altSummary.getEmail(), altSummary.getPassword());
            } else {
                premiumLogin = true;
            }
        } else if (!file.exists() || altSummary.getToken().isEmpty() || altSummary.getUuid().isEmpty()) {
            premiumLogin = LoginUtils.login(altSummary, altSummary.getEmail(), altSummary.getPassword());
        } else {
            premiumLogin = true;
            ((IMinecraft) Minecraft.getMinecraft()).setSession(new Session(altSummary.getName(), altSummary.getUuid(), altSummary.getToken(), "mojang"));
        }

        if (premiumLogin) {
            handlePremiumLogin();
        } else if (LoginUtils.loginOffline(altSummary.getName())) {
            handleCrackedLogin();
        }

        altManagerScreen.validSession = LoginUtils.sessionValid();
    }

    public void handlePremiumLogin() {
        altManagerScreen.getSelectionList().getEntries().forEach(e -> e.altSummary.setLoggedIn(false));
        altSummary.setCracked(false);
        altSummary.setLoggedIn(true);
        altSummary.setLastTimeLoggedIn(System.currentTimeMillis());
        altSummary.setName(Minecraft.getMinecraft().getSession().getUsername());
    }

    public void handleCrackedLogin() {
        altManagerScreen.getSelectionList().getEntries().forEach(e -> e.altSummary.setLoggedIn(false));
        altSummary.setCracked(true);
        altSummary.setLoggedIn(true);
        altSummary.setLastTimeLoggedIn(System.currentTimeMillis());
    }

    public void handleMicrosoftLogin() {

    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
    }

    @Override
    public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
    }

    private void loadFace() {

        String uuid = APIUtils.getUUIDFromName(altSummary.getName());

        if (uuid != null) {
            uuid = uuid.replaceAll("-", "");
            altSummary.setUuid(uuid);
        } else {
            altSummary.setCracked(true);
            return;
        }

        try {
            face = ImageIO.read(APIUtils.getFaceInputStream(uuid));
            Validate.validState(face.getWidth() == 64, "Must be 64 pixels wide");
            Validate.validState(face.getHeight() == 64, "Must be 64 pixels high");
        } catch (Throwable throwable) {
            System.err.println("Couldn't load face");
        }
    }
}