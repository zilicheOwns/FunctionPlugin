package ziliche.top.function.recyclerview;

import android.content.Context;

/**
 * @author eddie
 */
public class ScreenUtil {

    /**
     * dp转px
     *
     * @param context context
     * @param dpValue value
     * @return px
     */
    public static int dp2px(Context context, float dpValue) {
        final float fontScale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * fontScale + 0.5f);
    }

    /**
     * dp转px
     *
     * @param context context
     * @param pxValue value
     * @return px
     */
    public static int px2dp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / fontScale + 0.5f);
    }


}
