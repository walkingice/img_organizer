// vim: et sw=4 sts=4 tabstop=4
package cc.jchu.imgorg.lib

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import rx.Observable
import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.util.Arrays
import java.util.Locale

object Organizer {
    private val EXTENSIONS = arrayOf(
        "gif", "png", "bmp", "jpg", "jpeg", "mp4"
    )
    private val EXTENSIONS_EXCLUDE_VIDEO = arrayOf(
        "gif", "png", "bmp", "jpg", "jpeg"
    )
    private val FILTER = FilenameFilter { _, name ->
        for (ext in EXTENSIONS) {
            if (name.lowercase(Locale.getDefault()).endsWith(".$ext")) {
                return@FilenameFilter true
            }
        }
        false
    }
    private val FILTER_NO_VID = FilenameFilter { _, name ->
        for (ext in EXTENSIONS_EXCLUDE_VIDEO) {
            if (name.lowercase(Locale.getDefault()).endsWith(".$ext")) {
                return@FilenameFilter true
            }
        }
        false
    }

    @Throws(IOException::class)
    private fun ensureExists(file: File) {
        if (!file.exists()) {
            throw IOException(String.format("[%s] does not exists", file.path))
        }
    }

    @Throws(IOException::class)
    private fun ensureDir(dir: File) {
        ensureExists(dir)
        if (!dir.isDirectory) {
            throw IOException(String.format("[%s] is not a directory", dir.path))
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun findMedias(
        ctx: Context,
        fromPath: String,
        maximum: Int,
        video: Boolean,
        mockOption: Boolean
    ): List<Media> {
        //return findFileMedias(fromPath, maximum, video);
        return if (mockOption) {
            createMockMedias(maximum)
        } else {
            findDbMedias(ctx, fromPath, maximum)
        }
    }

    private fun createMockMedias(maximum: Int): List<Media> {
        val list: MutableList<Media> = ArrayList()
        for (i in 0 until maximum) {
            val filename = String.format("/sdcard/DCIM/Camera/%d.jpg", i)
            // start from 2017/01/25
            list.add(Media(filename, 1485385400000L + i * 10000000))
        }
        return list
    }

    private fun findDbMedias(ctx: Context, fromPath: String, maximum: Int): List<Media> {
        val cursor = queryDb(ctx, fromPath)
        var result: MutableList<Media> = ArrayList(cursor.count)
        if (cursor.moveToFirst()) {
            val dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            do {
                val data = cursor.getString(dataIdx)
                val date = cursor.getLong(dateIdx)
                result.add(Media(data, date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        val max = if (result.size > maximum) maximum else result.size
        result = result.subList(0, max)
        return result
    }

    private fun queryDb(ctx: Context, fromPath: String): Cursor {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN
        )
        val selection = MediaStore.Images.Media.BUCKET_ID + " = ?"
        val CAMERA_IMAGE_BUCKET_ID = getBucketId(fromPath)
        val selectionArgs = arrayOf(CAMERA_IMAGE_BUCKET_ID)
        val cursor = ctx.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        return cursor!!
    }

    private fun getBucketId(path: String): String {
        return path.lowercase(Locale.getDefault()).hashCode().toString()
    }

    @Throws(IOException::class)
    private fun findFileMedias(fromPath: String, maximum: Int, video: Boolean): List<Media> {
        val dir = File(fromPath)
        ensureDir(dir)
        // FIXME: currently, always ignore mp4 video files since I do not want to get EXIF from it
        val found = if (video) dir.listFiles(FILTER) else dir.listFiles(FILTER_NO_VID)
        val list = Arrays.asList(*found)
        val max = Math.min(maximum, list.size)
        list.subList(0, max)
        val medias: MutableList<Media> = ArrayList()
        Observable.from(list).subscribe { file -> medias.add(Media(file.absolutePath)) }
        return medias
    }

    @JvmStatic
    fun createOperation(media: Media?, destPath: String?, mockOption: Boolean): Operation {
        return if (mockOption) {
            MockOperation(media!!, destPath!!)
        } else {
            StoreOperation(media!!, destPath!!)
        }
    }

    private fun getStringPaths(ops: List<Operation>): Array<String?> {
        val paths: MutableList<String?> = ArrayList(ops.size)
        for (op in ops) {
            if (op.isFinished) {
                paths.add(op.destination)
            }
        }
        return paths.toTypedArray()
    }
}
