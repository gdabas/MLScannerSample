package com.gauravdabas.mlscannersample;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirebaseBarcodeScanning extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "FirebaseBarcodeScanning";
    public static final String  SCANNED_TEXT = "scanned_text";

    private static final int REQUEST_CODE = 1;
    private static final int PERMISSION_REQUESTS = 1;
    private CameraSourcePreview preview;
    private CameraSource cameraSource = null;
    ImageButton flashButton;
    ObjectAnimator animator = null;
    View scannerLayout;
    View scannerBar;

    /*Button startCamera;
    TextView scannedText;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        /*startCamera = findViewById(R.id.camera_button);
        scannedText = findViewById(R.id.scanned_text);*/
        scannerLayout = findViewById(R.id.scannerLayout);
        scannerBar = findViewById(R.id.scannerBar);
        flashButton = findViewById(R.id.flash_button);

        preview = (CameraSourcePreview) findViewById(R.id.firePreview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }

        preview.stop();
        if (allPermissionsGranted()) {
            createCameraSource();
        } else {
            getRuntimePermissions();
        }

        startCameraSource();

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (flashButton.getTag() != null && flashButton.getTag().equals("OFF")){
                        cameraSource.startFlashMode(true);
                        flashButton.setImageResource(R.drawable.baseline_flash_on_white_18dp);
                        flashButton.setTag("ON");
                        startCameraSource();
                    } else {
                        cameraSource.startFlashMode(false);
                        flashButton.setImageResource(R.drawable.baseline_flash_off_white_18dp);
                        flashButton.setTag("OFF");
                        startCameraSource();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ViewTreeObserver vto = scannerLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scannerLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    scannerLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    scannerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                float destination = (float)(scannerLayout.getY() + scannerLayout.getHeight());
                animator = ObjectAnimator.ofFloat(scannerBar, "translationY", scannerLayout.getY(), destination);
                animator.setRepeatMode(ValueAnimator.REVERSE);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(2000);
                animator.start();
            }
        });
        /*startCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                preview.start(cameraSource);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void setScannedResult(FirebaseVisionBarcode barcode){
        String result = barcode.getRawValue();

        Intent returnIntent = new Intent();
        returnIntent.putExtra(SCANNED_TEXT, result);
        setResult(Activity.RESULT_OK, returnIntent);
        preview.release();
        finish();
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this);
        }


        Log.i(TAG, "Using Barcode Detector Processor");
        cameraSource.setMachineLearningFrameProcessor(new BarcodeScanningProcessor(this));
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        preview.release();
        finish();
    }
}
