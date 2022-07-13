package me.bscal.betterwater.mixin;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterFluid.class)
public abstract class WaterFluidMixin extends FlowableFluid
{

    @Inject(method = "isInfinite", at = @At(value = "HEAD"), cancellable = true)
    public void isInfinite(CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(false);
    }

}
