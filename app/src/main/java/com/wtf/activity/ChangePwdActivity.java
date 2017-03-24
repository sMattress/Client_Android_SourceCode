package com.wtf.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.content.multi.MultipartBody;
import com.litesuits.http.request.content.multi.StringPart;
import com.litesuits.http.request.param.HttpMethods;
import com.litesuits.http.response.Response;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.model.AppMsg;
import com.wtf.model.URL;
import com.wtf.model.UserData;
import com.wtf.utils.ConnectDirect;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.MD5Util;
import com.wtf.utils.MyHttpListener;
import com.wtf.utils.Request;

import java.lang.ref.WeakReference;

public class ChangePwdActivity extends BaseActivityWithEditText {

    private EditText edit_old; // 原密码编辑框
    private EditText edit_new; // 新密码编辑框
    private EditText edit_again; // 确认新密码编辑框
    private TextInputLayout oldPwdWrapper, newPwdWrapper, newAgainWrapper;
    private String oldPassword;
    private String newPassword;
    private String newPwdAgain;
    private ProgressDialog mDialog;
    private int flag = 0;
    private String account;
    private pwd_handler pwdHandler;

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepwd);
        token=WTFApplication.userData.getToken();
        initViews();
    }

    private void initViews() {
        TextView tv_change = (TextView) findViewById(R.id.tv_main_title);
        tv_change.setText(R.string.change_pwd);
        // 通过findViewById方法获得实例
        oldPwdWrapper = (TextInputLayout) findViewById(R.id.oldPwdWrapper);
        newPwdWrapper = (TextInputLayout) findViewById(R.id.newPwdWrapper);
        newAgainWrapper = (TextInputLayout) findViewById(R.id.newAgainWrapper);
        edit_old = (EditText) findViewById(R.id.edit_change_oldPwd);
        edit_new = (EditText) findViewById(R.id.edit_change_newPwd);
        edit_again = (EditText) findViewById(R.id.edit_change_newAgain);
        edit_old.setFocusableInTouchMode(false);
        edit_new.setFocusableInTouchMode(false);
        edit_again.setFocusableInTouchMode(false);
        edit_old.setOnTouchListener(listener);
        edit_new.setOnTouchListener(listener);
        edit_again.setOnTouchListener(listener);
        edit_old.addTextChangedListener(oldWatcher);
        edit_new.addTextChangedListener(newWatcher);
        edit_again.addTextChangedListener(againWatcher);
        account = WTFApplication.userData.getAccount();
        pwdHandler = new pwd_handler(this);
    }

    private TextWatcher oldWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().equals("")) {
                oldPwdWrapper.setErrorEnabled(true);
                oldPwdWrapper.setError("请输入当前密码");
            } else {
                oldPwdWrapper.setErrorEnabled(false);
            }
        }
    };

    private TextWatcher newWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().equals("")) {
                newPwdWrapper.setErrorEnabled(true);
                newPwdWrapper.setError("请输入新密码");
            } else {
                newPwdWrapper.setErrorEnabled(false);
            }
        }
    };

    private TextWatcher againWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().equals("")) {
                newAgainWrapper.setErrorEnabled(true);
                newAgainWrapper.setError("请输入新密码");
            } else {
                newAgainWrapper.setErrorEnabled(false);
            }
        }
    };

    private View.OnTouchListener listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                // 当点击了相应tab时，选中相应tab
                case R.id.edit_change_oldPwd:
                    edit_old.setFocusableInTouchMode(true);
                    break;
                case R.id.edit_change_newPwd:
                    edit_new.setFocusableInTouchMode(true);
                    break;
                case R.id.edit_change_newAgain:
                    edit_again.setFocusableInTouchMode(true);
                    break;
                default:
                    break;
            }
            return false;
        }
    };


    public void back(View v) {
        finish();
    }

    public void changePwd(View v) {
        hideKeyboard();
        oldPassword = edit_old.getText().toString();
        newPassword = edit_new.getText().toString();
        newPwdAgain = edit_again.getText().toString();
        if ("".equals(oldPassword)) {
            oldPwdWrapper.setError("请输入当前密码");
        }  else if ("".equals(newPassword)) {
            newPwdWrapper.setError("请输入新密码");
        } else if ("".equals(newPwdAgain)) {
            newAgainWrapper.setError("请输入新密码");
        } else if (!(newPassword).equals(newPwdAgain)) {
            newAgainWrapper.setError("两次输入的密码不相同");
        } else {
            // 判断网络连接
            ConnectDirect cd = new ConnectDirect(this.getApplicationContext());
            Boolean isInternetPresent = cd.isConnectingToInternet();
            if (!isInternetPresent) {
                Toast.makeText(ChangePwdActivity.this, "未检测到网络，请打开网络连接",
                        Toast.LENGTH_SHORT).show();
            } else {
                // 开始获取数据线程
                mDialog = new ProgressDialog(ChangePwdActivity.this);
                mDialog.setMessage("修改密码...");
                mDialog.show();

                Long timestamp = WTFApplication.getTimeStamp();
                String sign = MD5Util.encrypt("/v1/user/update/secure_info" +
                        "?account=" + account + "&timestamp=" + timestamp + "&token=" + token);

                String updateUrl = URL.UPDATE_SECURE_INFO + "?account=" + account +
                        "&timestamp=" + timestamp + "&sign=" + sign; ;
                final Request postRequest = new Request(updateUrl)
                        .setMethod(HttpMethods.Post)
                        .setHttpListener(new MyHttpListener<AppMsg>(this) {
                            @Override
                            public void onSuccess(AppMsg s, Response<AppMsg> response) {
                                Log.i("flag", String.valueOf(s.getFlag()));
                                if (s.getFlag() == 1) {
                                    pwdHandler.sendEmptyMessage(0);
                                } else {
                                    Log.i("errcode", String.valueOf(s.getErr_code()));
                                    Log.i("cause", String.valueOf(s.getCause()));
                                    if (s.getErr_code()==2){
                                        pwdHandler.sendEmptyMessage(3);
                                    }else if (s.getErr_code()==17){
                                        pwdHandler.sendEmptyMessage(1);
                                    }else if (s.getErr_code()==22){
                                        pwdHandler.sendEmptyMessage(4);
                                    }else{
                                        pwdHandler.sendEmptyMessage(5);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, Response response) {
                                pwdHandler.sendEmptyMessage(2);
                                super.onFailure(e, response);
                            }
                        });

                MultipartBody body = new MultipartBody();
                body.addPart(new StringPart("old_password", oldPassword));
                body.addPart(new StringPart("new_password", newPassword));

                postRequest.setHttpBody(body);
                HttpUtil.liteHttp.executeAsync(postRequest);
            }
        }
    }

    // Handler
    static class pwd_handler extends Handler {
        WeakReference<ChangePwdActivity> mActivity;

        pwd_handler(ChangePwdActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ChangePwdActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    MainActivity.MainActivity.finish();
                    UserInfoActivity.userInfoActivity.finish();
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "修改成功！", Toast.LENGTH_SHORT).show();
                    MainActivity.state=0;
                    WTFApplication.userData=new UserData();
                    // 用户名，密码保存在SharedPreferences
                    WTFApplication.putUserData();
                    Intent intent = new Intent();
                    intent.setClass(theActivity, LoginActivity.class);
                    theActivity.startActivity(intent);
                    theActivity.finish();
                    break;
                case 1:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "用户不存在", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    theActivity.mDialog.cancel();
                    break;
                case 3:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "服务器时间与手机时间不一致", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "输入的原始密码错误", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "密码格式错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    ;

}
