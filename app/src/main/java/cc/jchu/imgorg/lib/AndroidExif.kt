// vim: et sw=4 sts=4 tabstop=4
package cc.jchu.imgorg.lib

import android.media.ExifInterface
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat

object AndroidExif {

    private val sParser = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")

    @JvmStatic
    @Throws(IOException::class, ParseException::class)
    fun getDate(jpgImg: File, sdf: SimpleDateFormat): String {
        val ei = ExifInterface(jpgImg.path)
        val datetime = ei.getAttribute(ExifInterface.TAG_DATETIME)
        return if (datetime != null) {
            sdf.format(sParser.parse(datetime))
        } else {
            throw ParseException("No exif time", 0)
        }
    }
}
