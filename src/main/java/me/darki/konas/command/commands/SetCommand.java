package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.command.chunks.ModuleChunk;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;

public class SetCommand extends Command {

    private Object value;

    public SetCommand() {
        super("set", "Change the values of settings", new ModuleChunk("<module>"), new SyntaxChunk("<setting>"), new SyntaxChunk("<value>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length < getChunks().size() + 1) {
            Logger.sendChatMessage(getChunksAsString());
            return;
        }

        Module module = parseModule(args[1]);

        if (module == null) {
            Logger.sendChatErrorMessage("Module not found!");
            return;
        }

        Setting setting = parseSetting(module, args[2]);

        if (setting == null) {
            Logger.sendChatErrorMessage("Setting not found!");
            return;
        }

        if (setting.getValue() instanceof Integer) {
            parseIntValue(setting, args[3]);
        }

        if (setting.getValue() instanceof Long) {
            parseLongValue(setting, args[3]);
        }

        if (setting.getValue() instanceof Float) {
            parseFloatValue(setting, args[3]);
        }

        if (setting.getValue() instanceof Double) {
            parseDoubleValue(setting, args[3]);
        }

        if (setting.getValue() instanceof Boolean) {
            parseBooleanValue(setting, args[3]);
        }

        if (setting.getValue() instanceof Enum) {
            parseEnumValue(setting, args[3]);
        }

        if (setting.getValue() instanceof ColorSetting) {
            if(args[3].equalsIgnoreCase("offset")) {
                if(args.length < 5) {
                    Logger.sendChatErrorMessage("Please provide an offset value!");
                    return;
                }
                parseOffsetColorValue(setting, args[4]);
            } else {
                parseColorValue(setting, args[3]);
            }
        }

        if (value != null) {
            if (value instanceof String) {
                setting.setEnumValue((String) value);
            } else {
                setting.setValue(value);
            }
            String name = setting.getName();
            if (value instanceof ColorSetting) {
                if(args[3].equalsIgnoreCase("offset")) {
                    value = ((ColorSetting) value).getGlobalOffset();
                    name = "Offset";
                } else {
                    value = ((ColorSetting) value).getColor();
                }
            }
            Logger.sendChatMessage("Set " + name + " to " + value);
            value = null;
        } else {
            Logger.sendChatErrorMessage("Value is null!");
        }
    }

    private Module parseModule(String input) {
        if (ModuleManager.getModuleByName(input) != null) {
            return ModuleManager.getModuleByName(input);
        } else {
            return null;
        }
    }

    private Setting parseSetting(Module module, String input) {
        if (ModuleManager.getSettingByNameAndModuleName(module.getName(), input) != null) {
            return ModuleManager.getSettingByNameAndModuleName(module.getName(), input);
        } else {
            Logger.sendChatErrorMessage("Setting not found");
            return null;
        }
    }

    private void parseFloatValue(Setting<Float> setting, String input) {
        try {
            if (Float.parseFloat(input) >= setting.getMin() && Float.parseFloat(input) <= setting.getMax()) {
                value = Float.parseFloat(input);
                System.out.println("Value set to " + value);
            } else if (Float.parseFloat(input) < setting.getMin()) {
                Logger.sendChatErrorMessage("The minimum amount is " + setting.getMin());
            } else if (Float.parseFloat(input) > setting.getMax()) {
                Logger.sendChatErrorMessage("The maximum amount is " + setting.getMax());
            }
        } catch (Exception e) {
            Logger.sendChatErrorMessage("Please provide a Float as the value!");
        }
    }

    private void parseIntValue(Setting<Integer> setting, String input) {
        try {
            if (Integer.parseInt(input) >= setting.getMin() && Integer.parseInt(input) <= setting.getMax()) {
                value = Integer.parseInt(input);
            } else if (Integer.parseInt(input) < setting.getMin()) {
                Logger.sendChatErrorMessage("The minimum amount is " + setting.getMin());
            } else if (Integer.parseInt(input) > setting.getMax()) {
                Logger.sendChatErrorMessage("The maximum amount is " + setting.getMax());
            }
        } catch (Exception e) {
            Logger.sendChatErrorMessage("Please provide an Integer as the value!");
        }
    }

    private void parseLongValue(Setting<Long> setting, String input) {
        try {
            if (Long.parseLong(input) >= setting.getMin() && Long.parseLong(input) <= setting.getMax()) {
                value = Long.parseLong(input);
            } else if (Long.parseLong(input) < setting.getMin()) {
                Logger.sendChatErrorMessage("The minimum amount is " + setting.getMin());
            } else if (Long.parseLong(input) > setting.getMax()) {
                Logger.sendChatErrorMessage("The maximum amount is " + setting.getMax());
            }
        } catch (Exception e) {
            Logger.sendChatErrorMessage("Please provide a Long as the value!");
        }
    }

    private void parseDoubleValue(Setting<Double> setting, String input) {
        try {
            if (Double.parseDouble(input) >= setting.getMin() && Double.parseDouble(input) <= setting.getMax()) {
                value = Double.parseDouble(input);
            } else if (Double.parseDouble(input) < setting.getMin()) {
                Logger.sendChatErrorMessage("The minimum amount is " + setting.getMin());
            } else if (Double.parseDouble(input) > setting.getMax()) {
                Logger.sendChatErrorMessage("The maximum amount is " + setting.getMax());
            }
        } catch (Exception e) {
            Logger.sendChatErrorMessage("Please provide a Double as the value!");
        }
    }

    private void parseBooleanValue(Setting<Boolean> setting, String input) {
        try {
            if (input.equalsIgnoreCase("toggle")) {
                value = !setting.getValue();
            } else {
                value = Boolean.parseBoolean(input);
            }
        } catch (Exception e) {
            Logger.sendChatErrorMessage("Please provide a Boolean as the value!");
        }
    }

    private void parseColorValue(Setting<ColorSetting> setting, String input) {
        try {
            value = new ColorSetting(ColorSetting.parseColor(input), setting.getValue().isCycle());
        } catch (Exception e) {
            Logger.sendChatErrorMessage("Please provide an integer as the value!");
        }
    }

    private void parseEnumValue(Setting<Enum> setting, String input) {
        if (setting.getEnum(input) != -1) {
            try {
                value = setting.getValue().getClass().getEnumConstants()[setting.getEnum(input)].name();
                System.out.println(value);
            } catch (Exception exception) {
                final Enum e = setting.getValue().getClass().getEnumConstants()[setting.getEnum(input)];
                Logger.sendChatMessage("Something went wrong lol");
                Logger.sendChatMessage(e.name());
                Logger.sendChatMessage(("" + setting.getEnum(input)));
            }
        } else {
            Logger.sendChatMessage("Please provide a String as the value!");
        }
    }

    private void parseOffsetColorValue(Setting<ColorSetting> setting, String offset) {
        try {
            value = new ColorSetting(setting.getValue().getColor(), setting.getValue().isCycle(), Integer.parseInt(offset));
        } catch (Exception e) {
            Logger.sendChatErrorMessage("Please provide an integer as the offset!");
        }
    }

}
