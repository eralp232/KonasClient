package me.darki.konas.event.events;

import net.minecraft.util.MovementInput;

//TODO: This is a hacky workaround for the lack of event priority in cookies event system

public class SprintEvent {

    private final MovementInput input;

    public SprintEvent(MovementInput input) {
        this.input = input;
    }

    public MovementInput getInput() {
        return input;
    }

}
