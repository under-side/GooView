package org.under_side.gooview.ui;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;

public abstract class MyAnimationDrawable extends AnimationDrawable {
	// 判断结束的Handler
    Handler finishHandler;      
    public MyAnimationDrawable(AnimationDrawable ad) {
        // 这里需要自己手动的把每一帧加进去
        for (int i = 0; i < ad.getNumberOfFrames(); i++) {
            this.addFrame(ad.getFrame(i), ad.getDuration(i));
        }
    }
    /**
     * 首先用父类的start()
     * 然后启动线程，来调用onAnimationEnd()
     */
    @Override
    public void start() {
        super.start();
        
        finishHandler = new Handler();
        //当播放动画的时间全部结束后开始发送该Message，开始调用onAnimationEnd方法实现动画结束操作
        finishHandler.postDelayed(
            new Runnable() {
                public void run() {
                    onAnimationEnd();
                }
            }, getTotalDuration());
    }
    /**
     * 这个方法获得动画的持续时间（之后调用onAnimationEnd()）
     */
    public int getTotalDuration() {
        int durationTime = 0;
        for (int i = 0; i < this.getNumberOfFrames(); i++) {
            durationTime += this.getDuration(i);
        }
        return durationTime;
    }
    /**
     * 结束时调用的方法，一定要实现
     */
   public abstract void onAnimationEnd();
}
