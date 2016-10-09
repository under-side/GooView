package org.under_side.gooview.ui;

import GeometryUtil.GeometryUtils;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActionBar.LayoutParams;
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
import android.widget.ImageView;
import android.widget.Toast;

public class GooView extends View {

	private Paint myPaint;
	private PointF mDragCenter;
	private PointF mStickCenter;
	private float mDragRadius;
	private float mFinalDistance = 110;
	private float mStickRadius;
	
	//运用这两个标志变量来标识当前拖拽球的状态
	private boolean isOutOfRang=false;
	private boolean isDisappear=false;
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
	 * @author Administrator
	 *
	 */
	public interface onGooViewStateChangedListener
	{
		/**
		 * 如果该view消失了时，调用
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
	public void setOnGooViewStateChangedListener(onGooViewStateChangedListener listener)
	{
		this.listener=listener;
	}

	private void init() {
		// 开始创建该view时，进行初始化paint
		myPaint = new Paint();
		// 设置Paint的属性
		// 设置为抗锯齿
		myPaint.setAntiAlias(true);
		// 设置画笔颜色
		myPaint.setColor(Color.RED);

		mDragCenter = new PointF(400.0f, 400.0f);
		mDragRadius = 15.0f;
		mStickCenter = new PointF(400.0f, 400.0f);
		mStickRadius = 12.0f;
	}

	/*
	 * 在onDraw中进行view的绘画
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		//保存当前的
//		canvas.save();
		canvas.translate(0, -mStateBarHeight);
		
		myPaint.setStyle(Style.STROKE);
		canvas.drawCircle(mStickCenter.x, mStickCenter.y, 100, myPaint);
		myPaint.setStyle(Style.FILL);
		
		if(!isDisappear)
		{
			if(!isOutOfRang)
			{
				//获取临时的固定圆的半径
				float tempStickRadius=getTempDragCycleRadius();
				
				drawBezierLine(canvas,tempStickRadius);
				
				// 绘画固定圆
				canvas.drawCircle(mStickCenter.x, mStickCenter.y, tempStickRadius, myPaint);
			}
			// 绘画拖拽圆
			canvas.drawCircle(mDragCenter.x, mDragCenter.y, mDragRadius, myPaint);
		}
//		canvas.restore();
	}

	/*
	 * 根据手势拖动，动态的获取固定圆的半径
	 */
	private float getTempDragCycleRadius() {
		// 动态的获取拖拽圆时两个圆之间的距离
		float currentDistance = GeometryUtils.getDistanceBetween2Points(
				mStickCenter, mDragCenter);
		//获取最小的两者之间距离
		currentDistance=Math.min(currentDistance, mFinalDistance);
//		Utils.showToast(getContext(), ""+currentDistance);
		if(currentDistance!=0)
		{
			//利用变化的距离与最远距离，两者比值的百分数来动态的改变固定圆的半径
			float fraction = (currentDistance / mFinalDistance);
			Float currentStickRadius = evaluate(fraction, mStickRadius,
					0.2f * mStickRadius);
			return currentStickRadius;
		}
		return mStickRadius;
	}

	//绘画贝塞尔曲线
	private void drawBezierLine(Canvas canvas, float tempStickRadius) {
		
		Path myPath=new Path();
		
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

		myPaint.setColor(Color.RED);
		
		// 画连接区域
		
		// 使用moveTo方法使path调到指定的位置，如果未使用moveTo方法，则默认为在0，0位置开始
		myPath.moveTo(mStickPoints[0].x, mStickPoints[0].y);

		// 使用path中的一阶贝塞尔曲线去绘制曲线
		myPath.quadTo(controlPoint.x, controlPoint.y, mDragPoints[0].x,
				mDragPoints[0].y);
		// 使用lineTo方法，是将路径从上一个位置画直线到指定的点
		myPath.lineTo(mDragPoints[1].x, mDragPoints[1].y);
		myPath.quadTo(controlPoint.x, controlPoint.y, mStickPoints[1].x,
				mStickPoints[1].y);
		// 调用close()方法，将会自动将所画的图形闭包起来
		myPath.close();

		// 开始绘画path
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
			isOutOfRang=false;
			isDisappear=false;
			updateDragCycle(event);
			break;
		case MotionEvent.ACTION_MOVE:
			float currentDistance = getCurrentDistance(event);
			if(currentDistance>mFinalDistance)
			{
				//执行断开操作
				isOutOfRang=true;
			}
			updateDragCycle(event);
			break;
		case MotionEvent.ACTION_UP:
			if(isOutOfRang)
			{
				currentDistance=getCurrentDistance(event);
				if(currentDistance<mFinalDistance)
				{
					getSmoothViewByValueAnimator(event);
				}else{
					//disappear
					isDisappear=true;
					if(listener!=null)
					{
						listener.onGooViewDisappear
						(event.getRawX()+mStateBarHeight,event.getRawY()+mStateBarHeight);
					}
					
				}
				invalidate();
			}else{
				currentDistance=getCurrentDistance(event);
				if(currentDistance<mFinalDistance)
				{
					updateDragCycle(event);
				}
				//animation
				getSmoothViewByValueAnimator(event);
			}
			break;
		}
		return true;
	}

	//在touchEvent中去获取当前手势所在的位置与固定圆圆心之间的距离
	private float getCurrentDistance(MotionEvent event) {
		PointF motionEventPoint=new PointF(event.getRawX(),event.getRawY());
		float currentDistance=GeometryUtils.getDistanceBetween2Points(motionEventPoint, mStickCenter);
		return currentDistance;
	}

	//运用ValueAnimator实现平滑移动view的功能
	private void getSmoothViewByValueAnimator(MotionEvent event) {
		final float x;
		final float y;
		x = event.getRawX();
		y = event.getRawY();
		final ValueAnimator value=ValueAnimator.ofFloat(1.0f);
		value.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float fraction=value.getAnimatedFraction();
				float tempX=evaluate(fraction, x, mStickCenter.x);
				float tempY=evaluate(fraction, y, mStickCenter.y);
				mDragCenter.set(tempX, tempY);
				invalidate();
			}
		});
		value.setDuration(500);
		value.setInterpolator(new OvershootInterpolator(3.0f));
		value.start();
	}

	//更新拖拽圆的半径，并调用invalidate方法进行重绘工作
	private void updateDragCycle(MotionEvent event) {
		float x;
		float y;
		x = event.getRawX();
		y = event.getRawY();
		mDragCenter.set(x, y);
		invalidate();
	}

	//使用float的类型估值器方法去计算变化值
	public Float evaluate(float fraction, Number startValue, Number endValue) {
		float startFloat = startValue.floatValue();
		return startFloat + fraction * (endValue.floatValue() - startFloat);
	}
	
	//在该方法中去获取状态栏的高度
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Rect frame=new Rect();
		this.getWindowVisibleDisplayFrame(frame);
		mStateBarHeight = 60;
		Toast.makeText(getContext(), ""+mStateBarHeight+"  "+frame.top, Toast.LENGTH_LONG).show();
	}
}
