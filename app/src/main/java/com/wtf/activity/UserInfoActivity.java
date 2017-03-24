package com.wtf.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.content.multi.MultipartBody;
import com.litesuits.http.request.content.multi.StringPart;
import com.litesuits.http.request.param.HttpMethods;
import com.litesuits.http.response.Response;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.fragment.DatePickerFragment;
import com.wtf.model.AppMsg;
import com.wtf.model.Param;
import com.wtf.model.URL;
import com.wtf.model.UserData;
import com.wtf.utils.ConnectDirect;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.MD5Util;
import com.wtf.utils.MyHttpListener;
import com.wtf.utils.Request;
import com.wtf.utils.SharedPreUtil;
import com.wtf.utils.Tools;
import com.wtf.utils.wifi.ChangePasswordContent;

import java.io.File;
import java.lang.ref.WeakReference;

public class UserInfoActivity extends SBaseActivity implements
        DatePickerFragment.TheListener, OnClickListener {

    private TextView tv_userName, tv_cellphone, tv_userSex, tv_userAge;
    private ProgressDialog mDialog;
    private revise_handler reviseHandler;
    private ImageView iv_userImg;
    private String[] items = new String[]{"从相册中选择", "拍照"};
    private String[] gender_items = new String[]{"男", "女"};
    private String photoStr = "";
    public static UserInfoActivity userInfoActivity;

    /* 请求码 */
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RESULT_REQUEST_CUT = 2;
    /* 头像名称 */
    private static final String IMAGE_FILE_NAME = "user_img.jpg";
    // 创建一个以当前时间为名称的文件
    private File tempFile;
    private Bitmap photo;
    /* 修改内容 */
    private String birthday = "";
    private String name = "NoName";
    private int sex = 0;
    private String imgUrl;

    private Long timestamp;
    private String sign;
    private String token;
    private String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);
        userInfoActivity=this;
        initViews();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        TextView tv_title = (TextView) findViewById(R.id.tv_main_title);
        tv_title.setText(R.string.user_info);

        findViewById(R.id.rl_change_userImg).setOnClickListener(this);
        findViewById(R.id.rl_change_cellphone).setOnClickListener(this);
        findViewById(R.id.rl_change_userName).setOnClickListener(this);
        findViewById(R.id.rl_change_userSex).setOnClickListener(this);
        findViewById(R.id.rl_change_userAge).setOnClickListener(this);
        findViewById(R.id.rl_change_pwd).setOnClickListener(this);

        iv_userImg = (ImageView) findViewById(R.id.iv_change_userImg);
        tv_cellphone = (TextView) findViewById(R.id.tv_change_cellphone);
        tv_userName = (TextView) findViewById(R.id.tv_change_userName);
        tv_userSex = (TextView) findViewById(R.id.tv_change_userSex);
        tv_userAge = (TextView) findViewById(R.id.tv_change_userAge);

        String baseDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        Bitmap bitmap = BitmapFactory.decodeFile(baseDir + Param.FILE_NAME);
        if (bitmap != null) {
            iv_userImg.setImageBitmap(bitmap);
        }

        if (WTFApplication.userData.getAccount() != null) {
            account = WTFApplication.userData.getAccount();
            sex = WTFApplication.userData.getSex();
            name = WTFApplication.userData.getName();
            birthday = WTFApplication.userData.getBirthday();
            imgUrl = WTFApplication.userData.getImgUrl();

            token = WTFApplication.userData.getToken();

            tv_cellphone.setText(account);
            tv_userName.setText(name);
            if (sex == 1) {
                tv_userSex.setText("男");
            } else if (sex == 0) {
                tv_userSex.setText("女");
            }
            if (birthday.contains(" ")) {
                int index = birthday.indexOf(" ");
                birthday = birthday.substring(0, index);
            }
            tv_userAge.setText(birthday);
        }
        reviseHandler = new revise_handler(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // TODO: 2016/11/29 用户头像和手机号
            case R.id.rl_change_userImg:
                showImageDialog();
                break;
            /*case R.id.rl_change_cellphone:
				EditInfoActivity.actionStart(this, 1);
				break;*/
            case R.id.rl_change_userName:
                //EditInfoActivity.actionStart(this, 2);
                final EditText rename = new EditText(UserInfoActivity.this);
                AlertDialog.Builder build1 = new AlertDialog.Builder(UserInfoActivity.this);
                build1.setTitle("修改用户名")
                        .setView(rename)
                        .setPositiveButton(
                                "确认",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        if (!rename.getText().toString().equals(null) && !rename.getText().toString().equals("")) {
                                            Log.i("rename", rename.getText().toString());
                                            name = rename.getText().toString();
                                            toChange();
                                        } else {
                                            Toast.makeText(UserInfoActivity.this, "请输入更改的用户名", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        .setNegativeButton("取消", null).show();
                break;
            case R.id.rl_change_userSex:
                showGenderDialog();
                break;
            case R.id.rl_change_userAge:
                String date = birthday.replace("-", "");
                Log.i("date", date);
                int year = Integer.valueOf(date.substring(0, 4));
                int mouth = Integer.valueOf(date.substring(4, 6));
                int day = Integer.valueOf(date.substring(6));
                DialogFragment fragment = DatePickerFragment.getInstance(year, mouth, day);
                fragment.show(getSupportFragmentManager(), "datePicker");
                break;
            case R.id.rl_change_pwd:
                Intent intent = new Intent();
                intent.setClass(this, ChangePwdActivity.class);
                startActivity(intent);
            default:
                break;
        }
    }

    private void showGenderDialog() {
        new AlertDialog.Builder(this).setTitle("修改性别")
                .setItems(gender_items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            sex = 1;
                        } else {
                            sex = 0;
                        }
                        toChange();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 修改信息
     **/
    private void toChange() {
        if (!WTFApplication.isConnectingToInternet()) {
            reviseHandler.sendEmptyMessage(3);
        } else {
            // 开始获取数据线程
            mDialog = new ProgressDialog(this);
            mDialog.setMessage("修改中，请稍候...");
            mDialog.show();
            // TODO: 2016/11/28 修改信息

            timestamp = WTFApplication.getTimeStamp();
            sign = MD5Util.encrypt("/v1/user/update/base_info" +
                    "?account=" + account + "&timestamp=" + timestamp + "&token=" + token);
            String uploadUrl = URL.UPDATE_BASE_INFO + "?account=" + account +
                    "&timestamp=" + timestamp + "&sign=" + sign;
            final Request postRequest = new Request(uploadUrl)
                    .setMethod(HttpMethods.Post)
                    .setHttpListener(new MyHttpListener<AppMsg>(this) {
                        @Override
                        public void onSuccess(AppMsg s, Response<AppMsg> response) {
                            Log.i("flag", String.valueOf(s.getFlag()));
                            if (s.getFlag() == 1) {
                                reviseHandler.sendEmptyMessage(0);
                            } else {
                                Log.i("err", String.valueOf(s.getErr_code()));
                                Log.i("cause", String.valueOf(s.getCause()));
                                if (s.getErr_code() == 24) {
                                    reviseHandler.sendEmptyMessage(5);
                                }
                            }
                        }

                        @Override
                        public void onFailure(HttpException e, Response response) {
                            reviseHandler.sendEmptyMessage(2);
                            super.onFailure(e, response);
                        }
                    });

            MultipartBody body = new MultipartBody();
            body.addPart(new StringPart("name", name));
            body.addPart(new StringPart("birthday", birthday));
            body.addPart(new StringPart("sex", String.valueOf(sex)));
            body.addPart(new StringPart("img_data", imgUrl));

            postRequest.setHttpBody(body);
            HttpUtil.liteHttp.executeAsync(postRequest);
        }
    }

    /**
     * 显示头像选择对话框
     */
    private void showImageDialog() {
        new AlertDialog.Builder(this)
                .setTitle("设置你的头像")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intentFromGallery = new Intent();
                                intentFromGallery.setType("image/*"); // 设置文件类型
                                intentFromGallery
                                        .setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intentFromGallery,
                                        IMAGE_REQUEST_CODE);
                                break;
                            case 1:
                                Intent intentFromCapture = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                // 判断存储卡是否可以用，可用进行存储
                                if (Tools.hasSdcard()) {
                                    tempFile = new File(Environment.getExternalStorageDirectory(),
                                            IMAGE_FILE_NAME);
                                    intentFromCapture.putExtra(
                                            MediaStore.EXTRA_OUTPUT,
                                            Uri.fromFile(tempFile));
                                    startActivityForResult(intentFromCapture,
                                            CAMERA_REQUEST_CODE);
                                } else {
                                    Toast.makeText(UserInfoActivity.this,
                                            "请插入SD卡", Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 结果码不等于取消时候
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    startPhotoZoom(data.getData(), 150);
                    break;
                case CAMERA_REQUEST_CODE:
                    if (Tools.hasSdcard()) {
                        startPhotoZoom(Uri.fromFile(tempFile), 150);
                    } else {
                        Toast.makeText(getApplicationContext(), "未找到存储卡，无法存储照片！",
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case RESULT_REQUEST_CUT:
                    if (data != null) {
                        getImageToView(data);
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void returnDate(String date) {
        if (!birthday.equals(date)) {
            birthday = date;
            toChange();
        }
    }

    public void back(View v) {
        finish();
    }

    // Handler
    static class revise_handler extends Handler {
        WeakReference<UserInfoActivity> mActivity;

        revise_handler(UserInfoActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            UserInfoActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    theActivity.mDialog.cancel();
                    Log.i("mDialog", "" + theActivity.mDialog.isShowing());
                    WTFApplication.userData.setSex(theActivity.sex);
                    WTFApplication.userData.setName(theActivity.name);
                    WTFApplication.userData.setBirthday(theActivity.birthday);
                    WTFApplication.userData.setImgUrl(theActivity.imgUrl);
                    WTFApplication.putUserData();
                    theActivity.tv_cellphone.setText(WTFApplication.userData.getAccount());
                    theActivity.tv_userName.setText(WTFApplication.userData.getName());
                    if (WTFApplication.userData.getSex() == 1) {
                        theActivity.tv_userSex.setText("男");
                    } else if (WTFApplication.userData.getSex() == 0) {
                        theActivity.tv_userSex.setText("女");
                    }
                    if (theActivity.birthday.contains(" ")) {
                        int index = theActivity.birthday.indexOf(" ");
                        theActivity.birthday = theActivity.birthday.substring(0, index);
                    }
                    theActivity.tv_userAge.setText(theActivity.birthday);
                    Toast.makeText(theActivity, "修改成功！", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "网络连接超时了，请重试", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 2:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "修改失败！", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(theActivity, "未检测到网络，请打开网络连接",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "修改成功！", Toast.LENGTH_SHORT).show();
                    theActivity.iv_userImg.setImageBitmap(theActivity.photo);
                    Tools.setPicToStore(theActivity.photo);
                    break;
                case 5:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "生日不能超过当前日期",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     * @param size
     */
    public void startPhotoZoom(Uri uri, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, RESULT_REQUEST_CUT);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param data
     */
    private void getImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            photo = extras.getParcelable("data");
            photoStr = Tools.convertIconToString(photo);
            imgUrl = photoStr;
            /**
             * 将图片上传至服务器端
             */
            if (!WTFApplication.isConnectingToInternet()) {
                reviseHandler.sendEmptyMessage(3);
            } else {
                mDialog = new ProgressDialog(UserInfoActivity.this);
                mDialog.setMessage("正在更换头像...");
                mDialog.show();
                toChange();

			/*	user.setImgUrl(data);
				SharedPreUtil.getInstance().putUser(user);
				reviseHandler.sendEmptyMessage(4);*/
                // TODO: 2016/11/28 上传头像信息

				/*final String uploadUrl = "http://192.168.0.0:8080/upload";
				HttpListener uploadListener = new HttpListener<String>(true, false, true) {
					@Override
					public void onSuccess(String s, Response<String> response) {
						response.printInfo();
					}
					@Override
					public void onFailure(HttpException e, Response<String> response) {
						response.printInfo();
					}
					@Override
					public void onUploading(AbstractRequest<String> request, long total, long len) {
					}
				};
				final StringRequest upload = (StringRequest) new StringRequest(uploadUrl)
						.setMethod(HttpMethods.Post)
						.setHttpListener(uploadListener)
						.setHttpBody(new FileBody(new File("/sdcard/aaa.jpg")));
						//.setHttpBody(new FileBody(new File("/sdcard/aaa.jpg")));
				liteHttp.executeAsync(upload);*/


            }
        }
    }


}
