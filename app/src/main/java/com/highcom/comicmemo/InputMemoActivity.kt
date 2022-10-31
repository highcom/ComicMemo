package com.highcom.comicmemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.highcom.comicmemo.databinding.ActivityInputMemoBinding
import com.highcom.comicmemo.datamodel.Comic
import java.util.*

/**
 * 巻数メモ入力Activity
 */
class InputMemoActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
    /** バインディング */
    private lateinit var binding: ActivityInputMemoBinding
    /** 編集モードかどうか */
    private var isEdit = false
    /** 巻数データ */
    private lateinit var comic: Comic
    /** アクティブなセクションページ 0:続刊 1:完結 */
    private var status: Long = 0
    /** トグルボタン（続刊） */
    private var tbContinue: ToggleButton? = null
    /** トグルボタン（完結） */
    private var tbComplete: ToggleButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 渡されたデータを取得する
        val intent = intent
        isEdit = intent.getBooleanExtra("EDIT", false)
        comic = intent.getSerializableExtra("COMIC") as? Comic ?: Comic(0, "", "", "", "", "", 0)
        status = comic.status
        binding.editTitle.setText(comic.title)
        binding.editAuthor.setText(comic.author)
        binding.editNumber.setText(comic.number)
        binding.editMemo.setText(comic.memo)

        tbContinue = binding.toggleContinue
        tbComplete = binding.toggleComplete
        tbContinue?.setOnCheckedChangeListener(this)
        tbComplete?.setOnCheckedChangeListener(this)
        if (status == 0L) {
            // 続刊
            setEnableToggleContinue()
        } else {
            // 完結
            setEnableToggleComplete()
        }

        // タイトルの設定
        title = if (isEdit) {
            getString(R.string.input_edit)
        } else {
            getString(R.string.input_new)
        }

        // アクションバーの戻るボタンを表示
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * アクションバーのメニュー生成処理
     *
     * @param menu アクションばーメニュー
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * アクションバーのメニュー選択処理
     *
     * @param item 選択項目
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { finish() }
            R.id.action_done -> {
                var chgNumber = 0
                if (binding.editNumber.text.toString() != "") {
                    chgNumber = binding.editNumber.text.toString().toInt()
                }
                comic.title = binding.editTitle.text.toString()
                comic.author = binding.editAuthor.text.toString()
                comic.number = chgNumber.toString()
                comic.memo = binding.editMemo.text.toString()
                comic.inputdate = DateFormat.format("yyyy/MM/dd", Date()).toString()
                comic.status = java.lang.Long.valueOf(status)
                val intent = Intent()
                intent.putExtra("COMIC", comic)
                setResult(Activity.RESULT_OK, intent)
                // 詳細画面を終了
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * ポップアップメニュー選択イベント処理
     *
     * @param buttonView ポップアップメニューボタン
     * @param isChecked 選択したかどうか
     */
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.id == R.id.toggleContinue) {
            setEnableToggleContinue()
        } else if (buttonView.id == R.id.toggleComplete) {
            setEnableToggleComplete()
        }
    }

    /**
     * 続刊を選択時の処理
     *
     */
    private fun setEnableToggleContinue() {
        // 表示を続刊を選択された状態に設定
        tbContinue?.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
        tbContinue?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                R.drawable.toggle_select_button
            )
        )
        tbComplete?.setTextColor(ContextCompat.getColor(applicationContext, R.color.appcolor))
        tbComplete?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                R.drawable.toggle_unselect_button
            )
        )
        // 続刊のステータスに設定
        status = 0
    }

    /**
     * 完結を選択時の処理
     *
     */
    private fun setEnableToggleComplete() {
        // 表示を完結を選択された状態に設定
        tbContinue?.setTextColor(ContextCompat.getColor(applicationContext, R.color.appcolor))
        tbContinue?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                R.drawable.toggle_unselect_button
            )
        )
        tbComplete?.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
        tbComplete?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                R.drawable.toggle_select_button
            )
        )
        // 完結のステータスに設定
        status = 1
    }
}