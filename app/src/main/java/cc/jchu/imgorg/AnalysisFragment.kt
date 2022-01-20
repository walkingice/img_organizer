// vim: et sw=4 sts=4 tabstop=4
package cc.jchu.imgorg

import android.app.Fragment
import android.app.ProgressDialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import cc.jchu.imgorg.lib.Operation
import cc.jchu.imgorg.lib.Organizer.createOperation
import cc.jchu.imgorg.lib.Organizer.findMedias
import cc.jchu.imgorg.ui.ListItemPresenter
import cc.jchu.imgorg.ui.SelectorAdapter
import cc.jchu.imgorg.ui.SelectorAdapter.Presenter
import cc.jchu.imgorg.ui.SelectorAdapter.PresenterSelector
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.exceptions.Exceptions
import rx.functions.Func1
import rx.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

class AnalysisFragment : Fragment(), View.OnClickListener {
    private var mRes: Resources? = null
    private var mResults: RecyclerView? = null
    private var mFromDir: TextView? = null
    private var mOrganize: Button? = null
    private var mAdapter: SelectorAdapter<Operation>? = null
    private var mMax = ImgOrg.DEF_MAX.toInt()
    private var mMockOption = false
    private var mHandleVideo = ImgOrg.DEF_HANDLE_VIDEO
    private var mFromPath: String? = null
    private var mToPath: String? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mRes = resources
    }

    override fun onResume() {
        super.onResume()
        readPreferences()
        createOptions()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_analysis, container, false)
        mResults = root.findViewById(R.id.analy_results) as RecyclerView
        mFromDir = root.findViewById(R.id.analy_from_directory) as TextView
        mAdapter = SelectorAdapter(object : PresenterSelector<Operation> {
            var presenter: Presenter<Operation> = ListItemPresenter()
            override fun getPresenter(type: SelectorAdapter.Type): Presenter<Operation> {
                return presenter
            }
        })
        mResults!!.adapter = mAdapter
        mOrganize = root.findViewById(R.id.btn_organize) as Button
        mOrganize!!.setOnClickListener(this)
        return root
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_organize) {
            consumeOperations()
        }
    }

    private fun readPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val keyMax = mRes!!.getString(R.string.key_maximum)
        val keyHandleVideo = mRes!!.getString(R.string.key_handle_video)
        val keyFrom = mRes!!.getString(R.string.key_from_dir)
        val keyTo = mRes!!.getString(R.string.key_to_dir)
        val keyMockOption = mRes!!.getString(R.string.key_use_mock_operation)
        mMax = prefs.getString(keyMax, mMax.toString() + "")?.toInt() ?: 100
        mMockOption = prefs.getBoolean(keyMockOption, false)
        mHandleVideo = prefs.getBoolean(keyHandleVideo, ImgOrg.DEF_HANDLE_VIDEO)
        mFromPath = prefs.getString(keyFrom, ImgOrg.DEF_FROM.path)
        mToPath = prefs.getString(keyTo, ImgOrg.DEF_TO.path)
        mFromDir!!.text = mFromPath
    }

    private fun createOptions() {
        val dialog = ProgressDialog(this.activity)
        dialog.setMessage("Parsing...")
        dialog.setCancelable(false)
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog.show()
        Observable.just<Any?>(null)
            .subscribeOn(Schedulers.newThread())
            .flatMap(Func1 {
                try {
                    val medias = findMedias(
                        activity,
                        mFromPath!!,
                        mMax,
                        mHandleVideo,
                        mMockOption
                    )
                    dialog.max = medias.size
                    return@Func1 Observable.from(medias)
                } catch (e: IOException) {
                    throw Exceptions.propagate(e)
                }
            })
            .flatMap { media ->
                Observable.just(
                    createOperation(
                        media,
                        mToPath,
                        mMockOption
                    )
                )
            } //.take(3)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Operation>() {
                override fun onCompleted() {
                    mAdapter!!.notifyDataSetChanged()
                    dialog.cancel()
                    Log.d(ImgOrg.TAG, "Done")
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    dialog.cancel()
                }

                override fun onNext(op: Operation) {
                    mAdapter!!.addItem(op, SelectorAdapter.Type.A)
                    dialog.progress = mAdapter!!.itemCount
                }
            })
    }

    private fun consumeOperations() {
        if (mAdapter!!.itemCount == 0) {
            return
        }
        val dialog = ProgressDialog(this.activity)
        dialog.setMessage("Moving...")
        dialog.setCancelable(true)
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        val ctx: Context = activity
        val total = mAdapter!!.itemCount
        val subscription = Observable.just(total)
            .observeOn(Schedulers.newThread())
            .concatMap { size ->
                dialog.max = size
                val list: MutableList<Operation> = ArrayList()
                for (i in 0 until size) {
                    list.add(mAdapter!!.getItem(i))
                }
                Observable.from(list)
            }
            .concatMap { op ->
                op.consume(ctx)
                Observable.just(op)
                    .delay(100, TimeUnit.MILLISECONDS) // just for testing
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Operation>() {
                override fun onCompleted() {
                    mAdapter!!.notifyDataSetChanged()
                    dialog.cancel()
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    dialog.cancel()
                }

                override fun onNext(op: Operation) {
                    mAdapter!!.remove(op)
                    dialog.progress = total - mAdapter!!.itemCount
                }
            })
        dialog.setOnCancelListener {
            subscription.unsubscribe()
            mAdapter!!.notifyDataSetChanged()
        }
        dialog.show()
    }
}
