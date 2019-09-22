package ziliche.top.function.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 多功能recyclerView
 *
 * @author eddie
 * @date 2019/07/04
 */
@SuppressLint("ClickableViewAccessibility")
public class FunctionRecyclerView extends RecyclerView {

    private static final String TAG = "FunctionRecyclerView";
    private static final int VIEW_TYPE_REFRESH_HEADER = 9999;
    private static final int VIEW_TYPE_LOAD_MORE_FOOTER = 9998;
    private static final int VIEW_TYPE_CONTENT = 9997;

    private int mMaxHeaderViewHeight;
    private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFooterViews = new SparseArrayCompat<>();

    public static final int REFRESH_STATE_IDLE = 1;
    public static final int REFRESH_STATE_PULLING = 2;
    public static final int REFRESH_STATE_DRAGGING = 3;
    public static final int REFRESH_STATE_REFRESHING = 4;
    private int mTouchSlop;
    private int mLastEventY;
    private int mLastEventX;
    private AdapterDataObserver dataObserver;

    @IntDef({REFRESH_STATE_IDLE, REFRESH_STATE_DRAGGING, REFRESH_STATE_REFRESHING, REFRESH_STATE_PULLING})
    @Retention(RetentionPolicy.SOURCE)
    @interface State {
    }

    @State
    private int mState = REFRESH_STATE_IDLE;

    public FunctionRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public FunctionRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FunctionRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        dataObserver = new DataObserver();
        mMaxHeaderViewHeight = ScreenUtil.dp2px(context, 250);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            mLastEventY = (int) e.getY();
        }
        Log.d(TAG, "onInterceptTouchEvent action is " + action);
        if (isSlideTop() && action == MotionEvent.ACTION_MOVE && Math.abs(e.getY() - mLastEventY) > 0) {
            mLastEventY = (int) e.getY();
            mLastEventX = (int) e.getX();
            Log.d(TAG, "onInterceptTouchEvent is true");
            return true;
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        Log.d(TAG, "action is " + action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastEventY = (int) ev.getY();
                mLastEventX = (int) ev.getX();
                mState = REFRESH_STATE_PULLING;
                break;
            case MotionEvent.ACTION_MOVE:
                int currentY = (int) ev.getY();
                int currentX = (int) ev.getX();
                int shiftX = Math.abs(currentX - mLastEventX);
                int shiftY = Math.abs(currentY - mLastEventY);
                int dy = currentY - mLastEventY;
                mLastEventY = currentY;
                mLastEventX = currentX;
                Log.d(TAG, "dy is " + dy);
                //判断是否更新
                if (shiftY > shiftX && isSlideTop()) {
                    Log.d(TAG, "shiftX is " + shiftX + ", shiftY is " + shiftY + " touchSlop is " + mTouchSlop);
                    mState = REFRESH_STATE_DRAGGING;
                    refreshHeaderHeight(dy);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mState == REFRESH_STATE_DRAGGING) {

                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * @param dy dy
     */
    private void refreshHeaderHeight(int dy) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null) {
            throw new NullPointerException("layoutManager MUST not be null");
        }
        View headerView = layoutManager.findViewByPosition(0);
        Log.d(TAG, "headerView is " + headerView);
        if (headerView != null) {
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) headerView.getLayoutParams();
            marginLayoutParams.height += dy;
            Log.d(TAG, "height is " + marginLayoutParams.height);
            headerView.setLayoutParams(marginLayoutParams);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        WrapperAdapter mWrapperAdapter = new WrapperAdapter(adapter);
        super.setAdapter(mWrapperAdapter);
        adapter.registerAdapterDataObserver(dataObserver);
        dataObserver.onChanged();
    }

    private class DataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            super.onChanged();
        }
    }

    private class WrapperAdapter extends RecyclerView.Adapter {

        private final Adapter adapter;

        WrapperAdapter(Adapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder = null;
            if (viewType == VIEW_TYPE_REFRESH_HEADER) {
                viewHolder = LoadingHeaderHolder.create(parent);
            } else if (viewType == VIEW_TYPE_LOAD_MORE_FOOTER) {
                viewHolder = LoadingFooterHolder.create(parent);
            } else if (viewType == VIEW_TYPE_CONTENT) {
                return adapter.onCreateViewHolder(parent, viewType);
            } else {
                View view = mHeaderViews.get(viewType);
                if (view != null) {
                    viewHolder = new ViewHolder(view) {
                        @NonNull
                        @Override
                        public String toString() {
                            return super.toString();
                        }
                    };

                }
                View footerView = mFooterViews.get(viewType);
                if (footerView != null) {
                    viewHolder = new ViewHolder(footerView) {
                        @NonNull
                        @Override
                        public String toString() {
                            return super.toString();
                        }
                    };
                }
            }
            return viewHolder;
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (position == 0) {
                            return gridManager.getSpanCount();
                        } else if (isHeaders(position)) {
                            return gridManager.getSpanCount();
                        } else if (isContents(position)) {
                            return 1;
                        } else {
                            return gridManager.getSpanCount();
                        }
                    }
                });
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && (isHeaders(holder.getLayoutPosition()) ||
                    isFooter(holder.getLayoutPosition()) ||
                    holder.getLayoutPosition() == 0 ||
                    isFooters(holder.getLayoutPosition()))) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            int itemCount = 0;
            if (adapter != null) {
                itemCount = adapter.getItemCount();
            }
            return mHeaderViews.size() + mFooterViews.size() + 2 + itemCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return VIEW_TYPE_REFRESH_HEADER;
            } else if (isFooter(position)) {
                return VIEW_TYPE_LOAD_MORE_FOOTER;
            } else if (isHeaders(position)) {
                return mHeaderViews.keyAt(position - 1);
            } else if (isContents(position)) {
                return VIEW_TYPE_CONTENT;
            } else if (isFooters(position)) {
                if (adapter != null) {
                    return mFooterViews.keyAt(position - mHeaderViews.size() - adapter.getItemCount() - 1);
                } else {
                    return mFooterViews.keyAt(position - mHeaderViews.size() - 1);
                }
            } else {
                throw new RuntimeException("invalid Item type");
            }
        }


        boolean isFooter(int position) {
            return position == getItemCount() - 1;
        }

        boolean isHeaders(int position) {
            return mHeaderViews != null && mHeaderViews.size() > 0 && position <= mHeaderViews.size();
        }

        boolean isContents(int position) {
            return adapter != null && adapter.getItemCount() > 0 && position <= mHeaderViews.size() + adapter.getItemCount();
        }

        boolean isFooters(int position) {
            if (adapter == null) {
                return mFooterViews.size() > 0 && position <= mHeaderViews.size() + mFooterViews.size();
            } else {
                return mFooterViews.size() > 0 && position <= mHeaderViews.size() + adapter.getItemCount() + mFooterViews.size();
            }
        }
    }

    /**
     * 判断是否滑倒了顶部
     * -1代表顶部,返回true表示没到顶,还可以滑
     *
     * @return true
     */
    private boolean isSlideTop() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null) {
            throw new NullPointerException("layoutManager MUST not be null");
        }
        int firstVisiblePosition = RecyclerView.NO_POSITION;
        if (layoutManager instanceof LinearLayoutManager) {
            firstVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] firstVisiblePositions = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
            int minPosition = Integer.MAX_VALUE;
            for (int position : firstVisiblePositions) {
                if (position == RecyclerView.NO_POSITION) {
                    continue;
                }
                if (position < minPosition) {
                    minPosition = position;
                }
            }
            firstVisiblePosition = minPosition;
        }
        Log.d(TAG, "firstPosition is " + firstVisiblePosition);
        return firstVisiblePosition == 0;
    }
}
