// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg.lib;

import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public final class AndroidExif {

    private final static SimpleDateFormat sParser = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    public final static String getDate(File jpgImg, SimpleDateFormat sdf) throws IOException, ParseException {
        ExifInterface ei = new ExifInterface(jpgImg.getPath());
        String datetime = ei.getAttribute(ExifInterface.TAG_DATETIME);
        if (datetime != null) {
            return sdf.format(sParser.parse(datetime));
        } else {
            throw new ParseException("No exif time", 0);
        }
    }
}
