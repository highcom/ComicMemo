package com.highcom.comicmemo;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SearchView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;

public class ComicMemo extends FragmentActivity {

    private ListDataManager listDataManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private String mSearchWord = "";

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_memo);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        listDataManager = ListDataManager.createInstance(getApplicationContext());

        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.itemtabs);
        tabs.setupWithViewPager(viewPager);

        // 編集ボタン処理
        Button editbtn = (Button) findViewById(R.id.edit);
        editbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 編集状態の変更
                ((PlaceholderFragment)sectionsPagerAdapter.getCurrentFragment()).changeDelbtnEnable();
            }
        });

        // 追加ボタン処理
        Button addbtn = (Button) findViewById(R.id.add);
        addbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ComicMemo.this, InputMemo.class);
                if (sectionsPagerAdapter.getCurrentFragment() != null) {
                    long index = ((PlaceholderFragment) sectionsPagerAdapter.getCurrentFragment()).getIndex();
                    intent.putExtra("ID", listDataManager.getNewId());
                    intent.putExtra("STATUS", index);
                }
                intent.putExtra("EDIT", false);
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
                mSearchWord = searchWord;
                List<Fragment> fragments = sectionsPagerAdapter.getAllFragment();
                for (Fragment fragment : fragments) {
                    ((PlaceholderFragment)fragment).setSearchWordFilter(mSearchWord);
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // FragmentからのonAdapterClickedからではrequestCodeが引き継がれない
//        if (requestCode != 1001) {
//            return;
//        }

        List<Fragment> fragments = sectionsPagerAdapter.getAllFragment();
        for (Fragment fragment : fragments) {
            ((PlaceholderFragment)fragment).updateData();
            ((PlaceholderFragment)fragment).setSearchWordFilter(mSearchWord);
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
        mAdView.destroy();
        super.onDestroy();
    }

}
