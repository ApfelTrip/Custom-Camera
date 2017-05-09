package com.denger.customcamera;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by benjamindenger on 09.05.17.
 */

public class CustomCamera implements SurfaceHolder.Callback {
    private Activity context;
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;

    ImageButton buttonTakePicture;

    final int RESULT_SAVEIMAGE = 0;

    /**
     * Called when the activity is first created.
     */
    public CustomCamera(Activity activity) {
        context = activity;
        init();
    }

    private void init() {
        surfaceView = (SurfaceView) context.findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        controlInflater = LayoutInflater.from(context.getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.control, null);
        RelativeLayout.LayoutParams layoutParamsControl
                = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT);
        context.addContentView(viewControl, layoutParamsControl);

        buttonTakePicture = (ImageButton) context.findViewById(R.id.btnTakePicture);
        buttonTakePicture.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                camera.takePicture(myShutterCallback,
                        myPictureCallback_RAW, myPictureCallback_JPG);
            }
        });

        RelativeLayout layoutBackground = (RelativeLayout) context.findViewById(R.id.background);
        layoutBackground.setOnClickListener(new LinearLayout.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                buttonTakePicture.setEnabled(false);
                camera.autoFocus(myAutoFocusCallback);
            }
        });
    }

    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            buttonTakePicture.setEnabled(true);
        }
    };

    Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback() {

        @Override
        public void onShutter() {

        }
    };

    Camera.PictureCallback myPictureCallback_RAW = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {

        }
    };

    Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
   /*Bitmap bitmapPicture
    = BitmapFactory.decodeByteArray(arg0, 0, arg0.length); */

            Uri uriTarget = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());

            OutputStream imageFileOS;
            try {
                imageFileOS = context.getContentResolver().openOutputStream(uriTarget);
                imageFileOS.write(arg0);
                imageFileOS.flush();
                imageFileOS.close();

                Toast.makeText(context,
                        "Image saved: " + uriTarget.toString(),
                        Toast.LENGTH_LONG).show();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            camera.startPreview();
        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (previewing) {
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            releaseCameraAndPreview();
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            camera.setDisplayOrientation(90);
        } catch (Exception e) {
            Log.e(context.getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
    }

    private void releaseCameraAndPreview() {
        if (camera != null) {
            Log.e("releaseCamera: ", "yep");
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
            previewing = false;
        }
    }
}