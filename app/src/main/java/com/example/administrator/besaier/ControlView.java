package com.example.administrator.besaier;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganLei on 2018/4/24.
 */

public class ControlView extends View {

    public static final int ADD_BEZIER_STATE = 1;//添加贝塞尔模式
    public static final int REMOVE_BEZIER_STATE = -1;//删除贝塞尔模式
    public static final int MOVE_BEZIER_STATE = 0;//普通移动模式

    public static final int SHOW_MODE = 100;//演示模式，隐藏点和辅助线
    public static final int EDIT_MODE = 101;//编辑模式，显示点和辅助线

    private int editState = ADD_BEZIER_STATE;
    private int showState = EDIT_MODE;

    private Bezier nowBezier;//当前正在被控制的贝塞尔
    private PointF RecentPoint;//当前正在被控制的点

    private Paint mPaint;
    private List<Bezier> bezierList;
    private PathMeasure mPathMeasure;//路径工具

    public ControlView(Context context) {
        super(context);
        init();
    }

    public ControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        bezierList = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(8);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(60);
        mPathMeasure = new PathMeasure();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (editState) {
                    case ADD_BEZIER_STATE:
                        PointF pointF = new PointF(x, y);
                        nowBezier = new Bezier(pointF);
                        bezierList.add(nowBezier);
                        break;
                    case REMOVE_BEZIER_STATE:
                        for (Bezier bezier : bezierList) {
                            pointF = bezier.getRecentPointF(x, y);
                            if (pointF != null) {
                                nowBezier = bezier;
                                bezierList.remove(bezier);
                                break;
                            }
                        }
                        break;
                    default:
                        for (Bezier bezier : bezierList) {
                            //获取手指附近的贝塞尔
                            RecentPoint = bezier.getRecentPointF(x, y);
                            if (RecentPoint != null) {
                                nowBezier = bezier;
                                break;
                            }
                        }
                        break;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (editState == MOVE_BEZIER_STATE) {
                    if (nowBezier != null && RecentPoint != null) {
                        Log.d("move", "moveX" + x + "  moveY" + y);
                        nowBezier.movePointF(RecentPoint, x, y);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                nowBezier = null;
                RecentPoint = null;
                break;
        }
        invalidate();
        return true;
    }


    private float[] position = new float[2], tange = new float[2];
    private float fraction;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Bezier bezier : bezierList) {
            // 绘制数据点和控制点

            if (showState == EDIT_MODE) {
                mPaint.setColor(Color.GRAY);
                mPaint.setStrokeWidth(20);
                canvas.drawPoint(bezier.start.x, bezier.start.y, mPaint);
                canvas.drawPoint(bezier.end.x, bezier.end.y, mPaint);
                canvas.drawPoint(bezier.control.x, bezier.control.y, mPaint);

                // 绘制辅助线
                mPaint.setStrokeWidth(4);
                canvas.drawLine(bezier.start.x, bezier.start.y, bezier.control.x, bezier.control.y, mPaint);
                canvas.drawLine(bezier.end.x, bezier.end.y, bezier.control.x, bezier.control.y, mPaint);

                //绘制次级辅助线
                mPaint.setColor(Color.YELLOW);
                float x1, y1, x2, y2;
                x1 = bezier.start.x - (bezier.start.x - bezier.control.x) * fraction;
                y1 = bezier.start.y - (bezier.start.y - bezier.control.y) * fraction;
                x2 = bezier.control.x - (bezier.control.x - bezier.end.x) * fraction;
                y2 = bezier.control.y - (bezier.control.y - bezier.end.y) * fraction;
                canvas.drawLine(x1, y1, x2, y2, mPaint);
            }

            // 绘制贝塞尔曲线
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(8);
            Path path = new Path();
            Path pathDst = new Path();
            path.moveTo(bezier.start.x, bezier.start.y);
            path.quadTo(bezier.control.x, bezier.control.y, bezier.end.x, bezier.end.y);

            mPathMeasure.setPath(path, false);
            mPathMeasure.getSegment(0, fraction * mPathMeasure.getLength(), pathDst, true);
            if (fraction == 0) {
                canvas.drawPath(path, mPaint);
            } else {
                canvas.drawPath(pathDst, mPaint);
            }

            //绘制动态点
            bezier.pathM.setPath(path, false);
            float length = bezier.pathM.getLength();
            boolean flag = bezier.pathM.getPosTan(fraction * length, position, tange);
            if (flag) {
                //开始绘制运行轨迹
                mPaint.setColor(Color.BLACK);
                canvas.drawCircle(position[0], position[1], 10, mPaint);
            }
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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }


    public int getEditState() {
        return editState;
    }

    public void setEditState(int editState) {
        this.editState = editState;
        postInvalidate();
    }

    public int getShowState() {
        return showState;
    }

    public void setShowState(int showState) {
        this.showState = showState;
        postInvalidate();
    }

    class Bezier {
        PointF start, end,control;

        PathMeasure pathM;
        float size = 30;//触摸控制点最大范围

        public Bezier(PointF control) {
            start = new PointF();
            end = new PointF();

            this.control = control;
            this.start.x = control.x - 200;
            this.start.y = control.y + 100;
            this.end.x = control.x + 200;
            this.end.y = control.y + 100;
            pathM = new PathMeasure();
        }

        /**
         * 获取目标坐标最近的一个点
         *
         * @return 点
         */
        public PointF getRecentPointF(float x, float y) {
            if (x < start.x + size &&
                    x > start.x - size &&
                    y < start.y + size &&
                    y > start.y - size) {
                return start;
            } else if (x < end.x + size &&
                    x > end.x - size &&
                    y < end.y + size &&
                    y > end.y - size) {
                return end;
            } else if (x < control.x + size &&
                    x > control.x - size &&
                    y < control.y + size &&
                    y > control.y - size) {
                return control;
            } else {
                return null;
            }
        }

        public void movePointF(PointF pointF, float x, float y) {
            if (pointF == start) {
                start.x = x;
                start.y = y;
            } else if (pointF == end) {
                end.x = x;
                end.y = y;
            } else if (pointF == control) {
                control.x = x;
                control.y = y;
            }
        }
    }
}
