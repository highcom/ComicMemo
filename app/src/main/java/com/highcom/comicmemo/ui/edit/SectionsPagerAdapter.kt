package com.highcom.comicmemo.ui.edit

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.highcom.comicmemo.R

/**
 * タブレイアウトのセクションページアダプタ
 *
 * @property mContext コンテキスト
 *
 * @param fm フラグメントマネージャ
 */
class SectionsPagerAdapter(private val mContext: Context, private val activity: ComicMemoActivity, fm: FragmentManager?) :
    FragmentPagerAdapter(
        fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
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
    override fun getItem(position: Int): Fragment {
        val fragment: Fragment = PlaceholderFragment.newInstance(position, activity)
        fragmentList.add(fragment)
        return fragment
    }

    /**
     * セクションページのタイトル取得処理
     *
     * @param position セクションページ位置
     * @return タイトル名
     */
    override fun getPageTitle(position: Int): CharSequence {
        return mContext.resources.getString(TAB_TITLES[position])
    }

    /**
     * セクションページ数取得処理
     *
     * @return セクションページ数
     */
    override fun getCount(): Int {
        return 2
    }

    /**
     * 有効なセクションページの設定処理
     *
     * @param container コンテナ
     * @param position セクションページ位置
     * @param object セクションページフラグメント
     */
    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if (currentFragment !== `object`) {
            currentFragment = `object` as Fragment
        }
        super.setPrimaryItem(container, position, `object`)
    }

    override fun finishUpdate(container: ViewGroup) {
        super.finishUpdate(container)
        // 初期化が終わった後から通知を始める
        activity.notifyChangeCurrentFragment()
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