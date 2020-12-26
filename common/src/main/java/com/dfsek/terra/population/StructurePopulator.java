package com.dfsek.terra.population;

import com.dfsek.terra.TerraWorld;
import com.dfsek.terra.api.math.vector.Location;
import com.dfsek.terra.api.platform.TerraPlugin;
import com.dfsek.terra.api.platform.world.Chunk;
import com.dfsek.terra.api.platform.world.World;
import com.dfsek.terra.api.profiler.ProfileFuture;
import com.dfsek.terra.api.structures.structure.Rotation;
import com.dfsek.terra.api.world.generation.TerraBlockPopulator;
import com.dfsek.terra.biome.UserDefinedBiome;
import com.dfsek.terra.biome.grid.master.TerraBiomeGrid;
import com.dfsek.terra.config.base.ConfigPack;
import com.dfsek.terra.debug.Debug;
import com.dfsek.terra.generation.items.TerraStructure;
import com.dfsek.terra.util.PopulationUtil;
import net.jafama.FastMath;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class StructurePopulator implements TerraBlockPopulator {
    private final TerraPlugin main;

    public StructurePopulator(TerraPlugin main) {
        this.main = main;
    }

    @SuppressWarnings("try")
    @Override
    public void populate(@NotNull World world, @NotNull Random r, @NotNull Chunk chunk) {
        TerraWorld tw = main.getWorld(world);
        try(ProfileFuture ignored = tw.getProfiler().measure("StructureTime")) {
            Random random = PopulationUtil.getRandom(chunk);
            int cx = (chunk.getX() << 4);
            int cz = (chunk.getZ() << 4);
            if(!tw.isSafe()) return;
            TerraBiomeGrid grid = tw.getGrid();
            ConfigPack config = tw.getConfig();
            for(TerraStructure conf : config.getStructures()) {
                Location spawn = conf.getSpawn().getNearestSpawn(cx + 8, cz + 8, world.getSeed()).toLocation(world);

                if(!(FastMath.floorDiv(spawn.getBlockX(), 16) == chunk.getX()) || !(FastMath.floorDiv(spawn.getBlockZ(), 16) == chunk.getZ()))
                    continue;

                if(!((UserDefinedBiome) grid.getBiome(spawn)).getConfig().getStructures().contains(conf))
                    continue;
                Debug.info("Generating structure at (" + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ() + ")");
                conf.getStructure().execute(spawn.setY(conf.getSpawnStart().get(random)), random, Rotation.fromDegrees(90 * random.nextInt(4)));
            }
        }
    }
}
