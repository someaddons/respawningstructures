package com.respawningstructures.event;

import com.respawningstructures.structure.RespawnLevelData;
import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Calendar;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    /**
     * Conditions to trigger respawn watching:
     * - Spawner broken
     * - Spawner spawning blocked by player? difficult if some structure blocks it by default
     * - Dungeon chest looted - detect if it is a dungeon loot table
     * - Killing mobs
     * - Breaking some blocks
     * - placing torches
     * - fighting
     * - player dying
     * <p>
     * Conditions to fulfill for respawn:
     * - Time passed: Config, 10 ingame weeks?
     * - Not too many blocks placed next to, hard to track maybe per player?
     * - Threshold of trigger combination counts, 1 mob killed some blocks placed, sone chests looted, some spawner broken
     * - Not too high inhabited time diff since start of tracking
     * <p>
     * Extra difficulty:
     * Spawn additional or replace mobs during respawn(mark persistent)
     */

    private static long lastTime = 0;

    public static void onServerTick(final MinecraftServer server)
    {
        if (server.getTickCount() % 100 == 35)
        {
            if (lastTime == 0)
            {
                lastTime = Calendar.getInstance().getTimeInMillis();
                return;
            }

            if (server.getPlayerCount() > 0 && (Calendar.getInstance().getTimeInMillis() - lastTime) > 60 * 5 * 1000)
            {
                lastTime = Calendar.getInstance().getTimeInMillis();

                for (final ServerLevel level : server.getAllLevels())
                {
                    final RespawnLevelData data = level.getDataStorage().computeIfAbsent(RespawnLevelData::load, RespawnLevelData::new, RespawnLevelData.ID);
                    if (data != null)
                    {
                        data.increaseTime(60 * 5);
                    }
                }
            }
        }
    }

    public static void onLevelTick(final ServerLevel level)
    {
        if (!level.isClientSide && level.getGameTime() % 1000 == 17)
        {
            RespawnManager.onLevelTick(level);
        }
    }

    public static void onBlockBreak(final ServerPlayer player, final BlockPos pos)
    {
        final BlockState state = player.level().getBlockState(pos);
        if (state.hasBlockEntity() && player.level().getBlockEntity(pos) instanceof SpawnerBlockEntity)
        {
            RespawnManager.onSpawnerKilled((SpawnerBlockEntity) player.level().getBlockEntity(pos));
        }
        else
        {
            RespawnManager.onBlockBreak(player, pos);
        }
    }

    public static void onBlockPlaced(final ServerPlayer serverPlayer, final BlockPos blockPos, final BlockState state)
    {
        if (state.getLightEmission() > 0)
        {
            RespawnManager.onLightPlaced(serverPlayer, blockPos);
        }
        else
        {
            RespawnManager.onBlockPlaced(serverPlayer, blockPos);
        }
    }

    public static void onMobKilled(final LivingEntity entity, final DamageSource damageSource)
    {
        if (!entity.level().isClientSide() && damageSource.getEntity() instanceof ServerPlayer)
        {
            RespawnManager.onMobKilled(entity);
        }
        else if (entity instanceof ServerPlayer)
        {
            RespawnManager.onPlayerDeath((ServerPlayer) entity);
        }
    }

    public static boolean onEntityAdded(final Entity entity)
    {
        if (!entity.level().isClientSide && !RespawnManager.tryAddEntityDuringRespawn(entity, (ServerLevel) entity.level(), entity.blockPosition()))
        {
            entity.setRemoved(Entity.RemovalReason.DISCARDED);
            return false;
        }

        return true;
    }

    public static void onEntitySpawned(final Entity entity, final MobSpawnType type)
    {
        if (!entity.level().isClientSide() && type == MobSpawnType.SPAWNER && entity instanceof Mob)
        {
            RespawnManager.onSpawnerSpawn((Mob) entity);
        }
    }
}
