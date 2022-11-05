package me.darki.konas.event.listener;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.*;
import me.darki.konas.util.client.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Adapter {

    public static Adapter INSTANCE = new Adapter();

    @SubscribeEvent
    public void onRender2D(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            EventDispatcher.Companion.dispatch(new Render2DEvent());
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (event.isCanceled()) {
            return;
        }
        Minecraft.getMinecraft().profiler.startSection("konas");
        WorldRenderEvent worldRenderEvent = new WorldRenderEvent(event.getPartialTicks());
        EventDispatcher.Companion.dispatch(worldRenderEvent);
        Minecraft.getMinecraft().profiler.endSection();
    }

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        EventDispatcher.Companion.dispatch(event);
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        EventDispatcher.Companion.dispatch(event);
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        EventDispatcher.Companion.dispatch(event);
    }

    @SubscribeEvent
    public void onItemFov(EntityViewRenderEvent.FOVModifier event) {
        EventDispatcher.Companion.dispatch(event);
    }

    @SubscribeEvent
    public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        EventDispatcher.Companion.dispatch(event);
    }

    @SubscribeEvent
    public void onItemPickUp(PlayerEvent.ItemPickupEvent event) {
        EventDispatcher.Companion.dispatch(event);
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        EventDispatcher.Companion.dispatch(event);
    }

    @SubscribeEvent
    public void onEntityJump(LivingEvent.LivingJumpEvent event) {
        EventDispatcher.Companion.dispatch(event);
    }

    @SubscribeEvent
    public void onLoadGui(GuiOpenEvent event) {
        LoadGuiEvent loadGuiEvent = new LoadGuiEvent(event.getGui());
        EventDispatcher.Companion.dispatch(loadGuiEvent);
        event.setGui(loadGuiEvent.getGui());
        event.setCanceled(loadGuiEvent.isCancelled());
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        MoveInputEvent moveInputEvent = new MoveInputEvent(event.getEntityPlayer(), event.getMovementInput());
        EventDispatcher.Companion.dispatch(moveInputEvent);
        if(event.getEntityPlayer() == Minecraft.getMinecraft().player) {
            SprintEvent sprintEvent = new SprintEvent(moveInputEvent.getInput());
            EventDispatcher.Companion.dispatch(sprintEvent);
        }
    }
    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        EntityAttackEvent entityAttackEvent = EntityAttackEvent.get(event.getEntityPlayer(), event.getTarget());
        EventDispatcher.Companion.dispatch(entityAttackEvent);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        EventDispatcher.Companion.dispatch(event);
    }

    @SubscribeEvent
    public void onPlayerPush(PlayerSPPushOutOfBlocksEvent event) {
        BlockPushOutEvent pushEvent = BlockPushOutEvent.get(event.getEntityPlayer());
        EventDispatcher.Companion.dispatch(pushEvent);
        event.setCanceled(pushEvent.isCancelled());
    }

    @SubscribeEvent
    public void onFireOverlay(RenderBlockOverlayEvent event) {
        RenderOverlayEvent renderOverlayEvent = new RenderOverlayEvent(event.getOverlayType());
        EventDispatcher.Companion.dispatch(renderOverlayEvent);
        event.setCanceled(renderOverlayEvent.isCancelled());
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        PlayerUtils.lastLookAt = null;
        UpdateEvent updateEvent = UpdateEvent.get(event.phase);
        EventDispatcher.Companion.dispatch(updateEvent);
    }

    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        FogEvent.Density fogEvent = new FogEvent.Density(event.getDensity());
        EventDispatcher.Companion.dispatch(fogEvent);
        event.setDensity(fogEvent.getDensity());
        event.setCanceled(fogEvent.isCancelled());
    }

    @SubscribeEvent
    public void onFogColor(EntityViewRenderEvent.FogColors event) {
        FogEvent.Color fogEvent = new FogEvent.Color(event.getRed(), event.getGreen(), event.getBlue());
        EventDispatcher.Companion.dispatch(fogEvent);
        event.setRed(fogEvent.getR());
        event.setGreen(fogEvent.getG());
        event.setBlue(fogEvent.getB());
    }

    @SubscribeEvent
    public void onFogRender(EntityViewRenderEvent.FogDensity event) {
        RenderFogDensityEvent renderFogDensityEvent = new RenderFogDensityEvent(event.getRenderer(), event.getEntity(), event.getState(), event.getRenderPartialTicks());
        EventDispatcher.Companion.dispatch(renderFogDensityEvent);
        if (renderFogDensityEvent.isCancelled()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        EventDispatcher.Companion.dispatch(event);
    }

    @SubscribeEvent
    public void onPostBackgroundTooltipRender(RenderTooltipEvent.PostBackground event) {
        RenderTooltipBackgroundEvent renderTooltipBackgroundEvent = new RenderTooltipBackgroundEvent(event.getStack(), event.getX(), event.getY());
        EventDispatcher.Companion.dispatch(renderTooltipBackgroundEvent);
    }

    @SubscribeEvent
    public void onPostDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        DrawGuiScreenEvent drawGuiScreenEvent = DrawGuiScreenEvent.get(event.getGui());
        EventDispatcher.Companion.dispatch(drawGuiScreenEvent);
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        EventDispatcher.Companion.dispatch(event);
    }

    @SubscribeEvent
    public void onLivingEntityUseItemEvent(LivingEntityUseItemEvent event) {
        if (event.getEntity() instanceof EntityPlayerSP) {
            EventDispatcher.Companion.dispatch(new PlayerUseItemEvent());
        }
    }

    @SubscribeEvent
    public void onWorldEvent(WorldEvent event) {
        EventDispatcher.Companion.dispatch(event);
    }
}
