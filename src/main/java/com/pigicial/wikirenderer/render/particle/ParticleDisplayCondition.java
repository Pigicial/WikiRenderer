package com.pigicial.wikirenderer.render.particle;

import com.pigicial.wikirenderer.WikiRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ParticleDisplayCondition implements Predicate<Particle> {

    public static final ParticleDisplayCondition DURING_TICK = new ParticleDisplayCondition(particle -> WikiRenderer.inRenderableTick || ParticleRendererAndLooper.renderingParticles);
    public static final ParticleDisplayCondition SHOW_ALL = new ParticleDisplayCondition(particle -> true);
    public static final ParticleDisplayCondition HIDE_ALL = new ParticleDisplayCondition(particle -> false);

    private final Predicate<Particle> condition;

    private ParticleDisplayCondition(Predicate<Particle> condition) {
        this.condition = condition;
    }

    public static ParticleDisplayCondition inArea(AABB area) {
        return new ParticleDisplayCondition(particle -> area.intersects(particle.getBoundingBox()));
    }
    public static ParticleDisplayCondition inArea(Supplier<AABB> area) {
        return new ParticleDisplayCondition(particle -> area.get().intersects(particle.getBoundingBox()));
    }

    @Override
    public boolean test(Particle particle) {
        return condition.test(particle);
    }
}
