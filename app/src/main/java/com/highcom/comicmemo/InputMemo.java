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

    private long id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_memo);

        // 渡されたデータを取得する
        Intent intent = getIntent();
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

                // データベースに追加or編集する
                ContentValues addValues = new ContentValues();
                addValues.put("id", id);
                addValues.put("title", editTitle.getText().toString());
                addValues.put("number", chgNumber.toString());
                addValues.put("memo", editMemo.getText().toString());
                addValues.put("inputdate", ComicMemo.getNowDate());
                // idが合計データ数より小さい場合はデータ編集
                if (id < DatabaseUtils.queryNumEntries(ComicMemo.rdb, "comicdata")) {
                    ComicMemo.wdb.update("comicdata", addValues, "id=?", new String[] { Long.valueOf(id).toString() });
                } else {
                    long retid = ComicMemo.wdb.insert("comicdata", Long.valueOf(id).toString(), addValues);
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("id", Long.valueOf(id).toString());
                    data.put("title", editTitle.getText().toString());
                    data.put("number", chgNumber.toString());
                    data.put("memo", editMemo.getText().toString());
                    data.put("inputdate", ComicMemo.getNowDate());
                    ComicMemo.dataList.add(data);
                }

                ComicMemo.listView.setAdapter(ComicMemo.adapter);
                ComicMemo.reflesh();
                // 詳細画面を終了
                finish();
            }
        });
    }
}
