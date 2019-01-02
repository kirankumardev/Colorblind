package com.example.kirankumar.eee508androidopencvtutorial;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import org.opencv.core.Point;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static android.speech.tts.TextToSpeech.SUCCESS;
import static com.example.kirankumar.eee508androidopencvtutorial.R.*;
import static java.util.Locale.UK;

import static java.util.Locale.US;
import android.widget.Button;


public  class MainActivity extends AppCompatActivity implements CvCameraViewListener, OnTouchListener {
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    double x = -1;
    double y = -1;
    int h = 8;
    int w = 8;
    Paint myPaint = new Paint();
    Canvas c;
    //Speech variables
    TextToSpeech toSpeech = null;
    String text, detected_Color;

    TextView touch_coordinates;
    TextView touch_color;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {

                    mOpenCvCameraView.enableView();

                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        touch_coordinates = (TextView) findViewById(id.touch_coordinates);
        touch_color = (TextView) findViewById(id.touch_color);

        mOpenCvCameraView= (JavaCameraView) findViewById(id.opencv_tutorial_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        toSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == SUCCESS){
                    int result = toSpeech.setLanguage(US);
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error","This value not supported");
                    }
                    else {
                        ConvertTextToSpeech();
                    }
                }
                else
                {
                    Log.e("error", "Initialization failed");
                }
            }
        });



    }

    @Override
    public void onPause(){
        super.onPause();
        if(mOpenCvCameraView!=null)
            mOpenCvCameraView.disableView();
    }




    @Override
    public void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug())
        {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this,mLoaderCallback);
        }
        else
        {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView!=null)
            mOpenCvCameraView.disableView();
        if(toSpeech != null)
        {
            toSpeech.stop();
            toSpeech.shutdown();
        }

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);


    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }



    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        mRgba = inputFrame;

            Imgproc.rectangle(mRgba, new Point(x - 10, y - 10), new Point(x + 10, y + 10), new Scalar(0, 255, 0), 2);
            Imgproc.putText(mRgba, "Color is " + detected_Color, new Point(x - 10, y - 10), 3, 1, new Scalar(255, 255,255,255),2);

        return mRgba;
    }


    private Scalar convertScalarHsv2Rgba(Scalar hsvColor){
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1,1,CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv,pointMatRgba,Imgproc.COLOR_HSV2RGB_FULL,4);
        return new Scalar(pointMatRgba.get(0,0));
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int cols = mRgba.cols();
        int rows = mRgba.rows();
        double yLow = (double)mOpenCvCameraView.getHeight() * 0.2401961;
        double yHigh = (double)mOpenCvCameraView.getHeight() * 0.7696078;
        double xScale = (double)cols / (double)mOpenCvCameraView.getWidth();
        double yScale = (double)rows / (yHigh-yLow);
        x = event.getX();
        y = event.getY();


        y = y - yLow;
        x = x * xScale;
        y = y * yScale;
        //int w = 8;
        //int h = 8;
        //Rect rect1 = new Rect(120,120, w,h);

        //Imgproc.rectangle(mRgba,rect1.tl(),rect1.br(),new Scalar(0,255,0),2);
        if((x < 0)||(y < 0)||(x > cols)||(y > rows))return false;
        touch_coordinates.setText("X: " + Double.valueOf(x) +" Y: " + Double.valueOf(y));
        Rect touchedRect = new Rect();

        touchedRect.x = (int) x;
        touchedRect.y = (int) y;

        touchedRect.width = 20;
        touchedRect.height = 20;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);
        Mat touchedRegionHsv = new Mat();

        Imgproc.cvtColor(touchedRegionRgba,touchedRegionHsv,Imgproc.COLOR_RGB2HSV_FULL,4);
        Imgproc.cvtColor(touchedRegionRgba,touchedRegionHsv,Imgproc.COLOR_RGB2HSV_FULL,4);

        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;

        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);
        int color = Color.rgb((int) mBlobColorRgba.val[0],(int) mBlobColorRgba.val[1],(int) mBlobColorRgba.val[2]);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        float[] hsv = new float[3];
        Color.RGBToHSV(red,green,blue,hsv);
        float hue = hsv[0];
        float sat = hsv[1];
        float val = hsv[2];
        touch_color.setText("Color: #" + String.format("%02X", (int)mBlobColorRgba.val[0])+ " Color: #" + String.format("%02X",(int)mBlobColorRgba.val[1])+ " Color: #" + String.format("%02X",(int)mBlobColorRgba.val[2])+" Hue: #"+(int) hue);
        touch_color.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],(int) mBlobColorRgba.val[1],(int) mBlobColorRgba.val[2]));
        touch_coordinates.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],(int) mBlobColorRgba.val[1],(int) mBlobColorRgba.val[2]));
    if (hue >= 0 && hue <= 15)
        detected_Color = "Red";
    else if (hue > 15 && hue <= 30)
        detected_Color = "Warm Red";
    else if (hue > 30 && hue <= 45)
        detected_Color = "Orange";
    else if (hue > 45 && hue <= 60)
        detected_Color = "Warm Yellow";
    else if (hue > 60 && hue <= 75)
        detected_Color = "Yellow";
    else if (hue > 75 && hue <= 90)
        detected_Color = "Cool Yellow";
    else if (hue > 90 && hue <= 105)
        detected_Color = "Yellow Green";
    else if (hue > 105 && hue <= 120)
        detected_Color = "Warm Green";
    else if (hue > 120 && hue <= 135)
        detected_Color = "Green";
    else if (hue > 135 && hue <= 150)
        detected_Color = "Cool Green";
    else if (hue > 150 && hue <= 165)
        detected_Color = "Green Cyan";
    else if (hue > 165 && hue <= 180)
        detected_Color = "Warm  Cyan";
    else if (hue > 180 && hue <= 195)
        detected_Color = "Cyan";
    else if (hue > 195 && hue <= 210)
        detected_Color = "Cool Cyan";
    else if (hue > 210 && hue <= 225)
        detected_Color = "Blue Cyan";
    else if (hue > 225 && hue <= 240)
        detected_Color = "Cool Blue";
    else if (hue > 240 && hue <= 255)
        detected_Color = "Blue";
    else if (hue > 255 && hue <= 270)
        detected_Color = "Warm Blue";
    else if (hue > 270 && hue <= 285)
        detected_Color = "Violet";
    else if (hue > 285 && hue <= 300)
        detected_Color = "Cool Magenta";
    else if (hue > 300 && hue <= 315)
        detected_Color = "Magenta";
    else if (hue > 315 && hue <= 330)
        detected_Color = "Warm Magenta";
    else if (hue > 330 && hue <= 345)
        detected_Color = "Red Magenta";
    else if (hue >= 345 && hue <= 359)
        detected_Color = "Cool Red";






        ConvertTextToSpeech();


        Toast.makeText(getApplicationContext(),"Completed",Toast.LENGTH_SHORT).show();
        return false;


    }


    private void ConvertTextToSpeech() {
        /* TODO Auto-generated method stub */
        text = touch_color.getText().toString();
        if (text == null || "".equals(text)) {
            text = "Content not available";
            toSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else
            toSpeech.speak("Color is " +detected_Color, TextToSpeech.QUEUE_FLUSH, null, null);



    }


}
