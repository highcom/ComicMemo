package com.highcom.comicmemo;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ComicMemo extends Activity implements ListViewAdapter.AdapterListener {

    private ListDataManager manager;
    private RecyclerView recyclerView;
    private ListViewAdapter adapter;

    private String searchViewWord;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_memo);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        manager = ListDataManager.createInstance(this);

        adapter = new ListViewAdapter(
                this,
                manager.getDataList(),
                R.layout.row,
                new String[] { "title", "comment" },
                new int[] { android.R.id.text1,
                        android.R.id.text2 },
                this);

        recyclerView = (RecyclerView) findViewById(R.id.comicListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        // セル間に区切り線を実装する
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

        // ドラックアンドドロップの操作を実装する
        ItemTouchHelper itemDecor = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.ACTION_STATE_IDLE) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        if (adapter.getDelbtnEnable() && TextUtils.isEmpty(searchViewWord)) {
                            final int fromPos = viewHolder.getAdapterPosition();
                            final int toPos = target.getAdapterPosition();
                            adapter.notifyItemMoved(fromPos, toPos);
                            manager.rearrangeData(fromPos, toPos);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    }
                });
        itemDecor.attachToRecyclerView(recyclerView);

        // 編集ボタン処理
        Button editbtn = (Button) findViewById(R.id.edit);
        editbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 編集状態の変更
                if (adapter.getDelbtnEnable()) {
                    adapter.setDelbtnEnable(false);
                } else {
                    adapter.setDelbtnEnable(true);
                }
                recyclerView.setAdapter(adapter);
            }
        });

        // 追加ボタン処理
        Button addbtn = (Button) findViewById(R.id.add);
        addbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ComicMemo.this, ComicMemoTabActivity.class);
                //intent.putExtra("EDIT", false);
                //intent.putExtra("ID", manager.getNewId());
                startActivityForResult(intent, 1001);
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
                searchViewWord = searchWord;
                setSearchWordFilter();
                return false;
            }
        });
    }

    private void setSearchWordFilter() {
        Filter filter = ((Filterable) recyclerView.getAdapter()).getFilter();
        if (TextUtils.isEmpty(searchViewWord)) {
            filter.filter(null);
        } else {
            filter.filter(searchViewWord.toString());
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
        manager.closeData();
        mAdView.destroy();
        super.onDestroy();
    }

    @Override
    public void onAdapterClicked(View view, int position) {
        // 編集状態でない場合は入力画面に遷移しない
        if (!adapter.getDelbtnEnable()) {
            return;
        }
        // 入力画面を生成
        Intent intent = new Intent(ComicMemo.this, InputMemo.class);
        // 選択アイテムを設定
        ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
        intent.putExtra("EDIT", true);
        intent.putExtra("ID", holder.id.longValue());
        intent.putExtra("TITLE", holder.title.getText().toString());
        intent.putExtra("NUMBER", holder.number.getText().toString());
        intent.putExtra("MEMO", holder.memo.getText().toString());
        startActivityForResult(intent, 1001);
    }

    @Override
    public void onAdapterAddBtnClicked(View view) {
        ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
        // 巻数を+1する
        Integer num = Integer.parseInt(holder.number.getText().toString());
        // 999を上限とする
        if (num < 999) {
            num++;
            holder.number.setTextColor(Color.RED);
        }
        holder.number.setText(num.toString());
        holder.inputdate.setText(manager.getNowDate());

        // データベースを更新する
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", holder.id.toString());
        data.put("title", holder.title.getText().toString());
        data.put("number", holder.number.getText().toString());
        data.put("memo", holder.memo.getText().toString());
        data.put("inputdate", holder.inputdate.getText().toString());
        manager.setData(true, data);
    }

    @Override
    public void onAdapterDelBtnClicked(View view) {
        ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
        // データベースから削除する
        manager.deleteData(holder.id.toString());
        // adapterにデータが更新された事を通知する
        adapter.notifyDataSetChanged();
        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1001) {
            return;
        }

        // adapterにデータが更新された事を通知する
        adapter.notifyDataSetChanged();
        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter();
    }
}
