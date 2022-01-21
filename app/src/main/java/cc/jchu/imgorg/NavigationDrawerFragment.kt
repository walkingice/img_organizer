package cc.jchu.imgorg

import cc.jchu.imgorg.NavigationDrawerFragment.NavigationDrawerCallbacks
import android.support.v4.widget.DrawerLayout
import android.os.Bundle
import android.content.SharedPreferences
import android.preference.PreferenceManager
import cc.jchu.imgorg.NavigationDrawerFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import cc.jchu.imgorg.R
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.support.v4.view.GravityCompat
import android.app.Activity
import android.content.res.Configuration
import android.support.v4.app.ActionBarDrawerToggle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.view.MenuInflater
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import java.lang.ClassCastException

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the [
 * design guidelines](https://developer.android.com/design/patterns/navigation-drawer.html#Interaction) for a complete explanation of the behaviors implemented here.
 */
class NavigationDrawerFragment : Fragment() {
    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private var mCallbacks: NavigationDrawerCallbacks? = null

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var mDrawerListView: ListView? = null
    private var mFragmentContainerView: View? = null
    private var mCurrentSelectedPosition = 0
    private var mFromSavedInstanceState = false
    private var mUserLearnedDrawer = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false)
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION)
            mFromSavedInstanceState = true
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mDrawerListView = inflater.inflate(
            R.layout.fragment_navigation_drawer, container, false
        ) as ListView
        mDrawerListView!!.onItemClickListener =
            OnItemClickListener { parent, view, position, id -> selectItem(position) }
        mDrawerListView!!.adapter = ArrayAdapter(
            actionBar!!.themedContext,
            android.R.layout.simple_list_item_activated_1,
            android.R.id.text1, arrayOf(
                getString(R.string.title_section0),
                getString(R.string.title_section1)
            )
        )
        mDrawerListView!!.setItemChecked(mCurrentSelectedPosition, true)
        return mDrawerListView
    }

    val isDrawerOpen: Boolean
        get() = mDrawerLayout != null && mDrawerLayout!!.isDrawerOpen(mFragmentContainerView)

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    fun setUp(fragmentId: Int, drawerLayout: DrawerLayout?) {
        mFragmentContainerView = activity.findViewById(fragmentId)
        mDrawerLayout = drawerLayout

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout!!.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
        // set up the drawer's list view with items and click listener
        val actionBar = actionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = object : ActionBarDrawerToggle(
            activity,  /* host Activity */
            mDrawerLayout,  /* DrawerLayout object */
            R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
            R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
            R.string.navigation_drawer_close /* "close drawer" description for accessibility */
        ) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                if (!isAdded) {
                    return
                }
                activity.supportInvalidateOptionsMenu() // calls onPrepareOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                if (!isAdded) {
                    return
                }
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true
                    val sp = PreferenceManager
                        .getDefaultSharedPreferences(activity)
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply()
                }
                activity.supportInvalidateOptionsMenu() // calls onPrepareOptionsMenu()
            }
        }

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout!!.openDrawer(mFragmentContainerView)
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout!!.post { mDrawerToggle!!.syncState() }
        mDrawerLayout!!.setDrawerListener(mDrawerToggle)
    }

    private fun selectItem(position: Int) {
        mCurrentSelectedPosition = position
        if (mDrawerListView != null) {
            mDrawerListView!!.setItemChecked(position, true)
        }
        if (mDrawerLayout != null) {
            mDrawerLayout!!.closeDrawer(mFragmentContainerView)
        }
        if (mCallbacks != null) {
            mCallbacks!!.onNavigationDrawerItemSelected(position)
        }
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        mCallbacks = try {
            activity as NavigationDrawerCallbacks
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement NavigationDrawerCallbacks.")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mCallbacks = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen) {
            inflater.inflate(R.menu.global, menu)
            showGlobalContextActionBar()
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (mDrawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)

        /*
        if (item.getItemId() == R.id.action_example) {
            Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT).show();
            return true;
        }
        */
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private fun showGlobalContextActionBar() {
        val actionBar = actionBar
        actionBar!!.setDisplayShowTitleEnabled(true)
        actionBar.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD
        actionBar.setTitle(R.string.app_name)
    }

    private val actionBar: ActionBar?
        private get() = (activity as AppCompatActivity).supportActionBar

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        fun onNavigationDrawerItemSelected(position: Int)
    }

    companion object {
        /**
         * Remember the position of the selected item.
         */
        private const val STATE_SELECTED_POSITION = "selected_navigation_drawer_position"

        /**
         * Per the design guidelines, you should show the drawer on launch until the user manually
         * expands it. This shared preference tracks this.
         */
        private const val PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned"
    }
}
