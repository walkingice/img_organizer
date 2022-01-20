package cc.jchu.imgorg.lib

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date

class MockOperation internal constructor(private val mMedia: Media, dest: String) : Operation {
    override val destination: String
    override var isFinished = false
        private set

    override fun consume(ctx: Context?) {
        isFinished = true
        Log.d("Mock", "consumed operation")
    }

    override val source: String?
        get() = mMedia.data

    companion object {
        private val sSDF = SimpleDateFormat("yyyy-MM")
    }

    init {
        val d = mMedia.date ?: Date()
        val dstDate = sSDF.format(d)
        destination = "$dest/$dstDate"
    }
}
