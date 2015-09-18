// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg;

import android.app.Activity;
import android.os.Bundle;


public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFrag())
                .commit();
    }
}
