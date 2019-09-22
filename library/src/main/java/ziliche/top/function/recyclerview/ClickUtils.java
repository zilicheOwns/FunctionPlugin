

package ziliche.top.function.recyclerview;

import android.view.View;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;


public final class ClickUtils {


    private static Map<View, Long> viewWeakHashMap = new WeakHashMap<>();

    private static long FROZEN_TIME_MILLIS = 5000L;


    public static boolean isFastDoubleClick(View targetView) {
        Long nextClickTime = viewWeakHashMap.get(targetView);
        final long now = now();

        if (nextClickTime == null) {
            nextClickTime = now + FROZEN_TIME_MILLIS;
            viewWeakHashMap.put(targetView, nextClickTime);
            return false;
        }

        if (now >= nextClickTime) {
            nextClickTime = now + FROZEN_TIME_MILLIS;
            viewWeakHashMap.put(targetView, nextClickTime);
            return false;
        }

        return true;
    }

    private static long now() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }


    public static void setFrozenTimeMillis(long frozenTimeMillis) {
        FROZEN_TIME_MILLIS = frozenTimeMillis;
    }
}
