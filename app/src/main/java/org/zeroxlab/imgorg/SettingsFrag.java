// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFrag extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private Resources mRes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRes = getResources();

        addPreferencesFromResource(R.xml.settings);

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
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
        setPreferenceScreen(getPreferenceScreen());
    }

    @Override
    public void onActivityResult(int request, int result, Intent data) {
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        //getActivity().setResult();
    }
}
