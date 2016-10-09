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
	
	//������������־��������ʶ��ǰ��ק���״̬
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
	 * ���������view�Ľӿڣ����ڻص���ȡ��view����Ϣ�͸ı��view����ͼ
	 * @author Administrator
	 *
	 */
	public interface onGooViewStateChangedListener
	{
		/**
		 * �����view��ʧ��ʱ������
		 * @param x ��ǰ�������Ļ��x����
		 * @param y ��ǰ�������Ļ��Y����
		 */
		public void onGooViewDisappear(float x, float y);
		/**
		 * ����Ҫ�����ı��viewʱ���ø÷���
		 */
		public void onGooViewChanged();
	}
	
	onGooViewStateChangedListener listener;
	public void setOnGooViewStateChangedListener(onGooViewStateChangedListener listener)
	{
		this.listener=listener;
	}

	private void init() {
		// ��ʼ������viewʱ�����г�ʼ��paint
		myPaint = new Paint();
		// ����Paint������
		// ����Ϊ�����
		myPaint.setAntiAlias(true);
		// ���û�����ɫ
		myPaint.setColor(Color.RED);

		mDragCenter = new PointF(400.0f, 400.0f);
		mDragRadius = 15.0f;
		mStickCenter = new PointF(400.0f, 400.0f);
		mStickRadius = 12.0f;
	}

	/*
	 * ��onDraw�н���view�Ļ滭
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		//���浱ǰ��
//		canvas.save();
		canvas.translate(0, -mStateBarHeight);
		
		myPaint.setStyle(Style.STROKE);
		canvas.drawCircle(mStickCenter.x, mStickCenter.y, 100, myPaint);
		myPaint.setStyle(Style.FILL);
		
		if(!isDisappear)
		{
			if(!isOutOfRang)
			{
				//��ȡ��ʱ�Ĺ̶�Բ�İ뾶
				float tempStickRadius=getTempDragCycleRadius();
				
				drawBezierLine(canvas,tempStickRadius);
				
				// �滭�̶�Բ
				canvas.drawCircle(mStickCenter.x, mStickCenter.y, tempStickRadius, myPaint);
			}
			// �滭��קԲ
			canvas.drawCircle(mDragCenter.x, mDragCenter.y, mDragRadius, myPaint);
		}
//		canvas.restore();
	}

	/*
	 * ���������϶�����̬�Ļ�ȡ�̶�Բ�İ뾶
	 */
	private float getTempDragCycleRadius() {
		// ��̬�Ļ�ȡ��קԲʱ����Բ֮��ľ���
		float currentDistance = GeometryUtils.getDistanceBetween2Points(
				mStickCenter, mDragCenter);
		//��ȡ��С������֮�����
		currentDistance=Math.min(currentDistance, mFinalDistance);
//		Utils.showToast(getContext(), ""+currentDistance);
		if(currentDistance!=0)
		{
			//���ñ仯�ľ�������Զ���룬���߱�ֵ�İٷ�������̬�ĸı�̶�Բ�İ뾶
			float fraction = (currentDistance / mFinalDistance);
			Float currentStickRadius = evaluate(fraction, mStickRadius,
					0.2f * mStickRadius);
			return currentStickRadius;
		}
		return mStickRadius;
	}

	//�滭����������
	private void drawBezierLine(Canvas canvas, float tempStickRadius) {
		
		Path myPath=new Path();
		
		// ��ȡͨ��Բ�ĵ�ֱ�ߵ�б��
		double lineK = getLineK(mStickCenter, mDragCenter);

		// ͨ����װ������������ȡͨ��Բ�ĵ�ֱ����Բ�ཻ������
		PointF[] mStickPoints = GeometryUtils.getIntersectionPoints(
				mStickCenter, tempStickRadius, lineK);
		PointF[] mDragPoints = GeometryUtils.getIntersectionPoints(mDragCenter,
				mDragRadius, lineK);

		// ��ȡ���Ƶ�
		PointF controlPoint = GeometryUtils.getMiddlePoint(mStickCenter,
				mDragCenter);

		myPaint.setColor(Color.RED);
		
		// ����������
		
		// ʹ��moveTo����ʹpath����ָ����λ�ã����δʹ��moveTo��������Ĭ��Ϊ��0��0λ�ÿ�ʼ
		myPath.moveTo(mStickPoints[0].x, mStickPoints[0].y);

		// ʹ��path�е�һ�ױ���������ȥ��������
		myPath.quadTo(controlPoint.x, controlPoint.y, mDragPoints[0].x,
				mDragPoints[0].y);
		// ʹ��lineTo�������ǽ�·������һ��λ�û�ֱ�ߵ�ָ���ĵ�
		myPath.lineTo(mDragPoints[1].x, mDragPoints[1].y);
		myPath.quadTo(controlPoint.x, controlPoint.y, mStickPoints[1].x,
				mStickPoints[1].y);
		// ����close()�����������Զ���������ͼ�αհ�����
		myPath.close();

		// ��ʼ�滭path
		canvas.drawPath(myPath, myPaint);
	}

	// ��ȡͨ��Բ�ĵ�ֱ�ߵ�б��
	private double getLineK(PointF mStickCenter, PointF mDragCenter) {
		float offsetHeight = mStickCenter.y - mDragCenter.y;
		float offsetWeight = mStickCenter.x - mDragCenter.x;
		return offsetHeight / offsetWeight;
	}

	// ��дonTouchEvent������ʹdragcycle������
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
				//ִ�жϿ�����
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

	//��touchEvent��ȥ��ȡ��ǰ�������ڵ�λ����̶�ԲԲ��֮��ľ���
	private float getCurrentDistance(MotionEvent event) {
		PointF motionEventPoint=new PointF(event.getRawX(),event.getRawY());
		float currentDistance=GeometryUtils.getDistanceBetween2Points(motionEventPoint, mStickCenter);
		return currentDistance;
	}

	//����ValueAnimatorʵ��ƽ���ƶ�view�Ĺ���
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

	//������קԲ�İ뾶��������invalidate���������ػ湤��
	private void updateDragCycle(MotionEvent event) {
		float x;
		float y;
		x = event.getRawX();
		y = event.getRawY();
		mDragCenter.set(x, y);
		invalidate();
	}

	//ʹ��float�����͹�ֵ������ȥ����仯ֵ
	public Float evaluate(float fraction, Number startValue, Number endValue) {
		float startFloat = startValue.floatValue();
		return startFloat + fraction * (endValue.floatValue() - startFloat);
	}
	
	//�ڸ÷�����ȥ��ȡ״̬���ĸ߶�
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Rect frame=new Rect();
		this.getWindowVisibleDisplayFrame(frame);
		mStateBarHeight = 60;
		Toast.makeText(getContext(), ""+mStateBarHeight+"  "+frame.top, Toast.LENGTH_LONG).show();
	}
}
