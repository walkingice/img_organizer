package cc.jchu.imgorg.ui

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.ViewGroup

/**
 * A RecyclerView Adapter, to use PresenterSelector to decide corresponding presenter for specific
 * data item. When adding any item into this adapter, we should specify a type at the same time.
 *
 *
 * This Adapter is similar with ArrayObjectAdapter, but simpler. Instead of using 'instanceof'
 * operator, this Adapter use enum Type to get better performance.
 */
class SelectorAdapter<T> : RecyclerView.Adapter<ViewHolder?> {
    private val mSelector: PresenterSelector<T>
    private val mData: MutableList<T> = ArrayList()
    private val mTypes: MutableList<Type> = ArrayList()
    private val mTypesArray = Type.values()

    constructor(map: Map<Type?, Presenter<T>>) {
        mSelector = DefaultSelector(map)
    }

    constructor(selector: PresenterSelector<T>) {
        mSelector = selector
    }

    /**
     * Add data item into this adapter. Caller should also specify type for that item.
     *
     * @param obj  Data item to be added
     * @param type Which type of this item.
     */
    fun addItem(obj: T, type: Type) {
        mData.add(obj)
        mTypes.add(type)
    }

    fun replace(location: Int, obj: T, type: Type) {
        mData[location] = obj
        mTypes[location] = type
        notifyItemChanged(location)
    }

    fun remove(obj: T): Boolean {
        val idx = mData.indexOf(obj)
        return if (idx != -1) {
            mData.removeAt(idx)
            mTypes.removeAt(idx)
            true
        } else {
            false
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mTypes[position].ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val type = mTypesArray[viewType]
        val presenter = mSelector.getPresenter(type)
        return presenter.onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val type = mTypes[position]
        val data = mData[position]
        val presenter = mSelector.getPresenter(type)
        presenter.onBindViewHolder(holder, data)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun getItem(i: Int): T {
        return mData[i]
    }

    /**
     * Pre-defined types, to be used when adding items.
     */
    enum class Type {
        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O
    }

    interface Presenter<T> {
        fun onCreateViewHolder(parent: ViewGroup?): ViewHolder?
        fun onBindViewHolder(viewHolder: ViewHolder?, item: T)
        fun onUnbindViewHolder(viewHolder: ViewHolder?)
    }

    /**
     * A presenter selector which be used by SelectorAdapter
     */
    interface PresenterSelector<T> {
        /**
         * To return a presenter for specific Type.
         *
         * @param type
         * @return A presenter for corresponding Type
         */
        fun getPresenter(type: Type): Presenter<T>
    }

    private inner class DefaultSelector(
        map: Map<Type?, Presenter<T>>
    ) : PresenterSelector<T> {
        var mMap: MutableMap<Type?, Presenter<T>> = HashMap()

        init {
            mMap.putAll(map)
        }

        override fun getPresenter(type: Type): Presenter<T> {
            return mMap[type]!!
        }
    }
}
