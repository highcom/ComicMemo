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
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View.OnTouchListener
import kotlin.jvm.Synchronized
import android.graphics.RectF
import android.content.res.TypedArray
import android.database.Cursor
import java.util.*

class ListDataManager private constructor(context: Context) {
    private val rdb: SQLiteDatabase
    private val wdb: SQLiteDatabase
    private var data: MutableMap<String, String>? = null
    private val dataList1: MutableList<Map<String, String>>
    private val dataList2: MutableList<Map<String, String>>
    private var sortKey1 = "id"
    private var sortKey2 = "id"
    var lastUpdateId = 0
    fun setData(isEdit: Boolean, data: Map<String, String>) {
        // データベースに追加or編集する
        val values = ContentValues()
        values.put("id", java.lang.Long.valueOf(data["id"]).toLong())
        values.put("title", data["title"])
        values.put("author", data["author"])
        values.put("number", data["number"])
        values.put("memo", data["memo"])
        values.put("inputdate", data["inputdate"])
        values.put("status", data["status"])
        if (isEdit) {
            // 編集の場合
            wdb.update("comicdata", values, "id=?", arrayOf(data["id"]))
        } else {
            // 新規作成の場合
            wdb.insert("comicdata", data["id"], values)
        }
        remakeAllListData()
    }

    fun deleteData(id: String?) {
        // データベースから削除する
        wdb.delete("comicdata", "id=?", arrayOf(id))
        remakeAllListData()
    }

    fun getDataList(dataIndex: Long): List<Map<String, String>> {
        val dataList: List<Map<String, String>>
        dataList = if (dataIndex == 0L) {
            dataList1
        } else {
            dataList2
        }
        return dataList
    }

    fun rearrangeData(dataIndex: Long, fromPos: Int, toPos: Int) {
        var mov: Boolean
        var values: ContentValues
        val cur = getCurrentCursor(dataIndex)
        mov = cur.moveToPosition(fromPos)
        if (!mov) return
        val fromId = cur.getLong(0)
        mov = cur.moveToPosition(toPos)
        if (!mov) return
        val toId = cur.getLong(0)
        values = ContentValues()
        values.put("id", -1)
        wdb.update("comicdata", values, "id=?", arrayOf(java.lang.Long.toString(fromId)))
        values = ContentValues()
        values.put("id", fromId)
        wdb.update("comicdata", values, "id=?", arrayOf(java.lang.Long.toString(toId)))
        values = ContentValues()
        values.put("id", toId)
        wdb.update("comicdata", values, "id=?", arrayOf(java.lang.Long.toString(-1)))
        remakeAllListData()
    }

    val newId: Long
        get() {
            var newId: Long = 0
            val cur = allCursor
            var mov = cur.moveToFirst()
            var curId: Long
            while (mov) {
                curId = java.lang.Long.valueOf(cur.getString(0)).toLong()
                if (newId < curId) {
                    newId = curId
                }
                mov = cur.moveToNext()
            }
            return newId + 1
        }

    fun closeData() {
        rdb.close()
        wdb.close()
    }

    fun remakeAllListData() {
        remakeListData(0)
        sortListData(0, sortKey1)
        remakeListData(1)
        sortListData(1, sortKey2)
    }

    fun remakeListData(dataIndex: Long) {
        val dataList: MutableList<Map<String, String>>
        dataList = if (dataIndex == 0L) {
            dataList1
        } else {
            dataList2
        }
        dataList.clear()
        val cur = getCurrentCursor(dataIndex)
        var mov = cur.moveToFirst()
        while (mov) {
            data = HashMap()
            data["id"] = cur.getString(0)
            data["title"] = cur.getString(1)
            data["author"] = cur.getString(2)
            data["number"] = cur.getString(3)
            data["memo"] = cur.getString(4)
            data["inputdate"] = cur.getString(5)
            data["status"] = cur.getString(6)
            dataList.add(data)
            mov = cur.moveToNext()
        }
    }

    fun sortListData(dataIndex: Long, key: String) {
        val dataList: List<Map<String, String>>
        if (dataIndex == 0L) {
            dataList = dataList1
            sortKey1 = key
        } else {
            dataList = dataList2
            sortKey2 = key
        }
        Collections.sort(dataList, Comparator<Map<String?, String>> { stringStringMap, t1 ->
            var result: Int
            result = if (key === "id") {
                Integer.valueOf(stringStringMap["id"]).compareTo(
                    Integer.valueOf(
                        t1["id"]
                    )
                )
            } else {
                stringStringMap[key]!!.compareTo(t1[key]!!)
            }

            // ソート順が決まらない場合には、idで比較する
            if (result == 0) {
                result = Integer.valueOf(stringStringMap["id"]).compareTo(
                    Integer.valueOf(
                        t1["id"]
                    )
                )
            }
            result
        })
    }

    private fun getCurrentCursor(dataIndex: Long): Cursor {
        return rdb.query(
            "comicdata",
            arrayOf("id", "title", "author", "number", "memo", "inputdate", "status"),
            "status=?",
            arrayOf(java.lang.Long.toString(dataIndex)),
            null,
            null,
            "id ASC"
        )
    }

    private val allCursor: Cursor
        private get() = rdb.query(
            "comicdata",
            arrayOf("id", "title", "author", "number", "memo", "inputdate", "status"),
            null,
            null,
            null,
            null,
            "id ASC"
        )

    companion object {
        var instance: ListDataManager? = null
            private set

        fun createInstance(context: Context): ListDataManager? {
            instance = ListDataManager(context)
            return instance
        }
    }

    init {
        val helper = ListDataOpenHelper(context)
        rdb = helper.readableDatabase
        wdb = helper.writableDatabase
        dataList1 = ArrayList()
        dataList2 = ArrayList()
        remakeAllListData()
    }
}