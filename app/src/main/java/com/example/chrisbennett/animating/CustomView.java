package com.example.chrisbennett.animating;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class CustomView extends SurfaceView implements SurfaceHolder.Callback {

    protected Context context;
    private Bitmap balloon;
    private Bitmap bwBalloon;
    DrawingThread thread;
    Paint text;
    int x,y;
    int score;
    int speed;

    public CustomView(Context ctx, AttributeSet attrs) {
        super(ctx,attrs);
        context = ctx;

        balloon = BitmapFactory.decodeResource(context.getResources(),R.drawable.formation);
        bwBalloon=balloon.copy(Bitmap.Config.ARGB_8888, true);
        bwBalloon = resizeBitmap(bwBalloon,200,200);

        for(int i=0;i<bwBalloon.getWidth();i++) {
           for(int j=0;j<bwBalloon.getHeight();j++) {
                int g = Color.red(bwBalloon.getPixel(i,j));

            }
        }

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        text=new Paint();
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(Color.WHITE);
        text.setTextSize(24);
        x=0;
        y=0;
        score = 0;
        speed = 0;

    }


    public Bitmap resizeBitmap(Bitmap b, int newWidth, int newHeight) {
        int w = b.getWidth();
        int h = b.getHeight();
        float scaleWidth = ((float) newWidth) / w;
        float scaleHeight = ((float) newHeight) / h;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                b, 0, 0, w, h, matrix, false);
        b.recycle();
        return resizedBitmap;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
        boolean waitingForDeath = true;
        while(waitingForDeath) {
            try {
                thread.join();
                waitingForDeath = false;
            }
            catch (Exception e) {
                Log.v("Thread Exception", "Waiting on drawing thread to die: " + e.getMessage());
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread= new DrawingThread(holder, context, this);
        thread.setRunning(true);
        thread.start();
    }


    public void customDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bwBalloon,x,y,null);
        canvas.drawText("Score: " + score,600,50,text);
        //x++;
        y+=1;
        //Log.v("drawing", "y: " + y);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v("touch event",event.getX() + "," + event.getY());

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            double distance = Math.sqrt((x + bwBalloon.getWidth()/2) * (x - event.getX()) + (y - bwBalloon.getHeight()/2) * (y - event.getY()));
            if (distance < 100) {
                score++;
                x = (int) (Math.random() * 500);
                y = 0;
            }
            if(score > 5){
                speed = speed *2;
            }
        }
        return true;
    }


    class DrawingThread extends Thread {
        private boolean running;
        private Canvas canvas;
        private SurfaceHolder holder;
        private Context context;
        private CustomView view;

        private int FRAME_RATE = 30;
        private double delay = 1.0 / FRAME_RATE * 1000;
        private long time;

        public DrawingThread(SurfaceHolder holder, Context c, CustomView v) {
            this.holder=holder;
            context = c;
            view = v;
            time = System.currentTimeMillis();
        }

        void setRunning(boolean r) {
            running = r;
        }

        @Override
        public void run() {
            super.run();
            while(running){
                if(System.currentTimeMillis() - time > delay) {
                    time = System.currentTimeMillis();
                    canvas = holder.lockCanvas();
                    if(canvas!=null){
                        view.customDraw(canvas);
                        holder.unlockCanvasAndPost(canvas);
                    }

                }
            }
        }



    }
}
