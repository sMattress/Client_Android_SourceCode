package com.wtf.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.alibaba.fastjson.JSONObject;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.activity.MainActivity;
import com.wtf.model.AppMsg;
import com.wtf.model.DeviceData;
import com.wtf.model.Param;
import com.wtf.model.URL;
import com.wtf.model.UserData;
import com.wtf.utils.ConnectDirect;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.MD5Util;
import com.wtf.utils.MyHttpListener;
import com.wtf.utils.Request;
import com.wtf.utils.SharedPreUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyan on 2016/10/10.
 * 没使用
 */
public class SettingFragment extends Fragment {

    private FragmentManager fragmentManager;
    private FragmentActivity mActivity;

    private int fragmentState = 0;

    private TextView tv_title;
    private TextView tv_side;
    private ToggleButton tb_switch;
    private Button btn_is_change;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        initViews(view);
        //getData();
        fragmentManager = getChildFragmentManager();

        return view;
    }

    private void initViews(View view) {
        mActivity = this.getActivity();

        tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_side = (TextView) view.findViewById(R.id.tv_side);
        tb_switch = (ToggleButton) view.findViewById(R.id.tb_switch);
        btn_is_change = (Button) view.findViewById(R.id.btn_is_change);

        mViewPager = (ViewPager) view.findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) view.findViewById(R.id.tabs);

        //添加页卡标题
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.common)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.constant)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.enhance)));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //添加页卡视图
        PagerAdapter adapter = new PagerAdapter
                (getChildFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(1);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment f = fragmentManager.findFragmentByTag("setting");
            /*然后在碎片中调用重写的onActivityResult方法*/
        f.onActivityResult(requestCode, resultCode, data);
    }

    //ViewPager适配器
    public class PagerAdapter extends FragmentStatePagerAdapter {
        int nNumOfTabs;

        public PagerAdapter(FragmentManager fm, int nNumOfTabs) {
            super(fm);
            this.nNumOfTabs = nNumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            Log.i("position:", ""+position);
            position = position % 3;
            if (position == 0) {
                //即时控制界面
                Log.i("position:", "commonFragment");
                CommonFragment   commonFragment = new CommonFragment();
                fragmentState = 0;
                return commonFragment;
            }else if(position==1) {
                //加热预约界面
                Log.i("position:", "ConstantFragment");
                ConstantFragment constantFragment = new ConstantFragment();
                fragmentState = 1;
                return constantFragment;

            }else//  if(position==2)
            {
                //理疗预约界面
                Log.i("position:", "EnhanceFragment");
                EnhanceFragment enhanceFragment = new EnhanceFragment();
                fragmentState = 2;
                return enhanceFragment;
            }
        }

        @Override
        public int getCount() {
            return nNumOfTabs;
        }

    }
    public void toChangeVISIBLE(){
        tv_side.setVisibility(View.VISIBLE);
        tb_switch.setVisibility(View.VISIBLE);
        btn_is_change.setVisibility(View.GONE);
    }
    public void toChangeGone(){
        tv_side.setVisibility(View.GONE);
        tb_switch.setVisibility(View.GONE);
        btn_is_change.setVisibility(View.GONE);
    }

    public void onResume(){
//        WTFApplication.userData= SharedPreUtil.getInstance().getUser();//WTFApplication.upUserData();
        if(WTFApplication.userData.isHaveDevice()) {//防止意外
            tv_title.setText(WTFApplication.userData.getSelDeviceAlias());
        }
        else//防止意外,应该永不执行
        {
            MainActivity.MainActivity.finish();
            Intent intent = new Intent();
            intent.setClass(getActivity(), MainActivity.class);
            MainActivity.state = 0;
            startActivity(intent);
            getActivity().finish();
        }
        super.onResume();
    }

}
