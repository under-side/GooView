package org.under_side.gooview.ui;

import GeometryUtil.GeometryUtils;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class GooView extends View {

	private Paint myPaint;

	// 两圆的圆心坐标和半径变量
	private PointF mDragCenter;
	private PointF mStickCenter;
	private float mDragRadius;
	private float mStickRadius;

	// 限定手势拖拽的范围
	private float mFinalDistance = 300;

	// 运用这两个标志变量来标识当前拖拽球的状态
	private boolean isOutOfRang = false;
	private boolean isDisappear = false;

	// 状态栏的高度
	private int mStateBarHeight;

	public GooView(Context context) {
		this(context, null);
	}

	public GooView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GooView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	/**
	 * 定义监听该view的接口，用于回调获取该view的信息和改变该view的视图
	 * 
	 * @author under-side
	 * 
	 */
	public interface onGooViewStateChangedListener {
		/**
		 * 如果该view消失了时，调用该方法
		 * 
		 * @param x 当前相对与屏幕的x坐标
		 * @param y 当前相对于屏幕的Y坐标
		 */
		public void onGooViewDisappear(float x, float y);

		/**
		 * 当需要从外界改变该view时调用该方法
		 */
		public void onGooViewChanged();
	}

	onGooViewStateChangedListener listener;

	private Rect mFrame;

	public void setOnGooViewStateChangedListener(
			onGooViewStateChangedListener listener) {
		this.listener = listener;
	}

	// 进行两圆圆心的变量初始化操作和画笔的初始化
	private void init() {
		// 开始创建该view时，进行初始化paint
		myPaint = new Paint();
		// 设置Paint的属性
		// 设置为抗锯齿
		myPaint.setAntiAlias(true);
		// 设置画笔颜色
		myPaint.setColor(Color.RED);

		mDragCenter = new PointF(600f, 1000.0f);
		mDragRadius = 40f;
		mStickCenter = new PointF(600.0f, 1000.0f);
		mStickRadius = 30f;
	}

	/*
	 * 在onDraw中进行view的绘画
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 保存当前的canvas的设置
		canvas.save();

		// 将当前画布的区域向上移动-mStateBarHeight，使手势与拖拽圆同步
		canvas.translate(0, -mStateBarHeight);

		// 画一个参考圆，表示拖拽圆的范围
		myPaint.setStyle(Style.STROKE);
		canvas.drawCircle(mStickCenter.x, mStickCenter.y, mFinalDistance,
				myPaint);
		myPaint.setStyle(Style.FILL);

		/*
		 * 运用表示变量来进行对view的绘制
		 */

		// 如果当前isDisappear为false则绘制view，否则将不绘制任何的view
		if (!isDisappear) {
			// 如果当前isOutRang为false表示没有出范围，则进行绘制固定圆、拖拽圆、连接部分。否则，表示出范围了，则只绘制拖拽圆
			if (!isOutOfRang) {
				// 获取临时的固定圆的半径
				float tempStickRadius = getTempDragCycleRadius();

				// 绘制连接部分
				drawBezierLine(canvas, tempStickRadius);

				// 绘画固定圆
				canvas.drawCircle(mStickCenter.x, mStickCenter.y,
						tempStickRadius, myPaint);
			}
			// 绘画拖拽圆
			canvas.drawCircle(mDragCenter.x, mDragCenter.y, mDragRadius,
					myPaint);
		}
		// 将canvas恢复到上一次保存的状态，中间的设置将会无效
		canvas.restore();
	}

	/*
	 * 根据手势拖动，动态的获取固定圆的半径
	 */
	private float getTempDragCycleRadius() {
		// 动态的获取拖拽圆时两个圆之间的距离
		float currentDistance = GeometryUtils.getDistanceBetween2Points(
				mStickCenter, mDragCenter);

		// 获取最小的两者之间距离
		currentDistance = Math.min(currentDistance, mFinalDistance);

		if (currentDistance != 0) {
			// 利用变化的距离与最远距离，两者比值的百分数来动态的改变固定圆的半径
			float fraction = (currentDistance / mFinalDistance);
			Float currentStickRadius = evaluate(fraction, mStickRadius,
					0.3f * mStickRadius);
			return currentStickRadius;
		}
		return mStickRadius;
	}

	// 绘画贝塞尔曲线
	private void drawBezierLine(Canvas canvas, float tempStickRadius) {

		// 获取路径变量
		Path myPath = new Path();

		// 获取通过圆心的直线的斜率
		double lineK = getLineK(mStickCenter, mDragCenter);

		// 通过封装的求坐标的类获取通过圆心的直线与圆相交的坐标
		PointF[] mStickPoints = GeometryUtils.getIntersectionPoints(
				mStickCenter, tempStickRadius, lineK);
		PointF[] mDragPoints = GeometryUtils.getIntersectionPoints(mDragCenter,
				mDragRadius, lineK);

		// 获取控制点
		PointF controlPoint = GeometryUtils.getMiddlePoint(mStickCenter,
				mDragCenter);

		// 使用moveTo方法使path调到指定的位置，如果未使用moveTo方法，则默认为在0，0位置开始
		myPath.moveTo(mStickPoints[0].x, mStickPoints[0].y);

		// 使用path中的二阶贝塞尔曲线去绘制曲线
		myPath.quadTo(controlPoint.x, controlPoint.y, mDragPoints[0].x,
				mDragPoints[0].y);

		// 使用lineTo方法，是将路径从上一个位置画直线到指定的点
		myPath.lineTo(mDragPoints[1].x, mDragPoints[1].y);

		// 使用path中的二阶贝塞尔曲线去绘制曲线
		myPath.quadTo(controlPoint.x, controlPoint.y, mStickPoints[1].x,
				mStickPoints[1].y);

		// 调用close()方法，将会自动将所画的图形闭包起来
		myPath.close();

		// 使用canvas.drawPath方法去绘制已经设置完成的path
		canvas.drawPath(myPath, myPaint);
	}

	// 获取通过圆心的直线的斜率
	private double getLineK(PointF mStickCenter, PointF mDragCenter) {
		float offsetHeight = mStickCenter.y - mDragCenter.y;
		float offsetWeight = mStickCenter.x - mDragCenter.x;
		return offsetHeight / offsetWeight;
	}

	// 重写onTouchEvent方法，使dragcycle动起来
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isOutOfRang = false;
			isDisappear = false;
			updateDragCycle(event);
			break;
		case MotionEvent.ACTION_MOVE:
			float currentDistance = getCurrentDistance(event);
			if (currentDistance > mFinalDistance) {
				// 执行断开操作
				isOutOfRang = true;
			}
			updateDragCycle(event);
			break;
		case MotionEvent.ACTION_UP:
			if (isOutOfRang) {
				currentDistance = getCurrentDistance(event);
				if (currentDistance < mFinalDistance) {
					getSmoothViewByValueAnimator(event);
				} else {
					// disappear
					isDisappear = true;
					if (listener != null) {
						listener.onGooViewDisappear(event.getRawX(),
								event.getRawY() - mStateBarHeight);
//						Utils.showToast(getContext(), "top :" + mFrame.top
//								+ " bottom :" + mFrame.bottom + "\n" + "left :"
//								+ mFrame.left + " right :" + mFrame.right);
					}

				}
				invalidate();
			} else {
				currentDistance = getCurrentDistance(event);
				if (currentDistance < mFinalDistance) {
					updateDragCycle(event);
				}
				// animation
				getSmoothViewByValueAnimator(event);
			}
			break;
		}
		return true;
	}

	// 在touchEvent中去获取当前手势所在的位置与固定圆圆心之间的距离
	private float getCurrentDistance(MotionEvent event) {
		PointF motionEventPoint = new PointF(event.getRawX(), event.getRawY());
		float currentDistance = GeometryUtils.getDistanceBetween2Points(
				motionEventPoint, mStickCenter);
		return currentDistance;
	}

	// 运用ValueAnimator实现平滑移动view的功能
	private void getSmoothViewByValueAnimator(MotionEvent event) {
		final float x;
		final float y;
		x = event.getRawX();
		y = event.getRawY();
		final ValueAnimator value = ValueAnimator.ofFloat(1.0f);
		value.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float fraction = value.getAnimatedFraction();
				float tempX = evaluate(fraction, x, mStickCenter.x);
				float tempY = evaluate(fraction, y, mStickCenter.y);
				mDragCenter.set(tempX, tempY);
				invalidate();
			}
		});
		value.setDuration(500);
		value.setInterpolator(new OvershootInterpolator(3.0f));
		value.start();
	}

	// 更新拖拽圆的半径，并调用invalidate方法进行重绘工作
	private void updateDragCycle(MotionEvent event) {
		float x;
		float y;
		x = event.getRawX();
		y = event.getRawY();
		mDragCenter.set(x, y);
		invalidate();
	}

	// 使用float的类型估值器方法去计算变化值
	public Float evaluate(float fraction, Number startValue, Number endValue) {
		float startFloat = startValue.floatValue();
		return startFloat + fraction * (endValue.floatValue() - startFloat);
	}

	// 在该方法中去获取状态栏的高度
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mStateBarHeight = getStateBarHeight(this);

	}

	// 获取当前屏幕的状态栏高度
	private int getStateBarHeight(GooView gooView) {
		if (gooView == null) {
			return 0;
		}
		mFrame = new Rect();
		gooView.getWindowVisibleDisplayFrame(mFrame);
		return mFrame.top;
	}

}
