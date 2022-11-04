package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.SprintEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.util.MovementInput;
import org.lwjgl.input.Keyboard;

public class Sprint extends Module {

    private enum Mode {
        LEGIT, RAGE
    }

    private static Setting<Mode> mode = new Setting<>("Mode", Mode.LEGIT);

    private boolean shouldSprint = false;

    public Sprint() {
        super("Sprint", "Makes you Sprint!", Keyboard.KEY_NONE, Category.MOVEMENT);
    }

    @Subscriber
    public void onMoveInputSprint(SprintEvent event) {
        MovementInput movementInput = event.getInput();
        switch (mode.getValue()) {
            case LEGIT:
                shouldSprint = movementInput.moveForward > 0;
                break;
            case RAGE:
                shouldSprint = Math.abs(movementInput.moveForward) > 0 || Math.abs(movementInput.moveStrafe) > 0;
                break;
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isSneaking() && !mc.player.collidedHorizontally) {
            mc.player.setSprinting(shouldSprint);
        }
    }


}
