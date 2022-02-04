package com.vlad2305m.chromiumforfabric.mixin.cef;

import com.jogamp.opengl.GL2;
import com.mojang.blaze3d.platform.GlStateManager;
import com.vlad2305m.chromiumforfabric.ceffix.CefRendererAccess;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
//import org.cef.browser.CefRenderer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.EXTBGRA;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.nio.ByteBuffer;

import static com.vlad2305m.chromiumforfabric.ChromiumForFabricMod.LOGGER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;

@Mixin(targets = "org/cef/browser/CefRenderer")
public class CefRendererMixin implements CefRendererAccess {

    //montoyo: debug tool
    //private static final ArrayList<Integer> GL_TEXTURES = new ArrayList<>();
    //public static void dumpVRAMLeak() {
    //    LOGGER.info(">>>>> MCEF: Beginning VRAM leak report");
    //    GL_TEXTURES.forEach(tex -> LOGGER.warn(">>>>> MCEF: This texture has not been freed: " + tex));
    //    LOGGER.info(">>>>> MCEF: End of VRAM leak report");
    //}

    @Shadow(remap = false) private boolean transparent_;
    @Shadow(remap = false) private int[] texture_id_;
    @Shadow(remap = false) private int view_width_;
    @Shadow(remap = false) private int view_height_;
    @Shadow(remap = false) private Rectangle popup_rect_;

    @Inject(method = "<init>(Z)V", at = @At("RETURN"), remap = false)
    protected void onInit(boolean transparent, CallbackInfo ci) {
        initialize(null);
    }

    /**
     * @author montoyo
     * @reason null
     */
    @Overwrite(remap = false)
    protected void initialize(@Nullable GL2 gl2) {
        assert (gl2 == null);
        GlStateManager._enableTexture();
        texture_id_[0] = glGenTextures();

        //if(MCEF.CHECK_VRAM_LEAK)
        //    GL_TEXTURES.add(texture_id_[0]);

        GlStateManager._bindTexture(texture_id_[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        GlStateManager._bindTexture(0);
    }

    /**
     * @author montoyo
     * @reason null
     */
    @Overwrite(remap = false)
    protected void cleanup(@Nullable GL2 gl2) {
        assert (gl2 == null);
        if(texture_id_[0] != 0) {
            //if(MCEF.CHECK_VRAM_LEAK)
            //    GL_TEXTURES.remove((Object) texture_id_[0]);

            glDeleteTextures(texture_id_[0]);
        }
    }

    /**
     * @author montoyo
     * @reason null
     */
    @Overwrite(remap = false)
    public void render(@Nullable GL2 gl2) {
        assert (gl2 == null);
        render(0, 0, view_width_, view_height_); // ???
    }

    public void render(double x1, double y1, double x2, double y2) {
        if(view_width_ == 0 || view_height_ == 0)
            return;

        Tessellator t = Tessellator.getInstance();
        BufferBuilder vb = t.getBuffer();

        GlStateManager._bindTexture(texture_id_[0]);
        vb.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        vb.vertex(x1, y1, 0).texture(0, 1).color(255, 255, 255, 255).next();
        vb.vertex(x2, y1, 0).texture(1, 1).color(255, 255, 255, 255).next();
        vb.vertex(x2, y2, 0).texture(1, 0).color(255, 255, 255, 255).next();
        vb.vertex(x1, y2, 0).texture(0, 0).color(255, 255, 255, 255).next();
        t.draw();
        GlStateManager._bindTexture(0);
    }

    /**
     * @author montoyo
     * @reason null
     */
    @Overwrite(remap = false)
    public void onPaint(@Nullable GL2 gl2, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height) {
        assert (gl2 == null);
        onPaint(popup, dirtyRects, buffer, width, height, false);
    }

    public void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender) {
        if(transparent_) // Enable alpha blending.
            GlStateManager._enableBlend();

        final int size = (width * height) << 2;
        if(size > buffer.limit()) {
            LOGGER.warn("Bad data passed to CefRenderer.onPaint() triggered safe guards... (1)");
            return;
        }

        // Enable 2D textures.
        GlStateManager._enableTexture();
        GlStateManager._bindTexture(texture_id_[0]);

        int oldAlignement = glGetInteger(GL_UNPACK_ALIGNMENT);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        if(!popup) {
            if(completeReRender || width != view_width_ || height != view_height_) {
                // Update/resize the whole texture.
                view_width_ = width;
                view_height_ = height;
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, view_width_, view_height_, 0, EXTBGRA.GL_BGRA_EXT, GL_UNSIGNED_BYTE, buffer);
            } else {
                glPixelStorei(GL_UNPACK_ROW_LENGTH, view_width_);

                // Update just the dirty rectangles.
                for(Rectangle rect: dirtyRects) {
                    if(rect.x < 0 || rect.y < 0 || rect.x + rect.width > view_width_ || rect.y + rect.height > view_height_)
                        LOGGER.warn("Bad data passed to CefRenderer.onPaint() triggered safe guards... (2)");
                    else {
                        glPixelStorei(GL_UNPACK_SKIP_PIXELS, rect.x);
                        glPixelStorei(GL_UNPACK_SKIP_ROWS, rect.y);
                        glTexSubImage2D(GL_TEXTURE_2D, 0, rect.x, rect.y, rect.width, rect.height, EXTBGRA.GL_BGRA_EXT, GL_UNSIGNED_BYTE, buffer);
                    }
                }

                glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
                glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
                glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            }
        } else if(popup_rect_.width > 0 && popup_rect_.height > 0) {
            int skip_pixels = 0, x = popup_rect_.x;
            int skip_rows = 0, y = popup_rect_.y;
            int w = width;
            int h = height;

            // Adjust the popup to fit inside the view.
            if(x < 0) {
                skip_pixels = -x;
                x = 0;
            }
            if(y < 0) {
                skip_rows = -y;
                y = 0;
            }
            if(x + w > view_width_)
                w -= x + w - view_width_;
            if(y + h > view_height_)
                h -= y + h - view_height_;

            // Update the popup rectangle.
            glPixelStorei(GL_UNPACK_ROW_LENGTH, width);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, skip_pixels);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, skip_rows);
            glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, w, h, EXTBGRA.GL_BGRA_EXT, GL_UNSIGNED_BYTE, buffer);
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        }

        glPixelStorei(GL_UNPACK_ALIGNMENT, oldAlignement);
        GlStateManager._bindTexture(0);
    }

    public int getViewWidth() {
        return view_width_;
    }

    public int getViewHeight() {
        return view_height_;
    }
}
