package com.lwb.music.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class MusicView extends View {

    private int h, w, p;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int[] sh = new int[5];
    private int speed = 3;
    private int[] add = new int[]{-speed, speed, -speed, speed, -speed};

    public MusicView(Context context) {
        super(context);
        init();
    }

    public MusicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setColor(0xff8B2252);
        paint.setStrokeWidth(5f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        h = MeasureSpec.getSize(heightMeasureSpec);
        w = MeasureSpec.getSize(widthMeasureSpec);
        p = w / 6;
        int ph = h / 6;
        sh[0] = ph * 5;
        sh[1] = ph * 2;
        sh[2] = ph * 4;
        sh[3] = ph * 3;
        sh[4] = ph * 1;
    }

    long last;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (System.currentTimeMillis() - last > 20) {
            for (int i = 0; i < sh.length; i++) {
                sh[i] += add[i];
                if (sh[i] > h) {
                    sh[i] = h;
                    add[i] = -add[i];
                } else if (sh[i] < 0) {
                    sh[i] = 0;
                    add[i] = -add[i];
                }
            }
            last = System.currentTimeMillis();
        }
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(p * (i + 1), sh[i], p * (i + 1), h, paint);
        }
        if (isAttachedToWindow() && getVisibility() == VISIBLE) {
            invalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        invalidate();
    }
}