package me.darki.konas.util.client;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;

public class TickCalculation {

    public static TickCalculation INSTANCE = new TickCalculation();

    private long timeOfLastPacket = -1L;

    private final float[] array = new float[20];

    private int index;

    private TickCalculation() {
    }

    @Subscriber
    public void onPacket(PacketEvent.Receive event) {

        if (event.getPacket() instanceof SPacketTimeUpdate) {

            if (timeOfLastPacket != -1L) {
                long currentTime = System.currentTimeMillis();

                float timeDifference = (currentTime - timeOfLastPacket) / 1000F;

                array[index % array.length] = MathHelper.clamp(20F / timeDifference, 0F, 20F);
                index++;
            }
            timeOfLastPacket = System.currentTimeMillis();

        }

    }

    public float calculateTPS() {

        float numTicks = 0.0F;
        float sumTickRates = 0.0F;
        for (float tickRate : array) {
            if (tickRate > 0.0F) {
                sumTickRates += tickRate;
                numTicks += 1.0F;
            }
        }
        return MathHelper.clamp(sumTickRates / numTicks, 0.0F, 20.0F);

    }


}

