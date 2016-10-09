package org.under_side.gooview;

import org.under_side.gooview.ui.GooView;
import org.under_side.gooview.ui.GooView.onGooViewStateChangedListener;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
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
		setContentView(R.layout.activity_main);
		init();

		

	}

	private void initAnimation() {
		AnimationDrawable drawable = null;
		if(mImageView==null)
		{
			mImageView = new ImageView(this);
			//LayoutParams params = (LayoutParams) mImageView.getLayoutParams();
			mImageView.setBackgroundResource(R.drawable.anim_bubble_pop);
		}
		drawable=(AnimationDrawable) mImageView.getBackground();
		drawable.start();
	}

	private void init() {
		mLayout = (RelativeLayout) findViewById(R.id.layout);
		
		mGooView = (GooView) findViewById(R.id.goo_view);
		mGooView.setOnGooViewStateChangedListener(new onGooViewStateChangedListener() {

			@Override
			public void onGooViewChanged() {

			}

			@Override
			public void onGooViewDisappear(float x, float y) {
				initAnimation();
				LayoutParams params=(LayoutParams) mLayout.getLayoutParams();
				params.leftMargin=(int) x;
				params.topMargin=(int) y;
				params.width=LayoutParams.WRAP_CONTENT;
				params.height=LayoutParams.WRAP_CONTENT;
				mLayout.addView(mImageView,params);
			}
		});
	}
}
