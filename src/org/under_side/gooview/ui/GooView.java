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

	// ��Բ��Բ������Ͱ뾶����
	private PointF mDragCenter;
	private PointF mStickCenter;
	private float mDragRadius;
	private float mStickRadius;

	// �޶�������ק�ķ�Χ
	private float mFinalDistance = 300;

	// ������������־��������ʶ��ǰ��ק���״̬
	private boolean isOutOfRang = false;
	private boolean isDisappear = false;

	// ״̬���ĸ߶�
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
	 * 
	 * @author under-side
	 * 
	 */
	public interface onGooViewStateChangedListener {
		/**
		 * �����view��ʧ��ʱ�����ø÷���
		 * 
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

	private Rect mFrame;

	public void setOnGooViewStateChangedListener(
			onGooViewStateChangedListener listener) {
		this.listener = listener;
	}

	// ������ԲԲ�ĵı�����ʼ�������ͻ��ʵĳ�ʼ��
	private void init() {
		// ��ʼ������viewʱ�����г�ʼ��paint
		myPaint = new Paint();
		// ����Paint������
		// ����Ϊ�����
		myPaint.setAntiAlias(true);
		// ���û�����ɫ
		myPaint.setColor(Color.RED);

		mDragCenter = new PointF(600f, 1000.0f);
		mDragRadius = 40f;
		mStickCenter = new PointF(600.0f, 1000.0f);
		mStickRadius = 30f;
	}

	/*
	 * ��onDraw�н���view�Ļ滭
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// ���浱ǰ��canvas������
		canvas.save();

		// ����ǰ���������������ƶ�-mStateBarHeight��ʹ��������קԲͬ��
		canvas.translate(0, -mStateBarHeight);

		// ��һ���ο�Բ����ʾ��קԲ�ķ�Χ
		myPaint.setStyle(Style.STROKE);
		canvas.drawCircle(mStickCenter.x, mStickCenter.y, mFinalDistance,
				myPaint);
		myPaint.setStyle(Style.FILL);

		/*
		 * ���ñ�ʾ���������ж�view�Ļ���
		 */

		// �����ǰisDisappearΪfalse�����view�����򽫲������κε�view
		if (!isDisappear) {
			// �����ǰisOutRangΪfalse��ʾû�г���Χ������л��ƹ̶�Բ����קԲ�����Ӳ��֡����򣬱�ʾ����Χ�ˣ���ֻ������קԲ
			if (!isOutOfRang) {
				// ��ȡ��ʱ�Ĺ̶�Բ�İ뾶
				float tempStickRadius = getTempDragCycleRadius();

				// �������Ӳ���
				drawBezierLine(canvas, tempStickRadius);

				// �滭�̶�Բ
				canvas.drawCircle(mStickCenter.x, mStickCenter.y,
						tempStickRadius, myPaint);
			}
			// �滭��קԲ
			canvas.drawCircle(mDragCenter.x, mDragCenter.y, mDragRadius,
					myPaint);
		}
		// ��canvas�ָ�����һ�α����״̬���м�����ý�����Ч
		canvas.restore();
	}

	/*
	 * ���������϶�����̬�Ļ�ȡ�̶�Բ�İ뾶
	 */
	private float getTempDragCycleRadius() {
		// ��̬�Ļ�ȡ��קԲʱ����Բ֮��ľ���
		float currentDistance = GeometryUtils.getDistanceBetween2Points(
				mStickCenter, mDragCenter);

		// ��ȡ��С������֮�����
		currentDistance = Math.min(currentDistance, mFinalDistance);

		if (currentDistance != 0) {
			// ���ñ仯�ľ�������Զ���룬���߱�ֵ�İٷ�������̬�ĸı�̶�Բ�İ뾶
			float fraction = (currentDistance / mFinalDistance);
			Float currentStickRadius = evaluate(fraction, mStickRadius,
					0.3f * mStickRadius);
			return currentStickRadius;
		}
		return mStickRadius;
	}

	// �滭����������
	private void drawBezierLine(Canvas canvas, float tempStickRadius) {

		// ��ȡ·������
		Path myPath = new Path();

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

		// ʹ��moveTo����ʹpath����ָ����λ�ã����δʹ��moveTo��������Ĭ��Ϊ��0��0λ�ÿ�ʼ
		myPath.moveTo(mStickPoints[0].x, mStickPoints[0].y);

		// ʹ��path�еĶ��ױ���������ȥ��������
		myPath.quadTo(controlPoint.x, controlPoint.y, mDragPoints[0].x,
				mDragPoints[0].y);

		// ʹ��lineTo�������ǽ�·������һ��λ�û�ֱ�ߵ�ָ���ĵ�
		myPath.lineTo(mDragPoints[1].x, mDragPoints[1].y);

		// ʹ��path�еĶ��ױ���������ȥ��������
		myPath.quadTo(controlPoint.x, controlPoint.y, mStickPoints[1].x,
				mStickPoints[1].y);

		// ����close()�����������Զ���������ͼ�αհ�����
		myPath.close();

		// ʹ��canvas.drawPath����ȥ�����Ѿ�������ɵ�path
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
			isOutOfRang = false;
			isDisappear = false;
			updateDragCycle(event);
			break;
		case MotionEvent.ACTION_MOVE:
			float currentDistance = getCurrentDistance(event);
			if (currentDistance > mFinalDistance) {
				// ִ�жϿ�����
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

	// ��touchEvent��ȥ��ȡ��ǰ�������ڵ�λ����̶�ԲԲ��֮��ľ���
	private float getCurrentDistance(MotionEvent event) {
		PointF motionEventPoint = new PointF(event.getRawX(), event.getRawY());
		float currentDistance = GeometryUtils.getDistanceBetween2Points(
				motionEventPoint, mStickCenter);
		return currentDistance;
	}

	// ����ValueAnimatorʵ��ƽ���ƶ�view�Ĺ���
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

	// ������קԲ�İ뾶��������invalidate���������ػ湤��
	private void updateDragCycle(MotionEvent event) {
		float x;
		float y;
		x = event.getRawX();
		y = event.getRawY();
		mDragCenter.set(x, y);
		invalidate();
	}

	// ʹ��float�����͹�ֵ������ȥ����仯ֵ
	public Float evaluate(float fraction, Number startValue, Number endValue) {
		float startFloat = startValue.floatValue();
		return startFloat + fraction * (endValue.floatValue() - startFloat);
	}

	// �ڸ÷�����ȥ��ȡ״̬���ĸ߶�
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mStateBarHeight = getStateBarHeight(this);

	}

	// ��ȡ��ǰ��Ļ��״̬���߶�
	private int getStateBarHeight(GooView gooView) {
		if (gooView == null) {
			return 0;
		}
		mFrame = new Rect();
		gooView.getWindowVisibleDisplayFrame(mFrame);
		return mFrame.top;
	}

}
