package com.highcom.comicmemo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * スワイプメニュー用リスナークラス
 *
 * @param context コンテキスト
 * @param recyclerView 巻数データ一覧View
 * @param scale 画面の解像度
 * @param listener コールバック用リスナー
 */
@SuppressLint("ClickableViewAccessibility")
abstract class SimpleCallbackHelper(
    context: Context?,
    recyclerView: RecyclerView?,
    scale: Float,
    listener: SimpleCallbackListener
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.LEFT
) {
    /** 巻数データ一覧View */
    private val recyclerView: RecyclerView?
    /** スワイプで表示されるボタン */
    private lateinit var buttons: MutableList<UnderlayButton>
    /** 操作のジェスチャー検出 */
    private lateinit var gestureDetector: GestureDetector
    /** スワイプした位置 */
    private var swipedPos = -1
    /** スワイプ長さの閾値 */
    private var swipeThreshold = 0.5f
    /** スワイプで表示されるボタンのバッファ */
    private val buttonsBuffer: MutableMap<Int, MutableList<UnderlayButton>>
    /** スワイプボタンを表示している位置を覚えておくキュー */
    private lateinit var recoverQueue: Queue<Int>
    /** スワイプ時のコールバック用リスナー */
    private val simpleCallbackListener: SimpleCallbackListener
    /** スワイプが終わったかどうか */
    private var isMoved: Boolean

    /**
     * スワイプ時のコールバック用リスナークラス
     *
     */
    interface SimpleCallbackListener {
        fun onSimpleCallbackMove(
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean

        fun clearSimpleCallbackView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder)
    }

    /**
     * ジェスチャー検出用リスナー
     */
    private val gestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            for (button in buttons) {
                if (button.onClick(e.x, e.y)) break
            }
            return true
        }
    }

    /**
     * タッチ操作用リスナー
     */
    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { _, e ->
        if (swipedPos < 0) return@OnTouchListener false
        val point = Point(
            e.rawX.toInt(), e.rawY.toInt()
        )
        val swipedViewHolder = recyclerView?.findViewHolderForAdapterPosition(swipedPos)
        val swipedItem = swipedViewHolder?.itemView
        val rect = Rect()
        swipedItem?.getGlobalVisibleRect(rect)
        if (e.action == MotionEvent.ACTION_DOWN || e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_MOVE) {
            if (rect.top < point.y && rect.bottom > point.y) gestureDetector.onTouchEvent(e) else {
                recoverQueue.add(swipedPos)
                swipedPos = -1
                recoverSwipedItem()
            }
        }
        false
    }

    /**
     * スワイプ操作が有効かどうか
     *
     * @param enable スワイプ操作の有効・無効
     */
    fun setSwipeEnable(enable: Boolean) {
        if (enable) {
            setDefaultSwipeDirs(ItemTouchHelper.LEFT)
        } else {
            setDefaultSwipeDirs(ItemTouchHelper.ACTION_STATE_IDLE)
        }
    }

    /**
     * 移動操作中のイベント処理
     *
     * @param recyclerView
     * @param viewHolder
     * @param target
     * @return
     */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (target.itemView.getId() == R.id.row_footer) return false
        return simpleCallbackListener.onSimpleCallbackMove(viewHolder, target)
    }

    /**
     * 移動操作完了後の処理
     *
     * @param recyclerView
     * @param viewHolder
     * @param fromPos
     * @param target
     * @param toPos
     * @param x
     * @param y
     */
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

    /**
     * スワイプ操作処理
     *
     * @param viewHolder
     * @param direction
     */
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

    /**
     * 操作したViewを初期状態に戻す
     *
     * @param recyclerView
     * @param viewHolder
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (isMoved) {
            simpleCallbackListener.clearSimpleCallbackView(recyclerView, viewHolder)
        }
        isMoved = false
    }

    /**
     * スワイプ閾値の取得
     *
     * @param viewHolder
     * @return
     */
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    /**
     * スワイプ速度の取得
     *
     * @param defaultValue
     * @return
     */
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    /**
     * スワイプ速度の閾値取得
     *
     * @param defaultValue
     * @return
     */
    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
    }

    /**
     * スワイプ時のボタン表示
     *
     * @param c
     * @param recyclerView
     * @param viewHolder
     * @param dX
     * @param dY
     * @param actionState
     * @param isCurrentlyActive
     */
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

    /**
     * スワイプアイテムを元に戻す処理
     *
     */
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Synchronized
    private fun recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            val pos = recoverQueue.poll()
            if (pos > -1) {
                recyclerView!!.adapter!!.notifyItemChanged(pos)
            }
        }
    }

    /**
     * ボタンの描画
     *
     * @param c
     * @param itemView
     * @param buffer
     * @param pos
     * @param dX
     */
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

    /**
     * 操作イベントリスナーのアタッチ処理
     *
     */
    fun attachSwipe() {
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    abstract fun instantiateUnderlayButton(
        viewHolder: RecyclerView.ViewHolder,
        underlayButtons: MutableList<UnderlayButton>
    )

    /**
     * スワイプ時に表示するボタン描画用クラス
     *
     * @property text
     * @property imageResId
     * @property color
     * @property viewHolder
     * @property clickListener
     */
    class UnderlayButton(
        private val text: String,
        private val imageResId: Int,
        private val color: Int,
        private val viewHolder: ComicListAdapter.ComicViewHolder,
        private val clickListener: (Any, Any) -> Unit
    ) {
        private var pos = 0
        private var clickRegion: RectF? = null

        /**
         * ボタン選択時の処理
         *
         * @param x
         * @param y
         * @return
         */
        fun onClick(x: Float, y: Float): Boolean {
            if (clickRegion != null && clickRegion!!.contains(x, y)) {
                clickListener.invoke(viewHolder, pos)
                return true
            }
            return false
        }

        /**
         * ボタンの描画処理
         *
         * @param c
         * @param rect
         * @param pos
         */
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

    companion object {
        private const val BUTTON_WIDTH = 75
        private const val FONT_SIZE = 14
        private var BUTTON_WIDTH_DP: Int = 0
        private var FONT_SIZE_DP: Int = 0
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