package me.darki.konas.command.commands;

import io.netty.buffer.Unpooled;
import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.config.Config;
import me.darki.konas.util.client.ChatUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.EnumHand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BookCommand extends Command {

    private static final File BOOK_DIR = new File(Config.KONAS_FOLDER, "books");

    public BookCommand() {
        super("book", "Lets you sign a book with the text in a file", new SyntaxChunk("<file>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            ChatUtil.error(getChunksAsString());
            return;
        }

        if (!BOOK_DIR.exists()) BOOK_DIR.mkdir();

        File file = new File(BOOK_DIR, args[1].replaceAll(".txt", "") + ".txt");

        if (file.exists()) {
            String fileText = "";

            try {
                final BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    fileText = fileText.concat(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mc.player.getHeldItemMainhand().getItem() == Items.WRITABLE_BOOK) {
                final ItemStack writableBook = mc.player.getHeldItemMainhand();

                mc.player.openBook(writableBook, EnumHand.MAIN_HAND);

                final NBTTagList pages = new NBTTagList();

                for (String s : splitStringEvery(fileText, 254)) {
                    pages.appendTag(new NBTTagString(s));
                }

                if (writableBook.hasTagCompound()) writableBook.getTagCompound().setTag("pages", pages);
                else writableBook.setTagInfo("pages", pages);

                writableBook.setTagInfo("author", new NBTTagString(mc.player.getName()));
                writableBook.setTagInfo("title", new NBTTagString(args[1].trim()));

                final PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
                packetbuffer.writeItemStack(writableBook);
                mc.getConnection().sendPacket(new CPacketCustomPayload("MC|BSign", packetbuffer));
                mc.displayGuiScreen(null);

                ChatUtil.info("Signed book with name (h)%s(r)!",  args[1]);
            } else {
                ChatUtil.error("You need to be holding a book and quill.");
            }
        } else {
            ChatUtil.error("File (h)%s(r) does not exist.", args[1]);
        }
        
    }

    public String[] splitStringEvery(String s, int interval) {
        String[] result = new String[(int) Math.ceil(((s.length() / (double)interval)))];

        int j = 0;
        final int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = s.substring(j, j + interval);
            j += interval;
        }
        result[lastIndex] = s.substring(j);

        return result;
    }

}
