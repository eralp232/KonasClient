package me.darki.konas.mixin.mixins;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(GuiNewChat.class)
public interface IGuiNewChat {

    @Accessor(value = "drawnChatLines")
    List<ChatLine> getDrawnChatLines();

    @Accessor(value = "drawnChatLines")
    void setDrawnChatLines(List<ChatLine> drawnChatLines);

    @Accessor(value = "scrollPos")
    int getScrollPos();

    @Accessor(value = "scrollPos")
    void setScrollPos(int scrollPos);

    @Accessor(value = "isScrolled")
    boolean getIsScrolled();

    @Accessor(value = "isScrolled")
    void setIsScrolled(boolean isScrolled);


}
