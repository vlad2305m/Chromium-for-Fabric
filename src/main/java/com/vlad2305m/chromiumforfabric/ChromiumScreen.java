package com.vlad2305m.chromiumforfabric;

import com.mojang.blaze3d.platform.GlStateManager;
import com.vlad2305m.chromiumforfabric.ceffix.CefBrowserOsrAccess;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;

import static com.vlad2305m.chromiumforfabric.ChromiumForFabricMod.CEFapp;

public class ChromiumScreen extends Screen {

    final CefClient client;
    CefBrowser browser = null;

    public ChromiumScreen() {
        super(new LiteralText(""));
        if (!ChromiumForFabricMod.initialised) { client = null; return; }
        client = CEFapp.createClient();
        browser = client.createBrowser("https://www.google.com", true, true);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        if (client != null) {
            GlStateManager._disableDepthTest();
            GlStateManager._enableTexture();
            ((CefBrowserOsrAccess)browser).draw(0, 0, 600, 400);
            GlStateManager._enableDepthTest();
        }

    }

}
