package com.highcom.comicmemo

import androidx.fragment.app.FragmentActivity
import com.highcom.comicmemo.ListDataManager
import com.highcom.comicmemo.SectionsPagerAdapter
import com.google.firebase.analytics.FirebaseAnalytics
import android.widget.FrameLayout
import com.google.android.gms.ads.AdView
import android.os.Bundle
import com.highcom.comicmemo.R
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.RequestConfiguration
import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater
import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater.ShowRateDialogCondition
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.highcom.comicmemo.PlaceholderFragment
import android.content.Intent
import com.highcom.comicmemo.InputMemo
import android.util.DisplayMetrics
import android.app.Activity
import android.widget.CompoundButton
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import com.highcom.comicmemo.ListDataOpenHelper
import com.highcom.comicmemo.ListViewAdapter.AdapterListener
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.ImageButton
import android.util.TypedValue
import android.graphics.drawable.ColorDrawable
import android.annotation.SuppressLint
import android.content.Context
import android.view.View.OnLongClickListener
import android.widget.Filter.FilterResults
import android.database.sqlite.SQLiteOpenHelper
import com.highcom.comicmemo.SimpleCallbackHelper.SimpleCallbackListener
import com.highcom.comicmemo.PageViewModel
import com.highcom.comicmemo.ListViewAdapter
import com.highcom.comicmemo.SimpleCallbackHelper
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.highcom.comicmemo.SimpleCallbackHelper.UnderlayButton
import com.highcom.comicmemo.SimpleCallbackHelper.UnderlayButtonClickListener
import android.text.TextUtils
import androidx.fragment.app.FragmentPagerAdapter
import androidx.annotation.StringRes
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import kotlin.jvm.Synchronized
import android.graphics.RectF
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.*

class DividerItemDecoration(context: Context?, orientation: Int) : ItemDecoration() {
    private val mDivider: Drawable?
    private var mOrientation = 0
    fun setOrientation(orientation: Int) {
        require(!(orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST)) { "invalid orientation" }
        mOrientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    fun drawVertical(c: Canvas?, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                .layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c!!)
        }
    }

    fun drawHorizontal(c: Canvas?, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                .layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin
            val right = left + mDivider!!.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c!!)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (mOrientation == VERTICAL_LIST) {
            outRect[0, 0, 0] = mDivider!!.intrinsicHeight
        } else {
            outRect[0, 0, mDivider!!.intrinsicWidth] = 0
        }
    }

    companion object {
        private val ATTRS = intArrayOf(
            android.R.attr.listDivider
        )
        const val HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL
        const val VERTICAL_LIST = LinearLayoutManager.VERTICAL
    }

    init {
        val a = context!!.obtainStyledAttributes(ATTRS)
        mDivider = a.getDrawable(0)
        a.recycle()
        setOrientation(orientation)
    }
}