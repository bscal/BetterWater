package me.bscal.betterwater.mixin;

import me.bscal.betterwater.BetterWater;
import me.bscal.betterwater.common.FluidEvents;
import me.bscal.betterwater.common.FluidPhysics;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlassBottleItem.class)
public abstract class GlassBottleItemMixin extends Item
{
    public GlassBottleItemMixin(Settings settings)
    {
        super(settings);
    }

    @Inject(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/GlassBottleItem;raycast(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/RaycastContext$FluidHandling;)Lnet/minecraft/util/hit/BlockHitResult;"),
            cancellable = true)
    public void Use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir)
    {
        int levelsNeeded = BetterWater.Settings().BottleWaterLevel;
        if (levelsNeeded <= 0) return;

        var ray = FluidPhysics.Raycast(world, user, RaycastContext.FluidHandling.WATER);
        if (ray.getType() != HitResult.Type.BLOCK) return;

        var blockPos = ray.getBlockPos();
        if (world.canPlayerModifyAt(user, blockPos) && FluidPhysics.TryRemoveLevels(world, blockPos, world.getBlockState(blockPos), levelsNeeded))
        {
            var itemStack = user.getStackInHand(hand);
            var outputStack = PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER);

            // NOTE maybe make cancellable?
            FluidEvents.FILL_BOTTLE.invoker().OnFill(world, user, itemStack, outputStack, false);

            user.incrementStat(Stats.USED.getOrCreateStat(this));
            world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0f, 1.0f);
            world.emitGameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
            ItemUsage.exchangeStack(itemStack, user, outputStack);
            cir.setReturnValue(TypedActionResult.success(outputStack, world.isClient()));
        }
    }

    @Inject(method = "fill", at = @At(value = "RETURN"))
    public void Fill(ItemStack stack, PlayerEntity player, ItemStack outputStack, CallbackInfoReturnable<ItemStack> cir)
    {
        // NOTE maybe make cancellable?
        FluidEvents.FILL_BOTTLE.invoker().OnFill(player.world, player, stack, outputStack, true);
    }
}
