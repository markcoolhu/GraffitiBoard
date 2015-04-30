package hu.markcool.graffitiboard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class MainActivity extends Activity {


    private static final float TOUCH_TOLERANCE = 4;

    private SurfaceHolder surHolder = null;
    private Bitmap mBitmap = null; //create bitmap for to save the pixels
    private Canvas mCanvas = null;
    private Path mPath;
    private Paint mPaint;
    private float mX, mY;


    private boolean isPaint = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 上方狀態設定條消失
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


        SurfaceView surfViewDraw = (SurfaceView) findViewById(R.id.surfVDraw);
        surfViewDraw.setZOrderOnTop(true);
        surfViewDraw.setBackgroundColor(0Xffffffff);

        surHolder = surfViewDraw.getHolder();
        surHolder.setFormat(PixelFormat.TRANSLUCENT);





        // initial local layout object
        Button bSave = (Button) findViewById(R.id.btnSave);
        bSave.setOnClickListener(onSave);

        Button bClear = (Button) findViewById(R.id.clear);
        bClear.setOnClickListener(onClear);


        //set parameters of paint.
        mPaint = new Paint();

        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
//        mPaint.setColor(0xFFFFFFFF);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        //set the width of stroke.
        mPaint.setStrokeWidth(12);


        /**
         *  PointF mLeftSelectPoint = new PointF(0, 0);
         */
        mPath = new Path();

        surHolder.addCallback(new SurfaceHolder.Callback() {

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


                if (mBitmap == null) {
                    mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    mCanvas = new Canvas(mBitmap);  //初始化畫布

                }

                Log.i("surfaceChange", "surface");
            }

            public void surfaceCreated(SurfaceHolder holder) {
            }

            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

    }


    // save & upload sign photo
    private View.OnClickListener onSave = new View.OnClickListener() {
        public void onClick(View arg0) {

            if (isPaint) {

                saveBitmap(mBitmap);


            }
        }
    };


    // end activity
    private View.OnClickListener onClear = new View.OnClickListener() {
        public void onClick(View arg0) {

            isPaint = false;
            for (int i = 0; i < 4; i++) {
                clearDraw();
            }


        }
    };


    public void saveBitmap(Bitmap bitmap) {


        String savePath = Environment.getExternalStorageDirectory().getPath() + "/myboard/";
        String fileName = "signing.png";
        // thumbnail
        Bitmap newBitmapSize = reBitmapSize(bitmap, 300);


        // Matrix 物件
        Matrix matrix = new Matrix();

        // 設定 Matrix 物件，設定 x,y 向縮放比例為 0.55, 1.0
//            matrix.postScale(0.55F, 1.0F);    // 直立螢幕
        matrix.postScale(1.00F, 0.7F);     // 橫立螢幕

        // 設定 Matrix 物件，設定順時針旋轉270度
//            matrix.postRotate(0.0F);        // 橫立螢幕
//            matrix.postRotate(270.0F);    // 直立螢幕

        newBitmapSize = Bitmap.createBitmap(newBitmapSize, 0, 0,
                newBitmapSize.getWidth(), newBitmapSize.getHeight(),
                matrix, true);

        saveBitmap(newBitmapSize, savePath, fileName);


    }


    public static Bitmap reBitmapSize(Bitmap bit, int MaxPx) {


        int width = bit.getWidth(), height = bit.getHeight();


        if (width < MaxPx && height < MaxPx) {
            return bit;// 傳進來的圖比要的小，不處理
        }

        float ratio = 1;// 縮放比例


        if (width > height && width > MaxPx) {


            //這張圖比較寬，依寬度進行等比例縮放

            ratio = (float) MaxPx / (float) width;


        } else if (height > MaxPx) {


            //這張圖比較高，依高度進行等比例縮放

            ratio = (float) MaxPx / (float) height;

        }


        if (ratio >= 1) {


            //如果比例不需要縮小，返回原圖，如果你有放大需求，可以修改這邊

            return bit;


        } else {


            int newW = (int) (width * ratio), newH = (int) (height * ratio);


            if (newW <= 0 || newH <= 0) {


                if (width > height && width > MaxPx) {


                    ratio = (float) 1024.0 / (float) width;


                } else if (height > MaxPx) {


                    ratio = (float) 1024.0 / (float) height;


                }


                newW = (int) (width * ratio);


                newH = (int) (height * ratio);


            }


            Bitmap marker = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888);


            Canvas canvas = new Canvas(marker);


            try {

                System.out.println("width:" + width);
                System.out.println("height:" + height);
                System.out.println("newW:" + newW);
                System.out.println("newH:" + newH);


                canvas.drawBitmap(bit, new Rect(0, 0, width, height), new Rect(0, 0, newW, newH), null);


            } catch (Exception ignored) {
            }


            return marker;


        }


    }

    public void saveBitmap(Bitmap bitmap, String savePath, String saveFileName) {


        FileOutputStream fOut;

        try {


            File dir = new File(savePath);

            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    //do something
                    System.out.println("mkdirs savePath:" + savePath);
                }
            }

            fOut = new FileOutputStream(savePath + saveFileName);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);

            fOut.flush();
            fOut.close();

            showMsg("Save Success, file path->" + savePath + saveFileName);

        } catch (IOException e) {
            e.printStackTrace();
            showMsg("Save Error :" + e.toString());
        }


    }


    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
        mCanvas.drawPath(mPath, mPaint);

        myDraw();
    }

    private void touch_up() {
        System.out.println("mX:" + mX + ", mY:" + mY);

        mPath.lineTo(mX, mY);
        mPath.reset();
        myDraw();
        saveBuffer();

        //setting paint is true
        isPaint = true;

    }


    public void myDraw() {
        Canvas canvas = surHolder.lockCanvas();
        canvas.drawBitmap(mBitmap, 0, 0, null);
        surHolder.unlockCanvasAndPost(canvas);
    }


    // 清除畫面
    private void clearDraw() {

        Canvas canvas = surHolder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT);
//
        Paint p = new Paint();
        //清畫面
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(p);

        mCanvas.drawPaint(p);

        surHolder.unlockCanvasAndPost(canvas);
    }

    private byte[] saveBuffer() {
        byte[] buffer = new byte[mBitmap.getRowBytes() * mBitmap.getHeight()];
        Buffer byteBuffer = ByteBuffer.wrap(buffer);
        mBitmap.copyPixelsToBuffer(byteBuffer);
        return buffer;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                break;
        }
        return true;
    }


    private void showMsg(String message) {

        Builder alertDialog = new Builder(this);
        alertDialog.setTitle("MsgTitle");
        alertDialog.setMessage(message);
        DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // if don't do anything, close dialog
            }
        };
        alertDialog.setNeutralButton("OK", okClick);
        alertDialog.show();

//		Toast.makeText(this, context.getResources().getString(R.string.programs_network_error), Toast.LENGTH_LONG).show();

    }
}
