package me.darki.konas.macro;

import java.util.concurrent.CopyOnWriteArrayList;

public class MacroManager {

    private static CopyOnWriteArrayList<Macro> macros = new CopyOnWriteArrayList<>();

    public static void addMacro(Macro macro) {
        if (!macros.contains(macro)) {
            macros.add(macro);
        }
    }

    public static void removeMacro(Macro macro) {
        macros.remove(macro);
    }

    public static void clearMacros() {
        macros.clear();
    }

    public static CopyOnWriteArrayList<Macro> getMacros() {
        return macros;
    }

    public static void setMacros(CopyOnWriteArrayList<Macro> macros) {
        MacroManager.macros = macros;
    }

    public static Macro getMacroByName(String name) {
        for (Macro macro : getMacros()) {
            if (macro.getName().equalsIgnoreCase(name)) {
                return macro;
            }
        }
        return null;
    }

}
