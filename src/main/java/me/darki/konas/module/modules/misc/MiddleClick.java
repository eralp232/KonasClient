package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.EventDispatcher;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.DirectMessageEvent;
import me.darki.konas.event.events.KeyEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.gui.middleclick.GuiMiddleClickMenu;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MiddleClick extends Module {

    private static Setting<Action> action = new Setting<>("Action", Action.MENU);
    private static Setting<Integer> range = new Setting<>("Range", 40, 250, 10, 10).withVisibility(() -> action.getValue() == Action.MENU);
    private static Setting<Boolean> throughWalls = new Setting<>("ThroughWalls", true).withVisibility(() -> action.getValue() == Action.MENU);
    private Setting<Boolean> rocket = new Setting<>("Rocket", false);
    private Setting<Boolean> ep = new Setting<>("EP", false);
    private Setting<Boolean> xp = new Setting<>("XP", false);
    private Setting<Boolean> xpInHoles = new Setting<>("XPInHoles", false).withVisibility(xp::getValue);

    private enum Action {
        MENU, FRIEND, MISC
    }
    
    public MiddleClick() {
        super("MiddleClick", Category.MISC, "MiddleClickPearl", "MiddleClickEP", "MCF", "MCP", "MiddleClickFriends");
    }

    private Timer xpTimer = new Timer();

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getPhase() == TickEvent.Phase.END) return;
        if (action.getValue() != Action.MENU) return;
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindPickBlock)) return;
        EntityPlayer rayTracedEntity = getEntityUnderMouse(range.getValue());
        if (rayTracedEntity != null) {
            xpTimer.reset();
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new GuiMiddleClickMenu(rayTracedEntity));
            }
        }
    }

    public static EntityPlayer getEntityUnderMouse(int range) {
        Entity entity = mc.getRenderViewEntity();

        if (entity != null) {
            Vec3d pos = mc.player.getPositionEyes(1F);
            for (float i = 0F; i < range; i += 0.5F) {
                pos = pos.add(mc.player.getLookVec().scale(0.5));
                if (!throughWalls.getValue()) {
                    if (mc.world.getBlockState(new BlockPos(pos.x, pos.y, pos.z)).getBlock() != Blocks.AIR) return null;
                }
                for (EntityPlayer player : mc.world.playerEntities) {
                    if (player == mc.player) continue;
                    AxisAlignedBB bb = player.getEntityBoundingBox();
                    if (bb == null) continue;
                    if (player.getDistance(mc.player) > 6) {
                        bb = bb.grow(0.5);
                    }
                    if (bb.contains(pos)) return player;
                }
            }
        }

        return null;
    }

    private static Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

    @Subscriber
    public void onKey(KeyEvent event) {

        if (mc.player == null || mc.world == null) return;

        if (event.getKey() != mc.gameSettings.keyBindPickBlock.getKeyCode()) return;

        if (action.getValue() == Action.FRIEND &&
                mc.objectMouseOver.entityHit != null) {
            Entity entity = mc.objectMouseOver.entityHit;
            if (entity instanceof EntityPlayer) {
                if (Friends.isFriend(entity.getName())) {
                    Friends.delFriend(entity.getName());
                    Logger.sendChatMessage("Removed §b" + entity.getName() + "§r as a friend!");
                } else {
                    Friends.addFriend(entity.getName(), entity.getUniqueID().toString());
                    if (ModuleManager.getModuleByClass(ExtraChat.class).isEnabled() && ExtraChat.friended.getValue()) {
                        EventDispatcher.Companion.dispatch(new DirectMessageEvent(entity.getName(), "I just friended you on Konas!"));
                    }
                    Logger.sendChatMessage("Added §b" + entity.getName() + "§r as a friend!");
                }
                xpTimer.reset();
                return;
            }
        }

        if (rocket.getValue() && findRocketSlot() != -1) {
            xpTimer.reset();
            int rocketSlot = findRocketSlot();
            int originalSlot = mc.player.inventory.currentItem;

            if (rocketSlot != -1) {
                mc.player.inventory.currentItem = rocketSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(rocketSlot));

                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                mc.player.inventory.currentItem = originalSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(originalSlot));
                return;
            }
        }

        if (ep.getValue() && (!xp.getValue() || (xpInHoles.getValue() && !BlockUtils.isHole(new BlockPos(mc.player))))) {
            int epSlot = findEPSlot();
            int originalSlot = mc.player.inventory.currentItem;

            if (epSlot != -1) {
                mc.player.inventory.currentItem = epSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(epSlot));

                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                mc.player.inventory.currentItem = originalSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(originalSlot));
                return;
            }
        }
    }

    int originalSlot = -1;
    boolean firstSwap = true;

    @Subscriber
    public void onUpdate(UpdateWalkingPlayerEvent.Post event) {

        if(GameSettings.isKeyDown(mc.gameSettings.keyBindPickBlock) && xpTimer.hasPassed(350)) {

            if (xp.getValue() && (!xpInHoles.getValue() || BlockUtils.isHole(new BlockPos(mc.player)))) {
                int epSlot = findXPSlot();
                if(firstSwap) {
                    originalSlot = mc.player.inventory.currentItem;
                    firstSwap = false;
                }

                if (epSlot != -1) {
                    mc.player.inventory.currentItem = epSlot;
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(epSlot));
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                }
            }
        } else if(originalSlot != -1) {
            mc.player.inventory.currentItem = originalSlot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(originalSlot));
            originalSlot = -1;
            firstSwap = true;
        }

    }

    private int findRocketSlot() {
        int rocketSlot = -1;

        if (mc.player.getHeldItemMainhand().getItem() == Items.FIREWORKS) {
            rocketSlot = mc.player.inventory.currentItem;
        }


        if (rocketSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.FIREWORKS) {
                    rocketSlot = l;
                    break;
                }
            }
        }

        return rocketSlot;
    }


    private int findEPSlot() {
        int epSlot = -1;

        if (mc.player.getHeldItemMainhand().getItem() == Items.ENDER_PEARL) {
            epSlot = mc.player.inventory.currentItem;
        }


        if (epSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.ENDER_PEARL) {
                    epSlot = l;
                    break;
                }
            }
        }

        return epSlot;
    }

    private int findXPSlot() {
        int epSlot = -1;

        if (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE) {
            epSlot = mc.player.inventory.currentItem;
        }


        if (epSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.EXPERIENCE_BOTTLE) {
                    epSlot = l;
                    break;
                }
            }
        }

        return epSlot;
    }

}
