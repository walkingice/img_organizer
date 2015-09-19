// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

public class SettingsFrag extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private Context mCtx;

    private final static int CHOOSE_FROM = 0x01;
    private final static int CHOOSE_TO = 0x02;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        setCallbacks();
        resetSummaries();
    }

    private void setCallbacks() {
        Preference prefFrom = findPreference(getString(R.string.key_from_dir));
        prefFrom.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String from = getStrValue(R.string.key_from_dir);
                launchChooser(CHOOSE_FROM, from);
                return true;
            }
        });
        Preference prefTo = findPreference(getString(R.string.key_to_dir));
        prefTo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String to = getStrValue(R.string.key_from_dir);
                launchChooser(CHOOSE_TO, to);
                return true;
            }
        });
    }

    private void launchChooser(int type, String path) {
        final Intent chooserIntent = new Intent(mCtx, DirectoryChooserActivity.class);
        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .allowReadOnlyDirectory(true)
                .newDirectoryName("new_dir")
                .allowNewDirectoryNameModification(false)
                .initialDirectory(path)
                .build();

        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
        startActivityForResult(chooserIntent, type);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCtx = getActivity();
        setPreferenceScreen(getPreferenceScreen());
    }

    @Override
    public void onActivityResult(int request, int result, Intent data) {
        if (result == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
            String path = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
            if (request == CHOOSE_FROM || request == CHOOSE_TO) {
                onDirChosen(request, path);
            }
        }
    }

    private void onDirChosen(int reqCode, String path) {
        if (CHOOSE_FROM == reqCode) {
            setStrValue(R.string.key_from_dir, path);
        } else {
            setStrValue(R.string.key_to_dir, path);
        }
    }

    private boolean setStrValue(int resKey, String val) {
        String key = getString(resKey);
        return setStrValue(key, val);
    }

    private boolean setStrValue(String key, String val) {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, val);
        return editor.commit();
    }

    private String getStrValue(int resKey) {
        String key = getString(resKey);
        return getStrValue(key);
    }

    private String getStrValue(String key) {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        return prefs.getString(key, "");
    }

    private void resetSummaries() {
        String keyFrom = getString(R.string.key_from_dir);
        String keyTo = getString(R.string.key_to_dir);
        String keyMax = getString(R.string.key_maximum);
        findPreference(keyFrom).setSummary(getStrValue(keyFrom));
        findPreference(keyTo).setSummary(getStrValue(keyTo));
        findPreference(keyMax).setSummary(getStrValue(keyMax));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        resetSummaries();
    }
}
