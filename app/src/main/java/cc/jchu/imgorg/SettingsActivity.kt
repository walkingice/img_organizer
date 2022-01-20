// vim: et sw=4 sts=4 tabstop=4
package cc.jchu.imgorg

import android.R
import android.app.Activity
import android.os.Bundle

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Display the fragment as the main content.
        fragmentManager.beginTransaction()
            .replace(R.id.content, SettingsFragment())
            .commit()
    }
}
