package org.under_side.gooview;

import org.under_side.gooview.ui.GooView;
import org.under_side.gooview.ui.GooView.onGooViewStateChangedListener;
import org.under_side.gooview.ui.MyAnimationDrawable;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

	private GooView mGooView;
	private ImageView mImageView;
	private RelativeLayout mLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		init();
	}

	//���г�ʼ������
	private void init() {
		mLayout = (RelativeLayout) findViewById(R.id.layout);

		mGooView = (GooView) findViewById(R.id.goo_view);
		mGooView.setOnGooViewStateChangedListener(new onGooViewStateChangedListener() {

			@Override
			public void onGooViewChanged() {
			}

			@Override
			public void onGooViewDisappear(float x, float y) {
				showDisappearAnimation(x, y);
			}
		});

	}

	//ִ����ʧ��������
	private void showDisappearAnimation(float x, float y) {

		// ��ʼ��ImageView
		if (mImageView == null) {
			mImageView = new ImageView(this);
		}

		
		/*
		 *  ��̬���ImageView�������ļ���ȥ
		 *  ��̬���view��������ȥʱ��������ʲô�ͻ�ȡ�ĸ����ֵ�LayoutParams�����ý�Ҫ���
		 *  ��view�Ĳ������ԡ�
		 */
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		//��view��ӵ������ͷŵ�λ����
		params.leftMargin = (int) x;
		params.topMargin = (int) y;
		
		mLayout.addView(mImageView,params);

		AnimationDrawable ad=(AnimationDrawable) getResources().getDrawable(R.drawable.anim_bubble_pop);
		//ʹ���Զ����AnimationDrawable�������жϵ�ǰ����ʱ�䣬�����ж��������ж�
		MyAnimationDrawable drawable = new MyAnimationDrawable(ad) {
			
			public void onAnimationEnd() {
				mLayout.removeView(mImageView);
			}
		};
		mImageView.setBackgroundDrawable(drawable);
		drawable.start();
	}
}
