package me.bscal.betterwater.common;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public interface FluidEvents
{

    Event<FillBottleEvent> FILL_BOTTLE = EventFactory.createArrayBacked(FillBottleEvent.class,
            (listeners) -> (world, player, emptyBottleStack, filledBottleStack, isSrcBlock) ->
            {
                for (FillBottleEvent listener : listeners)
                    listener.OnFill(world, player, emptyBottleStack, filledBottleStack, isSrcBlock);
                return ActionResult.SUCCESS;
            });

    interface FillBottleEvent
    {
        ActionResult OnFill(World world, PlayerEntity player, ItemStack emptyBottleStack, ItemStack filledBottleStack, boolean isSrcBlock);
    }

}
