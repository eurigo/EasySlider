package com.eurigo.easyslider;

/**
 * @author eurigo
 * Created on 2023/11/23 15:01
 * desc   : 进度监听
 */
public interface OnValueChangeListener {

    /**
     * 进度改变
     * @param value 当前进度
     * @param isTouch 是否是手动拖动
     */
     void onValueChange(int value, boolean isTouch);


    /**
     * 结束拖动
     * @param value 当前进度
     */
     void onStopTrackingTouch(int value);
}
