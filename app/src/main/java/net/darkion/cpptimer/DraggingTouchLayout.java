package net.darkion.cpptimer;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by DarkionAvey
 */


public class DraggingTouchLayout extends FrameLayout {
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Here is the place where you can inject whatever layout you want.

        leftRL = findViewById(R.id.leftRL);
        rightRL = findViewById(R.id.rightRL);
        leftWatch = (TextView) findViewById(R.id.leftWatch);
        rightWatch = (TextView) findViewById(R.id.rightWatch);

    }

    long startTimeRight = -1L;
    long timeInMillisecondsRight = 0L;
    long timeSwapBuffRight = 0L;
    long updatedTimeRight = 0L;
    int secsRight = 0;
    int minsRight = 0;
    int millisecondsRight = 0;


    long startTimeLeft = -1L;
    long timeInMillisecondsLeft = 0L;
    long timeSwapBuffLeft = 0L;
    long updatedTimeLeft = 0L;
    int secsLeft = 0;
    int minsLeft = 0;
    int millisecondsLeft = 0;
    TextView leftWatch, rightWatch;


    private void updateRightTime() {
        timeInMillisecondsRight = SystemClock.uptimeMillis() - startTimeRight;
        updatedTimeRight = timeSwapBuffRight + timeInMillisecondsRight;

        secsRight = (int) (updatedTimeRight / 1000);
        minsRight = secsRight / 60;
        secsRight = secsRight % 60;
        millisecondsRight = (int) (updatedTimeRight % 1000);

        rightWatch.setText("" + minsRight + ":" + String.format("%02d", secsRight) + ":"
                + String.format("%03d", millisecondsRight) + "\n(" + (minsRight * 60 + secsRight) + ")");
    }

    private void updateLeftTime() {
        timeInMillisecondsLeft = SystemClock.uptimeMillis() - startTimeLeft;
        updatedTimeLeft = timeSwapBuffLeft + timeInMillisecondsLeft;

        secsLeft = (int) (updatedTimeLeft / 1000);
        minsLeft = secsLeft / 60;
        secsLeft = secsLeft % 60;
        millisecondsLeft = (int) (updatedTimeLeft % 1000);

        leftWatch.setText("" + minsLeft + ":" + String.format("%02d", secsLeft) + ":"
                + String.format("%03d", millisecondsLeft) + "\n(" + (minsLeft * 60 + secsLeft) + ")");
    }

    Rect outRect = new Rect();

    private boolean isViewInBounds(View view, MotionEvent motionEvent) {
        outRect.left = view.getLeft();
        outRect.right = view.getRight();
        outRect.top = view.getTop();
        outRect.bottom = view.getBottom();
        return outRect.contains(view.getLeft() + (int) motionEvent.getX(), view.getTop() + (int) motionEvent.getY());
    }

    Rect rightRect = new Rect();
    Rect leftRect = new Rect();

    View leftRL, rightRL;

    private void refreshRectRight() {
        rightRL.getHitRect(rightRect);

    }

    private void refreshRectLeft() {
        leftRL.getHitRect(leftRect);

    }

    public void reset() {
        startTimeRight = -1L;
        timeInMillisecondsRight = 0L;
        timeSwapBuffRight = 0L;
        updatedTimeRight = 0L;
        secsRight = 0;
        minsRight = 0;
        millisecondsRight = 0;


        startTimeLeft = -1L;
        timeInMillisecondsLeft = 0L;
        timeSwapBuffLeft = 0L;
        updatedTimeLeft = 0L;
        secsLeft = 0;
        minsLeft = 0;
        millisecondsLeft = 0;

        updateRightTime();
        rightWatch.setText("----");

        updateLeftTime();
        leftWatch.setText("----");

        currentRunnable = null;
    }

    enum Location {
        LeftPane, RightPane, Other;
    }

    private Location getPointerLocation(MotionEvent motionEvent) {
        refreshRectLeft();
        refreshRectRight();
        if (rightRect.contains((int) motionEvent.getX(),rightRect.top)) {
            return Location.RightPane;
        } else if (leftRect.contains((int) motionEvent.getX(), leftRect.top)) {
            return Location.LeftPane;
        }

        return Location.Other;
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (currentLocation == Location.LeftPane) {
                updateLeftTime();
            } else if (currentLocation == Location.RightPane) {

                updateRightTime();
            }
            if (currentRunnable != null)
                mHandler.post(currentRunnable);

        }
    };
    Runnable currentRunnable = runnable;
    Handler mHandler = new Handler();
    Location currentLocation = Location.Other;

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        Location location = getPointerLocation(motionEvent);
        if (location != currentLocation) {
            currentLocation = getPointerLocation(motionEvent);
            Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(40);

        }
        //  Log.d("DIY", "Location " + location);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                currentRunnable = runnable;
                mHandler.post(currentRunnable);

                break;
            default:
                mHandler.removeCallbacks(currentRunnable);
                currentRunnable = null;

                if (currentLocation == Location.LeftPane) {
                    if (startTimeLeft < 0) startTimeLeft = SystemClock.uptimeMillis();
                    updateLeftTime();

                } else if (currentLocation == Location.RightPane) {
                    if (startTimeRight < 0) startTimeRight = SystemClock.uptimeMillis();
                    updateRightTime();
                }
                if (currentLocation != Location.RightPane) {
                    if (startTimeRight > 0) startTimeRight = -1;
                    else startTimeRight--;
                    if (startTimeRight > -2) {
                        timeSwapBuffRight += timeInMillisecondsRight;
                    }
                }
                if (currentLocation != Location.LeftPane) {
                    if (startTimeLeft > 0) startTimeLeft = -1;
                    else startTimeLeft--;
                    if (startTimeLeft > -2) {
                        timeSwapBuffLeft += timeInMillisecondsLeft;
                    }
                }

                //  Log.d("DIY", "startTimeLeft " + startTimeLeft + " startTimeRight " + startTimeRight);


        }
        return currentLocation != Location.Other;


    }

    //    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        if (isViewInBounds(leftRL, event)) {
//            return leftRL.dispatchTouchEvent(event);
//        } else if (isViewInBounds(rightRL, event)) {
//            return rightRL.dispatchTouchEvent(event);
//
//        }
//
//
//        return false;
//    }

    public DraggingTouchLayout(Context context) {
        super(context);
    }

    public DraggingTouchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggingTouchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
