package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.mixin.mixins.IEntityRenderer;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.combat.BowAim;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.FaceMasks;
import me.darki.konas.util.render.RenderUtil;
import me.darki.konas.util.render.TessellatorUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Trajectories extends Module {
    private static Setting<Boolean> highlightBlock = new Setting<>("Block", true);
    private static Setting<Boolean> facing = new Setting<>("Facing", true).withVisibility(highlightBlock::getValue);
    private static Setting<Boolean> vector = new Setting<>("Vector", true);
    private static final Setting<Float> radius = new Setting<>("Radius", 0.1F, 1F, 0.1F, 0.1F).withVisibility(() -> vector.getValue());
    private static final Setting<Integer> slices = new Setting<>("Slices", 8, 24, 3, 1).withVisibility(() -> vector.getValue());
    private static Setting<ColorSetting> fillColor = new Setting<>("Fill", new ColorSetting(0x22d81919, false));
    private static Setting<ColorSetting> outlineColor = new Setting<>("Outline", new ColorSetting(0xFFd81919, false));
    private static Setting<ColorSetting> lineColor = new Setting<>("Line", new ColorSetting(0xFFd81919, false));
    private static Setting<ColorSetting> vectorColor = new Setting<>("VectorColor", new ColorSetting(0xFFd81919, false));
    private static Setting<ColorSetting> selfFillColor = new Setting<>("SelfFill", new ColorSetting(0x2250b4b4, false));
    private static Setting<ColorSetting> selfOutlineColor = new Setting<>("SelfOutline", new ColorSetting(0xFF50b4b4, false));
    private static Setting<ColorSetting> selfLineColor = new Setting<>("SelfLine", new ColorSetting(0xFF50b4b4, false));
    private static Setting<ColorSetting> selfVectorColor = new Setting<>("SelfVector", new ColorSetting(0xFF50b4b4, false));
    private static Setting<Float> lineWidth = new Setting<>("LineWidth", 3f, 10f, 0.1f, 0.1f);
    private static Setting<Float> outlineWidth = new Setting<>("OutlineWidth", 1.5f, 10f, 0.1f, 0.1f);
    private static Setting<Float> vectorWidth = new Setting<>("VectorWidth", 1.5F, 10F, 0.1F, 0.1F);

    private final CopyOnWriteArrayList<Vec3d> flightPoints = new CopyOnWriteArrayList<>();

    public Trajectories() {
        super("Trajectories", Category.RENDER, "ThrowLines", "PearlLines", "ArrowPaths");
    }

    @Subscriber
    public void onWorldRender(Render3DEvent event) {
        mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase)
                .map(entity -> (EntityLivingBase) entity)
                .forEach(entity -> {
                    this.renderEntityTrajectory(entity, event.getPartialTicks());
                });
    }

    private ThrowableType getTypeFromCurrentItem(EntityLivingBase player) {
        // Check if we're holding an item first
        if (player.getHeldItemMainhand().isEmpty()) {
            return ThrowableType.NONE;
        }

        final ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        // Check what type of item this is
        switch (Item.getIdFromItem(itemStack.getItem())) {
            case 261: // ItemBow
                if (player.isHandActive())
                    return ThrowableType.ARROW;
                break;
            case 346: // ItemFishingRod
                return ThrowableType.FISHING_ROD;
            case 438: //splash potion
            case 441: //splash potion linger
                return ThrowableType.POTION;
            case 384: // ItemExpBottle
                return ThrowableType.EXPERIENCE;
            case 332: // ItemSnowball
            case 344: // ItemEgg
            case 368: // ItemEnderPearl
                return ThrowableType.NORMAL;
            default:
                break;
        }

        return ThrowableType.NONE;
    }

    enum ThrowableType {
        /**
         * Represents a non-throwable object.
         */
        NONE(0.0f, 0.0f),

        /**
         * Arrows fired from a bow.
         */
        ARROW(1.5f, 0.05f),

        /**
         * Splash potion entities
         */
        POTION(0.5f, 0.05f),

        /**
         * Experience bottles.
         */
        EXPERIENCE(0.7F, 0.07f),

        /**
         * The fishhook entity with a fishing rod.
         */
        FISHING_ROD(1.5f, 0.04f),

        /**
         * Any throwable entity that doesn't have unique
         * world velocity/gravity constants.
         */
        NORMAL(1.5f, 0.03f);

        private final float velocity;
        private final float gravity;

        ThrowableType(float velocity, float gravity) {
            this.velocity = velocity;
            this.gravity = gravity;
        }

        /**
         * The initial velocity of the entity.
         *
         * @return entity velocity
         */

        public float getVelocity() {
            return velocity;
        }

        /**
         * The constant gravity applied to the entity.
         *
         * @return constant world gravity
         */
        public float getGravity() {
            return gravity;
        }
    }

    public static boolean shouldSpoofAim(EntityLivingBase shooter) {
        Module bowAim = ModuleManager.getModuleByName("BowAim");
        if (shooter == mc.player && bowAim != null) {
            return bowAim.isEnabled() && !BowAim.angleInactivityTimer.hasPassed(350);
        }
        return false;
    }

    /**
     * A class used to mimic the flight of an entity.  Actual
     * implementation resides in multiple classes but the parent of all
     * of them is {@link net.minecraft.entity.projectile.EntityThrowable}
     */
    final class FlightPath {
        private EntityLivingBase shooter;
        private Vec3d position;
        private Vec3d motion;
        private float yaw;
        private float pitch;
        private final float pitchOffset;
        private AxisAlignedBB boundingBox;
        private boolean collided;
        private RayTraceResult target;
        private ThrowableType throwableType;

        FlightPath(EntityLivingBase player, ThrowableType throwableType) {
            this.shooter = player;
            this.throwableType = throwableType;

            // Set the starting angles of the entity
            this.setLocationAndAngles(this.shooter.posX, this.shooter.posY + this.shooter.getEyeHeight(), this.shooter.posZ,
                    shouldSpoofAim(this.shooter) ? (float) BowAim.yaw : this.shooter.rotationYaw, shouldSpoofAim(this.shooter) ? (float) BowAim.pitch : this.shooter.rotationPitch);

            if (throwableType == ThrowableType.EXPERIENCE) {
                this.pitchOffset = -20F;
            } else {
                this.pitchOffset = 0F;
            }

            Vec3d startingOffset = new Vec3d(MathHelper.cos(this.yaw / 180.0F * (float) Math.PI) * 0.16F, 0.1d,
                    MathHelper.sin(this.yaw / 180.0F * (float) Math.PI) * 0.16F);

            this.position = this.position.subtract(startingOffset);
            // Update the entity's bounding box
            this.setPosition(this.position);

            // Set the entity's motion based on the shooter's rotations
            this.motion = new Vec3d(-MathHelper.sin(this.yaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.pitch / 180.0F * (float) Math.PI),
                    -MathHelper.sin((this.pitch + pitchOffset) / 180.0F * (float) Math.PI),
                    MathHelper.cos(this.yaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.pitch / 180.0F * (float) Math.PI));

            this.setThrowableHeading(this.motion, this.getInitialVelocity());
        }

        /**
         * Update the entity's data in the world.
         */
        public void onUpdate() {
            // Get the predicted positions in the world
            Vec3d prediction = this.position.add(this.motion);
            // Check if we've collided with a block in the same time
            RayTraceResult blockCollision = this.shooter.getEntityWorld().rayTraceBlocks(this.position, prediction,
                    this.throwableType == ThrowableType.FISHING_ROD, !this.collidesWithNoBoundingBox(), false);

            // Check if we got a block collision
            if (blockCollision != null) {
                prediction = blockCollision.hitVec;
            }

            // Check entity collision
            this.onCollideWithEntity(prediction, blockCollision);

            // Check if we had a collision
            if (this.target != null) {
                this.collided = true;
                // Update position
                this.setPosition(this.target.hitVec);
                return;
            }

            // Sanity check to see if we've gone below the world (if we have we will never collide)
            if (this.position.y <= 0.0d) {
                // Force this to true even though we haven't collided with anything
                this.collided = true;
                return;
            }

            // Update the entity's position based on velocity
            this.position = this.position.add(this.motion);
            float motionModifier = 0.99F;
            // Check if our path will collide with water
            if (this.shooter.getEntityWorld().isMaterialInBB(this.boundingBox, Material.WATER)) {
                // Arrows move slower in water than normal throwables
                motionModifier = this.throwableType == ThrowableType.ARROW ? 0.6F : 0.8F;
            }

            // Apply the fishing rod specific motion modifier
            if (this.throwableType == ThrowableType.FISHING_ROD) {
                motionModifier = 0.92f;
            }

            // Slowly decay the velocity of the path
            this.motion = this.motion.scale(motionModifier);
            // Drop the motionY by the constant gravity
            this.motion = this.motion.subtract(0.0d, this.getGravityVelocity(), 0.0d);
            // Update the position and bounding box
            this.setPosition(this.position);
        }

        /**
         * Checks if a specific item type will collide
         * with a block that has no collision bounding box.
         *
         * @return true if type collides
         */
        private boolean collidesWithNoBoundingBox() {
            switch (this.throwableType) {
                case FISHING_ROD:
                case NORMAL:
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Check if our path collides with an entity.
         *
         * @param prediction     the predicted position
         * @param blockCollision block collision if we had one
         */
        private void onCollideWithEntity(Vec3d prediction, RayTraceResult blockCollision) {
            Entity collidingEntity = null;
            RayTraceResult collidingPosition = null;

            double currentDistance = 0.0d;
            // Get all possible collision entities disregarding the local player
            ArrayList<Entity> collisionEntities = (ArrayList<Entity>) mc.world.getEntitiesWithinAABBExcludingEntity(this.shooter, this.boundingBox.expand(this.motion.x, this.motion.y, this.motion.z).grow(1.0D, 1.0D, 1.0D));

            // Loop through every loaded entity in the world
            for (Entity entity : collisionEntities) {
                // Check if we can collide with the entity or it's ourself
                if (!entity.canBeCollidedWith()) {
                    continue;
                }

                // Check if we collide with our bounding box
                float collisionSize = entity.getCollisionBorderSize();
                AxisAlignedBB expandedBox = entity.getEntityBoundingBox().expand(collisionSize, collisionSize, collisionSize);
                RayTraceResult objectPosition = expandedBox.calculateIntercept(this.position, prediction);

                // Check if we have a collision
                if (objectPosition != null) {
                    double distanceTo = this.position.distanceTo(objectPosition.hitVec);

                    // Check if we've gotten a closer entity
                    if (distanceTo < currentDistance || currentDistance == 0.0D) {
                        collidingEntity = entity;
                        collidingPosition = objectPosition;
                        currentDistance = distanceTo;
                    }
                }
            }

            // Check if we had an entity
            if (collidingEntity != null) {
                // Set our target to the result
                this.target = new RayTraceResult(collidingEntity, collidingPosition.hitVec);
            } else {
                // Fallback to the block collision
                this.target = blockCollision;
            }
        }

        /**
         * Return the initial velocity of the entity at it's exact starting
         * moment in flight.
         *
         * @return entity velocity in flight
         */
        private float getInitialVelocity() {
            switch (this.throwableType) {
                // Arrows use the current use duration as a velocity multplier
                case ARROW:
                    // Check how long we've been using the bow
                    int useDuration = this.shooter.getHeldItem(EnumHand.MAIN_HAND).getItem().getMaxItemUseDuration(this.shooter.getHeldItem(EnumHand.MAIN_HAND)) - this.shooter.getItemInUseCount();
                    float velocity = (float) useDuration / 20.0F;
                    velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;
                    if (velocity > 1.0F) {
                        velocity = 1.0F;
                    }

                    // When the arrow is spawned inside of ItemBow, they multiply it by 2
                    return (velocity * 2.0f) * throwableType.getVelocity();
                default:
                    return throwableType.getVelocity();
            }
        }

        /**
         * Get the constant gravity of the item in use.
         *
         * @return gravity relating to item
         */
        private float getGravityVelocity() {
            return throwableType.getGravity();
        }

        /**
         * Set the position and rotation of the entity in the world.
         *
         * @param x     x position in world
         * @param y     y position in world
         * @param z     z position in world
         * @param yaw   yaw rotation axis
         * @param pitch pitch rotation axis
         */
        private void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
            this.position = new Vec3d(x, y, z);
            this.yaw = yaw;
            this.pitch = pitch;
        }

        /**
         * Sets the x,y,z of the entity from the given parameters. Also seems to set
         * up a bounding box.
         *
         * @param position position in world
         */
        private void setPosition(Vec3d position) {
            this.position = new Vec3d(position.x, position.y, position.z);
            // Usually this is this.width / 2.0f but throwables change
            double entitySize = (this.throwableType == ThrowableType.ARROW ? 0.5d : 0.25d) / 2.0d;
            // Update the path's current bounding box
            this.boundingBox = new AxisAlignedBB(position.x - entitySize,
                    position.y - entitySize,
                    position.z - entitySize,
                    position.x + entitySize,
                    position.y + entitySize,
                    position.z + entitySize);
        }

        /**
         * Set the entity's velocity and position in the world.
         *
         * @param motion   velocity in world
         * @param velocity starting velocity
         */
        private void setThrowableHeading(Vec3d motion, float velocity) {
            // Divide the current motion by the length of the vector
            this.motion = (motion.scale(1 / motion.length()));
            // Multiply by the velocity
            this.motion = this.motion.scale(velocity);
        }

        /**
         * Check if the path has collided with an object.
         *
         * @return path collides with ground
         */
        public boolean isCollided() {
            return collided;
        }

        /**
         * Get the target we've collided with if it exists.
         *
         * @return moving object target
         */
        public RayTraceResult getCollidingTarget() {
            return target;
        }
    }

    private void renderEntityTrajectory(EntityLivingBase entity, float partialTicks) {
        ThrowableType throwingType = this.getTypeFromCurrentItem(entity);

        if (throwingType == ThrowableType.NONE) {
            return;
        }

        FlightPath flightPath = new FlightPath(entity, throwingType);

        this.flightPoints.clear();

        while (!flightPath.isCollided()) {
            flightPath.onUpdate();

            this.flightPoints.add(new Vec3d(flightPath.position.x - mc.getRenderManager().viewerPosX,
                    flightPath.position.y - mc.getRenderManager().viewerPosY,
                    flightPath.position.z - mc.getRenderManager().viewerPosZ));
        }

        renderLine(entity, partialTicks);

        if (flightPath.collided) {
            final RayTraceResult hit = flightPath.target;
            AxisAlignedBB bb = null;

            if (hit == null) return;

            if (hit.typeOfHit == RayTraceResult.Type.BLOCK) {
                final BlockPos blockpos = hit.getBlockPos();
                final IBlockState iblockstate = mc.world.getBlockState(blockpos);

                if (iblockstate.getMaterial() != Material.AIR && mc.world.getWorldBorder().contains(blockpos)) {
                    if(vector.getValue()) renderVector(entity, hit);
                    bb = iblockstate.getSelectedBoundingBox(mc.world, blockpos).grow(0.0020000000949949026D);
                }
            } else if (hit.typeOfHit == RayTraceResult.Type.ENTITY && hit.entityHit != null && hit.entityHit != mc.player) {
                bb = hit.entityHit.getEntityBoundingBox();
            }

            if (bb != null && highlightBlock.getValue()) {
                if (facing.getValue() && hit.sideHit != null) {
                    switch (hit.sideHit) {
                        case DOWN:
                            bb = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ);
                            break;
                        case UP:
                            bb = new AxisAlignedBB(bb.minX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
                            break;
                        case NORTH:
                            bb = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.minZ);
                            break;
                        case SOUTH:
                            bb = new AxisAlignedBB(bb.minX, bb.minY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ);
                            break;
                        case EAST:
                            bb = new AxisAlignedBB(bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
                            break;
                        case WEST:
                            bb = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.minX, bb.maxY, bb.maxZ);
                            break;
                    }
                }
                TessellatorUtil.prepare();
                TessellatorUtil.drawBox(bb, true, 1, entity == mc.player ? selfFillColor.getValue() : fillColor.getValue(), entity == mc.player ? selfFillColor.getValue().getAlpha() : fillColor.getValue().getAlpha(), FaceMasks.Quad.ALL);
                TessellatorUtil.drawBoundingBox(bb, outlineWidth.getValue(), entity == mc.player ? selfOutlineColor.getValue() : outlineColor.getValue());
                TessellatorUtil.release();
                BlockHighlight.timer.reset();
            }
        }
    }

    public void renderLine(EntityLivingBase entity, float partialTicks) {

        final boolean bobbing = mc.gameSettings.viewBobbing;
        mc.gameSettings.viewBobbing = false;
        ((IEntityRenderer) mc.entityRenderer).iSetupCameraTransform(partialTicks, 0);

        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glPushMatrix();
        glDisable(GL_LIGHTING);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glDepthMask(false);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glLineWidth(lineWidth.getValue());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        Vec3d lastPos = this.flightPoints.get(0);

        Color c = entity == mc.player ? selfLineColor.getValue().getColorObject() : lineColor.getValue().getColorObject();

        for (Vec3d pos : this.flightPoints) {
            buffer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(lastPos.x, lastPos.y, lastPos.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
            buffer.pos(pos.x, pos.y, pos.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
            lastPos = pos;
            tessellator.draw();
        }

        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glEnable(GL_CULL_FACE);
        glEnable(GL_LIGHTING);
        glPopMatrix();
        glPopAttrib();

        mc.gameSettings.viewBobbing = bobbing;
        ((IEntityRenderer) mc.entityRenderer).iSetupCameraTransform(partialTicks, 0);

    }

    public void renderVector(EntityLivingBase entity, RayTraceResult result) {

        GlStateManager.pushMatrix();
        RenderUtil.beginRender();
        GlStateManager.glLineWidth(vectorWidth.getValue());
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();


        GL11.glLineWidth(vectorWidth.getValue());
        if(entity == mc.player) {
            GL11.glColor4f(selfVectorColor.getValue().getRed() / 255F, selfVectorColor.getValue().getGreen() / 255F, selfVectorColor.getValue().getBlue() / 255F, selfVectorColor.getValue().getAlpha() / 255F);
        } else {
            GL11.glColor4f(vectorColor.getValue().getRed() / 255F, vectorColor.getValue().getGreen() / 255F, vectorColor.getValue().getBlue() / 255F, vectorColor.getValue().getAlpha() / 255F);
        }
        GlStateManager.translate(result.hitVec.x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(), result.hitVec.y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(), result.hitVec.z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ());

        EnumFacing side = result.sideHit;

        switch (side) {
            case NORTH:
            case SOUTH:
                GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
                break;
            case WEST:
            case EAST:
                GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
                break;
        }

        Cylinder c = new Cylinder();
        GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f);
        c.setDrawStyle(GLU.GLU_LINE);

        c.draw(radius.getValue() * 2F, radius.getValue(), 0.0f, slices.getValue(), 1);

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        RenderUtil.endRender();
        GlStateManager.popMatrix();

    }

}
