package com.respawningstructures.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.UUID;

/**
 * Holds player respawn data, blockpos and last usage timepoint
 */
public class Respawn
{
    public final UUID playerUUID;

    /**
     * Respawn position
     */
    public final BlockPos position;

    /**
     * Last time of setting respawn point, or respawning in bed
     */
    public long lastUsageLevelTime = 0;

    public Respawn(final UUID playerUUID, final BlockPos position, final long lastUsageLevelTime)
    {
        this.playerUUID = playerUUID;
        this.position = position;
        this.lastUsageLevelTime = lastUsageLevelTime;
    }

    public Respawn(final CompoundTag tag)
    {
        this(tag.getUUID("playerUUID"), new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")), tag.getLong("lastUsageLevelTime"));
    }

    public Tag toNbt()
    {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("playerUUID", playerUUID);
        tag.putLong("lastUsageLevelTime", lastUsageLevelTime);
        tag.putInt("x", position.getX());
        tag.putInt("y", position.getY());
        tag.putInt("z", position.getZ());
        return tag;
    }
}
