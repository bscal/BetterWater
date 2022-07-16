package me.bscal.betterwater.mixin;

import me.bscal.betterwater.FluidPhysics;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlassBottleItem.class)
public abstract class GlassBottleItemMixin extends Item
{
    @Shadow
    protected abstract ItemStack fill(ItemStack stack, PlayerEntity player, ItemStack outputStack);

    public GlassBottleItemMixin(Settings settings)
    {
        super(settings);
    }

    @Inject(method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/GlassBottleItem;raycast(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/RaycastContext$FluidHandling;)Lnet/minecraft/util/hit/BlockHitResult;"),
            cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir)
    {
        var ray = FluidPhysics.Raycast(world, user, RaycastContext.FluidHandling.WATER);
        // COMMENT - returning here will continue defaults use method. This will send another ray.
        if (ray.getType() == HitResult.Type.MISS || ray.getType() != HitResult.Type.BLOCK) return;

        var blockPos = ray.getBlockPos();
        if (!world.canPlayerModifyAt(user, blockPos)) return;

        var itemStack = user.getStackInHand(hand);
        if (FluidPhysics.TryRemoveLevels(world, blockPos, world.getBlockState(blockPos), 2))
        {
            world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0f, 1.0f);
            world.emitGameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
            // TODO handle water quality or an event?
            cir.setReturnValue(TypedActionResult.success(this.fill(itemStack, user, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER)), world.isClient()));
        }
    }
}
