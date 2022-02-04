package com.vlad2305m.chromiumforfabric.mixin.cef;

import me.friwi.jcefmaven.impl.step.init.CefInitializer;
import org.cef.SystemBootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CefInitializer.class)
public class CefInitializerMixin {
    @Redirect(method = "initialize(Ljava/io/File;Ljava/util/List;Lorg/cef/CefSettings;)Lorg/cef/CefApp;", at = @At(value = "INVOKE", target = "org/cef/SystemBootstrap.setLoader (Lorg/cef/SystemBootstrap$Loader;)V"), remap = false)
    private static void keepLoader(SystemBootstrap.Loader loader) {}
}
