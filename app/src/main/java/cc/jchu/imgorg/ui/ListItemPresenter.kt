package cc.jchu.imgorg.ui

import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cc.jchu.imgorg.R
import cc.jchu.imgorg.lib.Operation
import cc.jchu.imgorg.ui.SelectorAdapter.Presenter
import java.io.File

class ListItemPresenter : Presenter<Operation> {

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder? {
        val inflater = LayoutInflater.from(parent?.context)
        val viewGroup = inflater.inflate(R.layout.view_list_item, parent, false) as ViewGroup
        return InnerViewHolder(viewGroup)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Operation) {
        val holder = viewHolder as InnerViewHolder
        val src = File(item.source)
        holder.iText1.text = src.name
        holder.iText2.text = item.destination
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
    }

    internal inner class InnerViewHolder(view: View) : ViewHolder(view) {

        var iText1: TextView
        var iText2: TextView

        init {
            iText1 = view.findViewById(android.R.id.text1) as TextView
            iText2 = view.findViewById(android.R.id.text2) as TextView
        }
    }
}
