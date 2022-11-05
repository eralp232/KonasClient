package me.darki.konas.command;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;

public abstract class Command {

    private String name;
    private String description;
    private String[] aliases;
    private ArrayList<SyntaxChunk> chunks = new ArrayList<>();

    public static String prefix = ".";

    public static char SECTIONSIGN = '\u00a7';

    public static final Minecraft mc = Minecraft.getMinecraft();


    public Command(String name, String description, SyntaxChunk... chunks) {
        this.name = name;
        this.description = description;
        this.aliases = new String[0];
        Collections.addAll(this.chunks, chunks);
    }

    public Command(String name, String description, String[] aliases, SyntaxChunk... chunks) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        Collections.addAll(this.chunks, chunks);
    }


    public void onFire(String[] args) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<SyntaxChunk> getChunks() {
        return this.chunks;
    }

    public String getChunksAsString() {
        StringBuilder str = new StringBuilder();
        chunks.forEach(syntaxChunk -> {
            str.append(syntaxChunk.getName() + " ");
        });
        return str.toString();
    }

    public static String getPrefix() {
        return prefix;
    }

    public static void setPrefix(String prefix) {
        Command.prefix = prefix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getAliases() {
        return aliases;
    }

    public void setAliases(String[] aliases) {
        this.aliases = aliases;
    }

    public boolean isNameOrAlias(String str) {
        if (name.equalsIgnoreCase(str)) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public String complete(String str) {
        if (name.toLowerCase().startsWith(str)) {
            return name;
        }
        for (String alias : aliases) {
            if (alias.toLowerCase().startsWith(str)) {
                return alias;
            }
        }
        return null;
    }
}
