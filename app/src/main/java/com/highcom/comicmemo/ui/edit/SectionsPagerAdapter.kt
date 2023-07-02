package com.highcom.comicmemo.ui.edit

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * タブレイアウトのセクションページアダプタ
 *
 * @property activity アクティビティ
 */
class SectionsPagerAdapter(private val activity: ComicMemoActivity) : FragmentStateAdapter(activity) {
    val allFragment: List<Fragment>
        get() = fragmentList
    private val fragmentList: MutableList<Fragment>
    var currentFragment: Fragment? = null
        private set

    interface SectionPagerAdapterListener {
        fun notifyChangeCurrentFragment()
    }

    /**
     * カレントのFragment取得処理
     *
     * @param position 現在のタブ位置
     * @return 巻数データ一覧のフラグメント
     */
    override fun createFragment(position: Int): Fragment {
        val fragment: Fragment = PlaceholderFragment.newInstance(position)
        fragmentList.add(fragment)
        // アクティブになる時に初めてインスタンスが生成されるのでカレントは必ず更新
        currentFragment = fragment
        return fragment
    }

    /**
     * セクションページ数取得処理
     *
     * @return セクションページ数
     */
    override fun getItemCount(): Int {
        return 2
    }

    /**
     * アクティブフラグメント設定処理
     *
     * @param fragment
     */
    fun setCurrentFragment(fragment: Fragment?) {
        currentFragment = fragment
        activity.notifyChangeCurrentFragment()
    }

    init {
        fragmentList = ArrayList()
    }
}