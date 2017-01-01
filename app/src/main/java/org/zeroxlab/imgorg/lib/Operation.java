// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg.lib;

public interface Operation {
    void consume();

    boolean isFinished();

    String getSource();

    String getDestination();
}
