package com.wtf.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.model.AppMsg;
import com.wtf.model.DeviceData;
import com.wtf.model.URL;
import com.wtf.model.UserData;
import com.wtf.ui.UpdateDialog;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.MD5Util;
import com.wtf.utils.MyHttpListener;
import com.wtf.utils.Request;

import java.lang.ref.WeakReference;

public class LoginActivity extends BaseActivityWithEditText {

    private final static String TAG = "LoginActivity";
    private ProgressDialog mDialog;
    private String cellphone, password;
    private AutoCompleteTextView edit_userName;
    private EditText edit_password;
    private login_handler loginHandler;
    private String imgUrl;
    private TextInputLayout accountWrapper;
    private TextInputLayout passwordWrapper;
    private TextView tv_find_password;

    private String cipherText = "1234";
    private int flag;

    private int versionCode = 0;
    private String versionName = "";
    private String download = "";

    private Long timestamp;
    private String sign;
    private String token;
    private String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        flag = getIntent().getIntExtra("flag", 0);
        initViews();
    }

    private void initViews() {
        loginHandler = new login_handler(this);
        if (flag == 2) {
            Bundle bundle = getIntent().getBundleExtra("updateInfo");
            versionName = bundle.getString("versionName");
            versionCode = bundle.getInt("versionCode");
            download = bundle.getString("download");
            loginHandler.sendEmptyMessage(5);
        }
        tv_find_password=(TextView) findViewById(R.id.tv_find_password);
        accountWrapper = (TextInputLayout) findViewById(R.id.usernameWrapper);
        passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);
        edit_userName = (AutoCompleteTextView) findViewById(R.id.edit_login_name);
        edit_password = (EditText) findViewById(R.id.edit_login_password);
        edit_userName.setFocusableInTouchMode(false);
        edit_password.setFocusableInTouchMode(false);
        edit_userName.addTextChangedListener(nameWatcher);
        edit_password.addTextChangedListener(pwdWatcher);

        tv_find_password.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, FindPwdActivity.class);
                startActivity(intent);
            }
        });
        edit_userName.setOnTouchListener(touchlistener);
        edit_password.setOnTouchListener(touchlistener);

    }

    private TextWatcher nameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().equals("")) {
                accountWrapper.setErrorEnabled(true);
                accountWrapper.setError("请输入手机号");
            } else {
                accountWrapper.setErrorEnabled(false);
            }
        }
    };

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
                passwordWrapper.setErrorEnabled(true);
                passwordWrapper.setError("请输入密码");
            } else {
                passwordWrapper.setErrorEnabled(false);
            }
        }
    };

    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 当点击了相应tab时，选中相应tab
                case R.id.edit_login_name:
                    edit_userName.setFocusableInTouchMode(true);
                    break;
                case R.id.edit_login_password:
                    edit_password.setFocusableInTouchMode(true);
                    break;
                default:
                    break;
            }
        }
    };
    private View.OnTouchListener touchlistener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.edit_login_name:
                    edit_userName.setFocusableInTouchMode(true);
                    break;
                case R.id.edit_login_password:
                    edit_password.setFocusableInTouchMode(true);
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    public void register(View v) {
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void login(View v) {
        attemptLogin();
    }

    private void attemptLogin() {
        hideKeyboard();
        cellphone = edit_userName.getText().toString();
        password = edit_password.getText().toString();
        if (cellphone.isEmpty() || cellphone.equals("")) {
            Toast.makeText(this, "请输入手机号！", Toast.LENGTH_SHORT)
                    .show();
            accountWrapper.setError("请输入手机号");
        } else if (password.isEmpty() || password.equals("")) {
            Toast.makeText(this, "请输入密码！", Toast.LENGTH_SHORT)
                    .show();
            passwordWrapper.setError("请输入密码");
        } else if (!cellphone.matches("^1[34578]\\d{9}$")) {
            Toast.makeText(this, "请输入11位的手机号！", Toast.LENGTH_SHORT)
                    .show();
            accountWrapper.setErrorEnabled(true);
            accountWrapper.setError("请输入11位的手机号");
        } else if (!password.matches("^[a-zA-Z][\\w/^]{7,31}$")) {
            Toast.makeText(this, "请输入正确的密码", Toast.LENGTH_SHORT)
                    .show();
            passwordWrapper.setError("请输入正确的密码");
        } else {
            if (!WTFApplication.isConnectingToInternet()) {
                Toast.makeText(LoginActivity.this, "未检测到网络，请打开网络连接",
                        Toast.LENGTH_SHORT).show();
            } else {
                // 开始获取数据线程
                mDialog = new ProgressDialog(this);
                mDialog.setMessage("登录中，请稍候...");
                mDialog.show();

                String uploadUrl = URL.CODE + "?account=" + cellphone;

                Request request = new Request(uploadUrl);
                request.setHttpListener(new MyHttpListener<AppMsg>(this) {
                    @Override
                    public void onSuccess(AppMsg s, Response<AppMsg> response) {
                        if (s.getFlag() == 1) {
                            if (s.getParams() != null && !s.getParams().isEmpty()) {
                                WTFApplication.userData = JSONObject.parseArray(s.getParams().toJSONString(), UserData.class).get(0);
                                WTFApplication.putUserData();
                                cipherText = MD5Util.encrypt(password + WTFApplication.userData.getCode());
                                loginHandler.sendEmptyMessage(0);
                            } else {
                                loginHandler.sendEmptyMessage(3);
                            }
                        } else {
                            if (s.getErr_code() == 17) {
                                loginHandler.sendEmptyMessage(1);
                            } else {
                                loginHandler.sendEmptyMessage(3);
                            }
                        }
                    }

                    public void onFailure(HttpException e, Response response) {
                        loginHandler.sendEmptyMessage(7);
                        super.onFailure(e, response);
                    }
                });
                HttpUtil.liteHttp.executeAsync(request);
            }
        }
    }

    // Handler
    static class login_handler extends Handler {
        WeakReference<LoginActivity> mActivity;

        login_handler(LoginActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LoginActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    theActivity.loginAccount();
                    break;
                case 1:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "账户不存在，请重新确认", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 2:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "请输入正确的密码", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 3:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "登录失败了，请重试", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 4:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "未连接网络，请先联网", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 5:
                    //对话框通知用户升级程序
                    UpdateDialog updateDialog = new UpdateDialog(theActivity.versionName, theActivity.download, theActivity);
                    updateDialog.showUpdateDialog();
                    break;
                case 6:
                    theActivity.mDialog.cancel();
                    Intent intent = new Intent();
                    WTFApplication.putUserData();
                    intent.setClass(theActivity, MainActivity.class);
                    theActivity.startActivity(intent);
                    theActivity.finish();
                    break;
                case 7:
                    theActivity.mDialog.cancel();
                    break;
            }
        }
    }

    private void loginAccount() {
        String uploadUrl = URL.LOGIN + "?account=" + cellphone + "&password=" + cipherText;

        Request request = new Request(uploadUrl);
        request.setHttpListener(new MyHttpListener<AppMsg>(this) {
            @Override
            public void onSuccess(AppMsg s, Response<AppMsg> response) {
                if (s.getFlag() == 1) {
                    if (s.getParams() != null && !s.getParams().isEmpty()) {
                        WTFApplication.userData = JSONObject.parseArray(s.getParams().toJSONString(), UserData.class).get(0);
                        Log.i("Params()", s.getParams().toJSONString());
                        Log.i("token", WTFApplication.userData.getToken());
                        long current = WTFApplication.getTimeStamp();
                        long tokenExpireTime = current + WTFApplication.userData.getExpiresIn();
                        WTFApplication.userData.setTokenExpireTime(tokenExpireTime);
                        Log.i("tokenExpireTime", tokenExpireTime + "");
                        WTFApplication.userData.setAccount(cellphone);
                        WTFApplication.putUserData();
                        getData();
                    } else
                        loginHandler.sendEmptyMessage(3);
                } else {
                    if (s.getErr_code() == 17) {//账号不存在
                        loginHandler.sendEmptyMessage(1);
                    } else if (s.getErr_code() == 22) {//密码错误
                        loginHandler.sendEmptyMessage(2);
                    } else
                        loginHandler.sendEmptyMessage(3);
                }
            }

            public void onFailure(HttpException e, Response response) {
                super.onFailure(e, response);
                loginHandler.sendEmptyMessage(7);
            }
        });
        HttpUtil.liteHttp.executeAsync(request);
    }

	/*class getUserImgFromServer implements Runnable {
		@Override
		public void run() {
			try {
				URL url = new URL(imgUrl);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.connect();
				if(connection.getResponseCode()==200) {
					InputStream is = connection.getInputStream();
					Bitmap bitmap = BitmapFactory.decodeStream(is);
					Tools.setPicToStore(bitmap);
					is.close();
				}
				connection.disconnect();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}*/

    /**
     * 获取设备数据
     */
    private void getData() {
        account = WTFApplication.userData.getAccount();
        token = WTFApplication.userData.getToken();
        Log.i("Token", account);
        timestamp = WTFApplication.getTimeStamp();
        Log.i("timestamp", String.valueOf(timestamp));
        sign = MD5Util.encrypt("/v1/user/device/list" +
                "?account=" + account + "&timestamp=" + timestamp + "&token=" + token);
        String uploadUrl = URL.DEVICES_LIST + "?account=" + account +
                "&timestamp=" + timestamp + "&sign=" + sign;

        Request request = new Request(uploadUrl);
        request.setHttpListener(new MyHttpListener<AppMsg>(this) {
            @Override
            public void onSuccess(AppMsg s, Response<AppMsg> response) {
                if (s.getFlag() == 1) {
                    if (s.getParams() != null && !s.getParams().isEmpty()) {
                        WTFApplication.userData.mData = JSONObject.parseArray(s.getParams().toJSONString(), DeviceData.class);
                    }
                }
                loginHandler.sendEmptyMessage(6);
            }

            public void onFailure(HttpException e, Response response) {
                super.onFailure(e, response);
                loginHandler.sendEmptyMessage(6);

            }
        });
        HttpUtil.liteHttp.executeAsync(request);
    }

   /* public void findPwd(View v) {
        Intent intent = new Intent();
        intent.setClass(this, FindPwdActivity.class);
        startActivity(intent);
    }*/

}
