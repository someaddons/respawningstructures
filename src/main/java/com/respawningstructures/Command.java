package com.respawningstructures;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.respawningstructures.structure.RespawnLevelData;
import com.respawningstructures.structure.RespawnManager;
import com.respawningstructures.structure.StructureData;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.*;

public class Command
{
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext)
    {
        return Commands.literal(RespawningStructures.MOD_ID)
          .then(
            Commands.literal("respawnClosestStructure")
              .requires(stack -> stack.hasPermission(2))
              .executes(context ->
              {
                  final ServerLevel world = context.getSource().getLevel();
                  final Map<Structure, LongSet> structures = new HashMap<>();

                  final ChunkPos start = new ChunkPos(BlockPos.containing(context.getSource().getPosition()));

                  for (final Map.Entry<Structure, LongSet> entry : world.structureManager()
                    .getAllStructuresAt(new BlockPos((start.x) << 4, 0, (start.z) << 4))
                    .entrySet())
                  {
                      structures.computeIfAbsent(entry.getKey(), k -> new LongOpenHashSet(entry.getValue())).addAll(entry.getValue());
                  }

                  if (structures.isEmpty())
                  {
                      for (int x = -5; x < 5; x++)
                      {
                          for (int z = -5; z < 5; z++)
                          {
                              for (final Map.Entry<Structure, LongSet> entry : world.structureManager()
                                .getAllStructuresAt(new BlockPos((start.x + x) << 4, 0, (start.z + z) << 4))
                                .entrySet())
                              {
                                  structures.computeIfAbsent(entry.getKey(), k -> new LongOpenHashSet(entry.getValue())).addAll(entry.getValue());
                              }
                          }
                      }
                  }

                  Map<BlockPos, StructureStart> structurePositions = new HashMap<>();
                  for (Map.Entry<Structure, LongSet> structureEntry : structures.entrySet())
                  {
                      world.structureManager().fillStartsForStructure(structureEntry.getKey(), structureEntry.getValue(),
                        structureStart ->
                        {
                            structurePositions.put(structureStart.getBoundingBox().getCenter(), structureStart);
                        }
                      );
                  }

                  final List<Map.Entry<BlockPos, StructureStart>> sortedStructures = new ArrayList<>(structurePositions.entrySet());
                  sortedStructures.sort(Comparator.comparingDouble(p -> p.getKey().distSqr(BlockPos.containing(context.getSource().getPosition()))));

                  if (sortedStructures.isEmpty())
                  {
                      context.getSource()
                        .sendSystemMessage(Component.literal("No Structure nearby, anything visible nearby is not an actual structure but just a feature, those can't respawn")
                          .withStyle(ChatFormatting.RED));
                      return 1;
                  }

                  context.getSource()
                    .sendSystemMessage(Component.literal("Respawning structure: " +
                                                           context.getSource()
                                                             .registryAccess()
                                                             .registry(Registries.STRUCTURE)
                                                             .get()
                                                             .getKey(sortedStructures.get(0).getValue().getStructure()))
                      .append(Component.literal(" at: " + sortedStructures.get(0).getKey()).withStyle(ChatFormatting.YELLOW)
                        .withStyle(style ->
                                     style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                       "/tp " + sortedStructures.get(0).getKey().getX() + " " + sortedStructures.get(0).getKey().getY() + " " + sortedStructures.get(0)
                                         .getKey()
                                         .getZ()))
                        )));

                  RespawnManager.respawnStructure(world, RespawnManager.getForPos(world, sortedStructures.get(0).getValue().getBoundingBox().getCenter(), false), false);

                  return 1;
              })
          )
          .then(
            Commands.literal("setClosestStructureRespawningFlag")
              .requires(stack -> stack.hasPermission(2))
              .then(Commands.argument("doesrespawn", BoolArgumentType.bool())
                .executes(context ->
                {
                    final boolean shouldRespawn = BoolArgumentType.getBool(context, "doesrespawn");

                    final ServerLevel world = context.getSource().getLevel();
                    final Map<Structure, LongSet> structures = new HashMap<>();

                    final ChunkPos start = new ChunkPos(BlockPos.containing(context.getSource().getPosition()));

                    for (final Map.Entry<Structure, LongSet> entry : world.structureManager()
                      .getAllStructuresAt(new BlockPos((start.x) << 4, 0, (start.z) << 4))
                      .entrySet())
                    {
                        structures.computeIfAbsent(entry.getKey(), k -> new LongOpenHashSet(entry.getValue())).addAll(entry.getValue());
                    }

                    if (structures.isEmpty())
                    {
                        for (int x = -5; x < 5; x++)
                        {
                            for (int z = -5; z < 5; z++)
                            {
                                for (final Map.Entry<Structure, LongSet> entry : world.structureManager()
                                  .getAllStructuresAt(new BlockPos((start.x + x) << 4, 0, (start.z + z) << 4))
                                  .entrySet())
                                {
                                    structures.computeIfAbsent(entry.getKey(), k -> new LongOpenHashSet(entry.getValue())).addAll(entry.getValue());
                                }
                            }
                        }
                    }

                    Map<BlockPos, StructureStart> structurePositions = new HashMap<>();
                    for (Map.Entry<Structure, LongSet> structureEntry : structures.entrySet())
                    {
                        world.structureManager().fillStartsForStructure(structureEntry.getKey(), structureEntry.getValue(),
                          structureStart ->
                          {
                              structurePositions.put(structureStart.getBoundingBox().getCenter(), structureStart);
                          }
                        );
                    }

                    final List<Map.Entry<BlockPos, StructureStart>> sortedStructures = new ArrayList<>(structurePositions.entrySet());
                    sortedStructures.sort(Comparator.comparingDouble(p -> p.getKey().distSqr(BlockPos.containing(context.getSource().getPosition()))));

                    if (sortedStructures.isEmpty())
                    {
                        context.getSource()
                          .sendSystemMessage(Component.literal("No Structure nearby, anything visible nearby is not an actual structure but just a feature, those can't respawn")
                            .withStyle(ChatFormatting.RED));
                        return 1;
                    }

                    context.getSource()
                      .sendSystemMessage(Component.literal("Set respawn to " + shouldRespawn + " for structure: " +
                                                             context.getSource()
                                                               .registryAccess()
                                                               .registry(Registries.STRUCTURE)
                                                               .get()
                                                               .getKey(sortedStructures.get(0).getValue().getStructure()))
                        .append(Component.literal(" at: " + sortedStructures.get(0).getKey()).withStyle(ChatFormatting.YELLOW)
                          .withStyle(style ->
                                       style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                         "/tp " + sortedStructures.get(0).getKey().getX() + " " + sortedStructures.get(0).getKey().getY() + " " + sortedStructures.get(0)
                                           .getKey()
                                           .getZ()))
                          )));
                    RespawnManager.getForPos(world, sortedStructures.get(0).getValue().getBoundingBox().getCenter(), false).disabledRespawn = !shouldRespawn;
                    final RespawnLevelData respawnData =
                      context.getSource().getLevel().getDataStorage().computeIfAbsent(RespawnLevelData::load, RespawnLevelData::new, RespawnLevelData.ID);
                    if (respawnData != null)
                    {
                        respawnData.setDirty();
                    }

                    return 1;
                })
              ))
          .then(
            Commands.literal("listNearbyStructures")
              .requires(stack -> stack.hasPermission(2))
              .executes(context ->
              {
                  final ServerLevel world = context.getSource().getLevel();
                  final Map<Structure, LongSet> structures = new HashMap<>();

                  final ChunkPos start = new ChunkPos(BlockPos.containing(context.getSource().getPosition()));

                  for (final Map.Entry<Structure, LongSet> entry : world.structureManager()
                    .getAllStructuresAt(new BlockPos((start.x) << 4, 0, (start.z) << 4))
                    .entrySet())
                  {
                      structures.computeIfAbsent(entry.getKey(), k -> new LongOpenHashSet(entry.getValue())).addAll(entry.getValue());
                  }

                  if (structures.isEmpty())
                  {
                      for (int x = -5; x < 5; x++)
                      {
                          for (int z = -5; z < 5; z++)
                          {
                              for (final Map.Entry<Structure, LongSet> entry : world.structureManager()
                                .getAllStructuresAt(new BlockPos((start.x + x) << 4, 0, (start.z + z) << 4))
                                .entrySet())
                              {
                                  structures.computeIfAbsent(entry.getKey(), k -> new LongOpenHashSet(entry.getValue())).addAll(entry.getValue());
                              }
                          }
                      }
                  }

                  Map<BlockPos, StructureStart> structurePositions = new HashMap<>();
                  for (Map.Entry<Structure, LongSet> structureEntry : structures.entrySet())
                  {
                      world.structureManager().fillStartsForStructure(structureEntry.getKey(), structureEntry.getValue(),
                        structureStart ->
                        {
                            structurePositions.put(structureStart.getBoundingBox().getCenter(), structureStart);
                        }
                      );
                  }

                  final List<Map.Entry<BlockPos, StructureStart>> sortedStructures = new ArrayList<>(structurePositions.entrySet());
                  sortedStructures.sort(Comparator.comparingDouble(p -> p.getKey().distSqr(BlockPos.containing(context.getSource().getPosition()))));

                  if (sortedStructures.isEmpty())
                  {
                      context.getSource()
                        .sendSystemMessage(Component.literal("No Structure nearby, anything visible nearby is not an actual structure but just a feature, those can't respawn")
                          .withStyle(ChatFormatting.RED));
                      return 1;
                  }

                  context.getSource()
                    .sendSystemMessage(Component.literal("Structures: ").withStyle(ChatFormatting.BLUE));

                  for (final var entry : sortedStructures)
                  {
                      final StructureData data = RespawnManager.getForPos(context.getSource().getLevel(), entry.getKey(), false);

                      context.getSource()
                        .sendSystemMessage(Component.literal("" + data.id)
                          .append(Component.literal(" at: {" + entry.getKey().toShortString() + "}").withStyle(ChatFormatting.YELLOW)
                            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                              "/tp " + entry.getKey().getX() + " " + entry.getKey().getY() + " " + entry.getKey().getZ()))
                            )
                            .append(Component.literal(" {stats}").withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                              "/" + RespawningStructures.MOD_ID + " structureRespawnStatus " + entry.getKey().getX() + " " + entry.getKey().getY() + " " + entry.getKey()
                                .getZ()))))));
                  }
                  return 1;
              })
          )
          .then(
            Commands.literal("structureRespawnStatus")
              .requires(stack -> stack.hasPermission(2))
              .then(Commands.argument("position", BlockPosArgument.blockPos())
                .executes(context ->
                {
                    final BlockPos pos = BlockPosArgument.getBlockPos(context, "position");

                    final StructureData data = RespawnManager.getForPos(context.getSource().getLevel(), pos, false);

                    if (data == null)
                    {
                        context.getSource()
                          .sendSystemMessage(Component.literal("No structure at this position"));
                        return 1;
                    }

                    context.getSource()
                      .sendSystemMessage(Component.literal("Structure: " + data.id).withStyle(ChatFormatting.YELLOW));

                    if (RespawningStructures.config.getCommonConfig().blacklistedStructures.contains(data.id.toString()))
                    {
                        context.getSource()
                          .sendSystemMessage(Component.literal("Respawn is blacklisted in config").withStyle(ChatFormatting.RED));
                    }

                    if (!RespawningStructures.config.getCommonConfig().enableAutomaticRespawn)
                    {
                        context.getSource()
                          .sendSystemMessage(Component.literal("All respawn is disabled in config").withStyle(ChatFormatting.RED));
                    }

                    if (data.disabledRespawn)
                    {
                        context.getSource()
                          .sendSystemMessage(Component.literal("This structure is manually set to not respawn").withStyle(ChatFormatting.RED));
                    }

                    if (data.portalUsage > 0)
                    {
                        context.getSource()
                          .sendSystemMessage(Component.literal("Respawn disabled due to portal in structure").withStyle(ChatFormatting.RED));
                    }

                    if (data.blocksPlaced > 100 + (data.bbSize / 10000d) && (double) data.blocksBroken > 20 + (data.bbSize / 100000d))
                    {
                        context.getSource()
                          .sendSystemMessage(Component.literal("Respawn disabled due to large amount of broken and placed blocks, indicating a player built structure within.")
                            .withStyle(ChatFormatting.RED));
                    }

                    if (!data.checkStats())
                    {
                        context.getSource()
                          .sendSystemMessage(Component.literal("Too low usage to respawn").withStyle(ChatFormatting.RED));
                    }

                    int minutes_remaining = Math.max(0,
                      (int) (1 / 60d * (RespawningStructures.config.getCommonConfig().minutesUntilRespawn * 60L - (
                        context.getSource().getLevel().getDataStorage().computeIfAbsent(RespawnLevelData::load, RespawnLevelData::new, RespawnLevelData.ID).getLevelTime()
                          - data.lastActivity))));

                    context.getSource()
                      .sendSystemMessage(Component.literal("Remaining minutes until respawn: " + minutes_remaining)
                        .withStyle(minutes_remaining == 0 ? ChatFormatting.GREEN : ChatFormatting.RED));

                    context.getSource()
                      .sendSystemMessage(Component.literal("Statistics:").withStyle(ChatFormatting.YELLOW));
                    context.getSource()
                      .sendSystemMessage(Component.literal("Broken blocks: " + data.blocksBroken));
                    context.getSource()
                      .sendSystemMessage(Component.literal("Placed blocks: " + data.blocksPlaced));
                    context.getSource()
                      .sendSystemMessage(Component.literal("Portal usage: " + data.portalUsage));
                    context.getSource()
                      .sendSystemMessage(Component.literal("Containers looted: " + (data.containerLooted + data.dungeonContainerLooted)));
                    context.getSource()
                      .sendSystemMessage(Component.literal("Mobs killed: " + data.mobsKilled));
                    context.getSource()
                      .sendSystemMessage(Component.literal("Player deaths: " + data.playerDeaths));
                    context.getSource()
                      .sendSystemMessage(Component.literal("Spawners broken: " + data.spawnerBreak));

                    return 1;
                })))
          ;
    }
}
