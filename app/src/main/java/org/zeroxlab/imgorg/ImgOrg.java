// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg;

import java.io.File;

import android.os.Environment;

public final class ImgOrg {
    public final static String TAG = "ImgOrg";

    public final static File DEF_DIR =
            Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM);

    public final static File DEF_FROM = new File(DEF_DIR.getPath() + "/Camera");
    public final static File DEF_TO = new File(DEF_DIR.getPath() + "/Organized");
}
