// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg.lib;

import java.util.Date;

public class Media {
    final public String data;
    final public Date date;

    public Media(String data) {
        this(data, -1);
    }

    Media(String data, long dateInt) {
        this.data = data;
        this.date = new Date(dateInt);
    }
}
