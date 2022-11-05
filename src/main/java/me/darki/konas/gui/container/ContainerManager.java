package me.darki.konas.gui.container;

import cookiedragon.eventsystem.EventDispatcher;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.KeyEvent;
import me.darki.konas.event.events.Render2DEvent;
import me.darki.konas.gui.container.containers.*;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class ContainerManager {

    private final ArrayList<Container> containers = new ArrayList<>();

    public void init() {

        EventDispatcher.Companion.register(this);
        EventDispatcher.Companion.subscribe(this);

        // containers.add(new ContainerPenis());

        containers.add(new ArraylistContainer());
        containers.add(new PlayerContainer());
        containers.add(new TabGuiContainer());
        containers.add(new InventoryContainer());
        containers.add(new WatermarkContainer());
        containers.add(new CoordinatesContainer());
        containers.add(new TPSContainer());
        containers.add(new SpeedometerContainer());
        containers.add(new PingContainer());
        containers.add(new FPSContainer());
        containers.add(new PitchContainer());
        containers.add(new MobRadarContainer());
        containers.add(new RadarContainer());
        containers.add(new ServerBrandContainer());

    }

    @Subscriber
    public void onRender(Render2DEvent event) {
        for(Container container : getContainers()) {
            if(container.isVisible()) {
                container.onRender();
            }
        }
    }

    @Subscriber
    public void onKeyEvent(KeyEvent event) {
        for(Container container : getContainers()) {
            if(container.isVisible()) {
                container.onKeyTyped(event.getKey());
            }
        }
    }

    public ArrayList<Container> getContainers() {
        return containers;
    }

    public static ArrayList<Setting> getSettingList(Container inputContainer) {
        Container container = (Container) inputContainer.getClass().getSuperclass().cast(inputContainer);
        ArrayList<Setting> settingList = new ArrayList<>();
        for (Field field : container.getClass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    if(ListenableSettingDecorator.class.isAssignableFrom(field.getType())) {
                        settingList.add((ListenableSettingDecorator) field.get(container));
                    } else {
                        settingList.add((Setting) field.get(container));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        for(Field field : container.getClass().getSuperclass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    settingList.add((Setting) field.get(container));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return settingList;
    }

}
