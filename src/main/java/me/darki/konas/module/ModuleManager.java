package me.darki.konas.module;

import com.google.common.reflect.ClassPath;
import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.ModuleInitialisationEvent;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.setting.Setting;
import net.minecraft.launchwrapper.Launch;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ModuleManager {

    private static ArrayList<Module> modules = new ArrayList<>();

    public static void init() {

        EventDispatcher.Companion.dispatch(new ModuleInitialisationEvent.Pre());

        //modules.add()

        try {
            for (ClassPath.ClassInfo classInfo : ClassPath.from(Launch.classLoader).getAllClasses()) {
                if(classInfo.getName().startsWith("me.darki.konas.module.modules")) {
                    Class clazz = classInfo.load();
                    if(!Modifier.isAbstract(clazz.getModifiers()) && Module.class.isAssignableFrom(clazz)) {
                        for (Constructor constructor : clazz.getConstructors()) {
                            if (constructor.getParameterCount() == 0) {
                                Module m = (Module) clazz.newInstance();
                                modules.add(m);
                            }
                        }
                    }
                }
            }
        } catch (IOException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        EventDispatcher.Companion.dispatch(new ModuleInitialisationEvent.Post());

    }

    public static void handleWorldJoin() {
        for (Module module : modules) {
        }
    }

    public static ArrayList<Module> getModules() {
        return modules;
    }

    public static Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
            for (String alias : module.getAliases()) {
                if (alias.toLowerCase().startsWith(name.toLowerCase())) {
                    return module;
                }
            }
        }
        return null;
    }

    public static Module getModuleByClass(Class<? extends Module> clazz) {
        for (Module module : modules) {
            if (module.getClass() == clazz) return module;
        }
        return null;
    }

    public static Setting getSettingByNameAndModuleName(String moduleName, String settingName) {
        if (ModuleManager.getModuleByName(moduleName) != null) {
            for (Setting setting : getSettingList(ModuleManager.getModuleByName(moduleName))) {
                if (setting.getName().equalsIgnoreCase(settingName)) {
                    return setting;
                }
            }
        }
        return null;
    }

    public static ArrayList<Module> getEnabledModules() {
        ArrayList<Module> enabledModules = new ArrayList<>();
        modules.forEach(module -> {
            if (module.isEnabled()) {
                enabledModules.add(module);
            }
        });

        return enabledModules;

    }

    public static ArrayList<Module> getEnabledVisibleModules() {
        return (ArrayList<Module>) modules.stream().filter(m -> m.isEnabled()).filter(m -> m.isVisible()).collect(Collectors.toList());
    }

    public static ArrayList<Module> getModulesByCategory(Module.Category category) {
        ArrayList<Module> modulesInCategory = new ArrayList<>();
        modules.forEach(module -> {
            if (module.getCategory() == category) {
                modulesInCategory.add(module);
            }
        });

        return modulesInCategory;

    }

    public static ArrayList<Setting> getSettingList(Module inputModule) {
        Module module = (Module) inputModule.getClass().getSuperclass().cast(inputModule);
        ArrayList<Setting> settingList = new ArrayList<>();
        for (Field field : module.getClass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    if(ListenableSettingDecorator.class.isAssignableFrom(field.getType())) {
                        settingList.add((ListenableSettingDecorator) field.get(module));
                    } else {
                        settingList.add((Setting) field.get(module));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        for(Field field : module.getClass().getSuperclass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    settingList.add((Setting) field.get(module));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return settingList;
    }
}
