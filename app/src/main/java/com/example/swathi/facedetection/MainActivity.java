package com.example.swathi.facedetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    /**
     * There's nothing special about the Intent object you use when starting an activity for a result,
     * but you do need to pass an additional integer argument to the startActivityForResult() method.
     *
     * The integer argument is a "request code" that identifies your request. When you receive the result Intent,
     * the callback provides the same request code so that your app can properly identify the result and determine how to handle it.
     */
    private static final int TAKE_PICTURE_CODE = 100;

    private static final int MAX_FACES = 5;

    private Bitmap cameraBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        (findViewById(R.id.take_picture)).setOnClickListener(btnClick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(TAKE_PICTURE_CODE == requestCode){
            processCameraImage(data);
        }
    }

    private void processCameraImage(Intent data) {

        setContentView(R.layout.detectlayout);

        ((Button)findViewById(R.id.detect_face)).setOnClickListener(btnClick);

        ImageView imageView = (ImageView) findViewById(R.id.image_view);

        cameraBitmap = (Bitmap)data.getExtras().get("data");

        imageView.setImageBitmap(cameraBitmap);
    }

    private void openCamera()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE_CODE);
    }

    private void detectFace()
    {
        if(null != cameraBitmap)
        {
            int width = cameraBitmap.getWidth();
            int height = cameraBitmap.getHeight();

            FaceDetector detector = new FaceDetector(width,height,MAX_FACES);

            FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];

            Bitmap bitmap565 = Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);

            Paint ditherPaint = new Paint();

            Paint drawPaint = new Paint();

            ditherPaint.setDither(true);
            drawPaint.setColor(Color.RED);
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeWidth(2);

            Canvas canvas = new Canvas();

            canvas.setBitmap(bitmap565);
            canvas.drawBitmap(cameraBitmap, 0,0, ditherPaint);

            int facesFound = detector.findFaces(bitmap565,faces);

            PointF midPoint = new PointF();

            float eyeDistance = 0.0f;
            float confidence = 0.0f;

            Log.i("FaceDetector","Number of faces found : "+facesFound);

            TextView faceCountTxt = (TextView)findViewById(R.id.face_count);
            faceCountTxt.setText("Number of faces found : "+facesFound);
            if(facesFound == 0)
            {
                faceCountTxt.setTextColor(Color.RED);
            }
            else
            {
                faceCountTxt.setTextColor(Color.GREEN);
            }
            if(facesFound > 0)
            {
                for(int index=0; index<facesFound; ++index)
                {
                    faces[index].getMidPoint(midPoint);
                    eyeDistance = faces[index].eyesDistance();
                    confidence = faces[index].confidence();

                    Log.i("FaceDetector", "Confidence : "+confidence +
                            ", Eye distance : " + eyeDistance +
                            ", Mid Point : ("+midPoint.x+" , "+midPoint.y +")");

                    canvas.drawRect(midPoint.x - eyeDistance,
                            midPoint.y - eyeDistance,
                            midPoint.x + eyeDistance,
                            midPoint.y + eyeDistance,drawPaint);



                }
            }

            String filepath = Environment.getExternalStorageDirectory()+"/facedetect"+System.currentTimeMillis()+".jpg";

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(filepath);

                bitmap565.compress(Bitmap.CompressFormat.JPEG,90,fileOutputStream);

                fileOutputStream.flush();

                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ImageView imageView = (ImageView)findViewById(R.id.image_view);
            imageView.setImageBitmap(bitmap565);
        }

    }
    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.take_picture : openCamera();
                    break;
                case R.id.detect_face : detectFace();
                    break;
            }
        }
    };
}
