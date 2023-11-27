package com.eurigo.easyslider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
import android.graphics.PointF;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;

/**
 * @author eurigo
 * Created on 2023/11/20 10:37
 * desc   :
 */
public class EasySlider extends View {

    private int minValue;
    private int maxValue;
    private int value;
    private float percent;
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
    private boolean isShowProgressPoint;
    private float progressTextSize;
    private int progressTextColor;
    private String progressTextSuffix;
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
    private final PointF mDownPoint = new PointF();
    private ValueAnimator valueAnimator;
    private final DecimalFormat percentFormat = new DecimalFormat("0.00000");
    private final DecimalFormat valueFormat = new DecimalFormat("0");
    private final DecimalFormat progressPointFormat = new DecimalFormat("0.00%");
    private final DecimalFormat progressFormat = new DecimalFormat("0%");

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
        minValue = typedArray.getInt(R.styleable.EasySlider_es_minValue, 0);
        maxValue = typedArray.getInt(R.styleable.EasySlider_es_maxValue, 100);
        value = typedArray.getInt(R.styleable.EasySlider_es_value, 0);
        percent = calculatePercent(value);
        checkValue();

        // 进度条
        trackActiveColor = typedArray.getColor(R.styleable.EasySlider_es_trackActiveColor, DEFAULT_TRACK_ACTIVE_COLOR);
        String gradientColor = typedArray.getString(R.styleable.EasySlider_es_trackActiveGradientColor);
        trackActiveGradientColor = parseGradientColor(gradientColor);
        trackInactiveColor = typedArray.getColor(R.styleable.EasySlider_es_trackInactiveColor, DEFAULT_TRACK_INACTIVE_COLOR);
        String inactiveGradientColor = typedArray.getString(R.styleable.EasySlider_es_trackInactiveGradientColor);
        trackInactiveGradientColor = parseGradientColor(inactiveGradientColor);
        trackRadius = typedArray.getDimension(R.styleable.EasySlider_es_trackRadius, dp2px(12));

        // 标志
        isShowThumb = typedArray.getBoolean(R.styleable.EasySlider_es_showThumb, true);
        thumbColor = typedArray.getColor(R.styleable.EasySlider_es_thumbColor, COLOR_WHITE);
        thumbRadius = typedArray.getDimension(R.styleable.EasySlider_es_thumbRadius, dp2px(12));
        thumbPadding = typedArray.getDimension(R.styleable.EasySlider_es_thumbPadding, dp2px(4));
        thumbWidth = typedArray.getDimension(R.styleable.EasySlider_es_thumbWidth, dp2px(24));
        thumbHeight = typedArray.getDimension(R.styleable.EasySlider_es_thumbHeight, dp2px(24));

        // 进度文本
        isShowProgressText = typedArray.getBoolean(R.styleable.EasySlider_es_showProgressText, true);
        isShowProgressPoint = typedArray.getBoolean(R.styleable.EasySlider_es_showProgressPoint, false);
        progressTextSize = typedArray.getDimension(R.styleable.EasySlider_es_progressTextSize, sp2px(16));
        progressTextColor = typedArray.getColor(R.styleable.EasySlider_es_progressTextColor, COLOR_WHITE);
        progressTextGravity = typedArray.getInt(R.styleable.EasySlider_es_progressTextGravity, 0);
        progressTextPadding = typedArray.getDimension(R.styleable.EasySlider_es_progressTextPadding, 0);
        String suffix = typedArray.getString(R.styleable.EasySlider_es_progressTextSuffix);
        progressTextSuffix = TextUtils.isEmpty(suffix) ? "" : suffix;

        // 进度图标
        trackIconSize = typedArray.getDimension(R.styleable.EasySlider_es_trackIconSize, dp2px(16));
        trackIconTint = typedArray.getColor(R.styleable.EasySlider_es_trackIconTint, -1);
        trackIconPadding = typedArray.getDimension(R.styleable.EasySlider_es_trackIconPadding, dp2px(4));
        trackIconGravity = typedArray.getInt(R.styleable.EasySlider_es_trackIconGravity, 0);
        Drawable drawable = typedArray.getDrawable(R.styleable.EasySlider_es_trackIcon);
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
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawTrack(canvas);
        drawThumb(canvas);
        drawProgressText(canvas);
        drawTrackIcon(canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = Math.min(event.getX(), getInactiveTrackRight());
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                actionUp = false;
                mDownPoint.set(x, y);
                break;
            // 抬起
            case MotionEvent.ACTION_UP:
                actionUp = true;
                float percent = x / (getInactiveTrackRight() - getInactiveTrackLeft());
                updatePercent(percent, true, true);
                break;
            // 移动
            case MotionEvent.ACTION_MOVE:
                if (mDownPoint.equals(x, y)) {
                    return true;
                }
                float motionPercent = x / (getInactiveTrackRight() - getInactiveTrackLeft());
                updatePercent(motionPercent, true, false);
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
        String progressText = getPercent().concat(progressTextSuffix);
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
     * 获取最小进度值
     *
     * @return 最小值
     */
    public int getMinValue() {
        return minValue;
    }

    /**
     * 获取最大进度值
     *
     * @return 最大值
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * 获取当前进度值
     *
     * @return 当前进度值
     */
    public int getValue() {
        return Integer.parseInt(valueFormat.format(value));
    }

    /**
     * 获取进度百分比
     *
     * @return 进度百分比
     */
    public String getPercent() {
        String formatPercent = isShowProgressPoint ? progressPointFormat.format(percent) : progressFormat.format(percent);
        return formatPercent.replace("%", "");
    }

    private int calculateValue(float percent) {
        String valueString = valueFormat.format(minValue + (maxValue - minValue) * percent);
        return Integer.parseInt(valueString);
    }

    private float calculatePercent(int value) {
        String percentString = percentFormat.format(1f * (value - minValue) / (maxValue - minValue));
        return Float.parseFloat(percentString);
    }


    /**
     * 获取激活轨道右侧位置, 如果计算出来的位置小于轨道圆角的直径, 则返回轨道圆角的直径
     *
     * @return 激活轨道右侧位置
     */
    public float getActiveTrackRight() {
        float right = percent * (getInactiveTrackRight() - getInactiveTrackLeft());
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
        float percent = calculatePercent(value);
        actionUp = false;
        updatePercent(percent, false, true);
    }

    /**
     * 设置进度值
     *
     * @param value  进度值
     * @param isAnim 是否需要动画
     */
    public void setValue(int value, boolean isAnim) {
        float percent = calculatePercent(value);
        updatePercent(percent, false, isAnim);
    }

    /**
     * 设置进度值
     *
     * @param targetPercent 进度值
     * @param isTouch       是否是手动拖动
     * @param isAnim        是否需要动画
     */
    private void updatePercent(float targetPercent, boolean isTouch, boolean isAnim) {
        int targetValue = calculateValue(targetPercent);
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
            valueAnimator = ValueAnimator.ofFloat(percent, targetPercent);
            valueAnimator.setDuration(500);
            valueAnimator.addUpdateListener(animation -> {
                percent = (float) animation.getAnimatedValue();
                value = calculateValue(percent);
                invalidate();
            });
            valueAnimator.start();
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onValueChangeListener != null) {
                        onValueChangeListener.onValueChange(getValue(), getPercent(), isTouch);
                        if (actionUp) {
                            onValueChangeListener.onStopTrackingTouch(getValue(), getPercent());
                        }
                    }
                }
            });
        } else {
            value = targetValue;
            percent = targetPercent;
            invalidate();
            if (onValueChangeListener != null) {
                onValueChangeListener.onValueChange(getValue(), getPercent(), isTouch);
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
