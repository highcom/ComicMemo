package com.highcom.comicmemo.ui

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

/**
 * タップすると編集状態になるカスタムEditText
 *
 */
class DisableNoFocusEditText : AppCompatEditText {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            isFocusable = false
            isFocusableInTouchMode = false
        }
        return super.onKeyPreIme(keyCode, event)
    }
}
