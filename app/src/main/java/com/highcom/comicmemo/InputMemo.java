package com.highcom.comicmemo;

import android.app.Activity;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by koichi on 2016/08/07.
 */
public class InputMemo extends Activity {

    ListDataManager manager;
    private boolean isEdit;
    private long id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_memo);

        // TODO:新しいインスタンスにしているが、このままでいいか考える
        manager = new ListDataManager(this, -1);

        // 渡されたデータを取得する
        Intent intent = getIntent();
        isEdit = intent.getBooleanExtra("EDIT", false);
        id = intent.getLongExtra("ID", -1);
        ((EditText)findViewById(R.id.editTitle)).setText(intent.getStringExtra("TITLE"));
        ((EditText)findViewById(R.id.editNumber)).setText(intent.getStringExtra("NUMBER"));
        ((EditText)findViewById(R.id.editMemo)).setText(intent.getStringExtra("MEMO"));

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
                EditText editNumber = (EditText) findViewById(R.id.editNumber);
                Integer chgNumber = 0;
                if (!editNumber.getText().toString().equals("")) {
                    chgNumber = Integer.parseInt(editNumber.getText().toString());
                }
                EditText editMemo = (EditText) findViewById(R.id.editMemo);

                Map<String, String> data = new HashMap<String, String>();
                data.put("id", Long.valueOf(id).toString());
                data.put("title", editTitle.getText().toString());
                data.put("author", ""); // TODO:著作者の入力欄を設ける
                data.put("number", chgNumber.toString());
                data.put("memo", editMemo.getText().toString());
                data.put("inputdate", manager.getNowDate());
                data.put("status", "1"); // TODO:編集の場合はタブ情報を引継ぎ、新規の場合は続刊とし、選択欄を設ける
                // データベースに追加or編集する
                manager.setData(isEdit, data);
                // 詳細画面を終了
                finish();
            }
        });
    }
}
