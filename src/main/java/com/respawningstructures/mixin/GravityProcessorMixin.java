package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GravityProcessor.class)
public class GravityProcessorMixin
{
    @Shadow
    @Final
    private int offset;

    @Inject(method = "processBlock", at = @At("RETURN"), cancellable = true)
    private void checkAndAdjustRespawn(
        final LevelReader levelReader,
        final BlockPos p_74110_,
        final BlockPos p_74111_,
        final BlockPos templateRelativePos,
        final StructureTemplate.StructureBlockInfo relativeInfo,
        final StructurePlaceSettings settings,
        final CallbackInfoReturnable<StructureTemplate.StructureBlockInfo> cir)
    {
        StructureTemplate.StructureBlockInfo result = cir.getReturnValue();

        if (result == null || RespawnManager.respawnInProgress == null || !Thread.currentThread()
            .getName()
            .toLowerCase()
            .contains("server"))
        {
            return;
        }

        if (!result.state().isAir() && offset < 0)
        {
            boolean existsBelow = false;

            for (int i = 0; i < 7; i++)
            {
                final BlockState prevState = levelReader.getBlockState(result.pos().below(relativeInfo.pos().getY() + i));
                if (prevState.getBlock() == result.state().getBlock())
                {
                    existsBelow = true;
                    break;
                }
            }

            if (existsBelow)
            {
                int relativeY = relativeInfo.pos().getY();
                if (RespawnManager.heightMap != null)
                {
                    int res = RespawnManager.heightMap.getInt(new BlockPos(relativeInfo.pos().getX(), 0, relativeInfo.pos().getZ()));
                    if (res > 0)
                    {
                        relativeY = res;
                    }
                }

                BlockPos fixedpos =
                    new BlockPos(result.pos().getX(),
                        Math.min(p_74111_.getY() + 10, Math.max(p_74110_.getY() - 10, result.pos().getY() - (relativeY))),
                        result.pos().getZ());

                if (result.state().is(BlockTags.CROPS))
                {
                    // Adjust crops one lower to replace themselves
                    if (levelReader.getBlockState(fixedpos.below()).is(BlockTags.CROPS))
                    {
                        //fixedpos = fixedpos.below();
                    }

                    if (!(levelReader.getBlockState(fixedpos.below()).is(Blocks.FARMLAND) || levelReader.getBlockState(fixedpos.below()).getBlock() instanceof FarmlandBlock
                        || levelReader.getBlockState(fixedpos.below()).is(BlockTags.DIRT)))
                    {
                        cir.setReturnValue(null);
                        return;
                    }
                }

                cir.setReturnValue(new StructureTemplate.StructureBlockInfo(fixedpos, result.state(), result.nbt()));
            }
        }

        // Do no place ontop of leaves
        if (levelReader.getBlockState(cir.getReturnValue().pos().below()).is(BlockTags.LEAVES))
        {
            cir.setReturnValue(null);
            return;
        }

        // Place below crops instead of within
        if (!result.state().is(BlockTags.CROPS) && levelReader.getBlockState(result.pos()).is(BlockTags.CROPS))
        {
            cir.setReturnValue(new StructureTemplate.StructureBlockInfo(result.pos().below(), result.state(), result.nbt()));
        }

        final int y = result.pos().getY();

        // Limit y changes
        if (y > p_74110_.getY() + 10 || y < p_74110_.getY() - 10)
        {
            int relativeY = relativeInfo.pos().getY();
            if (RespawnManager.heightMap != null)
            {
                int res = RespawnManager.heightMap.getInt(new BlockPos(relativeInfo.pos().getX(), 0, relativeInfo.pos().getZ()));
                if (res > 0)
                {
                    relativeY = res;
                }
            }

            BlockPos fixedpos =
                new BlockPos(result.pos().getX(),
                    Math.min(p_74111_.getY() + 10, Math.max(p_74110_.getY() - 10, result.pos().getY() - relativeY)),
                    result.pos().getZ());
            cir.setReturnValue(new StructureTemplate.StructureBlockInfo(fixedpos, result.state(), result.nbt()));
        }
    }
}
