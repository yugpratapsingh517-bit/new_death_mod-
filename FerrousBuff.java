package com.deathmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = DeathMod.MODID)
public class FerrousBuff {

    private static final ResourceLocation FERROUS =
            new ResourceLocation("mowziesmobs", "ferrous_wroughtnaut");

    private static final double HEALTH    = 445.0;
    private static final double DAMAGE    = 70.0;
    private static final double ARMOR     = 3.0;
    private static final double TOUGHNESS = 3.0;

    // Projectile behaviour: projectiles heal him instead of hurting him
    private static final double PROJECTILE_HEAL_RATIO = 1.0;   // 1.0 = heals the full damage amount
    private static final boolean CANCEL_PROJECTILE_DAMAGE = true;

    private static boolean isFerrous(Entity e) {
        return FERROUS.equals(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()));
    }

    private static void setBase(LivingEntity e, Attribute attr, double value) {
        AttributeInstance inst = e.getAttribute(attr);
        if (inst != null && inst.getBaseValue() != value) inst.setBaseValue(value);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof LivingEntity boss) || !isFerrous(boss)) return;

        boolean wasFull = boss.getHealth() >= boss.getMaxHealth();

        setBase(boss, Attributes.MAX_HEALTH, HEALTH);
        setBase(boss, Attributes.ATTACK_DAMAGE, DAMAGE);
        setBase(boss, Attributes.ARMOR, ARMOR);
        setBase(boss, Attributes.ARMOR_TOUGHNESS, TOUGHNESS);

        if (wasFull) boss.setHealth(boss.getMaxHealth()); // don't heal a damaged boss on reload
    }

    @SubscribeEvent
    public static void onAttacked(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || !isFerrous(target)) return;
        if (!event.getSource().is(DamageTypeTags.IS_PROJECTILE)) return;

        target.heal((float) (event.getAmount() * PROJECTILE_HEAL_RATIO));
        if (CANCEL_PROJECTILE_DAMAGE) event.setCanceled(true);
    }
}
