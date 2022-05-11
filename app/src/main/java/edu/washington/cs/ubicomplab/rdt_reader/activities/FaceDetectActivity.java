package edu.washington.cs.ubicomplab.rdt_reader.activities;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;



import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.washington.cs.ubicomplab.rdt_reader.R;

public class FaceDetectActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    JavaCameraView javaCameraView;
    File cascFile;
    CascadeClassifier faceDetector;
    private Mat mRgba, mGrey;
    private final int PERMISSIONS_READ_CAMERA = 1;
    public static final int MY_PERMISSION_REQUEST_CODE = 100;
    int activeCamera = CameraBridgeViewBase.CAMERA_ID_FRONT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);

        javaCameraView =
                (JavaCameraView)findViewById(R.id.javaCameraView);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);


// checking if the permission has already been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions granted");
            initializeCamera(javaCameraView, activeCamera);
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_REQUEST_CODE);
        }


      //  initializeCamera(javaCameraView,activeCamera);

       // javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.setMaxFrameSize(1280, 720);







        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseCallback);
        } else {
            try {
                baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

       // javaCameraView.setCvCameraViewListener(this);
    }


  /*  @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setContentView(R.layout.activity_detect);

    }
*/



    @Override
    protected void onResume() {
        super.onResume();
        //initView();
        //getPermission();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mGrey = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGrey.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGrey = inputFrame.gray();

        //detectface

        MatOfRect faceDetection = new MatOfRect();
        faceDetector.detectMultiScale(mRgba, faceDetection);

        for (Rect rect : faceDetection.toArray()) {
            Imgproc.rectangle(mRgba, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(255, 0, 0));
        }


        return null;
    }

    private BaseLoaderCallback baseCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
try {
    switch (status) {
        case LoaderCallbackInterface.SUCCESS: {
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
            File cascadeDir = getDir("cascade", MODE_PRIVATE);
            cascFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");
            FileOutputStream fos = new FileOutputStream(cascFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            is.close();
            fos.close();

            faceDetector = new CascadeClassifier(cascFile.getAbsolutePath());

            if (faceDetector.empty()) {
                faceDetector = null;
            } else
                cascadeDir.delete();

            javaCameraView.enableView();
            break;
        }

        default: {
            super.onManagerConnected(status);
            break;
        }


    }
}catch (Exception e){e.printStackTrace();}

        }
    };

    private void activateOpenCVCameraView() {


//        javaCameraView.setVisibility(SurfaceView.VISIBLE);
       // javaCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.enableView();

/*
        // everything needed to start a camera preview
        javaCameraView = binding.cameraView;
        javaCameraView.setCameraPermissionGranted();
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY)
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
        mOpenCvCameraView.enableView()*/
    }



    // callback to be executed after the user has givenapproval or rejection via system prompt
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                initializeCamera(javaCameraView, activeCamera);
            } else {
                // camera will stay off
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void initializeCamera(JavaCameraView javaCameraView, int activeCamera){

        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCameraIndex(activeCamera);
        javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }
}
