package com.kreezcraft.subterraneanwaters;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;

public final class SubterraneanWatersConfiguration {

    private final int[] excludedBiomeIds;

    private final int generationPercentage;

	private final Map<Integer, SubterraneanWatersConfiguration.PerDimension> configsByDimensionId = new HashMap<>();

    private static final String CATEGORY_GENERATION = "generation";

	SubterraneanWatersConfiguration(final Configuration configuration) {
        final int[] defaultExcludedBiomeIds = new int[] {
              0, /* Ocean             */
              6, /* Swamp             */
              7, /* River             */
             10, /* Frozen Ocean      */
             11, /* Frozen River      */
             24, /* Deep Ocean        */
             50, /* Deep Frozen Ocean */
            134  /* Swamp Hills       */
        };
        configuration.load();

        excludedBiomeIds =
            configuration
                .get(
                    "generation",
                    CATEGORY_GENERATION,
                    defaultExcludedBiomeIds,
                    "IDs associated with biome in which no world generation should take place.")
                .getIntList();
        generationPercentage =
            configuration.getInt(
                "chanceGenerate",
                CATEGORY_GENERATION,
                25,
                0,
                100,
                "Set to 0 to disable world generation.");

        loadPerDimensionConfigurations(configuration);

        configuration.save();
    }

	private void loadPerDimensionConfigurations(final Configuration configuration) {
	    final File configDir = configuration.getConfigFile().getParentFile();

        final int[] dimensionIds =
            configuration
                .get(
                    CATEGORY_GENERATION,
                    "dimensionIds",
                    new int[] { 0 },
                    "Dimensions to generate in. Configuration files will be created for each dimension on startup.")
                .getIntList();
        for (int dimensionId : dimensionIds) {
            final Configuration dimConfig = new Configuration(
                new File(configDir, "Subterranean Waters" + System.lineSeparator() + dimensionId + ".cfg"));

            final PerDimension perDimConfig = new PerDimension(dimConfig);
            dimConfig.load();

            configsByDimensionId.put(dimensionId, perDimConfig);
        }
    }

    // <editor-fold desc="Getters">
    public int[] getExcludedBiomeIds() {
        return excludedBiomeIds;
    }

    public int getGenerationPercentage() {
        return generationPercentage;
    }

    public Map<Integer, PerDimension> getPerDimensionConfigsByDimensionId() {
        return configsByDimensionId;
    }
    // </editor-fold>

    static final class PerDimension {

		private final int lowLimit;
        private final int lowOverheadLimit;

		private final int highLimit;
        private final int highOverheadLimit;

		private final int noiseSummand;
		private final int liquidLevel;

		private PerDimension(final Configuration configuration) {
		    configuration.load();

            configuration.addCustomCategoryComment(
                "GenerationVariables",
                "Height changes with step of 4 during noise generation. So 32 here ~ 128 in world. Some of these values are causing striations.");

            lowLimit =
                configuration.getInt(
                    "LowLimit",
                    "GenerationVariables",
                    3,
                    1,
                    33,
                    "Height at which noise value begins to halt.");
            lowOverheadLimit =
                configuration.getInt(
                    "LowOverheadLimit",
                    "GenerationVariables",
                    3,
                    1,
                    32,
                    "Height at which noise is guaranteed to halt.");
            highLimit =
                configuration.getInt(
                    "HighLimit",
                    "GenerationVariables",
                    4,
                    0,
                    32,
                    "Height at which noise value begins to halt.");
            highOverheadLimit =
                configuration.getInt(
                    "HighOverheadLimit",
                    "GenerationVariables",
                    18,
                    1,
                    32,
                    "Height at which noise is guaranteed to halt.");
            noiseSummand =
                configuration.getInt(
                    "NoiseSummand",
                    "GenerationVariables",
                    -7,
                    -100,
                    100,
                    "Value simply added to noise before height controling, allows to control size of caverns. Any value above -3 yield enormous caverns.");
            liquidLevel =
                configuration.getInt(
                    "LiquidLevel",
                    "GenerationVariables",
                    13,
                    1,
                    128,
                    "Water level. Actual Y-coordinate in world.");
            configuration.save();
        }

        // <editor-fold desc="Getters">
        public int getLowLimit() {
            return lowLimit;
        }

        public int getLowOverheadLimit() {
            return lowOverheadLimit;
        }

        public int getHighLimit() {
            return highLimit;
        }

        public int getHighOverheadLimit() {
            return highOverheadLimit;
        }

        public int getNoiseSummand() {
            return noiseSummand;
        }

        public int getLiquidLevel() {
            return liquidLevel;
        }
        // </editor-fold>
    }
}
