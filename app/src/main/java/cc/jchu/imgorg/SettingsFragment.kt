// vim: et sw=4 sts=4 tabstop=4
package cc.jchu.imgorg

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceFragment
import net.rdrei.android.dirchooser.DirectoryChooserActivity
import net.rdrei.android.dirchooser.DirectoryChooserConfig

class SettingsFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {

    private var mCtx: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)
        preferenceScreen.sharedPreferences
            .registerOnSharedPreferenceChangeListener(this)
        setCallbacks()
        resetSummaries()
    }

    private fun setCallbacks() {
        val prefFrom = findPreference(getString(R.string.key_from_dir))
        prefFrom.onPreferenceClickListener = OnPreferenceClickListener {
            val from = getStrValue(R.string.key_from_dir)
            launchChooser(CHOOSE_FROM, from)
            true
        }
        val prefTo = findPreference(getString(R.string.key_to_dir))
        prefTo.onPreferenceClickListener = OnPreferenceClickListener {
            val to = getStrValue(R.string.key_from_dir)
            launchChooser(CHOOSE_TO, to)
            true
        }
    }

    private fun launchChooser(type: Int, path: String) {
        val chooserIntent = Intent(mCtx, DirectoryChooserActivity::class.java)
        val config = DirectoryChooserConfig.builder()
            .allowReadOnlyDirectory(true)
            .newDirectoryName("Organized")
            .allowNewDirectoryNameModification(true)
            .initialDirectory(path)
            .build()
        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config)
        startActivityForResult(chooserIntent, type)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        mCtx = activity
        preferenceScreen = preferenceScreen
    }

    override fun onActivityResult(request: Int, result: Int, data: Intent) {
        if (result == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
            val path = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR)
            if (request == CHOOSE_FROM || request == CHOOSE_TO) {
                onDirChosen(request, path)
            }
        }
    }

    private fun onDirChosen(reqCode: Int, path: String) {
        if (CHOOSE_FROM == reqCode) {
            setStrValue(R.string.key_from_dir, path)
        } else {
            setStrValue(R.string.key_to_dir, path)
        }
    }

    private fun setStrValue(resKey: Int, `val`: String): Boolean {
        val key = getString(resKey)
        return setStrValue(key, `val`)
    }

    private fun setStrValue(key: String, `val`: String): Boolean {
        val prefs = preferenceManager.sharedPreferences
        val editor = prefs.edit()
        editor.putString(key, `val`)
        return editor.commit()
    }

    private fun getStrValue(resKey: Int): String {
        val key = getString(resKey)
        return getStrValue(key)
    }

    private fun getStrValue(key: String): String {
        val prefs = preferenceManager.sharedPreferences
        return prefs.getString(key, "") ?: ""
    }

    private fun resetSummaries() {
        val keyFrom = getString(R.string.key_from_dir)
        val keyTo = getString(R.string.key_to_dir)
        val keyMax = getString(R.string.key_maximum)
        findPreference(keyFrom).summary = getStrValue(keyFrom)
        findPreference(keyTo).summary = getStrValue(keyTo)
        findPreference(keyMax).summary = getStrValue(keyMax)
    }

    override fun onSharedPreferenceChanged(pref: SharedPreferences, key: String) {
        resetSummaries()
    }

    companion object {
        private const val CHOOSE_FROM = 0x01
        private const val CHOOSE_TO = 0x02
    }
}
