package ceui.lisa.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.UUID;

import ceui.lisa.feature.ICompatibilityWithGestureNavigation;


public abstract class BaseFragment<Layout extends ViewDataBinding> extends Fragment implements ICompatibilityWithGestureNavigation {

    protected View rootView;
    @NonNull
    protected Layout baseBind;
    protected String className = getClass().getSimpleName() + " ";

    protected int mLayoutID = -1;

    protected FragmentActivity mActivity;
    protected Context mContext;
    private boolean isVertical;
    protected boolean isInit;
    protected String uuid;

    public BaseFragment() {
        uuid = UUID.randomUUID().toString();
        Log.d(className, " newInstance " + uuid);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mActivity = requireActivity();
            mContext = requireContext();

            Bundle bundle = getArguments();
            if (bundle != null) {
                initBundle(bundle);
            }

            Intent intent = mActivity.getIntent();
            if (intent != null) {
                Bundle activityBundle = intent.getExtras();
                if (activityBundle != null) {
                    initActivityBundle(activityBundle);
                }
            }

            initModel();

            //获取屏幕方向
            if (getResources() != null) {
                int ori = getResources().getConfiguration().orientation;
                if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                    isVertical = false;
                } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
                    isVertical = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        try {
            isInit = true;
            if (rootView != null) {
                if (baseBind == null) {
                    baseBind = DataBindingUtil.bind(rootView);
                }
                return rootView;
            }
            initLayout();

            if (mLayoutID != -1) {
                baseBind = DataBindingUtil.inflate(inflater, mLayoutID, container, false);
                if (baseBind != null) {
                    rootView = baseBind.getRoot();
                } else {
                    rootView = inflater.inflate(mLayoutID, container, false);
                }
                initView();
                initCompatibilityWithGestureNavigation();
                initData();
                return rootView;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            rootView.setTag(uuid);
            if (isVertical) {
                vertical();
            } else {
                horizon();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void initLayout();

    protected void initBundle(Bundle bundle) {

    }

    protected void initActivityBundle(Bundle bundle) {

    }

    protected void initView() {

    }

    protected void initData() {

    }

    public void horizon() {

    }

    public void vertical() {

    }

    public void finish() {
        if (mActivity != null) {
            mActivity.finish();
        }
    }

    public void initModel() {

    }


    /**
     * 适配手势导航栏
     */
    @Override
    public void initCompatibilityWithGestureNavigation() {
        if (!isAdaptTop() && !isAdaptBottom()) {
            return;
        }
        View decorView = baseBind.getRoot();
        decorView.setOnApplyWindowInsetsListener((v, insets) -> {
            int systemWindowInsetBottom = insets.getSystemWindowInsetBottom();
            int systemWindowInsetTop = insets.getSystemWindowInsetTop();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                topView().setPadding(
                        topView().getPaddingLeft(),
                        isAdaptTop() ? systemWindowInsetTop / 2 : 0,
                        topView().getPaddingRight(),
                        topView().getPaddingBottom());
                bottomView().setPadding(
                        bottomView().getPaddingLeft(),
                        bottomView().getPaddingTop(),
                        bottomView().getPaddingRight(),
                        isAdaptBottom() ? systemWindowInsetBottom : 0);
                if (isAdaptBackgroundColor()) {
                    TypedValue typedValue = new TypedValue();
                    if (mActivity.getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true)) {
                        // how to get color?
                        int colorWindowBackground = typedValue.data;// **just add this line to your code!!**
                        decorView.setBackgroundColor(colorWindowBackground);
                    }
                }
            }
            return insets;
        });

    }

    /**
     * 获取top-bar 在启用适配时调用，修改此view的padding top
     *
     * @return view 布局
     */
    @Override
    public View topView() {
        return baseBind.getRoot();
    }

    /**
     * 获取view bottom 在启用适配时调用，修改此view的padding bottom
     *
     * @return view 布局
     */
    @Override
    public View bottomView() {
        return baseBind.getRoot();
    }

    /**
     * 是否适配背景色
     *
     * @return 是否适配
     */
    @Override
    public boolean isAdaptBackgroundColor() {
        return true;
    }

    /**
     * 石佛启用top适配
     *
     * @return 是否适配
     */
    @Override
    public boolean isAdaptTop() {
        return false;
    }

    /**
     * 是否启用底部适配
     *
     * @return 是否适配
     */
    @Override
    public boolean isAdaptBottom() {
        return false;
    }
}
