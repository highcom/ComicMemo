package com.highcom.comicmemo;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Map;
import java.util.HashMap;

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
                // TODO 自動生成されたメソッド・スタブ
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
                EditText editMemo = (EditText) findViewById(R.id.editMemo);

                Map<String, String> data = new HashMap<String, String>();
                data.put("title", editTitle.getText().toString());
                data.put("number", editNumber.getText().toString() + "巻");
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
