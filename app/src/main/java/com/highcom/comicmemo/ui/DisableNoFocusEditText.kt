package com.highcom.comicmemo.ui

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContentProviderCompat.requireContext

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

    override fun onEditorAction(actionCode: Int) {
        // 改行を押下した場合にも入力を終了させる
        if (actionCode == EditorInfo.IME_ACTION_DONE) {
            this.isFocusable = false
            this.isFocusableInTouchMode = false
            this.requestFocus()
        }

        super.onEditorAction(actionCode)
    }
}
