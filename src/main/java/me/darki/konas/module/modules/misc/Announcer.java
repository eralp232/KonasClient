package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.EatFoodFinishEvent;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.PlayerConnectEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.timer.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.*;

public class Announcer extends Module {

    private final Setting<Boolean> welcome = new Setting<>("Welcome", true);
    private final Setting<Float> welcomeDelay = new Setting<>("WelcomeDelay", 2f, 60f, 0f, 1f);
    private final Setting<Boolean> announce = new Setting<>("Announce", false);
    private final Setting<Float> announceDelay = new Setting<>("AnnounceDelay", 20f, 60f, 1f, 1f);
    private final Setting<Float> globalDelay = new Setting<>("GlobalDelay", 1f, 60f, 0f, 1f);
    private final Setting<Boolean> advertisement = new Setting<>("Advertisement", true);

    public static final ArrayList<String> WELCOMES = new ArrayList<>();
    public static final ArrayList<String> GOODBYES = new ArrayList<>();

    private Random random = new Random();
    private Random randomEventChooser = new Random();

    private Timer welcomeTimer = new Timer();
    private Timer announceTimer = new Timer();
    private Timer globalTimer = new Timer();

    private LinkedHashMap<Type, Integer> events = new LinkedHashMap<>();

    private enum Type {
        WALK,
        PLACE,
        JUMP,
        DROP,
        BREAK,
        EAT,
        CRAFT,
        PICKUP
    }

    static double lastPositionX;
    static double lastPositionY;
    static double lastPositionZ;

    public Announcer() {
        super("Announcer", Category.MISC, "Welcomer");
        WELCOMES.add("Welcome <player>!");
        WELCOMES.add("Hello <player>");
        WELCOMES.add("Nice weather isn't it, <player>");

        GOODBYES.add("Goodbye <player>!");
        GOODBYES.add("Have a good day <player>");
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if(mc.player == null || mc.world == null) return;
        if(announce.getValue() && globalTimer.hasPassed(globalDelay.getValue() * 1000) && announceTimer.hasPassed(announceDelay.getValue() * 1000)) {
            double blocksWalked = getBlocksWalked(lastPositionX, lastPositionY, lastPositionZ);
            if(blocksWalked > 0 && blocksWalked < 5000 && mc.player.ticksExisted > 1000) {
                events.put(Type.WALK, (int) blocksWalked);
            }
            lastPositionX = mc.player.posX;
            lastPositionY = mc.player.posY;
            lastPositionZ = mc.player.posZ;
            if(events.isEmpty()) return;
            int index = randomEventChooser.nextInt(events.entrySet().size());
            for(int i = 0; i < events.entrySet().size(); i++) {
                if(i == index) {
                    List<Map.Entry<Type, Integer>> list = new ArrayList<>(events.entrySet());
                    Map.Entry<Type, Integer> entry = list.get(i);
                    mc.player.sendChatMessage(getMessage(entry.getKey(), entry.getValue()));
                    announceTimer.reset();
                    globalTimer.reset();
                    events.clear();
                }
            }
        }
    }

    @Subscriber
    public void onPacketSent(PacketEvent.Send event) {

        if(event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            addEvent(Type.PLACE);
        } else if(event.getPacket() instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging packet = (CPacketPlayerDigging) event.getPacket();
            if(packet.getAction() == CPacketPlayerDigging.Action.DROP_ALL_ITEMS || packet.getAction() == CPacketPlayerDigging.Action.DROP_ITEM) {
                addEvent(Type.DROP);
            } else if(packet.getAction() == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                addEvent(Type.BREAK);
            }
        }
    }

    @Subscriber
    public void onFoodEaten(EatFoodFinishEvent event) {
        if(event.getPlayer() == mc.player) {
            addEvent(Type.EAT);
        }
    }

    @Subscriber
    public void onJump(LivingEvent.LivingJumpEvent event) {
        if(event.getEntityLiving().equals(mc.player)) {
            addEvent(Type.JUMP);
        }
    }

    @Subscriber
    public void onItemPickUp(PlayerEvent.ItemPickupEvent event) {
        if(event.player.equals(mc.player)) {
            addEvent(Type.PICKUP);
        }
    }

    @Subscriber
    public void onItemCraft(PlayerEvent.ItemCraftedEvent event) {
        if(event.player.equals(mc.player)) {
            addEvent(Type.CRAFT);
        }
    }

    @Subscriber
    public void onJoinEvent(PlayerConnectEvent.Join event) {
        if(mc.player == null || mc.world == null) return;
        if(welcome.getValue() && mc.player.ticksExisted > 100 && event.getUuid() != mc.player.getUniqueID() && welcomeTimer.hasPassed(welcomeDelay.getValue() * 1000) && globalTimer.hasPassed(globalDelay.getValue() * 1000)) {
            sendWelcome(event.getName());
            welcomeTimer.reset();
            globalTimer.reset();
        }
    }

    @Subscriber
    public void onLeaveEvent(PlayerConnectEvent.Leave event) {
        if(mc.player == null || mc.world == null) return;
        if(welcome.getValue() && mc.player.ticksExisted > 100 && event.getUuid() != mc.player.getUniqueID() && welcomeTimer.hasPassed(welcomeDelay.getValue() * 1000) && globalTimer.hasPassed(globalDelay.getValue() * 1000)) {
            EntityPlayer player = mc.world.getPlayerEntityByUUID(event.getUuid());
            if (player != null) {
                sendGoodbye(player.getName());
                welcomeTimer.reset();
                globalTimer.reset();
            }
        }
    }

    public void sendWelcome(String name) {
        mc.player.sendChatMessage(WELCOMES.get(random.nextInt(WELCOMES.size())).replace("<player>", name));
    }

    public void sendGoodbye(String name) {
        mc.player.sendChatMessage(GOODBYES.get(random.nextInt(GOODBYES.size())).replace("<player>", name));
    }

    private void addEvent(Type type) {
        if(events.containsKey(type)) {
            events.put(type, events.get(type) + 1);
        } else {
            events.put(type, 1);
        }
    }

    private double getBlocksWalked(double lastPositionX, double lastPositionY, double lastPositionZ) {
        double d0 = lastPositionX - mc.player.posX;
        double d2 = lastPositionY - mc.player.posY;
        double d3 = lastPositionZ - mc.player.posZ;

        return Math.sqrt(d0 * d0 + d2 * d2 + d3 * d3);
    }

    private String getMessage(Type type, int count) {
        String ad = advertisement.getValue() ? " thanks to Konas!" : "!";
        String plural = count > 1 ? "s" : "";
        switch (type) {
            case PICKUP:
                return "I just picked up " + count + " item" + plural + ad;
            case CRAFT:
                return "I just crafted " + count + " item" + plural + ad;
            case BREAK:
                return "I just broke " + count + " block" + plural + ad;
            case PLACE:
                return "I just placed " + count + " block" + plural + ad;
            case DROP:
                return "I just dropped " + count + " item" + plural + ad;
            case EAT:
                return "I just ate " + count + " item" + plural + ad;
            case JUMP:
                return "I just jumped " + count + " time" + plural + ad;
            case WALK:
                return "I just walked " + count + " block" + plural + ad;
            default:
                return "I just did " + type + " " + count + " time" + plural + ad;
        }
    }

}
