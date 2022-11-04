package me.darki.konas.module.modules.render;

import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import org.lwjgl.util.glu.Project;

public class Viewport extends Module {
    private static final Setting<Boolean> hands = new Setting<>("Hands", true);
    private static final Setting<Boolean> fov = new Setting<>("FOV", true);
    private static final Setting<Integer> angle = new Setting<>("Angle", 90, 180, 10, 5).withVisibility(fov::getValue);

    private static final Setting<Boolean> aspect = new Setting<>("Aspect", true);
    private static final Setting<Float> ratio = new Setting<>("Ratio", 1.77f, 2.5F, 0.75F, 0.1f).withVisibility(aspect::getValue);

    private static Viewport INSTANCE;

    public Viewport() {
        super("Viewport", "Modify your viewport", Category.RENDER);
        INSTANCE = this;
    }

    public void onEnable() {
        if (mc.player != null && mc.player.getName().equalsIgnoreCase("johnmcswag")) {
            toggle();
            Logger.sendChatErrorMessage("Johnmcswag can not use this module!");
            for (Module module : ModuleManager.getEnabledModules()) {
                module.toggle();
            }
        }
    }

    public static void project(float oldFovY, float oldAspectRatio, float oldZNear, float oldZFar) {
        project(oldFovY, oldAspectRatio, oldZNear, oldZFar, false);
    }

    public static void project(float oldFovY, float oldAspectRatio, float oldZNear, float oldZFar, boolean fromHands) {
        if (INSTANCE.isEnabled() && (!fromHands || hands.getValue())) {
            Project.gluPerspective(fov.getValue() ? angle.getValue() : oldFovY, aspect.getValue() ? ratio.getValue() : oldAspectRatio, oldZNear, oldZFar);
        } else {
            Project.gluPerspective(oldFovY, oldAspectRatio, oldZNear, oldZFar);
        }
    }
}
