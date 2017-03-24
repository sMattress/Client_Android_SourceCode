package com.wtf.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.content.multi.MultipartBody;
import com.litesuits.http.request.content.multi.StringPart;
import com.litesuits.http.request.param.HttpMethods;
import com.litesuits.http.response.Response;
import com.wtf.R;
import com.wtf.model.AppMsg;
import com.wtf.model.URL;
import com.wtf.utils.ConnectDirect;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.MyHttpListener;
import com.wtf.utils.Request;

import java.lang.ref.WeakReference;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class FindPwdActivity extends BaseActivityWithEditText {

    private EditText edit_phone; // 手机号编辑框
    private EditText edit_code;// 验证码编辑框
    private TextInputLayout findPhoneWrapper, findCodeWrapper, resetPwdWrapper;
    private TextView tv_now;
    private Button btn_code;
    private EditText edit_pwd; // 新密码编辑框

    private String account;
    private String code;
    private String newPwd;

    private ProgressDialog mDialog;
    private int time = 60;
    private boolean flag = true;
    private find_handler pwdHandler;
    public static FindPwdActivity instance = null;
    public static final String APP_KEY = "1b728173e3684";
    public static final String APP_SECRET = "4edc636625f2c0517197dfb40bc76a0a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpwd);
        initViews();
    }

    private void initViews() {
        TextView tv_find = (TextView) findViewById(R.id.tv_main_title);
        tv_find.setText(R.string.find_pwd);
        // 通过findViewById方法获得实例
        findPhoneWrapper = (TextInputLayout) findViewById(R.id.findPhoneWrapper);
        findCodeWrapper = (TextInputLayout) findViewById(R.id.findCodeWrapper);
        edit_phone = (EditText) findViewById(R.id.edit_find_cellphone);
        edit_code = (EditText) findViewById(R.id.edit_find_code);
        tv_now = (TextView) findViewById(R.id.tv_find_now);
        btn_code = (Button) findViewById(R.id.btn_find_code);
        edit_phone.setFocusableInTouchMode(false);
        edit_code.setFocusableInTouchMode(false);
        edit_phone.setOnTouchListener(listener);
        edit_code.setOnTouchListener(listener);
        edit_phone.addTextChangedListener(phoneWatcher);
        edit_code.addTextChangedListener(codeWatcher);

        resetPwdWrapper = (TextInputLayout) findViewById(R.id.resetPwdWrapper);
        edit_pwd = (EditText) findViewById(R.id.edit_reset_pwd);
        edit_pwd.setFocusableInTouchMode(false);
        edit_pwd.setOnTouchListener(listener);
        edit_pwd.addTextChangedListener(pwdWatcher);
        // 实例化一个MyHandler对象
        pwdHandler = new find_handler(this);

        SMSSDK.initSDK(this, APP_KEY, APP_SECRET);
        EventHandler eh = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh);


    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        if (network != null) {
            return network.isAvailable();
        }
        return false;
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

    private TextWatcher phoneWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().equals("")) {
                findPhoneWrapper.setErrorEnabled(true);
                findPhoneWrapper.setError("请输入手机号");
            } else {
                findPhoneWrapper.setErrorEnabled(false);
            }
        }
    };

    private TextWatcher codeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().equals("")) {
                findCodeWrapper.setErrorEnabled(true);
                findCodeWrapper.setError("请输入验证码");
            } else {
                findCodeWrapper.setErrorEnabled(false);
            }
        }
    };

    private View.OnTouchListener listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                // 当点击了相应tab时，选中相应tab
                case R.id.edit_find_cellphone:
                    edit_phone.setFocusableInTouchMode(true);
                    break;
                case R.id.edit_find_code:
                    edit_code.setFocusableInTouchMode(true);
                    break;
                case R.id.edit_reset_pwd:
                    edit_pwd.setFocusableInTouchMode(true);
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    //验证码送成功后提示文字
    private void reminderText() {
        tv_now.setVisibility(View.VISIBLE);
        time = 60;
        handlerText.sendEmptyMessageDelayed(1, 0);
    }

    Handler handlerText = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (time > 0) {
                    tv_now.setText("验证码已发送" + time + "秒");
                    time--;
                    handlerText.sendEmptyMessageDelayed(1, 1000);
                } else {
                    time = 60;
                    tv_now.setVisibility(View.GONE);
                    btn_code.setVisibility(View.VISIBLE);
                }
            } else {
                time = 60;
                tv_now.setVisibility(View.GONE);
                btn_code.setVisibility(View.VISIBLE);
            }
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            /*if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                if(result == SMSSDK.RESULT_COMPLETE) {
                    boolean smart = (Boolean)data;
                    if(smart) {
                        //通过智能验证
                    } else {
                        //依然走短信验证
                    }
                }
            }*/
            if (result == SMSSDK.RESULT_COMPLETE) {
                //提交验证码成功,验证通过
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    handlerText.sendEmptyMessage(2);
                    toFind();
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {//服务器验证码发送成功
                    reminderText();
                    Toast.makeText(getApplicationContext(), "验证码已经发送", Toast.LENGTH_SHORT).show();
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {//返回支持发送验证码的国家列表
                    Toast.makeText(getApplicationContext(), "获取国家列表成功", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.i("result", "" + result);
                btn_code.setVisibility(View.VISIBLE);
                /*if (flag) {*/
                    Toast.makeText(FindPwdActivity.this, "验证码获取失败，请检查网络", Toast.LENGTH_SHORT).show();
                /*} else {
                    ((Throwable) data).printStackTrace();
                    int resId = com.mob.tools.utils.R.getStringRes(FindPwdActivity.this, "smssdk_network_error");
                    Toast.makeText(FindPwdActivity.this, "验证码获取失败，请检查网络", Toast.LENGTH_SHORT).show();
                    edit_code.selectAll();
                    if (resId > 0) {
                        Toast.makeText(FindPwdActivity.this, resId, Toast.LENGTH_SHORT).show();
                    }
                }*/
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    public void back(View v) {
        SMSSDK.unregisterAllEventHandler();
        finish();
    }

    public void getVerificationCode(View v) {
        if (!TextUtils.isEmpty(edit_phone.getText().toString().trim())) {
            if (edit_phone.getText().toString().trim().matches("^1[34578]\\d{9}$")) {
                account = edit_phone.getText().toString().trim();
                if (isNetworkAvailable(this)) {
                    SMSSDK.getVerificationCode("86", account);
                    edit_code.requestFocus();
                    btn_code.setVisibility(View.GONE);
                }else{
                    Toast.makeText(this, "网络不可用，请检测网络",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                findPhoneWrapper.setError("请输入正确的手机号码");
                edit_phone.requestFocus();
            }
        } else {
            findPhoneWrapper.setError("请输入手机号");
            edit_phone.requestFocus();
        }
    }

    public void findPwd(View v) {
        hideKeyboard();
        // 判断网络连接
        ConnectDirect cd = new ConnectDirect(this.getApplicationContext());
        Boolean isInternetPresent = cd.isConnectingToInternet();
        if (!isInternetPresent) {
            Toast.makeText(FindPwdActivity.this, "未检测到网络，请打开网络连接",
                    Toast.LENGTH_SHORT).show();
        } else {
            account = edit_phone.getText().toString();
            code = edit_code.getText().toString();
            newPwd = edit_pwd.getText().toString();
            if (account.isEmpty() || account.equals("")) {
                findPhoneWrapper.setError("请输入手机号");
            }
            if ("".equals(newPwd)) {
                resetPwdWrapper.setError("请输入新密码");
            } else if (!newPwd.matches("^[a-zA-Z][\\w/^]{7,31}$")) {
                resetPwdWrapper.setError("请输入正确的密码");
            } else if ("".equals(code)) {
                findCodeWrapper.setError("请输入验证码");
            } else if (code.length() != 4) {
                findCodeWrapper.setError("请输入完整验证码");
            } else {
                //SMSSDK.submitVerificationCode("86", account, code);
                toFind();
                flag = false;
            }
        }
    }

    private void toFind() {
/*        findPhoneWrapper.setEnabled(false);
        findCodeWrapper.setEnabled(false);
        resetPwdWrapper.setEnabled(false);*/
        // 开始获取数据线程
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("正在验证...");
        mDialog.show();


        String forgetUrl = URL.FORGET_SECURE_INFO + "?account=" + account + "&platform=android";
        final Request postRequest = new Request(forgetUrl)
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
                            if (s.getErr_code() == 2) {
                                pwdHandler.sendEmptyMessage(3);
                            } else if (s.getErr_code() == 17) {
                                pwdHandler.sendEmptyMessage(1);
                            } else if (s.getErr_code() == 22) {
                                pwdHandler.sendEmptyMessage(4);
                            } else if (s.getErr_code() == 20) {
                                pwdHandler.sendEmptyMessage(6);
                            } else {
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
        body.addPart(new StringPart("sms", code));
        body.addPart(new StringPart("new_password", newPwd));

        postRequest.setHttpBody(body);
        HttpUtil.liteHttp.executeAsync(postRequest);

    }

    // Handler
    class find_handler extends Handler {
        WeakReference<FindPwdActivity> mActivity;

        find_handler(FindPwdActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FindPwdActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "重置密码成功", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(theActivity, "密码无效", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "验证码错误", Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "密码格式错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

}
