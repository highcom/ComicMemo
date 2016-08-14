package com.highcom.comicmemo;

import android.app.Activity;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_memo);

        Button cancelBtn = (Button) findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        Button doneBtn = (Button) findViewById(R.id.done);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 入力データを登録する
                EditText editTitle = (EditText) findViewById(R.id.editTitle);
                EditText editNumber = (EditText) findViewById(R.id.editNumber);
                Integer chgNumber = Integer.parseInt(editNumber.getText().toString());
                EditText editMemo = (EditText) findViewById(R.id.editMemo);

                // データベースに追加する
                ContentValues addValues = new ContentValues();
                addValues.put("title", editTitle.getText().toString());
                addValues.put("number", chgNumber.toString());
                addValues.put("comment", editMemo.getText().toString());
                addValues.put("inputdate", ComicMemo.getNowDate());
                long id = ComicMemo.wdb.insert("comicdata", editTitle.getText().toString(), addValues);

                Map<String, String> data = new HashMap<String, String>();
                data.put("title", editTitle.getText().toString());
                data.put("number", chgNumber.toString());
                data.put("comment", editMemo.getText().toString());
                data.put("inputdate", ComicMemo.getNowDate());
                ComicMemo.dataList.add(data);
                ComicMemo.listView.setAdapter(ComicMemo.adapter);
                // 詳細画面を終了
                finish();
            }
        });
    }
}
