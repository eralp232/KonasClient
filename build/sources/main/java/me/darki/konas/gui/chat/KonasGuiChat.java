package me.darki.konas.gui.chat;

import me.darki.konas.command.Command;
import me.darki.konas.command.CommandManager;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.mixin.mixins.IGuiTextField;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;

public class KonasGuiChat extends GuiChat {

    private boolean drawOutline = true;

    public KonasGuiChat(String defaultText) {
        super(defaultText);
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {

        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
        } else if (keyCode != Keyboard.KEY_RETURN && keyCode != Keyboard.KEY_NUMPADENTER) {
            if (keyCode == Keyboard.KEY_UP) {
                this.getSentHistory(-1);
            } else if (keyCode == Keyboard.KEY_DOWN) {
                this.getSentHistory(1);
            } else if (keyCode == Keyboard.KEY_PRIOR) {
                this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1);
            } else if (keyCode == Keyboard.KEY_NEXT) {
                this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
            } else if (keyCode == Keyboard.KEY_TAB) {
                if (this.inputField.getText().length() > 1) {
                    String[] args = this.inputField.getText().replaceAll("([\\s])\\1+", "$1").split(" ");
                    System.out.println(args.length);
                    if (args.length > 1) {
                        Command cmd = CommandManager.getCommandByName(args[0]);
                        if (cmd != null && args.length - 2 <= cmd.getChunks().size() - 1) {
                            SyntaxChunk chunk = cmd.getChunks().get(args.length - 2);
                            if (chunk != null) {
                                String latestArg = args[args.length - 1];
                                String completedArg = chunk.predict(latestArg);
                                String text = inputField.getText();
                                text = text.substring(0, text.length() - latestArg.length());
                                text = text.concat(completedArg);
                                this.inputField.setText(text);
                            }
                        }
                    } else if (args.length == 1) {
                        for (Command cmd : CommandManager.getCommands()) {
                            String completion = cmd.complete(args[0].substring(1).toLowerCase());
                            if (completion != null) {
                                this.inputField.setText(Command.getPrefix() + completion);
                                break;
                            }
                        }
                    }
                }
            } else {
                this.inputField.textboxKeyTyped(typedChar, keyCode);
            }
        } else {
            String s = this.inputField.getText().trim();

            if (!s.isEmpty()) {
                this.sendChatMessage(s);
                mc.ingameGUI.getChatGUI().addToSentMessages(s);
            }

            this.mc.displayGuiScreen(null);
        }

        drawOutline = inputField.getText().replaceAll(" ", "").startsWith(Command.getPrefix());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);

        int tx = ((IGuiTextField)this.inputField).getFontRenderer().getStringWidth(this.inputField.getText() + "")+4;
        int ty = this.inputField.getEnableBackgroundDrawing() ? this.inputField.y + (this.inputField.height - 8) / 2 : this.inputField.y;
        ((IGuiTextField)this.inputField).getFontRenderer().drawStringWithShadow(calculateTooltip(this.inputField.getText()), tx, ty, 0x606060);

        this.inputField.drawTextBox();

        if (!drawOutline) return;

        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean texture2DEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glColor3f(255 / 255f, 85 / 255f, 255 / 255f);
        GL11.glLineWidth(2F);
        GL11.glBegin(GL11.GL_LINES);
        {
            int x = this.inputField.x - 2;
            int y = this.inputField.y - 2;
            int width = this.inputField.width;
            int height = this.inputField.height;

            // Upper Left Corner
            GL11.glVertex2d(x, y);
            // Upper Right Corner
            GL11.glVertex2d(x + width, y);
            // Upper Right Corner
            GL11.glVertex2d(x + width, y);
            // Lower Right Corner
            GL11.glVertex2d(x + width, y + height);
            // Lower Right Corner
            GL11.glVertex2d(x + width, y + height);
            // Lower Left Corner
            GL11.glVertex2d(x, y + height);
            // Lower Left Corner
            GL11.glVertex2d(x, y + height);
            // Upper Left Corner
            GL11.glVertex2d(x, y);

        }

        GL11.glEnd();

        if (blendEnabled) {
            GL11.glEnable(GL11.GL_BLEND);
        }

        if (texture2DEnabled) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

    }

    private String calculateTooltip(String currentText) {
        if (this.inputField.getText().length() < 1) return "";
        if (!currentText.startsWith(Command.getPrefix())) return "";
        String[] args = this.inputField.getText().split(" ");
        if (args.length > 1 || (this.inputField.getText().length() > 2 && this.inputField.getText().endsWith(" "))) {
            Command cmd = CommandManager.getCommandByName(args[0]);
            if (cmd != null) {
                ArrayList<SyntaxChunk> chunks = cmd.getChunks();
                StringBuilder str = new StringBuilder();
                int i = 0;
                for (SyntaxChunk chunk : chunks) {
                    if (i == args.length-2) {
                        String text = chunk.predict(args[i+1]);
                        int len = args[i+1].length();
                        try {
                            text = text.substring(Math.max(len, 0));
                        } catch (StringIndexOutOfBoundsException exception) {
                            exception.printStackTrace();
                        }
                        str.append(text);
                    } else if (i >= args.length-1) {
                        str.append(" " + chunk.getName());
                    }
                    i++;
                }
                return str.toString();
            }
        } else if (args.length == 1) {
            for (Command cmd : CommandManager.getCommands()) {
                if (cmd.getName().toLowerCase().startsWith(args[0].substring(1).toLowerCase())) {
                    String text = cmd.getName();
                    text = text.substring(args[0].substring(1).length());
                    return text;
                }
            }
        }
        return "";
    }
}
