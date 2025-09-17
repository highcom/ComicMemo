package com.highcom.comicmemo.ui.edit

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.highcom.comicmemo.R

/**
 * ViewPager2 用セクションページアダプタ
 *
 * @property mContext コンテキスト
 * @property fragment 親フラグメント
 */
class SectionsPagerAdapter(
    private val mContext: Context,
    private val fragment: ComicMemoFragment
) : FragmentStateAdapter(fragment) {

    private val fragmentList = mutableListOf<Fragment>()

    override fun getItemCount(): Int = TAB_TITLES.size

    override fun createFragment(position: Int): Fragment {
        // 生成時にリストへ格納（※ ViewPager2 は必要に応じて破棄するので注意）
        val fragment = PlaceholderFragment.newInstance(position, fragment)
        if (fragmentList.size <= position) {
            fragmentList.add(fragment)
        } else {
            fragmentList[position] = fragment
        }
        return fragment
    }

    /**
     * セクションページのタイトル取得処理
     *
     * @param position セクションページ位置
     * @return タイトル名
     */
    fun getPageTitle(position: Int): CharSequence {
        return mContext.getString(TAB_TITLES[position])
    }

    companion object {
        @StringRes
        private val TAB_TITLES = intArrayOf(R.string.tab_text_1, R.string.tab_text_2)
    }
}