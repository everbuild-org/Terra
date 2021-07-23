package com.dfsek.terra.api.world.biome;


import com.dfsek.terra.api.properties.PropertyHolder;
import com.dfsek.terra.api.util.collection.ProbabilityCollection;

import java.util.Set;

/**
 * Represents a custom biome
 */
public interface TerraBiome extends PropertyHolder {

    /**
     * Gets the Vanilla biome to represent the custom biome.
     *
     * @return TerraBiome - The Vanilla biome.
     */
    ProbabilityCollection<Biome> getVanillaBiomes();

    /**
     * Gets the BiomeTerrain instance used to generate the biome.
     *
     * @return BiomeTerrain - The terrain generation instance.
     */
    GenerationSettings getGenerator();

    int getColor();

    Set<String> getTags();

    String getID();
}
