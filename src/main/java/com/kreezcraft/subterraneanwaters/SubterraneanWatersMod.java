package com.kreezcraft.subterraneanwaters;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.terraingen.ChunkProviderEvent.ReplaceBiomeBlocks;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import org.apache.commons.lang3.ArrayUtils;

@Mod(
    modid = "subterranaenwaters",
    name =  "Subterranean Waters",
    version = "@VERSION@",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:Forge@[10.13.4.1566,)",
    acceptableRemoteVersions = "*")
public final class SubterraneanWatersMod {

    private SubterraneanWatersConfiguration modConfig;

    @Instance
    private static SubterraneanWatersMod instance;

	private static final SubterraneanWatersGenerator generator = new SubterraneanWatersGenerator();

    public static SubterraneanWatersMod getInstance() {
        return instance;
    }

	// <editor-fold desc="Event handlers">
	@EventHandler
	public void onFMLPreInitialization(final FMLPreInitializationEvent event) {
		final Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
		modConfig = new SubterraneanWatersConfiguration(configuration);

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onReplaceBiomeBlocks(final ReplaceBiomeBlocks event) {
		final Chunk        chunk = new Chunk(event.world, event.chunkX, event.chunkZ);

		/* TODO: Maybe respect the fact that a single chunk could contain more than one biome. */
		final BiomeGenBase biome = chunk.getBiomeGenForWorldCoords(0, 0, event.world.getWorldChunkManager());

		if (!ArrayUtils.contains(modConfig.getExcludedBiomeIds(), biome.biomeID))
            generator.generate(event.blockArray, event.world, event.chunkX, event.chunkZ);
	}
	// </editor-fold>

    public SubterraneanWatersConfiguration getModConfig() {
        return modConfig;
    }
}
