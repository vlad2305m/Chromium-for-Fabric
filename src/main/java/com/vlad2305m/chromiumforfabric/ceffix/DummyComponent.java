//Created by montoyo for MCEF

package com.vlad2305m.chromiumforfabric.ceffix;

import java.awt.Component;
import java.awt.Point;

public class DummyComponent extends Component {

    @Override
    public Point getLocationOnScreen() {
        return new Point(0, 0);
    }

}
