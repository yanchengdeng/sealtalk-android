package cn.rongcloud.im.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;

/**
 * 上拉自动加载更多listview
 *
 * Created by Administrator on 2017/9/6.
 */

public class AutoLoadListView extends RecyclerView {
    private static final String TAG = "AutoLoadListView";

    private static final int VISIBLE_THRESHOLD = 6; //到最后第几个开始加载更多

    private boolean mIsLoading = true; //是否正在加载
    private OnLoadMoreCallback mLoadingMore;

    public void setOnloadMore(OnLoadMoreCallback callback){
        mLoadingMore = callback;
    }

    public AutoLoadListView(Context context) {
        super(context);
    }

    public AutoLoadListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoLoadListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        LayoutManager layoutManager = getLayoutManager();
        int itemCount = layoutManager.getItemCount();
        if (layoutManager instanceof LinearLayoutManager){
            LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
            int lastPosition = manager.findLastVisibleItemPosition();

            if (!mIsLoading && (lastPosition >= (itemCount - 5))) {
                if (mLoadingMore != null) {
                    mIsLoading = true;
                    mLoadingMore.onLoadMore();
                }
            }
        }else if (layoutManager instanceof StaggeredGridLayoutManager){
            StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) layoutManager;
            int[] lastPositions = manager.findLastVisibleItemPositions(new int[manager.getSpanCount()]);
            int lastVisiblePos = getMaxElem(lastPositions);
            if (!mIsLoading && lastVisiblePos > (itemCount - VISIBLE_THRESHOLD)){
                mIsLoading = true;
                mLoadingMore.onLoadMore();
            }
        }else if (layoutManager instanceof GridLayoutManager){
            GridLayoutManager manager = (GridLayoutManager) layoutManager;
            int lastPositions = manager.findLastVisibleItemPosition();
            if (!mIsLoading && (lastPositions >= (itemCount - VISIBLE_THRESHOLD))) {
                if (mLoadingMore != null) {
                    mIsLoading = true;
                    mLoadingMore.onLoadMore();
                }
            }
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
    }

    /**
     * 加载完毕
     */
    public void completeLoad(){
        Log.v(TAG, "completeLoad");
        mIsLoading = false;
    }

    private int getMaxElem(int[] arr) {
        int size = arr.length;
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            if (arr[i]>maxVal)
                maxVal = arr[i];
        }
        return maxVal;
    }

    /**
     * Created by Administrator on 2017/9/6.
     */

    public interface OnLoadMoreCallback {
        void onLoadMore();
    }
}
