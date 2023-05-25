package ceui.lisa.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowInsets;

import com.blankj.utilcode.util.BarUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import ceui.lisa.R;
import ceui.lisa.feature.ICompatibilityWithGestureNavigation;
import ceui.lisa.interfaces.FeedBack;
import ceui.lisa.utils.Common;
import ceui.lisa.utils.Local;


public abstract class BaseActivity<Layout extends ViewDataBinding> extends AppCompatActivity implements ICompatibilityWithGestureNavigation {

    protected Context mContext;
    protected FragmentActivity mActivity;
    protected int mLayoutID;
    protected Layout baseBind;
    protected String className = this.getClass().getSimpleName() + " ";

    public static final int ASK_URI = 42;
    private FeedBack mFeedBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            updateTheme();

            mLayoutID = initLayout();

            mContext = this;
            mActivity = this;

            Intent intent = getIntent();
            if (intent != null) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    initBundle(bundle);
                }
            }

            if (hideStatusBar()) {
                BarUtils.transparentStatusBar(this);
            } else {
                getWindow().setStatusBarColor(Common.resolveThemeAttribute(mContext, R.attr.colorPrimary));
            }
            try {
                baseBind = DataBindingUtil.setContentView(mActivity, mLayoutID);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            initModel();
            initView();
            initCompatibilityWithGestureNavigation();
            initData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 适配手势导航栏
     */
    @Override
    public void initCompatibilityWithGestureNavigation(){
        View decorView = getWindow().getDecorView();
        if (decorView != null) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                int systemWindowInsetBottom = insets.getSystemWindowInsetBottom();
                int systemWindowInsetTop = insets.getSystemWindowInsetTop();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    topView().setPadding(
                            topView().getPaddingLeft(),
                            isAdaptTop()?systemWindowInsetTop/2:0,
                            topView().getPaddingRight(),
                            topView().getPaddingBottom());
                    bottomView().setPadding(
                            bottomView().getPaddingLeft(),
                            bottomView().getPaddingTop(),
                            bottomView().getPaddingRight(),
                            isAdaptBottom()?systemWindowInsetBottom:0);
                    if (isAdaptBackgroundColor()) {
                        TypedValue typedValue = new TypedValue();
                        if (getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true)) {
                            // how to get color?
                            int colorWindowBackground = typedValue.data;// **just add this line to your code!!**
                            decorView.setBackgroundColor(colorWindowBackground);
                        }
                    }
                }
                return insets;
            });
        }

    }

    /**
     * 获取top-bar 在启用适配时调用，修改此view的padding top
     * @return view 布局
     */
    @Override
    public View topView(){
        return getWindow().getDecorView();
    }

    /**
     * 获取view bottom 在启用适配时调用，修改此view的padding bottom
     * @return view 布局
     */
    @Override
    public View bottomView(){
        return getWindow().getDecorView();
    }

    /**
     * 是否适配背景色
     * @return 是否适配
     */
    @Override
    public boolean isAdaptBackgroundColor(){
        return true;
    }

    /**
     * 石佛启用top适配
     * @return 是否适配
     */
    @Override
    public boolean isAdaptTop(){
        return false;
    }

    /**
     * 是否启用底部适配
     * @return 是否适配
     */
    @Override
    public boolean isAdaptBottom(){
        return false;
    }

    public void initModel() {

    }

    protected void initBundle(Bundle bundle) {

    }

    protected abstract int initLayout();

    protected abstract void initView();

    protected abstract void initData();

    public boolean hideStatusBar() {
        return false;
    }

    public static void newInstance(Intent intent, Context context) {
        context.startActivity(intent);
    }

    public void gray(boolean gray) {
        if (gray) {
            Paint grayPaint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0.0f);
            grayPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            getWindow().getDecorView().setLayerType(View.LAYER_TYPE_HARDWARE, grayPaint);
        } else {
            Paint normalPaint = new Paint();
            getWindow().getDecorView().setLayerType(View.LAYER_TYPE_HARDWARE, normalPaint);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ASK_URI) {
            if (resultCode != RESULT_OK || data == null) {
                return;
            }
            Uri treeUri = data.getData();
            if (treeUri != null) {
                Common.showLog(className + "onActivityResult " + treeUri.toString());
                Shaft.sSettings.setRootPathUri(treeUri.toString());
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                mContext.getContentResolver().takePersistableUriPermission(treeUri,takeFlags);
                Common.showToast("授权成功！");
                Local.setSettings(Shaft.sSettings);
                doAfterGranted();
            }
        }
    }

    public void doAfterGranted() {
        if (mFeedBack != null) {
            mFeedBack.doSomething();
        }
    }

    public void setFeedBack(FeedBack feedBack) {
        mFeedBack = feedBack;
    }

    private void updateTheme() {
        int current = Shaft.sSettings.getThemeIndex();
        switch (current) {
            case 0:
                setTheme(R.style.AppTheme_Index0);
                break;
            case 1:
                setTheme(R.style.AppTheme_Index1);
                break;
            case 2:
                setTheme(R.style.AppTheme_Index2);
                break;
            case 3:
                setTheme(R.style.AppTheme_Index3);
                break;
            case 4:
                setTheme(R.style.AppTheme_Index4);
                break;
            case 5:
                setTheme(R.style.AppTheme_Index5);
                break;
            case 6:
                setTheme(R.style.AppTheme_Index6);
                break;
            case 7:
                setTheme(R.style.AppTheme_Index7);
                break;
            case 8:
                setTheme(R.style.AppTheme_Index8);
                break;
            case 9:
                setTheme(R.style.AppTheme_Index9);
                break;
            default:
                setTheme(R.style.AppTheme_Default);
                break;
        }
    }
}
