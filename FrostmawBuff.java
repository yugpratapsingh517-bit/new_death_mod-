package com.deathmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = DeathMod.MODID)
public class FrostmawBuff {

    private static final ResourceLocation FROSTMAW = new ResourceLocation("mowziesmobs", "frostmaw");

    private static final double HEALTH = 725.0, DAMAGE = 50.0, ARMOR = 15.0, TOUGHNESS = 19.0;

    private static final float SLAM_DAMAGE = 100.0f;

    // Freeze projectile (ice ball)
    private static final float ICEBALL_DRAIN = 0.08f;       // fraction of the target's MAX health
    private static final int FATIGUE_TICKS = 200;           // 10 seconds
    private static final int FATIGUE_LEVEL = 1;             // 0 = level I, 1 = level II

    private static boolean busy = false;

    private static boolean isFrostmaw(Entity e) {
        return FROSTMAW.equals(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()));
    }

    private static void setBase(LivingEntity e, Attribute attr, double value) {
        AttributeInstance inst = e.getAttribute(attr);
        if (inst != null && inst.getBaseValue() != value) inst.setBaseValue(value);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof LivingEntity boss) || !isFrostmaw(boss)) return;

        boolean wasFull = boss.getHealth() >= boss.getMaxHealth();
        setBase(boss, Attributes.MAX_HEALTH, HEALTH);
        setBase(boss, Attributes.ATTACK_DAMAGE, DAMAGE);
        setBase(boss, Attributes.ARMOR, ARMOR);
        setBase(boss, Attributes.ARMOR_TOUGHNESS, TOUGHNESS);
        if (wasFull) boss.setHealth(boss.getMaxHealth());
    }

    // Work out which Frostmaw attack is dealing this hit
    private static String attackType() {
        return StackWalker.getInstance().walk(frames -> frames.limit(40)
                .map(f -> {
                    String cls = f.getClassName();
                    String m = f.getMethodName();
                    if (cls.endsWith("EntityIceBall")) return "iceball";
                    // the slam hit is dealt from Frostmaw's own tick method
                    if (cls.endsWith("EntityFrostmaw") && (m.equals("m_8119_") || m.equals("tick"))) return "slam";
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst().orElse(null));
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (busy) return;
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (event.getAmount() <= 0) return;                        // blocked hits stay blocked

        Entity attacker = event.getSource().getEntity();
        if (attacker == null || !isFrostmaw(attacker)) return;

        String type = attackType();
        if (type == null) return;

        if (type.equals("slam")) {
            event.setAmount(SLAM_DAMAGE);
            return;
        }

        // ice ball: replace damage with a percent drain, plus mining fatigue
        event.setAmount(0);
        busy = true;
        try {
            target.invulnerableTime = 0;
            target.hurt(target.level().damageSources().indirectMagic(attacker, attacker),
                    target.getMaxHealth() * ICEBALL_DRAIN);
        } finally {
            busy = false;
        }
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, FATIGUE_TICKS, FATIGUE_LEVEL));
    }
}
