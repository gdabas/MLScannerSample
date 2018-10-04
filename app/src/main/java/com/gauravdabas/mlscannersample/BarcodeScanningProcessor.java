package com.gauravdabas.mlscannersample;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.IOException;
import java.util.List;

public class BarcodeScanningProcessor extends VisionProcessorBase<List<FirebaseVisionBarcode>> {
    private static final String TAG = "BarcodeScanProc";

    private final FirebaseVisionBarcodeDetector detector;
    private final FirebaseBarcodeScanning mContext;

    public BarcodeScanningProcessor(FirebaseBarcodeScanning context) {
        // Note that if you know which format of barcode your app is dealing with, detection will be
        // faster to specify the supported barcode formats one by one, e.g.
        // new FirebaseVisionBarcodeDetectorOptions.Builder()
        //     .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
        //     .build();
        mContext = context;
        detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Barcode Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionBarcode>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(@NonNull List<FirebaseVisionBarcode> barcodes) {
        for (int i = 0; i < barcodes.size(); ++i) {
            FirebaseVisionBarcode barcode = barcodes.get(i);
            mContext.setScannedResult(barcode);
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Barcode detection failed " + e);
    }
}