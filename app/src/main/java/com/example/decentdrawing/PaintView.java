package com.example.decentdrawing;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PaintView extends View {

    private int BRUSH_SIZE = 10;
    private static final int COLOR_PEN = Color.RED;
    private static final int COLOR_ERASER = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;

    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private int currentColor;
    private ArrayList<FingerPath> paths = new ArrayList<>();

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    private ProgressDialog pd;
    public int mode = 1;
    private final Point p1 = new Point();
    public float currentX;
    public float currentY;



    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();

        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(COLOR_PEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);

        pd = new ProgressDialog(context);
    }

    public void init(DisplayMetrics metrics){
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = COLOR_PEN;
    }

    public void changeBrushColor(String color) {
        currentColor = Color.parseColor(color);
        mPaint.setColor(currentColor);
    }

    public void changeMode(int input) {
        mode = input;
    }

    public void pen(){
        //currentColor = COLOR_PEN;
    }

    public void eraser(){
        currentColor = COLOR_ERASER;
    }

    public void clear(){
        paths.clear();
        pen();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        // mCanvas.drawColor(DEFAULT_BG_COLOR);

        for (FingerPath fp : paths){
            mPaint.setColor(fp.getColor());
            mPaint.setStrokeWidth(fp.getStrokeWidth());
            mPaint.setMaskFilter(null);
            mCanvas.drawPath(fp.getPath(), mPaint);
        }

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y){
        if (MainActivity.EraserMode) {
            BRUSH_SIZE = 40;
        } else {
            BRUSH_SIZE = 10;
        }
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, BRUSH_SIZE, mPath);
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y){
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp(){
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        currentX = x;
        currentY = y;

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN :
                if (mode > 0) {
                    touchStart(x, y);
                    invalidate();
                    break;
                } else {
                    p1.x = (int) x;
                    p1.y = (int) y;
                    final int sourceColor = mBitmap.getPixel((int) x, (int) y);
                    final int targetColor = currentColor;
                    new TheTask(mBitmap, p1, sourceColor, targetColor).execute();
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE :
                if (mode > 0) {
                    touchMove(x, y);
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_UP :
                if (mode > 0) {
                    touchUp();
                    invalidate();
                    break;
                }
        }

        return true;
    }

    class TheTask extends AsyncTask<Void, Integer, Void> {

        Bitmap bmp;
        Point pt;
        int replacementColor, targetColor;

        public TheTask(Bitmap bm, Point p, int sc, int tc) {
            this.bmp = bm;
            this.pt = p;
            this.replacementColor = tc;
            this.targetColor = sc;
            pd.setMessage("Filling....");
            pd.show();
        }

        @Override
        protected void onPreExecute() {
            pd.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        @Override
        protected Void doInBackground(Void... params) {
            FloodFill f = new FloodFill();
            f.floodFill(bmp, pt, targetColor, replacementColor);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
            invalidate();
        }
    }

//    public float getCurrentX() {
//        return currentX;
//    }
//    public float getCurrentY() {
//        return currentY;
//    }
}
