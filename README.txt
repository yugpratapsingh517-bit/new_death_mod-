DEATH MOD - A MOWZIE'S MOBS ADDON  (Minecraft 1.20.1, Forge 47.x)
Requires Mowzie's Mobs 1.8.2+ (and its own dependency GeckoLib).

HOW TO BUILD THE JAR
1. Install JDK 17.
2. Add the Gradle wrapper (this folder doesn't include it). EITHER:
   - copy "gradle/", "gradlew" and "gradlew.bat" from the official Forge 1.20.1 MDK
     (files.minecraftforge.net -> 1.20.1 -> MDK) into this folder, OR
   - if Gradle 8.1+ is installed, run:  gradle wrapper --gradle-version 8.1.1
3. In this folder run:   gradlew build        (Windows)   or   ./gradlew build
4. The jar is at build/libs/deathmod-1.0.0.jar
   (use that one, not any "-sources"/"-slim" file)
5. Put it in your mods folder next to Mowzie's Mobs 1.8.2 and GeckoLib.

If you already have a working 1.20.1 Forge workspace, you can skip all of this:
copy the 5 files in src/main/java/com/deathmod/ and src/main/resources/META-INF/mods.toml
into it and change the mod id to deathmod.

WHAT IT DOES
Ferrous Wroughtnaut: HP 445, damage 70, armor 3, toughness 3, projectiles heal him instead of hurting.
Umvuthi: HP 400, armor 11, toughness 11, sunstrike 12 dmg, solar flare burst 14 dmg,
         solar beam drains 2% max health per hit, supernova drains 14.5% max health (once per second).
Frostmaw: HP 725, damage 50, armor 15, toughness 19, slam 100 dmg,
          ice ball drains 8% max health + Mining Fatigue II for 10s.

TUNING: every number is a constant at the top of FerrousBuff / UmvuthiBuff / FrostmawBuff.

TESTING: /summon mowziesmobs:ferrous_wroughtnaut   (also umvuthi, frostmaw)

BUILD WITHOUT INSTALLING ANYTHING (GitHub)
1. Make a free GitHub account and create a new empty repository.
2. Upload the CONTENTS of this folder (build.gradle must be at the top level of the repo,
   and the hidden ".github" folder must come with it).
3. Open the repository's "Actions" tab -> "Build Death Mod jar" -> wait for the green tick.
4. Open that run and download the "deathmod-jar" artifact. Unzip it: deathmod-1.0.0.jar is your mod.
