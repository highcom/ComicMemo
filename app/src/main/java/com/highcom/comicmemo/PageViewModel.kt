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
import android.view.Display
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
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.ImageButton
import android.util.TypedValue
import android.view.WindowManager
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.annotation.SuppressLint
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
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View.OnTouchListener
import kotlin.jvm.Synchronized
import android.graphics.RectF
import android.content.res.TypedArray

class PageViewModel : ViewModel() {
    private val listDataManager: ListDataManager? = ListDataManager.Companion.getInstance()
    private var mListData: MutableLiveData<List<Map<String?, String?>?>?>? = null
    fun getListData(index: Long): LiveData<List<Map<String?, String?>?>?> {
        if (mListData == null) {
            mListData = MutableLiveData()
        }
        mListData!!.setValue(listDataManager!!.getDataList(index))
        return mListData
    }

    fun setData(index: Long, isEdit: Boolean, data: Map<String, String>) {
        listDataManager!!.setData(isEdit, data)
        mListData!!.value = listDataManager.getDataList(index)
    }

    fun updateData(index: Long) {
        mListData!!.value = listDataManager!!.getDataList(index)
    }

    fun deleteData(index: Long, id: String?) {
        listDataManager!!.deleteData(id)
        mListData!!.value = listDataManager.getDataList(index)
    }

    fun sortData(index: Long, key: String) {
        listDataManager!!.sortListData(index, key)
        mListData!!.value = listDataManager.getDataList(index)
    }

    fun closeData() {
        listDataManager!!.closeData()
    }

    fun rearrangeData(dataIndex: Long, fromPos: Int, toPos: Int) {
        listDataManager!!.rearrangeData(dataIndex, fromPos, toPos)
    }
}