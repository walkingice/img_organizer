// vim: et sw=4 sts=4 tabstop=4
package cc.jchu.imgorg.lib

import android.content.Context
import cc.jchu.imgorg.lib.AndroidExif.getDate
import cc.jchu.imgorg.lib.AndroidExif
import cc.jchu.imgorg.lib.FileOperation
import java.io.File
import java.io.IOException
import java.lang.StringBuilder
import java.text.ParseException
import java.text.SimpleDateFormat

class FileOperation(private val mFrom: File, destDir: File) : Operation {
    // true, if this file should be moved
    var isPending = false
        private set
    override var isFinished = false
        private set
    private var mTo: File? = null
    private val mContainerPath: String
    override var source = ""
    override var destination = ""
        private set

    constructor(media: Media, destDir: String?) : this(File(media.data), File(destDir)) {}

    override fun consume(ctx: Context?) {
        if (!isPending) {
            return
        }
        val parent = mTo!!.parentFile
        if (!parent.exists()) {
            parent.mkdirs()
        }
        if (!parent.isDirectory) {
            return
        }
        mFrom.renameTo(mTo)
        isFinished = true
    }

    private fun probe(): Boolean {
        // to see its type and destination file existence.
        // if it is img or film, find its create date and create dest dir
        // use EXIF to create dest filename
        try {
            val time = getDate(mFrom, sSDF)
            mTo = File(String.format("%s/%s/%s", mContainerPath, time, mFrom.name))
            destination = mTo!!.absolutePath
            isPending = !mTo!!.exists()
        } catch (e: IOException) {
            isPending = false
        } catch (e: ParseException) {
            isPending = false
        }
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("from: " + mFrom.path)
        if (mTo != null) {
            sb.append(" to: " + mTo!!.path)
        }
        return sb.toString()
    }

    companion object {
        private val sSDF = SimpleDateFormat("yyyy-MM")
    }

    init {
        source = mFrom.absolutePath
        mContainerPath = destDir.absolutePath
        probe()
    }
}
