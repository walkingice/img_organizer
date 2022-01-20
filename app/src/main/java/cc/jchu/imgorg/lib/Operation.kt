// vim: et sw=4 sts=4 tabstop=4
package cc.jchu.imgorg.lib

import android.content.Context

interface Operation {
    fun consume(ctx: Context?)
    val isFinished: Boolean
    val source: String?
    val destination: String?
}
