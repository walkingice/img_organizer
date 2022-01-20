// vim: et sw=4 sts=4 tabstop=4
package cc.jchu.imgorg

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class MainFragment : Fragment(), View.OnClickListener {

    private var mCtx: Context? = null
    private var mRes: Resources? = null
    private var mFromDir: TextView? = null
    private var mToDir: TextView? = null
    private var mBtnAnalysis: View? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mCtx = activity
        mRes = resources
        Log.d(ImgOrg.TAG, "MainFrag onCreate")
    }

    override fun onResume() {
        super.onResume()
        initPreferences()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.main_btn_analysis) {
            val i = Intent(mCtx, AnalysisActivity::class.java)
            startActivity(i)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(v: View, state: Bundle?) {
        super.onViewCreated(v, state)
        val root = view
        mFromDir = root!!.findViewById(R.id.main_from_path) as TextView
        mToDir = root.findViewById(R.id.main_to_path) as TextView
        mBtnAnalysis = root.findViewById(R.id.main_btn_analysis)
        mBtnAnalysis!!.setOnClickListener(this)
        val writePermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (writePermission == PackageManager.PERMISSION_GRANTED) {
            mBtnAnalysis!!.setEnabled(true)
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQ_CODE_WRITE_EXTERNAL
            )
        }
    }

    override fun onRequestPermissionsResult(
        reqCode: Int,
        permissions: Array<String>,
        response: IntArray
    ) {
        if (reqCode == REQ_CODE_WRITE_EXTERNAL
            && response[0] == PackageManager.PERMISSION_GRANTED
        ) {
            mBtnAnalysis!!.isEnabled = true
        }
    }

    private fun initPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val keyMax = mRes!!.getString(R.string.key_maximum)
        val keyVideo = mRes!!.getString(R.string.key_handle_video)
        val keyFrom = mRes!!.getString(R.string.key_from_dir)
        val keyTo = mRes!!.getString(R.string.key_to_dir)
        val keyMockOption = mRes!!.getString(R.string.key_use_mock_operation)
        val max = prefs.getString(keyMax, ImgOrg.DEF_MAX)?.toInt() ?: 100
        val handleVideo = prefs.getBoolean(keyVideo, ImgOrg.DEF_HANDLE_VIDEO)
        val mockOption = prefs.getBoolean(keyMockOption, false)
        val from = prefs.getString(keyFrom, ImgOrg.DEF_FROM.path)
        val to = prefs.getString(keyTo, ImgOrg.DEF_TO.path)
        mFromDir!!.text = from
        mToDir!!.text = to
        val editor = prefs.edit()
        editor.putBoolean(keyVideo, handleVideo)
        editor.putString(keyMax, "" + max)
        editor.putString(keyFrom, from)
        editor.putString(keyTo, to)
        editor.putBoolean(keyMockOption, mockOption)
        editor.commit()
    }

    companion object {
        private const val REQ_CODE_WRITE_EXTERNAL = 0x42
    }
}
