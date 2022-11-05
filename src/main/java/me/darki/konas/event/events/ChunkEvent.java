package me.darki.konas.event.events;

import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.world.chunk.Chunk;

public class ChunkEvent {

    private Chunk chunk;
    private SPacketChunkData packet;

    public ChunkEvent(Chunk chunk, SPacketChunkData packet) {
        this.chunk = chunk;
        this.packet = packet;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public SPacketChunkData getPacket() {
        return packet;
    }

}
