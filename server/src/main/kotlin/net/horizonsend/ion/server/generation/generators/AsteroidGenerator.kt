package net.horizonsend.ion.server.generation.generators

import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.generation.Asteroid
import net.horizonsend.ion.server.generation.configuration.AsteroidConfiguration
import net.horizonsend.ion.server.generation.configuration.AsteroidFeatures
import net.horizonsend.ion.server.generation.configuration.Palette
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.Random
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

object AsteroidGenerator {
	// default asteroid configuration values
	private val configuration: AsteroidConfiguration =
		loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_configuration.conf")

	// features (e.g. asteroid belts)
	private val features: AsteroidFeatures =
		loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_features.conf")

	fun postGenerateAsteroid(
		world: World,
		chunk: Chunk,
		asteroid: Asteroid
	) {
		val noise = SimplexOctaveGenerator(Random(world.seed), 1)

		val worldX = chunk.x * 16
		val worldZ = chunk.z * 16

		for (x in worldX - (asteroid.size * 1.5).toInt()..worldX + 15 + (asteroid.size * 1.5).toInt()) {
			val xDouble = x.toDouble()
			val xSquared = (xDouble - asteroid.x) * (xDouble - asteroid.x)

			for (z in worldZ - (asteroid.size * 1.5).toInt()..worldZ + 15 + (asteroid.size * 1.5).toInt()) {
				val zDouble = z.toDouble()
				val zSquared = (zDouble - asteroid.z) * (zDouble - asteroid.z)

				for (y in (asteroid.y - (1.5 * asteroid.size)).toInt() until (asteroid.y + (1.5 * asteroid.size)).toInt()) {
					val yDouble = y.toDouble()
					val ySquared = (yDouble - asteroid.y) * (yDouble - asteroid.y)

					noise.setScale(0.15)

					var fullNoise =
						0.0 // Full noise is used as the radius of the asteroid, and it is offset by the noise of each block pos.

					for (octave in 0..asteroid.octaves) {
						noise.setScale(0.015 * (octave + 1.0).pow(2.25))

						val offset = abs(
							noise.noise(
								xDouble,
								yDouble,
								zDouble,
								0.0,
								1.0,
								false
							)
						) * (asteroid.size / (octave + 1.0).pow(2.25))

						fullNoise += offset
					}

					if (
						xSquared +
						ySquared +
						zSquared
					> (fullNoise).pow(2)
					) {
						continue // Continue if block is not inside any asteroid
					}

					val weightedMaterials = materialWeights(asteroid.palette)

					noise.setScale(0.15)

					val paletteSample = (
						(
							(
								noise.noise(
									worldX + xDouble,
									yDouble,
									worldZ + zDouble,
									1.0,
									1.0,
									true
								) + 1
								) / 2
							) * (weightedMaterials.size - 1)
						).toInt() // Calculate a noise pattern with a minimum at zero, and a max peak of the size of the materials list.

					val material =
						weightedMaterials[paletteSample] // Weight the list by adding duplicate entries, then sample it for the material.

					world.setBlockData(x, y, z, material.createBlockData())
				}
			}
		}
	}

	fun generateAsteroid(x: Int, y: Int, z: Int, random: Random): Asteroid {
		val noise = SimplexOctaveGenerator(random, 1)

		// Get material palette

		noise.setScale(0.15)

		val weightedPalette = paletteWeights()
		val paletteSample = random.nextInt(weightedPalette.size)

		val blockPalette: Palette = weightedPalette[paletteSample]

		val size = random.nextDouble(5.0, 20.0)
		val octaves = floor(5 * 0.95.pow(size)).toInt()

		return Asteroid(x, y, z, blockPalette, size, octaves)
	}

	private fun materialWeights(palette: Palette): List<Material> {
		val weightedList = mutableListOf<Material>()

		for (material in palette.materials) {
			for (occurrence in material.value downTo 0) {
				weightedList.add(material.key)
			}
		}

		return weightedList
	}

	fun parseDensity(world: World, x: Double, y: Double, z: Double): Double {
		val densities = mutableSetOf<Double>()
		densities.add(configuration.baseAsteroidDensity)

		for (feature in features.features) {
			if (feature.worldName != world.name) continue

			if ((sqrt((x - feature.x).pow(2) + (z - feature.z).pow(2)) - feature.tubeSize).pow(2) + (y - feature.y).pow(
					2
				) < feature.tubeRadius.pow(2)
			) {
				densities.add(feature.baseDensity)
			}
		}

		return densities.max()
	}

	/**
	 * Weights the list of Palettes in the configuration by adding duplicate entries based on the weight.
	 */
	private fun paletteWeights(): List<Palette> {
		val weightedList = mutableListOf<Palette>()

		for (palette in configuration.blockPalettes) {
			for (occurrence in palette.weight downTo 0) {
				weightedList.add(palette)
			}
		}

		return weightedList
	}
}