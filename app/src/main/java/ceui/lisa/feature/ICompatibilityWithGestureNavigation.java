package ceui.lisa.feature;

import android.view.View;

public interface ICompatibilityWithGestureNavigation {
    /**
     * 适配手势导航栏
     */
    void initCompatibilityWithGestureNavigation();

    /**
     * 获取top-bar 在启用适配时调用，修改此view的padding top
     *
     * @return view 布局
     */
    View topView();

    /**
     * 获取view bottom 在启用适配时调用，修改此view的padding bottom
     *
     * @return view 布局
     */
    View bottomView();

    /**
     * 是否适配背景色
     *
     * @return 是否适配
     */
    default boolean isAdaptBackgroundColor() {
        return true;
    }

    /**
     * 石佛启用top适配
     *
     * @return 是否适配
     */
    default boolean isAdaptTop() {
        return false;
    }

    /**
     * 是否启用底部适配
     *
     * @return 是否适配
     */
    default boolean isAdaptBottom() {
        return false;
    }

}
