package io.rong.app;

import android.app.Application;
import android.os.CountDownTimer;
import android.test.ApplicationTestCase;

import com.blankj.utilcode.util.LogUtils;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    private CountDownTimer countDownTimer;
    public ApplicationTest() {
        super(Application.class);

        countDownTimer = new CountDownTimer(1000,8000) {
            @Override
            public void onTick(long l) {
                LogUtils.w("dyc",l/1000);

            }

            @Override
            public void onFinish() {

            }
        };

        countDownTimer.start();
    }



}