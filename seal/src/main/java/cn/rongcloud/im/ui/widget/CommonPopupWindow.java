package cn.rongcloud.im.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;


/**
 * 通用PopupWindow
 * Created by Administrator on 2017/6/15.
 */

public class CommonPopupWindow extends PopupWindow {

    PopupController mController;

    @Override
    public int getWidth() {
       return mController.mContentView.getWidth();
    }

    public int getHeight(){
        return mController.mContentView.getHeight();
    }

    private CommonPopupWindow(Context context) {
        mController = new PopupController(context, this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mController.setBackGroundLevel(1.0f);
    }

    public static class Builder {
        private PopupController.PopupParams mParams;
        private ViewInterface mInterface;

        public Builder(Context context) {
            mParams = new PopupController.PopupParams(context);
        }

        public Builder setView(int layoutId){
            mParams.view = null;
            mParams.layoutId = layoutId;
            return this;
        }

        public Builder setView(View view){
            mParams.view = view;
            mParams.layoutId = 0;
            return this;
        }

        public Builder setWidthAndHeight(int width, int height){
            mParams.width = width;
            mParams.height = height;
            return this;
        }

        public Builder setBackgroundAlpha(float level){
            mParams.alpha = level;
            mParams.isShowBg = true;
            return this;
        }

        public Builder setOutsideTouchable(boolean touchable){
            mParams.isTouchOutside = touchable;
            return this;
        }

        public Builder setAnimationStyle(int animationStyle){
            mParams.animotionStyle = animationStyle;
            mParams.isAnim = true;
            return this;
        }

        public Builder setViewInterface(ViewInterface viewInterface){
            mInterface = viewInterface;
            return this;
        }

        public CommonPopupWindow create(){
            CommonPopupWindow popupWindow = new CommonPopupWindow(mParams.context);
            mParams.apply(popupWindow.mController);

            if (mInterface != null && mParams.layoutId != 0){
                mInterface.setChildView(popupWindow.mController.mContentView, mParams.layoutId);
            }

            measureWidthAndHeight(popupWindow.mController.mContentView);
            return popupWindow;
        }
    }

    /**
     * 具体子view的操作放到接口里，由调用者具体实现
     */
    public interface ViewInterface{
        /**
         * @param view 内容view
         * @param childId 子view id
         */
        void setChildView(View view, int childId);
    }

    public static void measureWidthAndHeight(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
    }
}
