package me.darki.konas.setting;

import org.jetbrains.annotations.NotNull;

/**
 * This is a one way ticket, you can only send changes made by the user when clicking the gui.
 * This code is not able to bind the value in a duplex fashion (change setting and gui state from outside).
 * (Like it would be useful for baritone ;[)
 * @param <T>
 */
public class ListenableSettingDecorator<T> extends Setting<T> {

    IRunnable<T> run = null;

    boolean cancelled = false;

    public ListenableSettingDecorator(String name, T value, @NotNull IRunnable<T> run) {
        super(name, value);
        this.run = run;
    }

    public ListenableSettingDecorator(String name, T value, T max, T min, T steps, @NotNull IRunnable<T> run) {
        super(name, value, max, min, steps);
        this.run = run;
    }

    @Override
    public void setValue(T value) {
        T cache = getValue();
        super.setValue(value);
        run.run(value);
        if(cancelled) {
            super.setValue(cache);
            cancelled = false;
        }
    }

    @Override
    public void setEnumValue(String value) {
        super.setEnumValue(value);
        if(getEnumByString(value) != null) {
            run.run((T) getEnumByString(value));
        }
    }

    public void cancel() {
        cancelled = true;
    }

}
