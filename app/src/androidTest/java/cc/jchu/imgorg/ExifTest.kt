package cc.jchu.imgorg

import android.content.Context
import cc.jchu.imgorg.lib.AndroidExif.getDate
import android.test.InstrumentationTestCase
import cc.jchu.imgorg.lib.AndroidExif
import android.content.res.AssetManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.ArrayList
import kotlin.Throws

class ExifTest : InstrumentationTestCase() {

    var mContext: Context? = null
    var withExif: File? = null
    var n5: File? = null
    var f1: File? = null
    var f2: File? = null
    var f3: File? = null
    var f4: File? = null
    var f5: File? = null
    var f6: File? = null
    var mOpened: MutableList<File?> = ArrayList()

    override fun setUp() {
        mContext = instrumentation.context
        withExif = copyFromAssetToTmp(mContext, "with_exif.jpg", "with_exif", "jpg")
        n5 = copyFromAssetToTmp(mContext, "DEF_Camera-N5.jpg", "n5_def_camera", "jpg")
        f1 = copyFromAssetToTmp(mContext, "FV5-N5-Test_EXIF.jpg", "fv1", "jpg")
        f2 = copyFromAssetToTmp(mContext, "FV5-N5-Test_EXIF_only.jpg", "fv2", "jpg")
        f3 = copyFromAssetToTmp(mContext, "FV5-N5-Test_EXIF_XMP.jpg", "fv3", "jpg")
        f4 = copyFromAssetToTmp(mContext, "FV5-N5-Test_MetaMode1.jpg", "fv4", "jpg")
        f5 = copyFromAssetToTmp(mContext, "FV5-N5-Test_MetaMode2.jpg", "fv5", "jpg")
        f6 = copyFromAssetToTmp(mContext, "FV5-N5-Test_MetaMode3.jpg", "fv6", "jpg")
        mOpened.add(withExif)
        mOpened.add(n5)
        mOpened.add(f1)
        mOpened.add(f2)
        mOpened.add(f3)
        mOpened.add(f4)
        mOpened.add(f5)
        mOpened.add(f6)
    }

    override fun tearDown() {
        for (opened in mOpened) {
            opened?.delete()
        }
        mOpened.clear()
    }

    fun testCorrectExif() {
        val format = SimpleDateFormat("yyyy-MM")
        try {
            val date = getDate(withExif!!, format)
            assertEquals("2015-10", date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun testCameraFV5Pics() {
        val format = SimpleDateFormat("yyyy-MM")
        try {
            // only these two mode could find EXIF data via Android API
            assertEquals("2015-11", getDate(f4!!, format))
            assertEquals("2015-11", getDate(f6!!, format))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /* We want File object for testing. Copy data content from assets to tmp file */
    private fun copyFromAssetToTmp(
        ctx: Context?,
        assetPath: String,
        tmpName: String,
        suffix: String
    ): File? {
        val res = ctx!!.resources
        val asset = res.assets
        var tmp: File? = null
        try {
            val dir = ctx.cacheDir
            val `in` = asset.open(assetPath)
            tmp = File.createTempFile(tmpName, suffix, dir)
            val out = FileOutputStream(tmp)
            copy(`in`, out)
            `in`.close()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return tmp
    }

    @Throws(IOException::class)
    private fun copy(`in`: InputStream, out: OutputStream) {
        val size = 0x1000
        val buffer = ByteArray(size)
        var len: Int
        while (`in`.read(buffer).also { len = it } != -1) {
            out.write(buffer, 0, len)
        }
    }
}
