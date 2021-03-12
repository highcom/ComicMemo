package com.highcom.comicmemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by koichi on 2016/08/07.
 */
public class InputMemo extends Activity implements CompoundButton.OnCheckedChangeListener {

    ListDataManager manager;
    private boolean isEdit;
    private long id;
    private long status;
    ToggleButton tbContinue;
    ToggleButton tbComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_memo);

        // 渡されたデータを取得する
        Intent intent = getIntent();
        isEdit = intent.getBooleanExtra("EDIT", false);
        id = intent.getLongExtra("ID", -1);
        status = intent.getLongExtra("STATUS", 0);
        ((EditText)findViewById(R.id.editTitle)).setText(intent.getStringExtra("TITLE"));
        ((EditText)findViewById(R.id.editAuthor)).setText(intent.getStringExtra("AUTHOR"));
        ((EditText)findViewById(R.id.editNumber)).setText(intent.getStringExtra("NUMBER"));
        ((EditText)findViewById(R.id.editMemo)).setText(intent.getStringExtra("MEMO"));

        manager = ListDataManager.getInstance();

        // キャンセルボタン処理
        Button cancelBtn = (Button) findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        // 完了ボタン処理
        Button doneBtn = (Button) findViewById(R.id.done);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 入力データを登録する
                EditText editTitle = (EditText) findViewById(R.id.editTitle);
                EditText editAuthor = (EditText) findViewById(R.id.editAuthor);
                EditText editNumber = (EditText) findViewById(R.id.editNumber);
                Integer chgNumber = 0;
                if (!editNumber.getText().toString().equals("")) {
                    chgNumber = Integer.parseInt(editNumber.getText().toString());
                }
                EditText editMemo = (EditText) findViewById(R.id.editMemo);

                Map<String, String> data = new HashMap<String, String>();
                data.put("id", Long.valueOf(id).toString());
                data.put("title", editTitle.getText().toString());
                data.put("author", editAuthor.getText().toString());
                data.put("number", chgNumber.toString());
                data.put("memo", editMemo.getText().toString());
                data.put("inputdate", getNowDate());
                data.put("status", Long.valueOf(status).toString());
                // データベースに追加or編集する
                manager.setData(isEdit, data);
                // 詳細画面を終了
                finish();
            }
        });

        tbContinue = (ToggleButton) findViewById(R.id.toggleContinue);
        tbComplete = (ToggleButton) findViewById(R.id.toggleComplete);
        tbContinue.setOnCheckedChangeListener(this);
        tbComplete.setOnCheckedChangeListener(this);
        if (status == 0) {
            setEnableToggleContinue();
        } else {
            setEnableToggleComplete();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.toggleContinue) {
            setEnableToggleContinue();
        } else if (buttonView.getId() == R.id.toggleComplete) {
            setEnableToggleComplete();
        }
    }

    private void setEnableToggleContinue() {
        tbContinue.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        tbContinue.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.toggle_select_button));
        tbComplete.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
        tbComplete.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.toggle_unselect_button));
        status = 0;
    }

    private void setEnableToggleComplete() {
        tbContinue.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
        tbContinue.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.toggle_unselect_button));
        tbComplete.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        tbComplete.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.toggle_select_button));
        status = 1;
    }

    private String getNowDate(){
        Date date = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(date);
    }
}
