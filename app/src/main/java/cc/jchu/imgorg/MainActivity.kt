package cc.jchu.imgorg

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import cc.jchu.imgorg.NavigationDrawerFragment.NavigationDrawerCallbacks

class MainActivity : AppCompatActivity(), NavigationDrawerCallbacks {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private var mNavigationDrawerFragment: NavigationDrawerFragment? = null

    /**
     * Used to store the last screen title. For use in [.restoreActionBar].
     */
    private var mTitle: CharSequence? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mNavigationDrawerFragment = supportFragmentManager.findFragmentById(R.id.navigation_drawer)
            as? NavigationDrawerFragment

        mTitle = title

        // Set up the drawer.
        mNavigationDrawerFragment!!.setUp(
            R.id.navigation_drawer,
            findViewById(R.id.drawer_layout) as DrawerLayout
        )
    }

    override fun onAttachFragment(fragment: Fragment) {
        val bundle = fragment.arguments
            ?: // in initialization
            return
        val number = bundle.getInt(PlaceholderFragment.ARG_SECTION_NUMBER, 0)
        when (number) {
            0 -> mTitle = getString(R.string.title_section0)
            1 -> mTitle = getString(R.string.title_section1)
        }
    }

    override fun onNavigationDrawerItemSelected(position: Int) {
        // update the main content by replacing fragments
        val fragmentManager = supportFragmentManager
        var fragment: Fragment? = null
        if (position == 0) {
            fragment = MainFragment()
            val b = Bundle()
            // FIXME: we should get rid of place-holder-fragment
            b.putInt(PlaceholderFragment.ARG_SECTION_NUMBER, 0)
            fragment.setArguments(b)
        } else {
            fragment = PlaceholderFragment.newInstance(position)
        }
        fragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun restoreActionBar() {
        val actionBar = supportActionBar
        actionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD
        actionBar.setDisplayShowTitleEnabled(true)
        actionBar.title = mTitle
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!mNavigationDrawerFragment!!.isDrawerOpen) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            menuInflater.inflate(R.menu.main, menu)
            restoreActionBar()
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
