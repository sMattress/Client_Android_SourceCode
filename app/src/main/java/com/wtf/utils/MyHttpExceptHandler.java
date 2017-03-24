package com.wtf.utils;

import android.app.Activity;
import android.widget.Toast;

import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.exception.ClientException;
import com.litesuits.http.exception.HttpClientException;
import com.litesuits.http.exception.HttpNetException;
import com.litesuits.http.exception.HttpServerException;
import com.litesuits.http.exception.NetException;
import com.litesuits.http.exception.ServerException;
import com.litesuits.http.exception.handler.HttpExceptionHandler;
import com.litesuits.http.utils.HttpUtil;

public class MyHttpExceptHandler extends HttpExceptionHandler {
    private Activity activity;

    public MyHttpExceptHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected void onClientException(HttpClientException e, ClientException type) {
        switch (e.getExceptionType()) {
            case UrlIsNull:
                Toast.makeText(activity, "Url 为空", Toast.LENGTH_SHORT).show();
                break;
            case ContextNeeded:
                // some action need app context
                break;
            case PermissionDenied:
                Toast.makeText(activity, "未获取访问网络或SD卡权限", Toast.LENGTH_SHORT).show();
                break;
            case SomeOtherException:
                Toast.makeText(activity, "客户端发生异常", Toast.LENGTH_SHORT).show();
                break;
        }
        //HttpUtil.showTips(activity, "LiteHttp2.0", "Client Exception:\n" + e.toString());
        activity = null;
    }

    @Override
    protected void onNetException(HttpNetException e, NetException type) {
        switch (e.getExceptionType()) {
            case NetworkNotAvilable:
                Toast.makeText(activity, "未连接网络", Toast.LENGTH_SHORT).show();
                break;
            case NetworkUnstable:
                // maybe retried but fail
                Toast.makeText(activity, "网络不稳定", Toast.LENGTH_SHORT).show();
                break;
            case NetworkDisabled:
                Toast.makeText(activity, "已禁用该网络类型", Toast.LENGTH_SHORT).show();
                break;
            case NetworkUnreachable:
                Toast.makeText(activity, "网络无法访问", Toast.LENGTH_SHORT).show();
            default:
                break;
        }
       // HttpUtil.showTips(activity, "LiteHttp2.0", "Network Exception:\n" + e.toString());
        activity = null;
    }

    @Override
    protected void onServerException(HttpServerException e, ServerException type,
                                     HttpStatus status) {
        switch (e.getExceptionType()) {
            case ServerInnerError:
                // status code 5XX error
                Toast.makeText(activity, "服务器内部异常", Toast.LENGTH_SHORT).show();

                break;
            case ServerRejectClient:
                // status code 4XX error
                Toast.makeText(activity, "服务器拒绝或无法提供服务", Toast.LENGTH_SHORT).show();

                break;
            case RedirectTooMuch:
                Toast.makeText(activity, "重定向次数过多", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
       // HttpUtil.showTips(activity, "LiteHttp2.0", "Server Exception:\n" + e.toString());
        activity = null;
    }
}