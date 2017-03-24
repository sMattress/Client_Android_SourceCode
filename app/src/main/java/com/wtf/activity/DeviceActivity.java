package com.wtf.activity;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.wtf.R;
/**
 * Created by liyan on 2016/11/23.
 */

public class DeviceActivity extends SBaseActivity implements View.OnClickListener {
    private ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wifi_activity_connect);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
//        //动态创建fragement
//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.add(R.id.body_frame,  WTFApplication.wifiFragment);
//        // ---add to the back stack---
//        transaction.addToBackStack(null);
//        transaction.commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }


}
