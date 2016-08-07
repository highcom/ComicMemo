package com.highcom.comicmemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

public class ComicMemo extends Activity {

    Map<String, String> data;
    public static List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
    public static ListView listView;
    public static ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_memo);

        int MAXDATA = 10;
        for (int i = 0; i < MAXDATA; i++) {
            data = new HashMap<String, String>();
            data.put("title", "タイトル欄" + i);
            data.put("comment", "COMMENT欄" + i);
            data.put("number", i + "巻");
            dataList.add(data);
        }

        adapter = new ListViewAdapter(
                this,
                dataList,
                R.layout.row,
                new String[] { "title", "comment" },
                new int[] { android.R.id.text1,
                        android.R.id.text2 });

        listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);

        Button editbtn = (Button) findViewById(R.id.edit);
        editbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO 自動生成されたメソッド・スタブ
                if (ListViewAdapter.delbtnEnable) {
                    ListViewAdapter.delbtnEnable = false;
                } else {
                    ListViewAdapter.delbtnEnable = true;
                }
                listView.setAdapter(adapter);
            }
        });

        Button addbtn = (Button) findViewById(R.id.add);
        addbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO 自動生成されたメソッド・スタブ
                //Intent intent = new Intent();
                Intent intent = new Intent(ComicMemo.this, InputMemo.class);
                //intent.setClassName("com.highcom.comicmemo", "com.highcom.comicmemo.InputMemo");
                startActivity(intent);
                /*
                data = new HashMap<String, String>();
                data.put("title", "タイトル欄");
                data.put("comment", "COMMENT欄");
                data.put("number", "0巻");
                dataList.add(data);
                listView.setAdapter(adapter);*/
            }
        });

        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setQueryHint("検索文字を入力");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchWord) {
                if (TextUtils.isEmpty(searchWord)) {
                    listView.clearTextFilter();
                } else {
                    listView.setFilterText(searchWord.toString());
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comic_memo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
