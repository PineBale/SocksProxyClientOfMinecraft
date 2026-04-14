package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.HttpUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.Proxy;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;proxy:Ljava/net/Proxy;", opcode = Opcodes.GETFIELD))
    private Proxy redirectedGet(Minecraft instance) {
        return HttpUtils.getProxyObject();
    }
}
