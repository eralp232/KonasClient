package me.darki.konas.command.commands;

import com.google.common.io.Files;
import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.config.Config;
import me.darki.konas.module.modules.render.Breadcrumbs;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.ChatUtil;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BreadcrumsCommand extends Command {
    public BreadcrumsCommand() {
        super("Breadcrums", "Load and save breadcrums", new SyntaxChunk("<save/load/list/clear>"), new SyntaxChunk("<name>"));
    }

    public static final File BREADCRUMS = new File(Config.KONAS_FOLDER, "breadcrums");

    @Override
    public void onFire(String[] args) {
        if (args.length < 2) {
            Logger.sendChatErrorMessage(getChunksAsString());
            return;
        }

        if (!BREADCRUMS.exists()) BREADCRUMS.mkdir();

        switch (args[1].toLowerCase()) {
            case "s":
            case "save": {
                if (args.length < 3) {
                    Logger.sendChatErrorMessage(getChunksAsString());
                    return;
                }
                try {
                    FileWriter csvWriter = new FileWriter(BREADCRUMS + File.separator + args[2] + ".csv");
                    for (Vec3d vertex : Breadcrumbs.vertices) {
                        csvWriter.append(vertex.x + "," + vertex.y + "," + vertex.z);
                        csvWriter.append("\n");
                    }
                    csvWriter.flush();
                    csvWriter.close();
                } catch (IOException e) {
                    Logger.sendChatErrorMessage("Error while saving!");
                }
                break;
            }
            case "load": {
                File csvFile = new File(BREADCRUMS + File.separator + args[2] + ".csv");
                if (csvFile.isFile()) {
                    try {
                        BufferedReader csvReader = new BufferedReader(new FileReader(BREADCRUMS + File.separator + args[2] + ".csv"));
                        Breadcrumbs.vertices.clear();
                        String row;
                        while ((row = csvReader.readLine()) != null) {
                            String[] data = row.split(",");
                            Vec3d vertex = new Vec3d(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]));
                            Breadcrumbs.vertices.add(vertex);
                        }
                        csvReader.close();
                        Breadcrumbs.onlyRender.setValue(true);
                    } catch (Exception e) {
                        Logger.sendChatErrorMessage("Error while loading, please ensure your file is not corrupted!");
                    }
                } else {
                    Logger.sendChatErrorMessage("Invalid filename!");
                }
            }
            case "list": {
                ChatUtil.info("(h)Saved Breadcrums:");
                if (BREADCRUMS.listFiles() != null) {
                    List<File> files = Arrays.stream(BREADCRUMS.listFiles()).filter(f -> f.getName().endsWith(".csv")).collect(Collectors.toList());
                    files.forEach(file -> {
                        ChatUtil.info("%s", Files.getNameWithoutExtension(file.getName()));
                    });
                }
                break;
            }
            case "clear": {
                Breadcrumbs.vertices.clear();
                break;
            }
            default: {
                Logger.sendChatErrorMessage(getChunksAsString());
                return;
            }
        }
    }
}
