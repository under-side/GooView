package org.under_side.gooview.ui;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;

public abstract class MyAnimationDrawable extends AnimationDrawable {
	// �жϽ�����Handler
    Handler finishHandler;      
    public MyAnimationDrawable(AnimationDrawable ad) {
        // ������Ҫ�Լ��ֶ��İ�ÿһ֡�ӽ�ȥ
        for (int i = 0; i < ad.getNumberOfFrames(); i++) {
            this.addFrame(ad.getFrame(i), ad.getDuration(i));
        }
    }
    /**
     * �����ø����start()
     * Ȼ�������̣߳�������onAnimationEnd()
     */
    @Override
    public void start() {
        super.start();
        
        finishHandler = new Handler();
        //�����Ŷ�����ʱ��ȫ��������ʼ���͸�Message����ʼ����onAnimationEnd����ʵ�ֶ�����������
        finishHandler.postDelayed(
            new Runnable() {
                public void run() {
                    onAnimationEnd();
                }
            }, getTotalDuration());
    }
    /**
     * ���������ö����ĳ���ʱ�䣨֮�����onAnimationEnd()��
     */
    public int getTotalDuration() {
        int durationTime = 0;
        for (int i = 0; i < this.getNumberOfFrames(); i++) {
            durationTime += this.getDuration(i);
        }
        return durationTime;
    }
    /**
     * ����ʱ���õķ�����һ��Ҫʵ��
     */
   public abstract void onAnimationEnd();
}
