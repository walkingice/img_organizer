// vim: et sw=4 sts=4 tabstop=4
package cc.jchu.imgorg.lib

import java.util.Date

class Media(
    @JvmField
    val data: String?,
    val dateInt: Long = -1
) {
    @JvmField
    val date: Date

    constructor(data: String?) : this(data, -1) {}

    init {
        date = Date(dateInt)
    }
}
