package com.example.ntut.weshare.dealDetail;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.ntut.weshare.R;
import com.example.ntut.weshare.goods.GoodsBoxPageChange;
import com.example.ntut.weshare.goods.GoodsBoxPageGive;
import com.example.ntut.weshare.goods.GoodsBoxPageWish;

import java.util.ArrayList;
import java.util.List;


public class dealDetailActivity extends AppCompatActivity {
    private List<Fragment> fragmentList;
    private ViewPager vpager;
    private TabLayout gTablayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deal_page_activity);
        initData();
        initView();
    }
    public void onCancelClick(View view) {
        finish();
    }
    private void initData() {
        fragmentList = new ArrayList<>();
        fragmentList.add(new NotDeal());
        fragmentList.add(new Dealing());
        fragmentList.add(new Dealed());
    }
    private void initView() {
        gTablayout = (TabLayout) findViewById(R.id.tabs);
        gTablayout.addTab(gTablayout.newTab().setText("未接受"));
        gTablayout.addTab(gTablayout.newTab().setText("進行中"));
        gTablayout.addTab(gTablayout.newTab().setText("已完成"));

        FragmentAdapter fAdapter = new FragmentAdapter(getSupportFragmentManager(), fragmentList);//Adapter有多個項目，(,顯示內容)
        vpager = (ViewPager) findViewById(R.id.pager);
        vpager.setAdapter(fAdapter);

        //mViewPager = (ViewPager) findViewById(R.id.pager);
        //vpager.setAdapter(memberAdapter);
        initListener();
    }
    private void initListener() {
        gTablayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        vpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(gTablayout));
    }
    private class FragmentAdapter extends FragmentStatePagerAdapter {//FragmentManager
        List<Fragment> fragmentList;

        private FragmentAdapter(FragmentManager fm, List<Fragment> memberList) {
            super(fm);
            this.fragmentList = memberList;
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public Fragment getItem(int position) {//position第幾個項目
            Fragment goods = fragmentList.get(position);
            //MemberFragment fragment = new MemberFragment();//View圖片
            //Bundle args = new Bundle();
            //args.putSerializable("member", member);//資料Data
            //fragment.setArguments(args);
            return fragmentList.get(position);
        }
    }
}
