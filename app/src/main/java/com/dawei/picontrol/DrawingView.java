package com.dawei.picontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dawei.picontrol.fragment.DrawingArmControl;
import com.dawei.picontrol.module.Arm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

public class DrawingView extends View implements View.OnClickListener {

    private static final String TAG = "DrawingView";
    private static final String DIR = "DrawPad";

    private static final float LEFT_TOP_X = 30f;
    private static final float LEFT_TOP_Y = 30f;
    private static final int IMG_WIDTH = 972;
    private static final int IMG_HEIGHT = 480;
    /**
     * Four basics for drawing: paint, path, bitmap, canvas.
     */
    private Paint mPaint;
    private Path mPath;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private float mLastX;
    private float mLastY;

    private Stack<Trail> trails;
    private Stack<Trail> erasedTrails;

    private DrawingArmControl host;

    private boolean penUp;

    public class Trail {
        public Paint paint;
        public Path path;

        public Trail(Paint paint, Path path) {
            this.paint = paint;
            this.path = path;
        }

        public void draw(Canvas c) {
            c.drawPath(path, paint);
        }
    }

    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawingCacheEnabled(true);
        setDefaultPaint();
        mBitmap = Bitmap.createBitmap(IMG_WIDTH, IMG_HEIGHT, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        trails = new Stack<>();
        erasedTrails = new Stack<>();
    }

    public void setHost(DrawingArmControl f) {
        this.host = f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int a = event.getAction();
        // With regarding to bitmap.
        float currentX = event.getX() - LEFT_TOP_X;
        float currentY = event.getY() - LEFT_TOP_Y;
        if (!host.isEnabled())
            return true;
        switch (a) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "Action started at " + currentX + " ," + currentY);
                mLastX = currentX;
                mLastY = currentY;
                // Initialize a new trail/path.
                mPath = new Path();
                mPath.moveTo(currentX, currentY);
                break;
            case MotionEvent.ACTION_MOVE:
                if (checkRange(currentX, currentY)) {
                    Log.d(TAG, "Thread: " + Thread.currentThread().getName());
                    Log.d(TAG, String.format("Moving... from (%f, %f) to (%f, %f).", mLastX, mLastY, currentX, currentY));
                    mPath.quadTo(mLastX, mLastY, currentX, currentY);
                    if (host.isDrawing) {
                        host.arm.setPosition(host.arm.transferX(currentX), host.arm.transferY(currentY), Arm.DRAW_Z);
                    }
                    mCanvas.drawPath(mPath, mPaint);
                    invalidate();
                    mLastX = currentX;
                    mLastY = currentY;
                }
                break;
            case MotionEvent.ACTION_UP:
                saveTrail();
                Log.d(TAG, "Action finished at " + currentX + " ," + currentY);
                if (host.isDrawing) {
                    host.arm.setLeft(0);
                }
                break;

        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, LEFT_TOP_X, LEFT_TOP_Y, mPaint);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "Clicked!");
    }

    private void saveTrail() {
        trails.push(new Trail(mPaint, mPath));
    }

    public void undo() {
        if (!trails.isEmpty()) {
            erasedTrails.push(trails.pop());
        }
        reDraw();
    }

    public void redo() {
        if (!erasedTrails.isEmpty()) {
            trails.push(erasedTrails.pop());
        }
        reDraw();
    }

    private void reDraw(){
        if (trails != null) {
            mBitmap.eraseColor(Color.TRANSPARENT);
            for (Trail t : trails)
                t.draw(mCanvas);
            invalidate();
        }
    }

    public void clear() {
        while (!trails.isEmpty()) {
            erasedTrails.push(trails.pop());
        }
        reDraw();
    }

    public void setDefaultPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(12);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeMiter(1.0f);
    }

    public void saveImage() {
        File picDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), DIR);
        // Create required dirs.
        if (!picDir.exists()) {
            boolean success = picDir.mkdirs();
            if (!success) {
                Log.d(TAG, "Cannot find picture directory.");
                return;
            }
        }
        // Create required files.
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(picDir.getAbsolutePath(), fileName);
        try {
            boolean success = file.createNewFile();
            if (!success) {
                Log.d(TAG, "Cannot create the new BMP file: " + file.getAbsolutePath());
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            Log.d(TAG, "File saved.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "Cannot find the new created file.");
        } catch (IOException e) {
            Log.d(TAG, "Cannot save the file.");
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean checkRange(float x, float y) {
        return x >= 0 && x <= IMG_WIDTH && y >= 0 && y <= IMG_HEIGHT;
    }

    public Stack<Trail> getTrails() {
        return this.trails;
    }

    public static int getImgWidth() {
        return IMG_WIDTH;
    }

    public static int getImgHeight() {
        return IMG_HEIGHT;
    }
}
