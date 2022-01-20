// vim: et sw=4 sts=4 tabstop=4
package cc.jchu.imgorg.lib

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import cc.jchu.imgorg.lib.StoreOperation
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class StoreOperation internal constructor(source: Media, dest: String) : Operation {

    override val source: String?
    override val destination: String
    private val mDate: Date?
    override var isFinished = false
        private set

    init {
        this.source = source.data
        mDate = source.date
        val dstDate = sSDF.format(mDate)
        val src = File(this.source)
        val dst = File(dest + "/" + dstDate + "/" + src.name)
        destination = dst.absolutePath
    }

    override fun consume(ctx: Context?) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DATA, destination)
        val rows = ctx!!.contentResolver.update(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values,
            MediaStore.Images.Media.DATA + "='" + source + "'",
            null
        )
        if (rows == 1) {
            val from = File(source)
            val to = File(destination)
            val parent = to.parentFile
            if (!parent.exists()) {
                parent.mkdirs()
            }
            from.renameTo(to)
        }
        isFinished = true
    }

    companion object {
        private val sSDF = SimpleDateFormat("yyyy-MM")
    }
}
