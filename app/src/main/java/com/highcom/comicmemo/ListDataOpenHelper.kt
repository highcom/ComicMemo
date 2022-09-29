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
import java.lang.StringBuilder
import java.util.*

/**
 * Created by koichi on 2016/08/11.
 */
class ListDataOpenHelper(context: Context?) : SQLiteOpenHelper(context, "ComicMemoDB", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "create table comicdata("
                    + "id long not null,"
                    + "title text default '',"
                    + "author text default '',"
                    + "number text default '1',"
                    + "memo text default '',"
                    + "inputdate text default '',"
                    + "status long default 0"
                    + ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val targetTable = "comicdata"
        db.beginTransaction()
        try {
            // 初期化
            db.execSQL("ALTER TABLE $targetTable RENAME TO temp_$targetTable")
            // 元カラム一覧
            val columns = getColumns(db, "temp_$targetTable")
            onCreate(db)
            // 新カラム一覧
            val newColumns: List<String>? = getColumns(db, targetTable)

            // 変化しないカラムのみ抽出
            columns!!.retainAll(newColumns!!)

            // 共通データを移す。(OLDにしか存在しないものは捨てられ, NEWにしか存在しないものはNULLになる)
            val cols = join(columns, ",")
            db.execSQL(
                String.format(
                    "INSERT INTO %s (%s) SELECT %s from temp_%s", targetTable,
                    cols, cols, targetTable
                )
            )
            // 終了処理
            db.execSQL("DROP TABLE temp_$targetTable")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        private fun getColumns(db: SQLiteDatabase, tableName: String): MutableList<String>? {
            var ar: MutableList<String>? = null
            var c: Cursor? = null
            try {
                c = db.rawQuery("SELECT * FROM $tableName LIMIT 1", null)
                if (c != null) {
                    ar = ArrayList(Arrays.asList(*c.columnNames))
                }
            } finally {
                c?.close()
            }
            return ar
        }

        private fun join(list: List<String>?, delim: String): String {
            val buf = StringBuilder()
            val num = list!!.size
            for (i in 0 until num) {
                if (i != 0) buf.append(delim)
                buf.append(list[i])
            }
            return buf.toString()
        }
    }
}