package com.wtf.fragment;

/**
 * Created by liyan on 2016/11/30.
 */

import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * 延迟界面预加载
 */
public abstract class BaseFragment extends Fragment {

    /** Fragment当前状态是否可见 */
    protected boolean isVisible;
    /*是否已经准备好*/
    protected boolean isPrepared = false;

    private Toast toast = null;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }


    /**
     * 可见
     */
    protected void onVisible() {
        lazyLoad();
    }


    /**
     * 不可见
     */
    protected void onInvisible() {


    }


    /**
     * 延迟加载
     * 子类必须重写此方法
     */
    protected abstract void lazyLoad();

    /**
     * 显示最后的Toast
     */
    protected void showToast(String msg) {
        if(getActivity() != null) {
            if (toast == null) {
                toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
            } else {
                toast.setText(msg);
            }
            toast.show();
        }
    }
}
