// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg.lib;

import android.content.Context;

public interface Operation {
    void consume(Context ctx);

    boolean isFinished();

    String getSource();

    String getDestination();
}
