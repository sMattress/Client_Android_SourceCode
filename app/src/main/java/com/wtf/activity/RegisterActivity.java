package com.wtf.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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
import com.wtf.model.Param;
import com.wtf.model.URL;
import com.wtf.utils.ConnectDirect;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.MyHttpListener;
import com.wtf.utils.Request;

import java.lang.ref.WeakReference;

/*import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;*/

public class RegisterActivity extends BaseActivityWithEditText {

	private ProgressDialog mDialog;
	private String cellphone, password, confirmPassword;
	private EditText edit_cellphone, edit_password, edit_confirm_password;
	private register_handler registerHandler;
	private TextInputLayout cellphoneWrapper, passwordWrapper, confirmPasswordWrapper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initViews();
	}

	private void initViews() {
		TextView tv_title = (TextView)findViewById(R.id.tv_main_title);
		tv_title.setText(R.string.register_tv_title);
		cellphoneWrapper = (TextInputLayout) findViewById(R.id.cellphoneWrapper);
		passwordWrapper = (TextInputLayout) findViewById(R.id.nicknameWrapper);
		confirmPasswordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);
		edit_cellphone = (EditText) findViewById(R.id.edit_register_cellphone);
		edit_password = (EditText) findViewById(R.id.edit_register_password);
		edit_confirm_password = (EditText) findViewById(R.id.edit_register_confirm_password);

		edit_cellphone.setFocusableInTouchMode(false);
		edit_password.setFocusableInTouchMode(false);
		edit_confirm_password.setFocusableInTouchMode(false);
		edit_cellphone.addTextChangedListener(phoneWatcher);
		edit_password.addTextChangedListener(nameWatcher);
		edit_confirm_password.addTextChangedListener(pwdWatcher);

		edit_cellphone.setOnTouchListener(touchlistener);
		edit_password.setOnTouchListener(touchlistener);
		edit_confirm_password.setOnTouchListener(touchlistener);

		registerHandler = new register_handler(this);
	}

	private TextWatcher phoneWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			if(s.toString().trim().equals("")){
				cellphoneWrapper.setErrorEnabled(true);
				cellphoneWrapper.setError("请输入手机号");
			}else{
				cellphoneWrapper.setErrorEnabled(false);
			}
		}
	};


	private TextWatcher nameWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			if(s.toString().trim().equals("")){
				passwordWrapper.setErrorEnabled(true);
				passwordWrapper.setError("请输入密码");
			}else{
				passwordWrapper.setErrorEnabled(false);
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
			if(s.toString().trim().equals("")){
				confirmPasswordWrapper.setErrorEnabled(true);
				confirmPasswordWrapper.setError("请输入确认密码");
			}else{
				confirmPasswordWrapper.setErrorEnabled(false);
			}
		}
	};

	private View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				// 当点击了相应tab时，选中相应tab
				case R.id.edit_register_cellphone:
					edit_cellphone.setFocusableInTouchMode(true);
					break;
				case R.id.edit_register_password:
					edit_password.setFocusableInTouchMode(true);
					break;
				case R.id.edit_register_confirm_password:
					edit_confirm_password.setFocusableInTouchMode(true);
					break;
				default:
					break;
			}
		}
	};
	private OnTouchListener touchlistener = new OnTouchListener() {
		@Override
		public boolean  onTouch(View v, MotionEvent event) {
			int touch_flag=0;
			switch (v.getId()) {
				// 当点击了相应tab时，选中相应tab
				case R.id.edit_register_cellphone:
					edit_cellphone.setFocusableInTouchMode(true);
					break;
				case R.id.edit_register_password:
					edit_password.setFocusableInTouchMode(true);
					break;
				case R.id.edit_register_confirm_password:
					edit_confirm_password.setFocusableInTouchMode(true);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void register(View v) {
		hideKeyboard();
		cellphone = edit_cellphone.getText().toString();
		password = edit_password.getText().toString();
		confirmPassword = edit_confirm_password.getText().toString();
		if(cellphone.isEmpty()||cellphone.equals("")){
			Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT)
					.show();
			cellphoneWrapper.setErrorEnabled(true);
			cellphoneWrapper.setError("请输入手机号");

		}else if(password.isEmpty()|| password.equals("")){
			Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT)
					.show();
			passwordWrapper.setErrorEnabled(true);
			passwordWrapper.setError("请输入密码");

		}else if(!cellphone.matches("^1[34578]\\d{9}$")){
			Toast.makeText(this, "请输入11位的手机号！", Toast.LENGTH_SHORT)
					.show();
			cellphoneWrapper.setErrorEnabled(true);
			cellphoneWrapper.setError("请输入11位的手机号");
		}else if(!password.matches("^[a-zA-Z][\\w/^]{7,31}$")){
			Toast.makeText(this, "请输入正确的密码", Toast.LENGTH_SHORT)
					.show();
			passwordWrapper.setErrorEnabled(true);
			passwordWrapper.setError("请输入正确的密码");
		}else if(confirmPassword.isEmpty()|| !confirmPassword.equals(password)){
			Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT)
					.show();
			confirmPasswordWrapper.setErrorEnabled(true);
			confirmPasswordWrapper.setError("两次密码不一致");
		}else {
			getData();
		}
	}


	private void getData() {
		// 判断网络连接
		if (!WTFApplication.isConnectingToInternet()) {
			Toast.makeText(RegisterActivity.this, "未检测到网络，请打开网络连接",
					Toast.LENGTH_SHORT).show();
		} else {
			// 开始获取数据线程
			mDialog = new ProgressDialog(RegisterActivity.this);
			mDialog.setMessage("正在注册，请稍候...");
			mDialog.show();
			postHttp();
		}
	}

	// Handler
	static class register_handler extends Handler {
		WeakReference<RegisterActivity> mActivity;

		register_handler(RegisterActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			RegisterActivity theActivity = mActivity.get();
			switch (msg.what) {
				case 0:
					theActivity.mDialog.cancel();
					Toast.makeText(theActivity, "注册成功！", Toast.LENGTH_SHORT).show();
					/*Intent intent = new Intent();
					intent.setClass(theActivity, LoginActivity.class);
					theActivity.startActivity(intent);*/
					theActivity.finish();
					break;
				case 1:
					theActivity.mDialog.cancel();
					Toast.makeText(theActivity, "该手机号已经注册！", Toast.LENGTH_SHORT)
							.show();
					theActivity.cellphoneWrapper.setErrorEnabled(true);
					theActivity.cellphoneWrapper.setError("该手机号已经注册！");
					break;
				case 2:
					theActivity.mDialog.cancel();
					Toast.makeText(theActivity, "网络连接超时了，请稍后重试", Toast.LENGTH_SHORT)
							.show();
					break;
				case 4:
					theActivity.mDialog.cancel();
					Toast.makeText(theActivity, "输入手机号格式有误！", Toast.LENGTH_SHORT)
							.show();
					theActivity.cellphoneWrapper.setErrorEnabled(true);
					theActivity.cellphoneWrapper.setError("输入手机号格式有误!");
					break;
				case 5:
					theActivity.mDialog.cancel();
					Toast.makeText(theActivity, "输入密码格式错误！", Toast.LENGTH_SHORT)
							.show();
					theActivity.passwordWrapper.setErrorEnabled(true);
					theActivity.passwordWrapper.setError("输入密码格式错误");
					break;
			}
		}
	}

	private void postHttp(){
		String uploadUrl= URL.REGISTER ;
		final Request postRequest = new Request(uploadUrl)
				.setMethod(HttpMethods.Post)
				.setHttpListener(new MyHttpListener<AppMsg>(this) {
					@Override
					public void onSuccess(AppMsg s, Response<AppMsg> response) {
						Log.i("flag",String.valueOf(s.getFlag()));
                        if (s.getFlag()==1) {
                            registerHandler.sendEmptyMessage(0);
                        }else {
							if(s.getErr_code()==16){
								//账号已存在
								registerHandler.sendEmptyMessage(1);
							}else if(s.getErr_code()==20){
								//账号格式错误
								registerHandler.sendEmptyMessage(4);
							}else if(s.getErr_code()==21){
								//密码格式错误
								registerHandler.sendEmptyMessage(5);
							}
							else {
								registerHandler.sendEmptyMessage(2);
							}
                            Log.i("err",String.valueOf(s.getErr_code()));
                        }
					}
					public void onFailure(HttpException e, Response response) {
						registerHandler.sendEmptyMessage(2);
						super.onFailure(e, response);
					}
				});

		MultipartBody body = new MultipartBody();
		body.addPart(new StringPart("password", password));
		body.addPart(new StringPart("account", cellphone));
		postRequest.setHttpBody(body);
		HttpUtil.liteHttp.executeAsync(postRequest);
	}
}
