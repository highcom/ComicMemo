package com.highcom.comicmemo

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by koichi on 2016/08/07.
 */
class InputMemo : Activity(), CompoundButton.OnCheckedChangeListener {
    var manager: ListDataManager? = null
    private var isEdit = false
    private var id: Long = 0
    private var status: Long = 0
    var tbContinue: ToggleButton? = null
    var tbComplete: ToggleButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.input_memo)

        // 渡されたデータを取得する
        val intent = intent
        isEdit = intent.getBooleanExtra("EDIT", false)
        id = intent.getLongExtra("ID", -1)
        status = intent.getLongExtra("STATUS", 0)
        (findViewById<View>(R.id.editTitle) as EditText).setText(intent.getStringExtra("TITLE"))
        (findViewById<View>(R.id.editAuthor) as EditText).setText(intent.getStringExtra("AUTHOR"))
        (findViewById<View>(R.id.editNumber) as EditText).setText(intent.getStringExtra("NUMBER"))
        (findViewById<View>(R.id.editMemo) as EditText).setText(intent.getStringExtra("MEMO"))
        manager = ListDataManager.instance

        // キャンセルボタン処理
        val cancelBtn = findViewById<View>(R.id.cancel) as Button
        cancelBtn.setOnClickListener { finish() }

        // 完了ボタン処理
        val doneBtn = findViewById<View>(R.id.done) as Button
        doneBtn.setOnClickListener { // 入力データを登録する
            val editTitle = findViewById<View>(R.id.editTitle) as EditText
            val editAuthor = findViewById<View>(R.id.editAuthor) as EditText
            val editNumber = findViewById<View>(R.id.editNumber) as EditText
            var chgNumber = 0
            if (editNumber.text.toString() != "") {
                chgNumber = editNumber.text.toString().toInt()
            }
            val editMemo = findViewById<View>(R.id.editMemo) as EditText
            val data: MutableMap<String, String> = HashMap()
            data["id"] = java.lang.Long.valueOf(id).toString()
            data["title"] = editTitle.text.toString()
            data["author"] = editAuthor.text.toString()
            data["number"] = chgNumber.toString()
            data["memo"] = editMemo.text.toString()
            data["inputdate"] = nowDate
            data["status"] = java.lang.Long.valueOf(status).toString()
            // データベースに追加or編集する
            manager!!.setData(isEdit, data)
            // 詳細画面を終了
            finish()
        }
        tbContinue = findViewById<View>(R.id.toggleContinue) as ToggleButton
        tbComplete = findViewById<View>(R.id.toggleComplete) as ToggleButton
        tbContinue!!.setOnCheckedChangeListener(this)
        tbComplete!!.setOnCheckedChangeListener(this)
        if (status == 0L) {
            setEnableToggleContinue()
        } else {
            setEnableToggleComplete()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.id == R.id.toggleContinue) {
            setEnableToggleContinue()
        } else if (buttonView.id == R.id.toggleComplete) {
            setEnableToggleComplete()
        }
    }

    private fun setEnableToggleContinue() {
        tbContinue!!.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
        tbContinue!!.setBackgroundDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                R.drawable.toggle_select_button
            )
        )
        tbComplete!!.setTextColor(ContextCompat.getColor(applicationContext, R.color.blue))
        tbComplete!!.setBackgroundDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                R.drawable.toggle_unselect_button
            )
        )
        status = 0
    }

    private fun setEnableToggleComplete() {
        tbContinue!!.setTextColor(ContextCompat.getColor(applicationContext, R.color.blue))
        tbContinue!!.setBackgroundDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                R.drawable.toggle_unselect_button
            )
        )
        tbComplete!!.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
        tbComplete!!.setBackgroundDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                R.drawable.toggle_select_button
            )
        )
        status = 1
    }

    private val nowDate: String
        private get() {
            val date = Date()
            val sdf = SimpleDateFormat("yyyy/MM/dd")
            return sdf.format(date)
        }
}