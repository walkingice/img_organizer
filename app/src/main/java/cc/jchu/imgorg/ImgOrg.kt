// vim: et sw=4 sts=4 tabstop=4
package cc.jchu.imgorg

import android.os.Environment
import java.io.File

object ImgOrg {
    const val TAG = "ImgOrg"
    val DEF_DIR = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DCIM
    )

    // how many files could be processed at one time
    const val DEF_HANDLE_VIDEO = false
    const val DEF_MAX = "500"

    @JvmField
    val DEF_FROM = File(DEF_DIR.path + "/Camera")

    @JvmField
    val DEF_TO = File(DEF_DIR.path + "/Organized")
}
