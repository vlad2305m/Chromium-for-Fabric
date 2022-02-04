package com.vlad2305m.chromiumforfabric;

import me.friwi.jcefmaven.*;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.SystemBootstrap;
import org.cef.browser.CefBrowser;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static me.friwi.jcefmaven.EnumPlatform.PROPERTY_OS_ARCH;
import static me.friwi.jcefmaven.EnumPlatform.PROPERTY_OS_NAME;

public class ChromiumForFabricMod implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("ChromiumForFabric");

	public static boolean initialised = false;
	public static CefApp CEFapp;
	private static final String ChromiumPath = "ChromiumForFabric";

	@Override
	public void onInitializeClient() {
		// However, some things (like resources) may still be uninitialized.

		//LOGGER.warn("Mixin env:"+ MixinEnvironment.getCurrentEnvironment().getActiveTransformer().);



		LOGGER.info("Initializing Chromium (jcef)");
		SystemBootstrap.loadLibrary("test");
		CefAppBuilder builder = new CefAppBuilder(); //Create a new CefAppBuilder instance

		//Configure the builder instance
		builder.setInstallDir(new File(ChromiumPath+"/install/jcef-bundle"));
		builder.setProgressHandler(new ConsoleProgressHandler()); //Default
		//builder.addJCefArgs("--disable-gpu"); //Just an example
		CefSettings cefSettings = builder.getCefSettings();
		cefSettings.cache_path = new File(ChromiumPath).getAbsolutePath()+"/cache";

		builder.setAppHandler(new MavenCefAppHandlerAdapter(){
			@Override
			public void stateHasChanged(CefApp.CefAppState state) {
				// Shutdown the app if the native CEF part is terminated
				if (state == CefApp.CefAppState.TERMINATED) System.exit(0);
			}
		});
		try {
			//EnumPlatform platform = EnumPlatform.getCurrentPlatform();
			//EnumOS os = platform.getOs();

			CEFapp = builder.build(); //Build a CefApp instance using the configuration above
			initialised = true;
		} catch (IOException e) {
			LOGGER.error("Chromium installation file IO error:");
			e.printStackTrace();
		} catch (UnsupportedPlatformException e) {
			LOGGER.error(System.getProperty(PROPERTY_OS_NAME)+"("+System.getProperty(PROPERTY_OS_ARCH)+") is not supported by jcef-maven");
			e.printStackTrace();
		} catch (InterruptedException e) {
			LOGGER.error("Installation interrupted:");
			e.printStackTrace();
		} catch (CefInitializationException e) {
			LOGGER.error("Chromium embedded framework failed:");
			e.printStackTrace();
		}

		//CefClient client = CEFapp.createClient();
		//CefBrowser browser = client.createBrowser("google.com", true, true);


	}
}
