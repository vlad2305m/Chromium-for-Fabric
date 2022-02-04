package com.vlad2305m.chromiumforfabric.mixin.cef;

import net.minecraft.item.BookItem;
import org.cef.CefApp;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.SystemBootstrap;
import org.cef.handler.CefAppHandler;
import org.cef.handler.CefAppHandlerAdapter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CefApp.class)
public class CefAppMixin extends CefAppHandlerAdapter {

    @Shadow(remap = false) private static org.cef.CefApp.CefAppState state_;
    @Shadow(remap = false) private CefSettings settings_;
    @Shadow(remap = false) private static CefAppHandler appHandler_;

    public CefAppMixin(String[] args) {super(args);}

    //montoyo: Modified for MCEF (-awt, -swing)
    //@Inject(method = "<init>([Ljava/lang/String;Lorg/cef/CefSettings;)V", at = @At(value = "INVOKE_ASSIGN", target = "org/cef/handler/CefAppHandlerAdapter.<init> ([Ljava/lang/String;)V"))
    //private void initRewire(String[] args, CefSettings settings, CallbackInfo info) throws UnsatisfiedLinkError{
    //    if (settings != null) settings_ = settings.clone();
    //    if (OS.isWindows()) {
    //        SystemBootstrap.loadLibrary("jawt");
    //        SystemBootstrap.loadLibrary("chrome_elf");
    //        SystemBootstrap.loadLibrary("libcef");
//
    //        // Other platforms load this library in CefApp.startup().
    //        SystemBootstrap.loadLibrary("jcef");
    //    } else if (OS.isLinux()) {
    //        SystemBootstrap.loadLibrary("cef");
    //    }
    //    if (appHandler_ == null) {
    //        appHandler_ = this;
    //    }
//
    //    // Execute on the AWT event dispatching thread.
    //    try {
    //        if (!N_PreInitialize())
    //            throw new IllegalStateException("Failed to pre-initialize native code");
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //}

    @Mixin(SystemBootstrap.class)
    public static class BootStrapLoaderJammer {
        @Inject(method = "loadLibrary(Ljava/lang/String;)V", at = @At(value = "HEAD"), remap = false, cancellable = true, locals = LocalCapture.PRINT)
        private static void doNotLoadLibraries(String libname, CallbackInfo ci) {
            System.out.println("Hello mixins!");
            ci.cancel();
        }
    }
    @Mixin(BookItem.class)
    public static class DummyTest {
        public String hi = "hi";
    }

    @Redirect(method = "<init>([Ljava/lang/String;Lorg/cef/CefSettings;)V", at = @At(value = "INVOKE", target = "javax/swing/SwingUtilities.isEventDispatchThread ()Z"), remap = false)
    private static boolean yesRunNow0() { return true; }


    @Shadow(remap = false) private boolean N_PreInitialize() {return false;}

    //montoyo: Modified for MCEF (-swing)
    @Redirect(method = "initialize()V", at = @At(value = "INVOKE", target = "javax/swing/SwingUtilities.isEventDispatchThread ()Z"), remap = false)
    private static boolean yesRunNow() { return true; }
    @Redirect(method = "setState(Lorg/cef/CefApp$CefAppState;)V", at = @At(value = "INVOKE", target = "javax/swing/SwingUtilities.invokeLater (Ljava/lang/Runnable;)V"), remap = false)
    private static void runNow(Runnable r) { r.run(); }
    @Redirect(method = "handleBeforeTerminate()V", at = @At(value = "INVOKE", target = "javax/swing/SwingUtilities.invokeLater (Ljava/lang/Runnable;)V"), remap = false)
    private static void runNow3(Runnable r) { r.run(); }
    @Redirect(method = "shutdown()V", at = @At(value = "INVOKE", target = "javax/swing/SwingUtilities.invokeLater (Ljava/lang/Runnable;)V"), remap = false)
    private static void runNow4(Runnable r) { r.run(); } // //N_Shutdown(); ?

    //montoyo: Added for MCEF
    private static void forceShutdownState() {
        ////noinspection SynchronizeOnNonFinalField
        //synchronized (state_) {
            state_ = CefApp.CefAppState.SHUTTING_DOWN;
        //}
    }


    /**
     * @author vlad2305m
     * @reason null
     */
    @Overwrite(remap = false)
    public static final boolean startup(String[] args) {
        if (OS.isLinux() || OS.isMacintosh()) {
            //Modified by montoyo for MCEF
            //System.loadLibrary("jcef");
            return N_Startup(OS.isMacintosh() ? getCefFrameworkPath(args) : null);
        }
        return true;
    }

    @Shadow(remap = false) private @Final static native boolean N_Startup(String pathToCefFramework);

    @Shadow(remap = false) private static @Final String getCefFrameworkPath(String[] args) {return null;}

    //montoyo: modified for MCEF
    //public final native void N_Shutdown(); ???

}
