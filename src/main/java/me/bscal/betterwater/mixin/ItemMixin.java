package me.bscal.betterwater.mixin;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemConvertible, FabricItem
{
    @Shadow
    protected abstract boolean isIn(ItemGroup group);

    @Inject(method = "appendTooltip(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/item/TooltipContext;)V", at = @At(value = "TAIL"))
    public void AppendTooltipMixin(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci)
    {
    }

    @Inject(method = "appendStacks", at = @At(value = "TAIL"))
    public void AppendStacksMixin(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo ci)
    {
    }
}
