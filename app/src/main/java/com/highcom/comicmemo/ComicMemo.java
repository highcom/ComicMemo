package com.highcom.comicmemo;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SearchView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;
import java.util.Date;

import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater;

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

        RmpAppirater.appLaunched(this,
                new RmpAppirater.ShowRateDialogCondition() {
                    @Override
                    public boolean isShowRateDialog(
                            long appLaunchCount, long appThisVersionCodeLaunchCount,
                            long firstLaunchDate, int appVersionCode,
                            int previousAppVersionCode, Date rateClickDate,
                            Date reminderClickDate, boolean doNotShowAgain) {
                        // 現在のアプリのバージョンで3回以上起動したか
                        if (appThisVersionCodeLaunchCount < 3) {
                            return false;
                        }
                        // ダイアログで「いいえ」を選択していないか
                        if (doNotShowAgain) {
                            return false;
                        }
                        // ユーザーがまだ評価していないか
                        if (rateClickDate != null) {
                            return false;
                        }
                        // ユーザーがまだ「あとで」を選択していないか
                        if (reminderClickDate != null) {
                            // 「あとで」を選択してから3日以上経過しているか
                            long prevtime = reminderClickDate.getTime();
                            long nowtime = new Date().getTime();
                            long diffDays = (nowtime - prevtime) / (1000 * 60 * 60 * 24);
                            if (diffDays < 3) {
                                return false;
                            }
                        }

                        return true;
                    }
                }
        );

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
                PopupMenu popup = new PopupMenu(getApplicationContext(), arg0);
                popup.getMenuInflater().inflate(R.menu.menu_comic_memo, popup.getMenu());
                popup.show();

                // ポップアップメニューのメニュー項目のクリック処理
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        PlaceholderFragment fragment = (PlaceholderFragment)sectionsPagerAdapter.getCurrentFragment();
                        switch (item.getItemId()) {
                            case R.id.edit_mode:
                                // 編集状態の変更
                                fragment.sortData("id");
                                fragment.changeEditEnable();
                                break;
                            case R.id.sort_default:
                                fragment.sortData("id");
                                fragment.setEditEnable(false);
                                break;
                            case R.id.sort_title:
                                fragment.sortData("title");
                                fragment.setEditEnable(false);
                                break;
                            case R.id.sort_author:
                                fragment.sortData("author");
                                fragment.setEditEnable(false);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
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
    public void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }

}
