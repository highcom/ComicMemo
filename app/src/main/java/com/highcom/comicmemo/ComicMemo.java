package com.highcom.comicmemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class ComicMemo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_memo);
        List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();

        Map<String, String> data;
        int MAXDATA = 10;
        for (int i = 0; i < MAXDATA; i++) {
            data = new HashMap<String, String>();
            data.put("title", "タイトル欄" + i);
            data.put("comment", "COMMENT欄" + i);
            data.put("number", i + "巻");
            dataList.add(data);
        }

        ListViewAdapter adapter = new ListViewAdapter(
                this,
                dataList,
                R.layout.row,
                new String[] { "title", "comment" },
                new int[] { android.R.id.text1,
                        android.R.id.text2 });

        ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(adapter);//
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
