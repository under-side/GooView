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

	//进行初始化操作
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

	//执行消失动画操作
	private void showDisappearAnimation(float x, float y) {

		// 初始化ImageView
		if (mImageView == null) {
			mImageView = new ImageView(this);
		}

		
		/*
		 *  动态添加ImageView到布局文件中去
		 *  动态添加view到布局中去时，布局是什么就获取哪个布局的LayoutParams来设置将要添加
		 *  的view的布局属性。
		 */
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		//将view添加到手势释放的位置上
		params.leftMargin = (int) x;
		params.topMargin = (int) y;
		
		mLayout.addView(mImageView,params);

		AnimationDrawable ad=(AnimationDrawable) getResources().getDrawable(R.drawable.anim_bubble_pop);
		//使用自定义的AnimationDrawable来结束判断当前动画时间，来进行动画结束判断
		MyAnimationDrawable drawable = new MyAnimationDrawable(ad) {
			
			public void onAnimationEnd() {
				mLayout.removeView(mImageView);
			}
		};
		mImageView.setBackgroundDrawable(drawable);
		drawable.start();
	}
}
