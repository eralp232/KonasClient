package me.darki.konas.module.modules.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.command.Command;
import me.darki.konas.command.commands.FontCommand;
import me.darki.konas.event.events.Render2DEvent;
import me.darki.konas.event.events.RenderNameEvent;
import me.darki.konas.event.listener.TargetManager;
import me.darki.konas.mixin.mixins.IEntityRenderer;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.client.Waypoints;
import me.darki.konas.setting.*;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.render.font.CustomFontRenderer;
import me.darki.konas.util.render.font.DefaultFontRenderer;
import me.darki.konas.util.render.font.IFontRenderer;
import me.darki.konas.util.render.font.NametagFontRenderer;
import me.darki.konas.util.waypoint.Waypoint;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;

public class Nametags extends Module {
    public static final Setting<Parent> targeting = new Setting<>("Targets", new Parent(false));
    private static Setting<Boolean> animalSetting = new Setting<>("Animals", false).withParent(targeting);
    private static Setting<Boolean> mobSetting = new Setting<>("Mobs", false).withParent(targeting);
    private static Setting<Boolean> playerSetting = new Setting<>("Players", true).withParent(targeting);
    private static Setting<Boolean> waypointSetting = new Setting<>("Waypoints", true).withParent(targeting);
    private static Setting<Boolean> waypointCoords = new Setting<>("Coords", true).withParent(targeting).withVisibility(waypointSetting::getValue);
    private static Setting<Boolean> waypointDistance = new Setting<>("Dist", false).withParent(targeting).withVisibility(waypointSetting::getValue);

    public static final Setting<Parent> name = new Setting<>("Name", new Parent(false));
    private static Setting<Double> nameRange = new Setting<>("NameRange", 150D, 256D, 5D, 0.5D).withParent(name);
    private static Setting<Boolean> gamemode = new Setting<>("Gamemode", false).withParent(name);
    private static Setting<Boolean> ping = new Setting<>("Ping", false).withParent(name);
    private static Setting<Boolean> health = new Setting<>("Health", true).withParent(name);
    private static Setting<Boolean> pops = new Setting<>("Pops", false).withParent(name);
    private static Setting<Boolean> friends = new Setting<>("Friends", true).withParent(name);
    private static Setting<Boolean> fill = new Setting<>("Fill", true).withParent(name);
    private static Setting<Boolean> outline = new Setting<>("Outline", true).withParent(name);
    private static Setting<Float> lineWidth = new Setting<>("LineWidth", 1F, 10F, 0.1F, 0.1F).withParent(name).withVisibility(outline::getValue);

    public static final Setting<Parent> colors = new Setting<>("Colors", new Parent(false));
    private static Setting<ColorSetting> fillColorA = new Setting<>("FillColorA", new ColorSetting(0x80000000)).withParent(colors);
    private static Setting<ColorSetting> fillColorB = new Setting<>("FillColorB", new ColorSetting(0x80000000)).withParent(colors);
    private static Setting<ColorSetting> fillColorC = new Setting<>("FillColorC", new ColorSetting(0x80000000)).withParent(colors);
    private static Setting<ColorSetting> fillColorD = new Setting<>("FillColorD", new ColorSetting(0x80000000)).withParent(colors);
    private static Setting<ColorSetting> outlineColorA = new Setting<>("OutlineColorA", new ColorSetting(0xD0000000)).withParent(colors);
    private static Setting<ColorSetting> outlineColorB = new Setting<>("OutlineColorB", new ColorSetting(0xD0000000)).withParent(colors);
    private static Setting<ColorSetting> outlineColorC = new Setting<>("OutlineColorC", new ColorSetting(0xD0000000)).withParent(colors);
    private static Setting<ColorSetting> outlineColorD = new Setting<>("OutlineColorD", new ColorSetting(0xD0000000)).withParent(colors);

    public static final Setting<Parent> info = new Setting<>("Info", new Parent(false));
    private static Setting<Double> armorRange = new Setting<>("ArmorRange", 30D, 256D, 5D, 0.5D).withParent(info);
    private static Setting<Boolean> drawArmor = new Setting<>("Armor", true).withParent(info);
    private static Setting<Boolean> drawStackSize = new Setting<>("Stacks", true).withParent(info);
    private static Setting<Boolean> drawDurability = new Setting<>("Durability", true).withParent(info);
    private static Setting<Boolean> drawEnchants = new Setting<>("Enchants", true).withParent(info);

    public static final Setting<Parent> scaling = new Setting<>("Scaling", new Parent(false));
    private static Setting<Boolean> frustumCheck = new Setting<>("FrustumCheck", true).withParent(scaling);
    private static Setting<Double> zoomFactor = new Setting<>("ZoomFactor", 3D, 10D, 0D, 0.1D).withParent(scaling);
    private static Setting<Double> scaleSetting = new Setting<>("Scale", 1D, 5D, 0D, 0.1D).withParent(scaling);
    private static Setting<Double> scaleFactor = new Setting<>("ScaleFactor", 2D, 5D, 0D, 0.1D).withParent(scaling);
    private static Setting<Double> scaleLimit = new Setting<>("ScaleLimit", 3D, 10D, 0D, 0.1D).withParent(scaling);
    private static Setting<Integer> armorSpacing = new Setting<>("ArmorSpacing", 42, 70, 0, 1).withParent(scaling);
    private static Setting<Integer> enchantSpacing = new Setting<>("EnchantSpacing", 70, 100, -30, 5).withParent(scaling);
    private static Setting<Double> enchantScale = new Setting<>("EnchantScale", 1D, 2.5D, 0.5D, 0.05D).withParent(scaling);
    private static Setting<Double> yOffset = new Setting<>("YOffset", 0.2D, 1D, 0D, 0.05D).withParent(scaling);

    public static final Setting<Parent> misc = new Setting<>("Misc", new Parent(false));
    private static Setting<Boolean> selfNametags = new Setting<>("SelfNametag", false).withParent(misc);
    public static Setting<FontMode> customFont = new ListenableSettingDecorator<>("Font", FontMode.VANILLA, new IRunnable<FontMode>() {
        @Override
        public void run(FontMode value) {
            setFontRenderer(value);
        }
    });

    public static void setFontRenderer(FontMode value) {
        if (value == FontMode.HIGHRES) {
            NametagFontRenderer.setFontRenderer(highResFontRenderer);
        } else if (value == FontMode.CUSTOM) {
            NametagFontRenderer.setFontRenderer(customFontRenderer);
        } else {
            NametagFontRenderer.setFontRenderer(DefaultFontRenderer.INSTANCE);
        }
    }

    private final DecimalFormat df = new DecimalFormat("#.##");

    private static ICamera camera = new Frustum();

    public Nametags() {
        super("Nametags", Category.RENDER);
    }

    public enum FontMode {
        VANILLA, CUSTOM, HIGHRES
    }

    public static IFontRenderer customFontRenderer = new CustomFontRenderer(FontCommand.lastFont, 20);
    public static IFontRenderer highResFontRenderer = new CustomFontRenderer(FontCommand.lastFont, 60);

    @Subscriber
    public void onRenderName(RenderNameEvent event) {
        if (shouldRenderTag(event.getNameEntity())) {
            event.setCancelled(true);
        }
    }

    @Subscriber
    public void onRender2D(Render2DEvent event) {
        Vec3d camPos = new Vec3d(mc.getRenderViewEntity().lastTickPosX + (mc.getRenderViewEntity().posX - mc.getRenderViewEntity().lastTickPosX) * mc.getRenderPartialTicks(),
                mc.getRenderViewEntity().lastTickPosY + (mc.getRenderViewEntity().posY - mc.getRenderViewEntity().lastTickPosY) * mc.getRenderPartialTicks(),
                mc.getRenderViewEntity().lastTickPosZ + (mc.getRenderViewEntity().posZ - mc.getRenderViewEntity().lastTickPosZ) * mc.getRenderPartialTicks()).add(0, 2.0, 0);

        if (frustumCheck.getValue()) {
            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        }

        mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityLivingBase).filter(this::shouldRenderTag).filter(entity -> !frustumCheck.getValue() || camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox().grow(2))).forEach(entity -> {
            Vec3d pos = new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.getRenderPartialTicks(),
                    entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.getRenderPartialTicks(),
                    entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.getRenderPartialTicks()).add(0, entity.height + yOffset.getValue(), 0);

            Vec3d screenPos = KonasGlobals.INSTANCE.vectorUtils.toScaledScreenPos(pos);

            renderNametag(entity, screenPos, camPos, pos);
        });

        if (waypointSetting.getValue()) {
            Waypoints waypoints = (Waypoints) ModuleManager.getModuleByClass(Waypoints.class);

            if (waypoints != null) {
                if (waypoints.isEnabled()) {
                    for (Waypoint waypoint : KonasGlobals.INSTANCE.waypointManager.getWaypoints()) {
                        Vec3d pos = new Vec3d(waypoint.getX(), waypoint.getY(), waypoint.getZ()).add(0.5, 2.2, 0.5);

                        Vec3d screenPos = KonasGlobals.INSTANCE.vectorUtils.toScaledScreenPos(pos);

                        String name = waypoint.getName();

                        if (waypointCoords.getValue()) {
                            DecimalFormat df = new DecimalFormat("#.#");
                            double x = Double.parseDouble(df.format(waypoint.getX()));
                            double y = Double.parseDouble(df.format(waypoint.getY()));
                            double z = Double.parseDouble(df.format(waypoint.getZ()));
                            name = name + " " + x + ", " + y + ", " + z;
                        }

                        if (waypointDistance.getValue()) {
                            name = name + " " + (int) pos.distanceTo(camPos);
                        }

                        renderWaypointNametag(name, screenPos, camPos, pos);
                    }

                    for(Map.Entry<EntityPlayer, Long> entry : waypoints.getLoggedPlayers().entrySet()) {
                        EntityPlayer entity = entry.getKey();
                        if (entity != mc.player) {
                            double x = Double.parseDouble(df.format(entity.posX));
                            double y = Double.parseDouble(df.format(entity.posY));
                            double z = Double.parseDouble(df.format(entity.posZ));
                            Vec3d pos = new Vec3d(x, y, z).add(0, entity.height + yOffset.getValue(), 0);

                            Vec3d screenPos = KonasGlobals.INSTANCE.vectorUtils.toScaledScreenPos(pos);

                            String name = entity.getName();

                            if (waypointCoords.getValue()) {
                                name = name + " " + x + ", " + y + ", " + z;
                            }

                            if (waypointDistance.getValue()) {
                                name = name + " " + (int) pos.distanceTo(camPos);
                            }

                            renderWaypointNametag(name, screenPos, camPos, pos);
                        }
                    }
                }
            }
        }
    }

    public static float roundFloat(float value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    private void renderWaypointNametag(String name, Vec3d screenpos, Vec3d iSelfPos, Vec3d pos) {
        GL11.glPushMatrix();

        double dist = MathHelper.clamp(iSelfPos.distanceTo(pos), 0D, scaleLimit.getValue() * 10) * 0.2;

        dist = 1D / (dist * scaleFactor.getValue() + 1D);

        double scale = scaleSetting.getValue() * dist;

        if (customFont.getValue() != FontMode.HIGHRES) {
            scale *= 3;
        }

        GL11.glTranslated(screenpos.x, screenpos.y, 0);
        GL11.glScaled(scale, scale, 1);

        float nameWidth = NametagFontRenderer.getStringWidth(name);
        float nameHeight = NametagFontRenderer.getStringHeight(name);

        if (fill.getValue()) {
            quickDrawRect(-(nameWidth / 2) * 1.05F, -nameHeight, (nameWidth / 2) * 1.05F, nameHeight * 0.1F, fillColorA.getValue(), fillColorB.getValue(), fillColorC.getValue(), fillColorD.getValue(), false);
        }

        if (outline.getValue()) {
            GL11.glLineWidth(lineWidth.getValue());
            quickDrawRect(-(nameWidth / 2) * 1.05F, -nameHeight, (nameWidth / 2) * 1.05F, nameHeight * 0.1F, outlineColorA.getValue(), outlineColorB.getValue(), outlineColorC.getValue(), outlineColorD.getValue(), true);
        }

        NametagFontRenderer.drawString(name, -(nameWidth / 2), -nameHeight, -1);

        GL11.glPopMatrix();
    }

    private void renderNametag(Entity entity, Vec3d screenpos, Vec3d iSelfPos, Vec3d iPlayerPos) {
        GL11.glPushMatrix();

        double dist = MathHelper.clamp(iSelfPos.distanceTo(iPlayerPos), 0D, scaleLimit.getValue() * 10) * 0.2;

        dist = 1D / (dist * scaleFactor.getValue() + 1D);

        double scale = scaleSetting.getValue() * dist;

        if (ModuleManager.getModuleByClass(Zoom.class).isEnabled()) {
            scale *= (double) Zoom.zoom.getValue() * zoomFactor.getValue();
        }

        if (customFont.getValue() != FontMode.HIGHRES) {
            scale *= 3;
        }

        GL11.glTranslated(screenpos.x, screenpos.y, 0);
        GL11.glScaled(scale, scale, 1);

        String name = entity.getName();

        if (name.equalsIgnoreCase("antiflame")) {
            name = "god";
        }

        if (entity instanceof EntityPlayer) {
            if (gamemode.getValue()) {
                name = name + (((EntityPlayer) entity).isCreative() ? " [C]" : " [S]");
            }
            if (ping.getValue() && mc.getConnection() != null) {
                if (mc.getConnection().getPlayerInfo(entity.getUniqueID()) != null) {
                    int responseTime = mc.getConnection().getPlayerInfo(entity.getUniqueID()).getResponseTime();
                    name = name + " " + responseTime + "ms";
                }
            }
        }

        if (health.getValue() && entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            float playerHealth = roundFloat(living.getHealth() + living.getAbsorptionAmount(), 1);

            if (entity.getName().equalsIgnoreCase("antiflame")) {
                playerHealth = 420F;
            }

            String health;
            if (playerHealth < 5) {
                health = ChatFormatting.RED.toString() + playerHealth;
            } else if (playerHealth < 20) {
                health = ChatFormatting.YELLOW.toString() + playerHealth;
            } else {
                health = ChatFormatting.GREEN.toString() + playerHealth;
            }
            health = health.replace(".0", "");
            name = name + " " + health + ChatFormatting.RESET.toString();
        }

        if (pops.getValue()) {
            int pops = 0;
            if (TargetManager.popList.containsKey(entity.getName())) {
                pops = TargetManager.popList.get(entity.getName());
            }
            name = name + " " + pops;
        }

        float nameWidth = NametagFontRenderer.getStringWidth(name);
        float nameHeight = NametagFontRenderer.getStringHeight(name);

        if (fill.getValue()) {
            quickDrawRect(-(nameWidth / 2) * 1.05F, -nameHeight, (nameWidth / 2) * 1.05F, nameHeight * 0.1F, fillColorA.getValue(), fillColorB.getValue(), fillColorC.getValue(), fillColorD.getValue(), false);
        }

        if (outline.getValue()) {
            GL11.glLineWidth(lineWidth.getValue());
            quickDrawRect(-(nameWidth / 2) * 1.05F, -nameHeight, (nameWidth / 2) * 1.05F, nameHeight * 0.1F, outlineColorA.getValue(), outlineColorB.getValue(), outlineColorC.getValue(), outlineColorD.getValue(), true);
        }

        if (customFont.getValue() == FontMode.VANILLA) {
            nameHeight += 0.5F;
        }

        NametagFontRenderer.drawString((friends.getValue() ? Friends.isUUIDFriend(entity.getUniqueID().toString()) ? Command.SECTIONSIGN + "b" : "" : "") + name, -(nameWidth / 2), customFont.getValue() == FontMode.VANILLA ? -nameHeight + 2F : -nameHeight, -1);

        GL11.glColor4f(1f, 1f, 1f, 1f);

        if (customFont.getValue() != FontMode.HIGHRES) {
            GL11.glScaled(1D / scale, 1D / scale, 1);
            scale = scaleSetting.getValue() * dist;
            if (ModuleManager.getModuleByClass(Zoom.class).isEnabled()) {
                scale *= (double) Zoom.zoom.getValue() * zoomFactor.getValue();
            }
            GL11.glScaled(scale, scale, 1);
            nameHeight *= 3;
        }

        if (drawArmor.getValue() && entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;

            if (entity.getDistance(mc.player) <= armorRange.getValue()) {
                double xOffset = 0;

                for (ItemStack armourStack : entityPlayer.inventory.armorInventory) {
                    if (armourStack != null) {
                        xOffset -= armorSpacing.getValue() / 2D;
                    }
                }

                if (entityPlayer.getHeldItemMainhand() != null) {
                    xOffset -= armorSpacing.getValue();
                    ItemStack renderStack = entityPlayer.getHeldItemMainhand().copy(); // if we don't copy dosn't work, idk why
                    renderItem(entityPlayer, renderStack, xOffset, -(nameHeight + 55.0D));
                    xOffset += armorSpacing.getValue();
                }

                for (int index = 3; index >= 0; --index) {
                    ItemStack armourStack = entityPlayer.inventory.armorInventory.get(index);
                    if (armourStack != null) {
                        renderItem(entityPlayer, armourStack, xOffset, -(nameHeight + 55.0D));
                        xOffset += armorSpacing.getValue();
                    }
                }

                if (entityPlayer.getHeldItemOffhand() != null) {
                    ItemStack renderOffhand = entityPlayer.getHeldItemOffhand().copy(); // if we don't copy dosn't work, idk why
                    renderItem(entityPlayer, renderOffhand, xOffset, -(nameHeight + 55.0D));
                }
            }
        }

        GL11.glPopMatrix();
    }

    private void renderItem(EntityPlayer player, ItemStack stack, double x, double y) {
        GL11.glPushMatrix();
        GL11.glDepthMask(true);
        GlStateManager.clear(256);

        GlStateManager.disableDepth();
        GlStateManager.enableDepth();

        net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting(); // avoid conflicts with Konas's RenderHelper
        mc.getRenderItem().zLevel = -100.0F;
        GlStateManager.scale(3.0D, 3.0D, 0.01f);
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int) (x / 3.0D), (int) (y / 3.0D));
        renderItemOverlay(stack, x / 3.0D, y / 3.0D);
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.scale(1, 1, 1);
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GL11.glPopMatrix();
    }

    private void renderItemOverlay(ItemStack stack, double xPosition, double yPosition) {
        if (!stack.isEmpty()) {
            if (stack.getCount() != 1 && drawStackSize.getValue()) {
                String s = String.valueOf(stack.getCount());
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                GlStateManager.translate(xPosition + 15D, yPosition + 13D,0);
                double sf = 0.13D;
                if (customFont.getValue() != FontMode.HIGHRES) {
                    sf *= 3;
                }
                GlStateManager.scale(sf, sf, 1);
                NametagFontRenderer.drawString(s, -NametagFontRenderer.getStringWidth(s), 0, -1);
                GlStateManager.scale(1D / sf, 1D / sf, 1);
                GlStateManager.translate(-(xPosition + 15D), -(yPosition + 13D),0);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                GlStateManager.enableBlend();
            }

            if (stack.getItem().showDurabilityBar(stack) && drawDurability.getValue()) {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                double health = stack.getItem().getDurabilityForDisplay(stack);
                int rgbfordisplay = stack.getItem().getRGBDurabilityForDisplay(stack);
                GlStateManager.translate(xPosition + 3.5D, yPosition + 15D,0);
                GlStateManager.scale(0.75D, 0.75D, 1);
                quickDrawRect(0, 0, 12, 2, 0xFF000000, false);
                quickDrawRect(0, 0, Math.round(12.0F - (float)health * 12.0F), 1, rgbfordisplay | (255 << 24), false);
                GlStateManager.scale(1D / 0.75D, 1D / 0.75D, 1);
                GlStateManager.translate(-(xPosition + 3.5D), -(yPosition + 15D),0);
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }

            if (drawEnchants.getValue()) {
                float yOffset = enchantSpacing.getValue();

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                GlStateManager.translate(xPosition + 5D, yPosition + 10D, 0);
                double sf = 0.13D * enchantScale.getValue();
                if (customFont.getValue() != FontMode.HIGHRES) {
                    sf *= 3;
                    yOffset /= 3;
                }
                GlStateManager.scale(sf, sf, 1);
                NBTTagList enchants = stack.getEnchantmentTagList();
                for (int index = 0; index < enchants.tagCount(); ++index) {
                    short id = enchants.getCompoundTagAt(index).getShort("id");
                    short level = enchants.getCompoundTagAt(index).getShort("lvl");
                    Enchantment enc = Enchantment.getEnchantmentByID(id);
                    if (enc != null) {
                        try {
                            String encName = enc.isCurse()
                                    ? enc.getTranslatedName(level).substring(11).substring(0, 1).toLowerCase()
                                    : enc.getTranslatedName(level).substring(0, 1).toLowerCase();
                            encName = encName + level;

                            yOffset += NametagFontRenderer.getStringHeight(encName);

                            NametagFontRenderer.drawStringWithShadow(encName, 0, -yOffset, -1);
                        } catch (IndexOutOfBoundsException exception) {

                        }

                    }
                }

                GlStateManager.scale(1D / sf, 1D / sf, 1);
                GlStateManager.translate(-(xPosition + 15D), -(yPosition + 10D), 0);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                GlStateManager.enableBlend();
            }
        }
    }

    // swag efficient super not laggy type beat
    private static void quickDrawRect(final float x, final float y, final float x2, final float y2, final ColorSetting a, final ColorSetting b, final ColorSetting c, final ColorSetting d, boolean line) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bufferbuilder.begin(line ? GL11.GL_LINE_LOOP : GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y2, 0.0D).color(a.getRed() / 255F, a.getGreen() / 255F, a.getBlue() / 255F, a.getAlpha() / 255F).endVertex();
        bufferbuilder.pos(x2, y2, 0.0D).color(b.getRed() / 255F, b.getGreen() / 255F, b.getBlue() / 255F, b.getAlpha() / 255F).endVertex();
        bufferbuilder.pos(x2, y, 0.0D).color(c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F, c.getAlpha() / 255F).endVertex();
        bufferbuilder.pos(x, y, 0.0D).color(d.getRed() / 255F, d.getGreen() / 255F, d.getBlue() / 255F, d.getAlpha() / 255F).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void quickDrawRect(final float x, final float y, final float x2, final float y2, final int color, boolean line) {
        final float a = (color >> 24 & 0xFF) / 255F;
        final float r = (color >> 16 & 0xFF) / 255F;
        final float g = (color >> 8 & 0xFF) / 255F;
        final float b = (color & 0xFF) / 255F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        bufferbuilder.begin(line ? GL11.GL_LINE_LOOP : GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y2, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(x2, y2, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(x2, y, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(x, y, 0.0D).color(r, g, b, a).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public boolean shouldRenderTag(Entity e) {
        if (e.getDistance(mc.player) > nameRange.getValue()) return false;
        if (animalSetting.getValue() && e instanceof EntityAnimal) {
            return true;
        } else if (mobSetting.getValue() && e instanceof IMob) {
            return true;
        } else if (playerSetting.getValue() && e instanceof EntityPlayer && !FakePlayerManager.isFake(e)) {
            return selfNametags.getValue() || !(e instanceof EntityPlayerSP);
        }

        return false;

    }
}
