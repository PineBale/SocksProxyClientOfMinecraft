package dev.pinebale.minecraft.fabric.socksproxyclient.injection;

import com.google.common.collect.ImmutableSet;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.FabricModConflictList;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Don't forget to check mixins.json
 */
@Environment(EnvType.CLIENT)
public final class BaseMixinPlugin implements IMixinConfigPlugin {

    private static final Set<String> VIAFABRICPLUS_DISMISS = ImmutableSet.of(
        "dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin.MixinServerStatusPingerNoLegacyPing"
    );

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean ret = true;
        if (FabricModConflictList.viafabricplus.get() && VIAFABRICPLUS_DISMISS.contains(mixinClassName)) {
            LogUtils.logWarning("Dismiss Mixin {} because of ViaFabricPlus", mixinClassName);
            ret = false;
        }
        return ret;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
