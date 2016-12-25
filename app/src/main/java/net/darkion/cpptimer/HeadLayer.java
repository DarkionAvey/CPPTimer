package net.darkion.cpptimer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;


/**
 * Created by DarkionAvey
 */

public class HeadLayer extends View {

    private Context mContext;
    private DraggingTouchLayout mFrameLayout;
    private WindowManager mWindowManager;

    public HeadLayer(Context context) {
        super(context);

        mContext = context;

        mFrameLayout = (DraggingTouchLayout) LayoutInflater.from(mContext).inflate(R.layout.head, null);
        addToWindowManager();
    }


    private void hideControls() {
        mFrameLayout.findViewById(R.id.panels).setVisibility(GONE);
        controller.setVisibility(VISIBLE);

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        mWindowManager.updateViewLayout(mFrameLayout, params);
    }

    private void showControls() {
        mFrameLayout.findViewById(R.id.panels).setVisibility(VISIBLE);
        controller.setVisibility(GONE);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        params.width = width;
        params.height = height / 5;

        mWindowManager.updateViewLayout(mFrameLayout, params);
        controller = (ImageView) mFrameLayout.findViewById(R.id.imageView);
        ImageView remove = (ImageView) mFrameLayout.findViewById(R.id.remove);
        remove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                hideControls();
            }
        });
        remove.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (terminateListener != null) terminateListener.terminate();

                return true;
            }
        });

    }

    ImageView controller;
    WindowManager.LayoutParams params;

    private class SingleTapConfirm extends SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            showControls();
            return true;
        }


    }



    GestureDetector gestureDetector;

    private void addToWindowManager() {
        params = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mFrameLayout, params);


        gestureDetector = new GestureDetector(getContext(), new SingleTapConfirm());

        // Support dragging the image view
        controller = (ImageView) mFrameLayout.findViewById(R.id.imageView);

        controller.setOnTouchListener(listener);
        mFrameLayout.findViewById(R.id.move).setOnTouchListener(listener);



        mFrameLayout.findViewById(R.id.terminate).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mFrameLayout.reset();

            }
        });

    }

    TerminateListener terminateListener;

    public void setTerminateListener(TerminateListener terminateListener) {
        this.terminateListener = terminateListener;
    }

    interface TerminateListener {
        void terminate();
    }

    OnTouchListener listener = new OnTouchListener() {
        private int initX, initY;
        private int initTouchX, initTouchY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (gestureDetector.onTouchEvent(event)) {
                // single tap
                return true;
            } else {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initX = params.x;
                        initY = params.y;
                        initTouchX = x;
                        initTouchY = y;
                        return true;

                    case MotionEvent.ACTION_UP:
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initX + (x - initTouchX);
                        params.y = initY + (y - initTouchY);

                        // Invalidate layout
                        mWindowManager.updateViewLayout(mFrameLayout, params);
                        return true;
                }
            }
            return false;
        }
    };

    /**
     * Removes the view from window manager.
     */
    public void destroy() {
        mWindowManager.removeView(mFrameLayout);
    }
}