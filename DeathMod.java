package com.deathmod;

import net.minecraftforge.fml.common.Mod;

@Mod(DeathMod.MODID)
public class DeathMod {
    public static final String MODID = "deathmod";

    public DeathMod() {
        // Everything is handled by the @Mod.EventBusSubscriber classes
        // FerrousBuff, UmvuthiBuff and FrostmawBuff.
    }
}
