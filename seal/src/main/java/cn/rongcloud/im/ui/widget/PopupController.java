package cn.rongcloud.im.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * Created by Administrator on 2017/6/15.
 */

public class PopupController {
    private Context mContext;
    private PopupWindow mPopupWindow;
    private int mResLayout; //layout id
    private View mView;
    View mContentView; //内容view
    private Window mWindow;

    public PopupController(Context context, PopupWindow popupWindow) {
        this.mContext = context;
        this.mPopupWindow = popupWindow;
    }

    public void setView(View view){
        mView = view;
        mResLayout = 0;
        installContent();
    }

    public void setView(int resLayout){
        mView = null;
        mResLayout = resLayout;
        installContent();
    }

    /**
     * 设置内容view
     */
    private void installContent(){
        if (mView != null){
            mContentView = mView;
        }else if (mResLayout != 0){
            mContentView = LayoutInflater.from(mContext).inflate(mResLayout, null);
        }

        mPopupWindow.setContentView(mContentView);
    }

    /**
     * 设置宽高
     * @param width
     * @param height
     */
    public void setWidthAndHeigth(int width, int height){
        if (width == 0 || height == 0){
            mPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }else {
            mPopupWindow.setWidth(width);
            mPopupWindow.setHeight(height);
        }
    }

    /**
     * 设置背景透明度
     * @param level
     */
    public void setBackGroundLevel(float level){
        mWindow = ((Activity)mContext).getWindow();
        WindowManager.LayoutParams params = mWindow.getAttributes();
        params.alpha = level;
        mWindow.setAttributes(params);
    }

    /**
     * 设置动画
     * @param animationStyle
     */
    private void setAnimationStyle(int animationStyle){
        mPopupWindow.setAnimationStyle(animationStyle);
    }

    /**
     * 设置外部是否可点击
     * @param touchable
     */
    private void setOutsideTouchable(boolean touchable){
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000)); //设置透明背景
        mPopupWindow.setOutsideTouchable(touchable);
        mPopupWindow.setFocusable(touchable);
    }

    static class PopupParams{
        public Context context;
        public int layoutId; //布局id
        public float alpha; //透明度
        public int width, height; //款、高
        public boolean isShowBg; //是否显示背景
        public View view;
        public boolean isTouchOutside = true;
        public int animotionStyle; //动画id
        public boolean isAnim;

        public PopupParams(Context context) {
            this.context = context;
        }

        public void apply(PopupController controller){
            if (view != null){
                controller.setView(view);
            }else if (layoutId != 0){
                controller.setView(layoutId);
            }else {
                throw new IllegalArgumentException("PopupView's contentView is null");
            }

            controller.setWidthAndHeigth(width, height);
            controller.setOutsideTouchable(isTouchOutside);

            //背景
            if (isShowBg){
                controller.setBackGroundLevel(alpha);
            }
            //动画
            if (isAnim){
                controller.setAnimationStyle(animotionStyle);
            }
        }
    }
}
