package com.example.administrator.besaier;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ganLei on 2018/4/23.
 *
 */

public class MyView extends View {

    private PathMeasure pathM;
    private Bitmap bitmap;
    Paint mPaint;
    private int centerX, centerY;
    PointF start, end, control;
    Matrix matrix;

    private float fraction = 0.0f;

    public MyView(Context context) {
        super(context);
        init();
    }

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    void init() {

        pathM = new PathMeasure();
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(8);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(60);
        matrix = new Matrix();
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);


        start = new PointF(0, 0);
        end = new PointF(0, 0);
        control = new PointF(0, 0);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = w / 2;
        centerY = h / 2;

        // 初始化数据点和控制点的位置
        start.x = centerX - 200;
        start.y = centerY;
        end.x = centerX + 200;
        end.y = centerY;
        control.x = centerX;
        control.y = centerY - 100;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 根据触摸位置更新控制点，并提示重绘
        control.x = event.getX();
        control.y = event.getY();
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制数据点和控制点
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(20);
        canvas.drawPoint(start.x, start.y, mPaint);
        canvas.drawPoint(end.x, end.y, mPaint);
        canvas.drawPoint(control.x, control.y, mPaint);

        // 绘制辅助线
        mPaint.setStrokeWidth(4);
        canvas.drawLine(start.x, start.y, control.x, control.y, mPaint);
        canvas.drawLine(end.x, end.y, control.x, control.y, mPaint);

        //绘制次级辅助线
        mPaint.setColor(Color.YELLOW);
        float x1, y1, x2, y2;
        x1 = start.x - (start.x - control.x) * fraction;
        y1 = start.y - (start.y - control.y) * fraction;
        x2 = control.x - (control.x - end.x) * fraction;
        y2 = control.y - (control.y - end.y) * fraction;
        canvas.drawLine(x1,y1,x2,y2,mPaint);

        // 绘制贝塞尔曲线
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(8);
        Path path = new Path();
        path.moveTo(start.x, start.y);
        path.quadTo(control.x, control.y, end.x, end.y);
        canvas.drawPath(path, mPaint);

        //绘制动态点
        pathM.setPath(path, false);
        float length = pathM.getLength();
//        boolean flag = pathM.getPosTan(fraction * length, position, tange);
        boolean flag = pathM.getMatrix(fraction * length, matrix, PathMeasure.POSITION_MATRIX_FLAG | PathMeasure.TANGENT_MATRIX_FLAG);

        matrix.preTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 2);
        if (flag) {
            //开始绘制运行轨迹
            mPaint.setColor(Color.BLACK);
//            canvas.drawCircle(position[0], position[1],10,mPaint);
            canvas.drawBitmap(bitmap, matrix, mPaint);
        }
    }

    public void start() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
        animator.setDuration(4000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fraction = (float) animation.getAnimatedValue();
                postInvalidateDelayed(0);
            }
        });
        animator.start();
    }
}
