/*
 * Copyright (c) 2020-2021 Polyhedral Development
 *
 * The Terra Core Addons are licensed under the terms of the MIT License. For more details,
 * reference the LICENSE file in this module's root directory.
 */

package com.dfsek.terra.addons.ore.v2.ores;

import net.jafama.FastMath;

import java.util.BitSet;
import java.util.Map;
import java.util.Random;

import com.dfsek.terra.api.block.BlockType;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.structure.Structure;
import com.dfsek.terra.api.util.Rotation;
import com.dfsek.terra.api.util.collection.MaterialSet;
import com.dfsek.terra.api.util.vector.Vector3Int;
import com.dfsek.terra.api.world.WritableWorld;


public class VanillaOre implements Structure {
    
    private final BlockState material;
    
    private final double size;
    private final MaterialSet replaceable;
    private final boolean applyGravity;
    private final double exposed;
    private final Map<BlockType, BlockState> materials;
    
    public VanillaOre(BlockState material, double size, MaterialSet replaceable, boolean applyGravity,
                      double exposed, Map<BlockType, BlockState> materials) {
        this.material = material;
        this.size = size;
        this.replaceable = replaceable;
        this.applyGravity = applyGravity;
        this.exposed = exposed;
        this.materials = materials;
    }
    
    protected static boolean shouldNotDiscard(Random random, double chance) {
        if(chance <= 0.0F) {
            return true;
        } else if(chance >= 1.0F) {
            return false;
        } else {
            return random.nextFloat() >= chance;
        }
    }
    
    public static double lerp(double t, double v0, double v1) {
        return v0 + t * (v1 - v0);
    }
    
    @Override
    public boolean generate(Vector3Int location, WritableWorld world, Random random, Rotation rotation) {
        float randomRadian = random.nextFloat() * (float) Math.PI;
        double eigthSize = size / 8.0F;
        
        // Place points to form a line segment
        double startX = (double) location.getX() + FastMath.sin(randomRadian) * eigthSize;
        double endX = (double) location.getX() - FastMath.sin(randomRadian) * eigthSize;
        
        double startZ = (double) location.getZ() + FastMath.cos(randomRadian) * eigthSize;
        double endZ = (double) location.getZ() - FastMath.cos(randomRadian) * eigthSize;
        
        double startY = location.getY() + random.nextInt(3) - 2;
        double endY = location.getY() + random.nextInt(3) - 2;
        
        int sizeInt = (int) size;
        double[] points = new double[sizeInt * 4];
        
        // Compute initial point positions and radius
        for(int i = 0; i < sizeInt; ++i) {
            float t = (float) i / (float) sizeInt;
            double xt = lerp(t, startX, endX);
            double yt = lerp(t, startY, endY);
            double zt = lerp(t, startZ, endZ);
            double roll = random.nextDouble() * size / 16.0;
            // Taper radius closer to line ends
            double radius = ((FastMath.sin((float) Math.PI * t) + 1.0F) * roll + 1.0) / 2.0;
            points[i * 4] = xt;
            points[i * 4 + 1] = yt;
            points[i * 4 + 2] = zt;
            points[i * 4 + 3] = radius;
        }
        
        // Compare every point to every other point
        for(int a = 0; a < sizeInt - 1; ++a) {
            double radiusA = points[a * 4 + 3];
            if(radiusA > 0.0) {
                for(int b = a + 1; b < sizeInt; ++b) {
                    double radiusB = points[b * 4 + 3];
                    if(radiusB > 0.0) {
                        double dxt = points[a * 4] - points[b * 4];
                        double dyt = points[a * 4 + 1] - points[b * 4 + 1];
                        double dzt = points[a * 4 + 2] - points[b * 4 + 2];
                        double dRadius = radiusA - radiusB;
                        
                        // If the radius difference is greater than the distance between the two points
                        if(dRadius * dRadius > dxt * dxt + dyt * dyt + dzt * dzt) {
                            // Set smaller of two radii to -1
                            if(dRadius > 0.0) {
                                points[b * 4 + 3] = -1.0;
                            } else {
                                points[a * 4 + 3] = -1.0;
                            }
                        }
                    }
                }
            }
        }
        
        int outset = (int) FastMath.ceil((size / 16.0F * 2.0F + 1.0F) / 2.0F);
        int x = (int) (location.getX() - FastMath.ceil(eigthSize) - outset);
        int y = location.getY() - 2 - outset;
        int z = (int) (location.getZ() - FastMath.ceil(eigthSize) - outset);
        
        int horizontalSize = (int) (2 * (FastMath.ceil(eigthSize) + outset));
        int verticalSize = 2 * (2 + outset);
        
        int sphereCount = 0;
        BitSet visited = new BitSet(horizontalSize * verticalSize * horizontalSize);
        
        // Generate a sphere at each point
        for(int i = 0; i < sizeInt; ++i) {
            double radius = points[i * 4 + 3];
            if(radius > 0.0) {
                double xt = points[i * 4];
                double yt = points[i * 4 + 1];
                double zt = points[i * 4 + 2];
                
                int xLowerBound = (int) FastMath.max(FastMath.floor(xt - radius), x);
                int xUpperBound = (int) FastMath.max(FastMath.floor(xt + radius), xLowerBound);
                
                int yLowerBound = (int) FastMath.max(FastMath.floor(yt - radius), y);
                int yUpperBound = (int) FastMath.max(FastMath.floor(yt + radius), yLowerBound);
                
                int zLowerBound = (int) FastMath.max(FastMath.floor(zt - radius), z);
                int zUpperBound = (int) FastMath.max(FastMath.floor(zt + radius), zLowerBound);
                
                // Iterate over coordinates within bounds
                for(int xi = xLowerBound; xi <= xUpperBound; ++xi) {
                    double dx = ((double) xi + 0.5 - xt) / radius;
                    if(dx * dx < 1.0) {
                        for(int yi = yLowerBound; yi <= yUpperBound; ++yi) {
                            double dy = ((double) yi + 0.5 - yt) / radius;
                            if(dx * dx + dy * dy < 1.0) {
                                for(int zi = zLowerBound; zi <= zUpperBound; ++zi) {
                                    double dz = ((double) zi + 0.5 - zt) / radius;
                                    
                                    // If position is inside the sphere
                                    if(dx * dx + dy * dy + dz * dz < 1.0 && !(yi < world.getMinHeight() || yi >= world.getMaxHeight())) {
                                        int index = xi - x + (yi - y) * horizontalSize + (zi - z) * horizontalSize * verticalSize;
                                        if(!visited.get(index)) { // Skip blocks that have already been visited
                                            
                                            visited.set(index);
                                            BlockType block = world.getBlockState(xi, yi, zi).getBlockType();
                                            if(shouldPlace(block, random, world, xi, yi, zi)) {
                                                world.setBlockState(xi, yi, zi, getMaterial(block), isApplyGravity());
                                                ++sphereCount;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return sphereCount > 0;
    }
    
    public boolean shouldPlace(BlockType type, Random random, WritableWorld world, int x, int y, int z) {
        if(!getReplaceable().contains(type)) {
            return false;
        } else if(shouldNotDiscard(random, exposed)) {
            return true;
        } else {
            return !(world.getBlockState(x, y, z - 1).isAir() ||
                     world.getBlockState(x, y, z + 1).isAir() ||
                     world.getBlockState(x, y - 1, z).isAir() ||
                     world.getBlockState(x, y + 1, z).isAir() ||
                     world.getBlockState(x - 1, y, z).isAir() ||
                     world.getBlockState(x + 1, y, z).isAir());
        }
    }
    
    public BlockState getMaterial(BlockType replace) {
        return materials.getOrDefault(replace, material);
    }
    
    public MaterialSet getReplaceable() {
        return replaceable;
    }
    
    public boolean isApplyGravity() {
        return applyGravity;
    }
}