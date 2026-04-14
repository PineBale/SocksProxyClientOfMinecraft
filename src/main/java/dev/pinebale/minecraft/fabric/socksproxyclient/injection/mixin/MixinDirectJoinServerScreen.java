package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import dev.pinebale.minecraft.fabric.socksproxyclient.injection.ProxyMixinUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DirectJoinServerScreen.class)
@Environment(EnvType.CLIENT)
public abstract class MixinDirectJoinServerScreen extends Screen {
    /**
     * It's a pointer to new object.
     */
    @Shadow
    @Final
    private ServerData serverData;

    protected MixinDirectJoinServerScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void initInjected(CallbackInfo ci) {
        this.addRenderableWidget(ProxyMixinUtils.createOptoutButton(this, this.serverData, this.width / 2 - 60, this.height / 4 + 72 + 12, 120, 20));
    }
}
