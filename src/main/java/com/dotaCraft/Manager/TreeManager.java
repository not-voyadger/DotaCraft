package com.dotaCraft.Manager;

import com.dotaCraft.DotaCraft;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.http.WebSocket;
import java.util.*;

public class TreeManager implements Listener {

    private static final int MAX_TREE_BLOCKS = 250;
    private static final long REGEN_DELAY_TICKS = 180 * 20L;

    private static class CutBlockInfo {
        final Location location;
        final BlockData originalData;

        CutBlockInfo(Location location, BlockData originalData) {
            this.location = location;
            this.originalData = originalData;
        }
    }

    public boolean isTreeBlock(Block block) {
        if (block == null) return false;
        Material type = block.getType();
        return Tag.LOGS.isTagged(type) || Tag.LEAVES.isTagged(type) || Tag.STAIRS.isTagged(type) || Tag.FENCES.isTagged(type) || Tag.SLABS.isTagged(type) || Tag.TRAPDOORS.isTagged(type);
    }

    public boolean destroyTree(Block startBlock) {
        if (!isTreeBlock(startBlock)) return false;

        List<CutBlockInfo> cutBlocks = new ArrayList<>();
        Queue<Block> queue = new LinkedList<>();
        Set<Location> visited = new HashSet<>();

        queue.add(startBlock);
        visited.add(startBlock.getLocation());

        while (!queue.isEmpty() && cutBlocks.size() < MAX_TREE_BLOCKS) {
            Block current = queue.poll();
            cutBlocks.add(new CutBlockInfo(current.getLocation(), current.getBlockData()));

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block neighbor = current.getRelative(x, y, z);
                        Location neighborLoc = neighbor.getLocation();

                        if (!visited.contains(neighborLoc) && isTreeBlock(neighbor)) {
                            visited.add(neighborLoc);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        if (cutBlocks.isEmpty()) return false;

        // effects
        Location centerLoc = startBlock.getLocation().add(0.5, 0.5, 0.5);
        centerLoc.getWorld().playSound(centerLoc, Sound.BLOCK_WOOD_BREAK, 1.0f, 0.8f);
        centerLoc.getWorld().playSound(centerLoc, Sound.BLOCK_GRASS_BREAK, 1.0f, 1.0f);

        // tree becomes air
        for (CutBlockInfo info : cutBlocks) {
            info.location.getBlock().setType(Material.AIR, false);
        }

        // timer for tree regen
        new BukkitRunnable() {
            @Override
            public void run() {
                for (CutBlockInfo info : cutBlocks) {
                    Block b = info.location.getBlock();
                    b.setBlockData(info.originalData, false);
                }
                centerLoc.getWorld().playSound(centerLoc, Sound.BLOCK_CHERRY_WOOD_PLACE, 0.8f, 1.2f);
            }
        }.runTaskLater(DotaCraft.getInstance(), REGEN_DELAY_TICKS);

        return true;
    }
}