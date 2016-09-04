package com.highcom.comicmemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ComicMemo extends Activity {

    public static Map<String, String> data;
    public static List<Map<String, String>> dataList;
    public static ListView listView;
    public static ListViewAdapter adapter;

    public static SQLiteDatabase rdb;
    public static SQLiteDatabase wdb;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_memo);
        dataList = new ArrayList<Map<String, String>>();

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        ListDataOpenHelper helper = new ListDataOpenHelper(this);
        rdb = helper.getReadableDatabase();
        wdb = helper.getWritableDatabase();
        Cursor cur = rdb.query("comicdata", new String[] { "id", "title", "number", "memo", "inputdate" }, null, null, null, null, null);

        boolean mov = cur.moveToFirst();
        while (mov) {
            data = new HashMap<String, String>();
            data.put("id", cur.getString(0));
            data.put("title", cur.getString(1));
            data.put("number", cur.getString(2));
            data.put("memo", cur.getString(3));
            data.put("inputdate", cur.getString(4));
            dataList.add(data);
            mov = cur.moveToNext();
        }

        /*
        int MAXDATA = 10;
        for (int i = 0; i < MAXDATA; i++) {
            data = new HashMap<String, String>();
            data.put("title", "タイトル欄" + i);
            data.put("number", i + "巻");
            data.put("comment", "COMMENT欄" + i);
            data.put("inputdate", getNowDate());
            dataList.add(data);
        }
        */

        adapter = new ListViewAdapter(
                this,
                dataList,
                R.layout.row,
                new String[] { "title", "comment" },
                new int[] { android.R.id.text1,
                        android.R.id.text2 });

        listView = (ListView) findViewById(R.id.comicListView);
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);
        // アイテムクリック時ののイベントを追加
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View view, int pos, long id) {
                // 編集状態でない場合は入力画面に遷移しない
                if (ListViewAdapter.delbtnEnable == false) {
                    return;
                }
                // 入力画面を生成
                Intent intent = new Intent(ComicMemo.this, InputMemo.class);
                // 選択アイテムを設定
                ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
                intent.putExtra("ID", holder.id.longValue());
                intent.putExtra("TITLE", holder.title.getText().toString());
                intent.putExtra("NUMBER", holder.number.getText().toString());
                intent.putExtra("MEMO", holder.memo.getText().toString());
                startActivity(intent);
            }
        });

        // 編集ボタン処理
        Button editbtn = (Button) findViewById(R.id.edit);
        editbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 編集状態の変更
                if (ListViewAdapter.delbtnEnable) {
                    ListViewAdapter.delbtnEnable = false;
                } else {
                    ListViewAdapter.delbtnEnable = true;
                }
                listView.setAdapter(adapter);
            }
        });

        // 追加ボタン処理
        Button addbtn = (Button) findViewById(R.id.add);
        addbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Intent intent = new Intent();
                Intent intent = new Intent(ComicMemo.this, InputMemo.class);
                //intent.setClassName("com.highcom.comicmemo", "com.highcom.comicmemo.InputMemo");
                intent.putExtra("ID", DatabaseUtils.queryNumEntries(rdb, "comicdata"));
                startActivity(intent);
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
                Filter filter = ((Filterable) listView.getAdapter()).getFilter();
                if (TextUtils.isEmpty(searchWord)) {
                    listView.clearTextFilter();
                    filter.filter(null);

                } else {
                    //listView.setFilterText(searchWord.toString());
                    filter.filter(searchWord.toString());
                }
                return false;
            }
        });
    }

    // データの一覧を更新する
    public static void reflesh() {
        dataList.clear();

        Cursor cur = rdb.query("comicdata", new String[] { "id", "title", "number", "memo", "inputdate" }, null, null, null, null, null);

        boolean mov = cur.moveToFirst();
        while (mov) {
            data = new HashMap<String, String>();
            data.put("id", cur.getString(0));
            data.put("title", cur.getString(1));
            data.put("number", cur.getString(2));
            data.put("memo", cur.getString(3));
            data.put("inputdate", cur.getString(4));
            dataList.add(data);
            mov = cur.moveToNext();
        }
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

    @Override
    public void onDestroy() {
        rdb.close();
        wdb.close();
        mAdView.destroy();
        super.onDestroy();
    }

    public static String getNowDate(){
        Date date = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(date);
    }
}
