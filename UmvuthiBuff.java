package com.deathmod;

import net.minecraft.resources.ResourceLocation;
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

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = DeathMod.MODID)
public class UmvuthiBuff {

    private static final ResourceLocation UMVUTHI = new ResourceLocation("mowziesmobs", "umvuthi");

    private static final double HEALTH = 400.0, ARMOR = 11.0, TOUGHNESS = 11.0;

    private static final float SUNSTRIKE_DAMAGE   = 12.0f;
    private static final float SOLAR_FLARE_DAMAGE = 14.0f;

    // fraction of the target's MAX health
    private static final float BEAM_DRAIN = 0.02f;
    private static final float NOVA_DRAIN = 0.145f;

    // minimum ticks between drains on the same target (20 ticks = 1 second)
    private static final int BEAM_INTERVAL = 1;
    private static final int NOVA_INTERVAL = 20;

    private static final Map<Entity, Long> lastBeam = new WeakHashMap<>();
    private static final Map<Entity, Long> lastNova = new WeakHashMap<>();
    private static boolean busy = false;

    private static boolean isUmvuthi(Entity e) {
        return UMVUTHI.equals(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()));
    }

    private static void setBase(LivingEntity e, Attribute attr, double value) {
        AttributeInstance inst = e.getAttribute(attr);
        if (inst != null && inst.getBaseValue() != value) inst.setBaseValue(value);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof LivingEntity boss) || !isUmvuthi(boss)) return;

        boolean wasFull = boss.getHealth() >= boss.getMaxHealth();
        setBase(boss, Attributes.MAX_HEALTH, HEALTH);
        setBase(boss, Attributes.ARMOR, ARMOR);
        setBase(boss, Attributes.ARMOR_TOUGHNESS, TOUGHNESS);
        if (wasFull) boss.setHealth(boss.getMaxHealth());
    }

    // Which Mowzie's class is dealing this hit? (hurt events run inside the attacker's code)
    private static String classify(String className) {
        if (className.endsWith("EntitySunstrike")) return "sunstrike";
        if (className.endsWith("EntityUmvuthi$SolarFlareAbility")) return "flare";
        if (className.endsWith("EntitySolarBeam")) return "beam";
        if (className.endsWith("EntitySuperNova")) return "nova";
        return null;
    }

    private static String attackType() {
        return StackWalker.getInstance().walk(frames -> frames.limit(40)
                .map(StackWalker.StackFrame::getClassName)
                .map(UmvuthiBuff::classify)
                .filter(Objects::nonNull)
                .findFirst().orElse(null));
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (busy) return;
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (event.getAmount() <= 0) return;                       // blocked hits stay blocked

        Entity attacker = event.getSource().getEntity();
        if (attacker == null || !isUmvuthi(attacker)) return;     // only Umvuthi, not players using his powers

        String type = attackType();
        if (type == null) return;

        switch (type) {
            case "sunstrike" -> event.setAmount(SUNSTRIKE_DAMAGE);
            case "flare"     -> event.setAmount(SOLAR_FLARE_DAMAGE);
            case "beam"      -> drain(event, attacker, BEAM_DRAIN, BEAM_INTERVAL, lastBeam);
            case "nova"      -> drain(event, attacker, NOVA_DRAIN, NOVA_INTERVAL, lastNova);
            default          -> { }
        }
    }

    private static void drain(LivingHurtEvent event, Entity attacker, float fraction,
                              int interval, Map<Entity, Long> last) {
        LivingEntity target = event.getEntity();
        long now = target.level().getGameTime();
        event.setAmount(0);                                       // replaces the normal damage

        Long prev = last.get(target);
        if (prev != null && now - prev < interval) return;
        last.put(target, now);

        busy = true;
        try {
            target.invulnerableTime = 0;
            target.hurt(target.level().damageSources().indirectMagic(attacker, attacker),
                    target.getMaxHealth() * fraction);
        } finally {
            busy = false;
        }
    }
}
