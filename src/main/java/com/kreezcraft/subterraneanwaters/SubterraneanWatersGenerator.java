package com.kreezcraft.subterraneanwaters;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorOctaves;

import com.kreezcraft.subterraneanwaters.SubterraneanWatersConfiguration.PerDimension;

import static net.minecraft.util.MathHelper.clamp_double;
import static net.minecraft.util.MathHelper.sin;

public final class SubterraneanWatersGenerator {

	private boolean initialized = false;

	@SuppressWarnings("FieldCanBeLocal")
    private Random random;

	private NoiseGeneratorOctaves noiseGenStone1;
	private NoiseGeneratorOctaves noiseGenStone2;
	private NoiseGeneratorOctaves noiseGenStoneMask;
	private NoiseGeneratorOctaves noiseGenStoneMask2;

	private double[] tempArrayStone1;
	private double[] tempArrayStone2;
	private double[] tempArrayStoneMask;
	private double[] tempArrayStoneMask2;
	private double[] tempArrayStone;

	public void generate(final Block[] blockArray, final World world, final int chunkX, final int chunkZ) {
	    final SubterraneanWatersConfiguration modConfig = SubterraneanWatersMod.getInstance().getModConfig();

		final int chance = (int) (Math.random() * 100);

		if (chance > modConfig.getGenerationPercentage())
		    return;

		final PerDimension perDimConfig =
            modConfig
                .getPerDimensionConfigsByDimensionId()
                .get(world.provider.dimensionId);
		if (perDimConfig == null)
		    return;

        if (!initialized) {
            random = new Random(world.getSeed());

            noiseGenStone1     = new NoiseGeneratorOctaves(random, 16);
            noiseGenStone2     = new NoiseGeneratorOctaves(random, 16);
            noiseGenStoneMask  = new NoiseGeneratorOctaves(random,  8);
            noiseGenStoneMask2 = new NoiseGeneratorOctaves(random,  8);

            tempArrayStone = new double[3 * 33 * 3];

            initialized = true;
        }
        doGenerate(blockArray, perDimConfig, chunkX, chunkZ);
	}

    @SuppressWarnings("PointlessArithmeticExpression")
    private void doGenerate(
            final Block[] blockArray,
            final SubterraneanWatersConfiguration.PerDimension perDimConfig,
            final int chunkX,
            final int chunkZ) {
        initNoise(perDimConfig, chunkX * 2, 0, chunkZ * 2, 3, 33, 3);

        for (int sampleX = 0; sampleX < 2; ++sampleX) {
            for (int sampleZ = 0; sampleZ < 2; ++sampleZ) {
                for (int sampleY = 0; sampleY <  33 - 8; ++sampleY) {
                    double bottomNearLeft  = tempArrayStone[((sampleX + 0) * 3 + sampleZ + 0) * 33 + sampleY + 0];
                    double bottomNearRight = tempArrayStone[((sampleX + 0) * 3 + sampleZ + 1) * 33 + sampleY + 0];
                    double bottomFarLeft   = tempArrayStone[((sampleX + 1) * 3 + sampleZ + 0) * 33 + sampleY + 0];
                    double bottomFarRight  = tempArrayStone[((sampleX + 1) * 3 + sampleZ + 1) * 33 + sampleY + 0];

                    final double topNearLeft  = tempArrayStone[((sampleX + 0) * 3 + sampleZ + 0) * 33 + sampleY + 1];
                    final double topNearRight = tempArrayStone[((sampleX + 0) * 3 + sampleZ + 1) * 33 + sampleY + 1];
                    final double topFarLeft   = tempArrayStone[((sampleX + 1) * 3 + sampleZ + 0) * 33 + sampleY + 1];
                    final double topFarRight  = tempArrayStone[((sampleX + 1) * 3 + sampleZ + 1) * 33 + sampleY + 1];

                    final double dNearLeft  = (topNearLeft  - bottomNearLeft)  / 4;
                    final double dNearRight = (topNearRight - bottomNearRight) / 4;
                    final double dFarLeft   = (topFarLeft   - bottomFarLeft)   / 4;
                    final double dFarRight  = (topFarRight  - bottomFarRight)  / 4;

                    for (int shiftY = 0; shiftY < 4; ++shiftY) {
                        double currentLeft  = bottomNearLeft;
                        double currentRight = bottomNearRight;

                        final double dXLeft  = (bottomFarLeft  - bottomNearLeft)  / 8;
                        final double dXRight = (bottomFarRight - bottomNearRight) / 8;

                        for (int shiftX = 0; shiftX < 8; ++shiftX) {
                            final double dZ = (currentRight - currentLeft) / 16;

                            double currentValue = currentLeft;

                            for (int shiftZ = 0; shiftZ < 8; ++shiftZ) {
                                if (currentValue > 0) {
                                    final int worldX = shiftX + sampleX * 8;
                                    final int worldY = shiftY + sampleY * 4;
                                    final int worldZ = shiftZ + sampleZ * 8;

                                    final Block block = worldY > perDimConfig.getLiquidLevel() ? Blocks.air : Blocks.water;
                                    blockArray[((worldX & 15) * 16 + (worldZ & 15)) * 16 * 16 + worldY] = block;
                                }
                                currentValue += dZ;
                            }
                            currentLeft  += dXLeft;
                            currentRight += dXRight;
                        }
                        bottomNearLeft  += dNearLeft;
                        bottomNearRight += dNearRight;
                        bottomFarLeft   += dFarLeft;
                        bottomFarRight  += dFarRight;
                    }
                }
            }
        }
    }

	@SuppressWarnings("SameParameterValue")
    private void initNoise(
            final SubterraneanWatersConfiguration.PerDimension perDimConfig,
            final int shiftX,
            final int shiftY,
            final int shiftZ,
            final int sizeX,
            final int sizeY,
            final int sizeZ) {
		final double noiseScaleVertical   = 684.412;
        final double noiseScaleHorizontal = noiseScaleVertical * 2;

		tempArrayStoneMask =
            noiseGenStoneMask.generateNoiseOctaves(
                tempArrayStoneMask,
                shiftX,
                shiftY,
                shiftZ,
                sizeX,
				sizeY,
                sizeZ,
                noiseScaleHorizontal /  80.0,
                noiseScaleVertical   / 160.0,
                noiseScaleHorizontal /  80.0
            );
		tempArrayStoneMask2 =
            noiseGenStoneMask2.generateNoiseOctaves(
                tempArrayStoneMask2,
                shiftX,
                shiftY,
                shiftZ,
				sizeX,
                sizeY,
                sizeZ,
                noiseScaleHorizontal /  80.0,
                noiseScaleVertical   / 160.0,
				noiseScaleHorizontal /  80.0
            );
		tempArrayStone1 =
            noiseGenStone1.generateNoiseOctaves(
                tempArrayStone1,
                shiftX,
                shiftY,
                shiftZ,
                sizeX,
                sizeY,
				sizeZ,
                noiseScaleHorizontal / 2,
                noiseScaleVertical   / 2,
                noiseScaleHorizontal / 2
            );
		tempArrayStone2 =
            noiseGenStone2.generateNoiseOctaves(
                tempArrayStone2,
                shiftX,
                shiftY,
                shiftZ,
                sizeX,
                sizeY,
				sizeZ,
                noiseScaleHorizontal / 2,
                noiseScaleVertical   / 2,
                noiseScaleHorizontal / 2
            );

		int k = 0;
		for (int posX = 0; posX < sizeX; ++posX) {
			for (int posZ = 0; posZ < sizeZ; ++posZ) {
				for (int posY = 0; posY < sizeY; ++posY) {
					final double noiseValue1     = tempArrayStone1[k] / 512.0;
					final double noiseValue2     = tempArrayStone2[k] / 512.0;
					final double noiseValueMask1 = (tempArrayStoneMask[k]  / 10.0 + 1.0) / 2.0;
					final double noiseValueMask2 = (tempArrayStoneMask2[k] / 10.0 + 1.0) / 2.0;

					tempArrayStone[k] =
                        calculateNoiseValue(perDimConfig, posY, noiseValue1, noiseValue2, noiseValueMask1) - 3 * sin(posY / 1.5F);

					tempArrayStone[k] =
                        interpolateLinear(tempArrayStone[k], -3, noiseValueMask2);
					++k;
				}
			}
		}
	}

	private static double calculateNoiseValue(
	        final SubterraneanWatersConfiguration.PerDimension perDimConfig,
	        final int posY,
            final double noiseValue1,
            final double noiseValue2,
            final double noiseValueMask) {
		double noiseValue = interpolateLinear(noiseValue1, noiseValue2, noiseValueMask) + perDimConfig.getNoiseSummand();

		if (posY > perDimConfig.getHighLimit()) {
			final double heightOverhead = (posY - (double) (perDimConfig.getHighLimit())) / perDimConfig.getHighOverheadLimit();
			noiseValue = noiseValue * (1.0D - heightOverhead) + -100.0D * heightOverhead;
		}
		if (posY < perDimConfig.getLowLimit()) {
			final double heightOverhead = ((double) perDimConfig.getLowLimit() - posY) / perDimConfig.getLowOverheadLimit();
			noiseValue = noiseValue * (1.0D - heightOverhead) + -30.0D * heightOverhead;
		}
		return noiseValue;
	}

	private static double interpolateLinear(final double value1, final double value2, final double k) {
		return value1 + (value2 - value1) * clamp_double(k, 0.0, 1.0);
	}
}
