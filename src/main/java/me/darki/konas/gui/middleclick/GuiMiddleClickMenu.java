package me.darki.konas.gui.middleclick;

import me.darki.konas.command.commands.PartyCommand;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.FontRendererWrapper;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;

public class GuiMiddleClickMenu extends GuiScreen {
    private final EntityPlayer player;
    private final Timer timer = new Timer();

    public GuiMiddleClickMenu(EntityPlayer player) {
        this.player = player;
        timer.reset();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        Vec3d pos = new Vec3d(player.lastTickPosX + (player.posX - player.lastTickPosX) * mc.getRenderPartialTicks(),
                player.lastTickPosY + (player.posY - player.lastTickPosY) * mc.getRenderPartialTicks(),
                player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * mc.getRenderPartialTicks()).add(0, player.height, 0);

        Vec3d screenPos = KonasGlobals.INSTANCE.vectorUtils.toScaledScreenPos(pos);

        boolean friended = Friends.isFriend(player.getName());
        boolean partied = PartyCommand.party.contains(player.getName());

        boolean practice = false;

        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null) {
            if (mc.getCurrentServerData().serverIP.contains("pvp")) {
                practice = true;
            }
        }

        float width = Math.max(FontRendererWrapper.getStringWidth(friended ? "Unfriend" : "Friend"), FontRendererWrapper.getStringWidth(partied ? "Unparty" : "Party"));

        float height = 8F + FontRendererWrapper.getStringHeight(friended ? "Unfriend" : "Friend") + FontRendererWrapper.getStringHeight(partied ? "Unparty" : "Party");

        if (practice) {
            height += 4F + FontRendererWrapper.getStringHeight("Duel");
        }

        GuiRenderHelper.drawRect((float) screenPos.x - width/2F - 2F, (float) screenPos.y - 2F, width + 4F, height, 0x80000000);
        GuiRenderHelper.drawOutlineRect((float) screenPos.x - width/2F - 2F, (float) screenPos.y - 2F, width + 4F, height,1F, 0xD0000000);

        FontRendererWrapper.drawString(friended ? "Unfriend" : "Friend", (float) screenPos.x - FontRendererWrapper.getStringWidth(friended ? "Unfriend" : "Friend") / 2F, (float) screenPos.y, -1);
        FontRendererWrapper.drawString(partied ? "Unparty" : "Party", (float) screenPos.x - FontRendererWrapper.getStringWidth(partied ? "Unparty" : "Party") / 2F, (float) screenPos.y + 4F + FontRendererWrapper.getStringHeight(friended ? "Unfriend" : "Friend"), -1);
        if (practice) {
            FontRendererWrapper.drawString("Duel", (float) screenPos.x - FontRendererWrapper.getStringWidth("Duel") / 2F, (float) screenPos.y + 4F + FontRendererWrapper.getStringHeight(friended ? "Unfriend" : "Friend") + 4F + FontRendererWrapper.getStringHeight(partied ? "Unparty" : "Party"), -1);
        }

        if (timer.hasPassed(5000)) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        Vec3d pos = new Vec3d(player.lastTickPosX + (player.posX - player.lastTickPosX) * mc.getRenderPartialTicks(),
                player.lastTickPosY + (player.posY - player.lastTickPosY) * mc.getRenderPartialTicks(),
                player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * mc.getRenderPartialTicks()).add(0, player.height, 0);

        Vec3d screenPos = KonasGlobals.INSTANCE.vectorUtils.toScaledScreenPos(pos);

        boolean friended = Friends.isFriend(player.getName());
        boolean partied = PartyCommand.party.contains(player.getName());

        boolean practice = false;

        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null) {
            if (mc.getCurrentServerData().serverIP.contains("pvp")) {
                practice = true;
            }
        }

        if (mouseButton == 0) {
            if (mouseWithinBounds(mouseX, mouseY, (float) screenPos.x - FontRendererWrapper.getStringWidth(friended ? "Unfriend" : "Friend") / 2F, (float) screenPos.y, FontRendererWrapper.getStringWidth(friended ? "Unfriend" : "Friend"), FontRendererWrapper.getStringHeight(friended ? "Unfriend" : "Friend"))) {
                if (friended) {
                    Friends.delFriend(player.getName());
                } else {
                    Friends.addFriend(player.getName(), player.getUniqueID().toString().replace("-", ""));
                }
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                mc.displayGuiScreen(null);
            } else if (mouseWithinBounds(mouseX, mouseY, (float) screenPos.x - FontRendererWrapper.getStringWidth(partied ? "Unparty" : "Party") / 2F, (float) screenPos.y + 4F + FontRendererWrapper.getStringHeight(friended ? "Unfriend" : "Friend"), FontRendererWrapper.getStringWidth(partied ? "Unparty" : "Party"), FontRendererWrapper.getStringHeight(partied ? "Unparty" : "Party"))) {
                if (partied) {
                    PartyCommand.party.remove(player.getName());
                } else {
                    PartyCommand.party.add(player.getName());
                }
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                mc.displayGuiScreen(null);
            } else if (practice && mouseWithinBounds(mouseX, mouseY, (float) screenPos.x - FontRendererWrapper.getStringWidth(partied ? "Unparty" : "Party") / 2F, (float) screenPos.y + 4F + FontRendererWrapper.getStringHeight(friended ? "Unfriend" : "Friend") + 4F + FontRendererWrapper.getStringHeight(partied ? "Unparty" : "Party"), FontRendererWrapper.getStringWidth("Duel"), FontRendererWrapper.getStringHeight("Duel"))) {
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                mc.displayGuiScreen(null);
                mc.player.connection.sendPacket(new CPacketChatMessage("/duel " + player.getName()));
            }
        }
    }

    public static boolean mouseWithinBounds(int mouseX, int mouseY, double x, double y, double width, double height) {
        return (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height);
    }
}
