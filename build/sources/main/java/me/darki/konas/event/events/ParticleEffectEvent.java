package me.darki.konas.event.events;

import net.minecraft.client.particle.Particle;

public class ParticleEffectEvent extends CancellableEvent {
    private final Particle particle;

    public ParticleEffectEvent(Particle particle) {
        this.particle = particle;
    }

    public Particle getParticle() {
        return particle;
    }
}
