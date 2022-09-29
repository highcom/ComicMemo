package com.highcom.comicmemo

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val mContext: Context, fm: FragmentManager?) :
    FragmentPagerAdapter(
        fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
    private val fragmentList: MutableList<Fragment>
    var currentFragment: Fragment? = null
        private set

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        val fragment: Fragment = PlaceholderFragment.Companion.newInstance(position)
        fragmentList.add(fragment)
        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mContext.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if (currentFragment !== `object`) {
            currentFragment = `object` as Fragment
        }
        super.setPrimaryItem(container, position, `object`)
    }

    val allFragment: List<Fragment>
        get() = fragmentList

    companion object {
        @StringRes
        private val TAB_TITLES = intArrayOf(R.string.tab_text_1, R.string.tab_text_2)
    }

    init {
        fragmentList = ArrayList()
    }
}