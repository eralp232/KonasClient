package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.ItemTransformEvent;
import me.darki.konas.event.events.TransformEatFirstPersonEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.client.event.EntityViewRenderEvent;

public class ViewModel extends Module {

    //private static Setting<Float> mainHandHeight = new Setting<>("MainHandHeight", 100f, 100f, 0f, 1f);
    //private static Setting<Float> offHandHeight = new Setting<>("OffHand", 100f, 100f, 0f, 1f);

    private static Setting<Boolean> cancelEating = new Setting<>("CancelEating", false);
    private static Setting<Boolean> snapEat = new Setting<>("SnapEat", false).withVisibility(() -> !cancelEating.getValue());
    private static Setting<Boolean> customEating = new Setting<>("CustomEating", false).withVisibility(() -> !cancelEating.getValue());

    private static Setting<Float> mainHandX = new Setting<>("MainHandX", 0f, 100f, -100f, 1f);
    private static Setting<Float> mainHandY = new Setting<>("MainHandY", 0f, 100f, -100f, 1f);
    private static Setting<Float> mainHandZ = new Setting<>("MainHandZ", 0f, 100f, -100f, 1f);
    private static Setting<Float> mainHandXS = new Setting<>("MainHandXS", 0f, 100f, -100f, 1f);
    private static Setting<Float> mainHandYS = new Setting<>("MainHandYS", 0f, 100f, -100f, 1f);
    private static Setting<Float> mainHandZS = new Setting<>("MainHandZS", 0f, 100f, -100f, 1f);
    private static Setting<Float> mainHandYaw = new Setting<>("MainHandYaw", 0f, 180f, -180f, 1f);
    private static Setting<Float> mainHandPitch = new Setting<>("MainHandPitch", 0f, 180f, -180f, 1f);
    private static Setting<Float> mainHandRoll = new Setting<>("MainHandRoll", 0f, 180f, -180f, 1f);

    private static Setting<Float> offHandX = new Setting<>("OffHandX", 0f, 100f, -100f, 1f);
    private static Setting<Float> offHandY = new Setting<>("OffHandY", 0f, 100f, -100f, 1f);
    private static Setting<Float> offHandZ = new Setting<>("OffHandZ", 0f, 100f, -100f, 1f);
    private static Setting<Float> offHandXS = new Setting<>("OffHandXS", 0f, 100f, -100f, 1f);
    private static Setting<Float> offHandYS = new Setting<>("OffHandYS", 0f, 100f, -100f, 1f);
    private static Setting<Float> offHandZS = new Setting<>("OffHandZS", 0f, 100f, -100f, 1f);
    private static Setting<Float> offHandYaw = new Setting<>("OffHandYaw", 0f, 180f, -180f, 1f);
    private static Setting<Float> offHandPitch = new Setting<>("OffHandPitch", 0f, 180f, -180f, 1f);
    private static Setting<Float> offHandRoll = new Setting<>("OffHandRoll", 0f, 180f, -180f, 1f);

    private static Setting<Boolean> itemFov = new Setting<>("ItemFov", false);
    private static Setting<Float> itemFovValue = new Setting<>("ItemFovValue", 110f, 170f, 90f, 1f);

    public ViewModel() {
        super("ViewModel", "Lowers your hands", Category.RENDER, "SmallShield");
    }

    @Subscriber
    public void onTransformEatFirstPerson(TransformEatFirstPersonEvent event) {
        if (cancelEating.getValue()) {
            event.cancel();
        }

        if (customEating.getValue()) {
            float f = (float) mc.player.getItemInUseCount() - event.getP_187454_1_() + 1.0F;
            float f1 = f / (float) event.getStack().getMaxItemUseDuration();

            GlStateManager.translate(0.0F, 0.0F, f1 * (event.getHand() == EnumHandSide.RIGHT ? (-mainHandZ.getValue()) / 100 : (-offHandZ.getValue()) / 100));
        }
    }

    @Subscriber
    public void onItemFov(EntityViewRenderEvent.FOVModifier event) {
        if(itemFov.getValue()) {
            event.setFOV(itemFovValue.getValue());
        }
    }

    @Subscriber
    public void itemTransform(ItemTransformEvent event) {
        if (mc.player == null) return;

        event.cancel();

        boolean renderMain = true;
        boolean renderOff = true;

        if (snapEat.getValue() && (mc.player.getActiveItemStack().getItem() instanceof ItemFood || mc.player.getActiveItemStack().getItem() instanceof ItemPotion)) {
            if (mc.player.getActiveHand() == EnumHand.MAIN_HAND) {
                renderMain = false;
            } else if (mc.player.getActiveHand() == EnumHand.OFF_HAND) {
                renderOff = false;
            }
        }

        if(event.getType() == ItemTransformEvent.Type.MAINHAND) {
            if (renderMain) {
                event.setX(event.getX() + mainHandX.getValue() / 100);
                event.setY(event.getY() + mainHandY.getValue() / 100);
                event.setZ(event.getZ() + mainHandZ.getValue() / 100);
            }
            event.setScaleX(event.getScaleX() + mainHandXS.getValue() / 50);
            event.setScaleY(event.getScaleY() + mainHandYS.getValue() / 50);
            event.setScaleZ(event.getScaleZ() + mainHandZS.getValue() / 50);
            event.setYaw(mainHandYaw.getValue());
            event.setPitch(mainHandPitch.getValue());
            event.setRoll(mainHandRoll.getValue());
        } else if(event.getType() == ItemTransformEvent.Type.OFFHAND) {
            if (renderOff) {
                event.setX(event.getX() + offHandX.getValue() / 100);
                event.setY(event.getY() + offHandY.getValue() / 100);
                event.setZ(event.getZ() + offHandZ.getValue() / 100);
            }
            event.setScaleX(event.getScaleX() + offHandXS.getValue() / 50);
            event.setScaleY(event.getScaleY() + offHandYS.getValue() / 50);
            event.setScaleZ(event.getScaleZ() + offHandZS.getValue() / 50);
            event.setYaw(offHandYaw.getValue());
            event.setPitch(offHandPitch.getValue());
            event.setRoll(offHandRoll.getValue());
        }
    }

}