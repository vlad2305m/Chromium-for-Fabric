package com.vlad2305m.chromiumforfabric.ceffix;

import java.awt.*;
import java.nio.ByteBuffer;

public class PaintData {
    public ByteBuffer buffer;
    public int width;
    public int height;
    public Rectangle[] dirtyRects;
    public boolean hasFrame;
    public boolean fullReRender;
}
