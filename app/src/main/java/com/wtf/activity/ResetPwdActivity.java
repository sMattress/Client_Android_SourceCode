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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.utils.ConnectDirect;

import java.lang.ref.WeakReference;

public class ResetPwdActivity extends AppCompatActivity {
    private EditText edit_pwd; // 新密码编辑框
    private EditText edit_again; // 确认密码编辑框
    private TextInputLayout resetPwdWrapper, pwdAgainWrapper;
    private String newPwd;
    private String pwdAgain;
    private ProgressDialog mDialog;
    private int flag = 0;
    private String account;
    private String code;
    private reset_handler pwdHandler;

    private Long timestamp;
    private String sign;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpwd);
        token = WTFApplication.userData.getToken();
        initViews();
    }

    private void initViews() {
        TextView tv_title = (TextView) findViewById(R.id.tv_main_title);
        tv_title.setText(R.string.reset_pwd);
        // 通过findViewById方法获得实例
        resetPwdWrapper = (TextInputLayout) findViewById(R.id.resetPwdWrapper);
        pwdAgainWrapper = (TextInputLayout) findViewById(R.id.pwdAgainWrapper);
        edit_pwd = (EditText) findViewById(R.id.edit_reset_pwd);
        edit_again = (EditText) findViewById(R.id.edit_reset_pwdAgain);
        edit_pwd.setFocusableInTouchMode(false);
        edit_again.setFocusableInTouchMode(false);
        edit_pwd.setOnClickListener(listener);
        edit_again.setOnClickListener(listener);
        edit_pwd.addTextChangedListener(pwdWatcher);
        edit_again.addTextChangedListener(againWatcher);
        account = getIntent().getExtras().getString("account");
        code = getIntent().getExtras().getString("code");
        // 实例化一个MyHandler对象
        pwdHandler = new reset_handler(this);
    }

    private TextWatcher pwdWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().equals("")) {
                resetPwdWrapper.setErrorEnabled(true);
                resetPwdWrapper.setError("请输入新密码");
            } else {
                resetPwdWrapper.setErrorEnabled(false);
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
                pwdAgainWrapper.setErrorEnabled(true);
                pwdAgainWrapper.setError("请输入新密码");
            } else {
                pwdAgainWrapper.setErrorEnabled(false);
            }
        }
    };

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 当点击了相应tab时，选中相应tab
                case R.id.edit_reset_pwd:
                    edit_pwd.setFocusableInTouchMode(true);
                    break;
                case R.id.edit_reset_pwdAgain:
                    edit_again.setFocusableInTouchMode(true);
                    break;
                default:
                    break;
            }
        }
    };

    public void back(View v) {
        finish();
    }

    //隐藏键盘
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void resetPwd(View v) {
        hideKeyboard();
        newPwd =  edit_pwd.getText().toString();
        pwdAgain = edit_again.getText().toString();
        if ("".equals(newPwd)) {
            resetPwdWrapper.setError("请输入新密码");
        } else if (!newPwd.matches("^[a-zA-Z][\\w/^]{7,31}$")) {
            resetPwdWrapper.setError("请输入正确的密码");
        } else if ("".equals(pwdAgain)) {
            pwdAgainWrapper.setError("请输入确定新密码");
        } else if (!(newPwd).equals(pwdAgain)) {
            pwdAgainWrapper.setError("两次输入的密码不相同");
        } else {
            // 判断网络连接
            ConnectDirect cd = new ConnectDirect(this.getApplicationContext());
            Boolean isInternetPresent = cd.isConnectingToInternet();
            if (!isInternetPresent) {
                Toast.makeText(ResetPwdActivity.this, "未检测到网络，请打开网络连接",
                        Toast.LENGTH_SHORT).show();
            } else {
                // 开始获取数据线程
                mDialog = new ProgressDialog(ResetPwdActivity.this);
                mDialog.setMessage("重置密码...");
                mDialog.show();



				/*SmartSleepApplication application = (SmartSleepApplication)getApplication();
                application.getUserAction().resetPwd(account, newPwd, new ActionCallbackListener<Void>() {
					@Override
					public void onSuccess(Void data) {
						pwdHandler.sendEmptyMessage(0);
					}

					@Override
					public void onValidate(String errorEvent, String message) {

					}

					@Override
					public void onFailure(int flag) {
						if(flag==0){
							pwdHandler.sendEmptyMessage(1);
						}else{
							pwdHandler.sendEmptyMessage(2);
						}
					}
				});*/
            }
        }
    }

    // Handler
    static class reset_handler extends Handler {
        WeakReference<ResetPwdActivity> mActivity;

        reset_handler(ResetPwdActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ResetPwdActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "设置成功！", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(theActivity, LoginActivity.class);
                    theActivity.startActivity(intent);
                    FindPwdActivity.instance.finish();
                    theActivity.finish();
                    break;
                case 1:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "网络连接超时了，请重试", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 2:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "重置密码失败，请重试", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    ;
}
