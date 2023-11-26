package com.eurigo.easyslider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @author eurigo
 * Created on 2023/11/20 10:37
 * desc   :
 */
public class EasySlider extends View {

    private static final String TAG = "EasySlider";
    private int minValue;
    private int maxValue;
    private int value;
    private int trackActiveColor;
    private int[] trackActiveGradientColor;
    private int trackInactiveColor;
    private int[] trackInactiveGradientColor;
    private float trackRadius;
    private boolean isShowThumb;
    private int thumbColor;
    private float thumbRadius;
    private float thumbPadding;
    private float thumbWidth;
    private float thumbHeight;
    private boolean isShowProgressText;
    private float progressTextSize;
    private int progressTextColor;
    private String progressTextFormat;
    private float progressTextPadding;
    private int progressTextGravity;
    private Bitmap trackIcon;
    private float trackIconSize;
    private int trackIconTint;
    private float trackIconPadding;
    private int trackIconGravity;
    private static final int DEFAULT_TRACK_ACTIVE_COLOR = 0xff1d7dff;
    private static final int DEFAULT_TRACK_INACTIVE_COLOR = 0xff3e434c;
    private static final int COLOR_WHITE = 0xffffffff;

    private Paint trackActivePaint;
    private Paint trackInactivePaint;
    private Paint thumbPaint;
    private Paint progressTextPaint;
    private Paint trackIconPaint;
    private OnValueChangeListener onValueChangeListener;
    private boolean actionUp;
    private Point mDownPoint = new Point();
    private ValueAnimator valueAnimator;

    public EasySlider(Context context) {
        this(context, null);
    }

    public EasySlider(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasySlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.obtainAttrs(context, attrs);
        initPaint();
    }

    private void obtainAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EasySlider);
        // 进度值
        minValue = typedArray.getInt(R.styleable.EasySlider_minValue, 1);
        maxValue = typedArray.getInt(R.styleable.EasySlider_maxValue, 100);
        value = typedArray.getInt(R.styleable.EasySlider_value, 0);
        checkValue();

        // 进度条
        trackActiveColor = typedArray.getColor(R.styleable.EasySlider_trackActiveColor, DEFAULT_TRACK_ACTIVE_COLOR);
        String gradientColor = typedArray.getString(R.styleable.EasySlider_trackActiveGradientColor);
        trackActiveGradientColor = parseGradientColor(gradientColor);
        trackInactiveColor = typedArray.getColor(R.styleable.EasySlider_trackInactiveColor, DEFAULT_TRACK_INACTIVE_COLOR);
        String inactiveGradientColor = typedArray.getString(R.styleable.EasySlider_trackInactiveGradientColor);
        trackInactiveGradientColor = parseGradientColor(inactiveGradientColor);
        trackRadius = typedArray.getDimension(R.styleable.EasySlider_trackRadius, dp2px(12));

        // 标志
        isShowThumb = typedArray.getBoolean(R.styleable.EasySlider_showThumb, true);
        thumbColor = typedArray.getColor(R.styleable.EasySlider_thumbColor, COLOR_WHITE);
        thumbRadius = typedArray.getDimension(R.styleable.EasySlider_thumbRadius, dp2px(12));
        thumbPadding = typedArray.getDimension(R.styleable.EasySlider_thumbPadding, dp2px(4));
        thumbWidth = typedArray.getDimension(R.styleable.EasySlider_thumbWidth, dp2px(24));
        thumbHeight = typedArray.getDimension(R.styleable.EasySlider_thumbHeight, dp2px(24));

        // 进度文本
        isShowProgressText = typedArray.getBoolean(R.styleable.EasySlider_showProgressText, true);
        progressTextSize = typedArray.getDimension(R.styleable.EasySlider_progressTextSize, sp2px(16));
        progressTextColor = typedArray.getColor(R.styleable.EasySlider_progressTextColor, COLOR_WHITE);
        progressTextGravity = typedArray.getInt(R.styleable.EasySlider_progressTextGravity, 0);
        progressTextPadding = typedArray.getDimension(R.styleable.EasySlider_progressTextPadding, 0);
        progressTextFormat = typedArray.getString(R.styleable.EasySlider_progressTextFormat);

        // 进度图标
        trackIconSize = typedArray.getDimension(R.styleable.EasySlider_trackIconSize, dp2px(16));
        trackIconTint = typedArray.getColor(R.styleable.EasySlider_trackIconTint, -1);
        trackIconPadding = typedArray.getDimension(R.styleable.EasySlider_trackIconPadding, dp2px(4));
        trackIconGravity = typedArray.getInt(R.styleable.EasySlider_trackIconGravity, 0);
        Drawable drawable = typedArray.getDrawable(R.styleable.EasySlider_trackIcon);
        trackIcon = drawable == null ? null : getBitmap(drawable);
        typedArray.recycle();
    }

    /**
     * 统一初始化画笔
     */
    private void initPaint() {
        trackInactivePaint = getPaint(trackInactiveColor);
        trackActivePaint = getPaint(trackActiveColor);
        thumbPaint = getPaint(thumbColor);
        progressTextPaint = getPaint(progressTextColor);
        progressTextPaint.setTextSize(progressTextSize);
        trackIconPaint = getPaint(trackIconTint);
        if (trackIconTint != -1) {
            ColorFilter colorFilter = new PorterDuffColorFilter(trackIconTint, PorterDuff.Mode.SRC_IN);
            trackIconPaint.setColorFilter(colorFilter);
        }
    }

    private Paint getPaint(int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTrack(canvas);
        drawThumb(canvas);
        drawProgressText(canvas);
        drawTrackIcon(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                actionUp = false;
                mDownPoint.set(x, y);
                break;
            // 抬起
            case MotionEvent.ACTION_UP:
                actionUp = true;
                int targetValue = (int) (x / (getInactiveTrackRight() - getInactiveTrackLeft()) * maxValue);
                updateValue(targetValue, true, true);
                break;
            // 移动
            case MotionEvent.ACTION_MOVE:
                if (mDownPoint.equals(x, y)) {
                    return true;
                }
                int motionValue = (int) (x / (getInactiveTrackRight() - getInactiveTrackLeft()) * maxValue);
                updateValue(motionValue, true, false);
                break;
            default:
                break;
        }
        return true;
    }

    private void drawTrack(Canvas canvas) {
        // 底部轨道
        RectF rectF = new RectF();
        rectF.left = getInactiveTrackLeft();
        rectF.top = getInactiveTrackTop();
        rectF.right = getInactiveTrackRight();
        rectF.bottom = getInactiveTrackBottom();
        if (trackInactiveGradientColor != null) {
            LinearGradient gradient = new LinearGradient(rectF.left, rectF.top, rectF.right, rectF.bottom, trackInactiveGradientColor[0], trackInactiveGradientColor[1], Shader.TileMode.CLAMP);
            trackInactivePaint.setShader(gradient);
        }
        canvas.drawRoundRect(rectF, trackRadius, trackRadius, trackInactivePaint);
        // 进度轨道
        updateTrack(canvas, rectF);
    }

    /**
     * 更新进度轨道
     *
     * @param canvas 画布
     * @param rectF  底部轨道
     */
    private void updateTrack(Canvas canvas, RectF rectF) {
        rectF.right = getActiveTrackRight();
        if (trackActiveGradientColor != null) {
            LinearGradient gradient = new LinearGradient(rectF.left, rectF.top, rectF.right, rectF.bottom, trackActiveGradientColor[0], trackActiveGradientColor[1], Shader.TileMode.CLAMP);
            trackActivePaint.setShader(gradient);
        }
        canvas.drawRoundRect(rectF, trackRadius, trackRadius, trackActivePaint);
    }

    /**
     * 绘制标志
     *
     * @param canvas 画布
     */
    private void drawThumb(Canvas canvas) {
        if (!isShowThumb) {
            return;
        }
        RectF rectF = new RectF();
        rectF.left = getActiveTrackRight() - thumbPadding - thumbWidth;
        rectF.right = getActiveTrackRight() - thumbPadding;
        rectF.top = getHeight() / 2f - thumbHeight / 2f;
        rectF.bottom = getHeight() / 2f + thumbHeight / 2f;
        canvas.drawRoundRect(rectF, thumbRadius, thumbRadius, thumbPaint);
    }

    /**
     * 绘制进度文本
     *
     * @param canvas 画布
     */
    private void drawProgressText(Canvas canvas) {
        if (!isShowProgressText) {
            return;
        }
        // 文本
        String progressText = TextUtils.isEmpty(progressTextFormat) ? String.valueOf(value) : String.format(progressTextFormat, value);
        switch (progressTextGravity) {
            // 居中对齐, 设置居中时，padding无效
            case 0:
                drawCenterText(canvas, progressText);
                break;
            // 左对齐
            case 1:
                drawStartText(canvas, progressText);
                break;
            // 右对齐
            case 2:
                drawEndText(canvas, progressText);
                break;
            default:
                break;
        }
    }

    /**
     * 绘制进度图标
     *
     * @param canvas
     */
    private void drawTrackIcon(Canvas canvas) {
        // 如果没有图标需要绘制，直接返回
        if (!hasSingleTrackIcon()) {
            return;
        }
        switch (trackIconGravity) {
            // 单独显示在轨道中，并靠左
            case 2:
                canvas.drawBitmap(trackIcon, getInactiveTrackLeft() + trackIconPadding, getHeight() / 2f - trackIconSize / 2f, trackIconPaint);
                break;
            // 单独显示在轨道中，并靠右
            case 3:
                canvas.drawBitmap(trackIcon, getInactiveTrackRight() - trackIconSize - trackIconPadding, getHeight() / 2f - trackIconSize / 2f, trackIconPaint);
                break;
            // 单独显示在轨道中，并居中
            case 4:
                canvas.drawBitmap(trackIcon, getInactiveTrackRight() / 2 - trackIconSize / 2, getHeight() / 2f - trackIconSize / 2f, trackIconPaint);
                break;
            default:
                break;
        }
    }

    /**
     * 绘制居中对齐的文本
     *
     * @param canvas       画布
     * @param progressText 文本
     */
    private void drawCenterText(Canvas canvas, String progressText) {
        float textWidth = progressTextPaint.measureText(progressText);
        float left = getInactiveTrackRight() / 2 - (trackIconSize + trackIconPadding + textWidth) / 2;
        // 有跟随文本的图标需要绘制
        switch (trackIconGravity) {
            // 图标在文本左侧
            case 0:
                if (hasFollowProgressTextIcon()) {
                    canvas.drawBitmap(trackIcon, left, getHeight() / 2f - trackIconSize / 2f, trackIconPaint);
                    canvas.drawText(progressText, left + trackIconSize + trackIconPadding, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                } else {
                    // 没有跟随文本的图标，直接绘制文本
                    canvas.drawText(progressText, left, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                }
                break;
            // 图标在文本右侧
            case 1:
                if (hasFollowProgressTextIcon()) {
                    canvas.drawText(progressText, left, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                    canvas.drawBitmap(trackIcon, left + textWidth + trackIconPadding, getHeight() / 2f - trackIconSize / 2f, trackIconPaint);
                } else {
                    // 没有跟随文本的图标，直接绘制文本
                    canvas.drawText(progressText, left, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                }
                break;
            default:
                canvas.drawText(progressText, getInactiveTrackRight() / 2 - textWidth / 2, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                break;
        }
    }

    private void drawStartText(Canvas canvas, String progressText) {
        float textWidth = progressTextPaint.measureText(progressText);
        float left = getInactiveTrackLeft() + progressTextPadding;
        // 有跟随文本的图标需要绘制
        switch (trackIconGravity) {
            // 图标在文本左侧
            case 0:
                if (hasFollowProgressTextIcon()) {
                    canvas.drawBitmap(trackIcon, left, getHeight() / 2f - trackIconSize / 2f, trackIconPaint);
                    canvas.drawText(progressText, left + trackIconSize + trackIconPadding, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                } else {
                    // 没有跟随文本的图标，直接绘制文本
                    canvas.drawText(progressText, left, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                }
                break;
            // 图标在文本右侧
            case 1:
                if (hasFollowProgressTextIcon()) {
                    canvas.drawText(progressText, left, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                    canvas.drawBitmap(trackIcon, left + textWidth + trackIconPadding, getHeight() / 2f - trackIconSize / 2f, trackIconPaint);
                } else {
                    // 没有跟随文本的图标，直接绘制文本
                    canvas.drawText(progressText, left + progressTextPadding, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                }
                break;
            default:
                canvas.drawText(progressText, getInactiveTrackLeft() + progressTextPadding, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                break;
        }
    }

    private void drawEndText(Canvas canvas, String progressText) {
        float textWidth = progressTextPaint.measureText(progressText);
        float left = getInactiveTrackRight() - progressTextPadding - textWidth - trackIconPadding - trackIconSize;
        // 有跟随文本的图标需要绘制
        switch (trackIconGravity) {
            // 图标在文本左侧
            case 0:
                if (hasFollowProgressTextIcon()) {
                    canvas.drawBitmap(trackIcon, left, getHeight() / 2f - trackIconSize / 2f, trackIconPaint);
                    canvas.drawText(progressText, left + trackIconSize + trackIconPadding, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                } else {
                    // 没有跟随文本的图标，直接绘制文本
                    canvas.drawText(progressText, left, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                }
                break;
            // 图标在文本右侧
            case 1:
                if (hasFollowProgressTextIcon()) {
                    canvas.drawText(progressText, left, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                    canvas.drawBitmap(trackIcon, left + textWidth + trackIconPadding, getHeight() / 2f - trackIconSize / 2f, trackIconPaint);
                } else {
                    // 没有跟随文本的图标，直接绘制文本
                    canvas.drawText(progressText, getRight() - textWidth - progressTextPadding, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                }
                break;
            default:
                canvas.drawText(progressText, getInactiveTrackRight() - textWidth - progressTextPadding, getHeight() / 2f - (progressTextPaint.descent() + progressTextPaint.ascent()) / 2f, progressTextPaint);
                break;
        }
    }

    private int sp2px(float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, Resources.getSystem().getDisplayMetrics());
    }

    private int dp2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 获取未激活轨道左侧位置
     *
     * @return 未激活轨道左侧位置
     */
    private float getInactiveTrackLeft() {
        return getPaddingLeft();
    }

    /**
     * 获取未激活轨道顶部位置
     *
     * @return 未激活轨道顶部位置
     */
    private float getInactiveTrackTop() {
        return getPaddingTop();
    }

    /**
     * 获取未激活轨道右侧位置
     *
     * @return 未激活轨道右侧位置
     */
    private float getInactiveTrackRight() {
        return getWidth() - getPaddingRight();
    }

    /**
     * 获取未激活轨道底部位置
     *
     * @return 未激活轨道底部位置
     */
    private float getInactiveTrackBottom() {
        return getHeight() - getPaddingBottom();
    }

    /**
     * 是否有跟随进度文本的图标需要绘制
     */
    private boolean hasFollowProgressTextIcon() {
        return trackIcon != null && (trackIconGravity == 0 || trackIconGravity == 1);
    }

    /**
     * 是否有单独显示的图标需要绘制
     */
    private boolean hasSingleTrackIcon() {
        return trackIcon != null && (trackIconGravity == 2 || trackIconGravity == 3 || trackIconGravity == 4);
    }

    /**
     * 设置进度值
     *
     * @return Bitmap
     */
    public Bitmap getBitmap(Drawable drawable) {
        int iconSize = (int) trackIconSize;
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, true);
    }

    /**
     * 获取最小值
     *
     * @return 最小值
     */
    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getValue() {
        return value;
    }

    /**
     * 获取激活轨道右侧位置, 如果计算出来的位置小于轨道圆角的直径, 则返回轨道圆角的直径
     *
     * @return 激活轨道右侧位置
     */
    public float getActiveTrackRight() {
        int right = (getWidth() - getPaddingRight()) * value / maxValue;
        return Math.max(right, trackRadius * 2);
    }

    /**
     * 数值合法性检查
     */
    private void checkValue() {
        if (value < 0) {
            throw new IllegalArgumentException("value must be greater than 0");
        }
        if (value < minValue) {
            throw new IllegalArgumentException("value must be greater than minValue");
        }
        if (value > maxValue) {
            throw new IllegalArgumentException("value must be less than maxValue");
        }
    }

    /**
     * 设置进度值
     *
     * @param value 进度值
     */
    public void setValue(int value) {
        updateValue(value, false, true);
    }

    /**
     * 设置进度值
     *
     * @param value  进度值
     * @param isAnim 是否需要动画
     */
    public void setValue(int value, boolean isAnim) {
        updateValue(value, false, isAnim);
    }

    /**
     * 设置进度值
     *
     * @param targetValue 目标值
     * @param isTouch     是否是手动拖动
     * @param isAnim      是否需要动画
     */
    private void updateValue(int targetValue, boolean isTouch, boolean isAnim) {
        if (targetValue < minValue) {
            targetValue = minValue;
        }
        if (targetValue > maxValue) {
            targetValue = maxValue;
        }
        if (value == targetValue) {
            return;
        }
        if (isAnim) {
            valueAnimator = ValueAnimator.ofInt(value, targetValue);
            valueAnimator.setDuration(500);
            valueAnimator.addUpdateListener(animation -> {
                value = (int) animation.getAnimatedValue();
                invalidate();
            });
            valueAnimator.start();
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onValueChangeListener != null) {
                        onValueChangeListener.onValueChange(value, isTouch);
                        if (actionUp) {
                            onValueChangeListener.onStopTrackingTouch(value);
                        }
                    }
                }
            });
        } else {
            value = targetValue;
            invalidate();
            if (onValueChangeListener != null) {
                onValueChangeListener.onValueChange(value, isTouch);
            }
        }
    }

    /**
     * 解析渐变色
     *
     * @param gradientColor 渐变色
     * @return 渐变色数组
     */
    private int[] parseGradientColor(String gradientColor) {
        if (TextUtils.isEmpty(gradientColor)) {
            return null;
        }
        String[] colors = gradientColor.split(",");
        int[] colorArray = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            colorArray[i] = Color.parseColor(colors[i]);
        }
        return colorArray;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.removeAllListeners();
            valueAnimator = null;
        }
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }
}
