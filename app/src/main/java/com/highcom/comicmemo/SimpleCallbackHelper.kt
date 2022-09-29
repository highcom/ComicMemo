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
import android.content.res.TypedArray
import android.graphics.*
import android.view.*
import java.util.*

abstract class SimpleCallbackHelper(
    context: Context?,
    recyclerView: RecyclerView?,
    scale: Float,
    listener: SimpleCallbackListener
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.LEFT
) {
    private val recyclerView: RecyclerView?
    private var buttons: MutableList<UnderlayButton>
    private val gestureDetector: GestureDetector
    private var swipedPos = -1
    private var swipeThreshold = 0.5f
    private val buttonsBuffer: MutableMap<Int, MutableList<UnderlayButton>>
    private val recoverQueue: Queue<Int>
    private val simpleCallbackListener: SimpleCallbackListener
    private var isMoved: Boolean

    interface SimpleCallbackListener {
        fun onSimpleCallbackMove(
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean

        fun clearSimpleCallbackView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder)
    }

    private val gestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            for (button in buttons) {
                if (button.onClick(e.x, e.y)) break
            }
            return true
        }
    }
    private val onTouchListener = OnTouchListener { view, e ->
        if (swipedPos < 0) return@OnTouchListener false
        val point = Point(
            e.rawX.toInt(), e.rawY.toInt()
        )
        val swipedViewHolder = recyclerView!!.findViewHolderForAdapterPosition(swipedPos)
        val swipedItem = swipedViewHolder!!.itemView
        val rect = Rect()
        swipedItem.getGlobalVisibleRect(rect)
        if (e.action == MotionEvent.ACTION_DOWN || e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_MOVE) {
            if (rect.top < point.y && rect.bottom > point.y) gestureDetector.onTouchEvent(e) else {
                recoverQueue.add(swipedPos)
                swipedPos = -1
                recoverSwipedItem()
            }
        }
        false
    }

    fun setSwipeEnable(enable: Boolean) {
        if (enable) {
            setDefaultSwipeDirs(ItemTouchHelper.LEFT)
        } else {
            setDefaultSwipeDirs(ItemTouchHelper.ACTION_STATE_IDLE)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return simpleCallbackListener.onSimpleCallbackMove(viewHolder, target)
    }

    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        isMoved = true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val pos = viewHolder.adapterPosition
        if (swipedPos != pos) recoverQueue.add(swipedPos)
        swipedPos = pos
        if (buttonsBuffer.containsKey(swipedPos)) buttons =
            buttonsBuffer[swipedPos]!! else buttons.clear()
        buttonsBuffer.clear()
        swipeThreshold = 0.5f * buttons.size * BUTTON_WIDTH_DP
        recoverSwipedItem()
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (isMoved) {
            simpleCallbackListener.clearSimpleCallbackView(recyclerView, viewHolder)
        }
        isMoved = false
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val pos = viewHolder.adapterPosition
        var translationX = dX
        val itemView = viewHolder.itemView
        if (pos < 0) {
            swipedPos = pos
            return
        }
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                var buffer: MutableList<UnderlayButton> = ArrayList()
                if (!buttonsBuffer.containsKey(pos)) {
                    instantiateUnderlayButton(viewHolder, buffer)
                    buttonsBuffer[pos] = buffer
                } else {
                    buffer = buttonsBuffer[pos]!!
                }
                translationX = dX * buffer.size * BUTTON_WIDTH_DP / itemView.width
                drawButtons(c, itemView, buffer, pos, translationX)
            }
        }
        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            translationX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }

    @Synchronized
    private fun recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            val pos = recoverQueue.poll()
            if (pos > -1) {
                recyclerView!!.adapter!!.notifyItemChanged(pos)
            }
        }
    }

    private fun drawButtons(
        c: Canvas,
        itemView: View,
        buffer: List<UnderlayButton>,
        pos: Int,
        dX: Float
    ) {
        var right = itemView.right.toFloat()
        val dButtonWidth = -1 * dX / buffer.size
        for (button in buffer) {
            val left = right - dButtonWidth
            button.onDraw(
                c,
                RectF(
                    left,
                    itemView.top.toFloat(),
                    right,
                    itemView.bottom.toFloat()
                ),
                pos
            )
            right = left
        }
    }

    fun attachSwipe() {
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    abstract fun instantiateUnderlayButton(
        viewHolder: RecyclerView.ViewHolder,
        underlayButtons: MutableList<UnderlayButton>
    )

    class UnderlayButton(
        private val text: String,
        private val imageResId: Int,
        private val color: Int,
        private val viewHolder: ListViewAdapter.ViewHolder,
        private val clickListener: UnderlayButtonClickListener
    ) {
        private var pos = 0
        private var clickRegion: RectF? = null
        fun onClick(x: Float, y: Float): Boolean {
            if (clickRegion != null && clickRegion!!.contains(x, y)) {
                clickListener.onClick(viewHolder, pos)
                return true
            }
            return false
        }

        fun onDraw(c: Canvas, rect: RectF, pos: Int) {
            val p = Paint()

            // Draw background
            p.color = color
            c.drawRect(rect, p)

            // Draw Text
            p.color = Color.WHITE
            p.textSize = FONT_SIZE_DP.toFloat()
            val r = Rect()
            val cHeight = rect.height()
            val cWidth = rect.width()
            p.textAlign = Paint.Align.LEFT
            p.getTextBounds(text, 0, text.length, r)
            val x = cWidth / 2f - r.width() / 2f - r.left
            val y = cHeight / 2f + r.height() / 2f - r.bottom
            c.drawText(text, rect.left + x, rect.top + y, p)
            clickRegion = rect
            this.pos = pos
        }
    }

    interface UnderlayButtonClickListener {
        fun onClick(holder: ListViewAdapter.ViewHolder, pos: Int)
    }

    companion object {
        private const val BUTTON_WIDTH = 75
        private const val FONT_SIZE = 14
        private var BUTTON_WIDTH_DP: Int
        private var FONT_SIZE_DP: Int
    }

    init {
        BUTTON_WIDTH_DP = (BUTTON_WIDTH * scale).toInt()
        FONT_SIZE_DP = (FONT_SIZE * scale).toInt()
        this.recyclerView = recyclerView
        simpleCallbackListener = listener
        buttons = ArrayList()
        gestureDetector = GestureDetector(context, gestureListener)
        this.recyclerView!!.setOnTouchListener(onTouchListener)
        isMoved = false
        buttonsBuffer = HashMap()
        recoverQueue = object : LinkedList<Int>() {
            override fun add(o: Int): Boolean {
                return if (contains(o)) false else super.add(o)
            }
        }
        attachSwipe()
    }
}