package com.gauravdabas.mlscannersample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Vibrator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static com.gauravdabas.mlscannersample.FirebaseBarcodeScanning.SCANNED_TEXT;

public class MainActivity extends AppCompatActivity {

    File sdCard = Environment.getExternalStorageDirectory();
    File filePath = new File(sdCard.getAbsolutePath() + "/Scans");
    File file;
    FileOutputStream os;
    long startTime;
    long endTime;
    long tts;

    TextView mTtsText;
    public static final String DATETIME_FORMAT_MILLISECOND = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "dd-MMM-yyyy";
    Scan scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scan = new Scan();

        if (!filePath.exists()){
            filePath.mkdir();
        }

        try {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
            String formattedDate = df.format(c);
            file = new File(filePath, "Scans-" + formattedDate + ".csv");

            if (!file.exists()){
                file.createNewFile();
                writeToFile("StartTime,EndTime,BarcodeText,TTS");
            }
        } catch (Exception e){
            System.out.println("e: " + e);
        }

        mTtsText = findViewById(R.id.tts_text);
        Button button = findViewById(R.id.scan_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FirebaseBarcodeScanning.class);
                startActivityForResult(intent, 1);
                startTime = System.currentTimeMillis();
                scan.setStartTime(getCurrentTime(Calendar.getInstance().getTime()));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                endTime = System.currentTimeMillis();
                float tts = (endTime - startTime) / 1000;
                scan.setTts(String.valueOf(tts));

                mTtsText.setText("Time to scan : " + tts + " sec");

                try {
                    MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.decodeshort);
                    mp.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(500);

                String scannedBarcode = data.getStringExtra(SCANNED_TEXT);
                scan.setEndTime(getCurrentTime(Calendar.getInstance().getTime()));
                scan.setBarcodeText(scannedBarcode);
                writeToFile(scan.toString());

                Toast.makeText(this, "Scanned Item: " + scannedBarcode, Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public String getCurrentTime(Date DateTime){
        SimpleDateFormat df = new SimpleDateFormat(DATETIME_FORMAT_MILLISECOND);
        String formattedDate = df.format(DateTime);
        return formattedDate;
    }

    public void writeToFile(String scanResult){
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(scanResult);
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


     public class Scan {
        private String startTime;
        private String endTime;
        private String barcodeText;
        private String tts;

         public String getStartTime() {
             return startTime;
         }

         public void setStartTime(String startTime) {
             this.startTime = startTime;
         }

         public String getEndTime() {
             return endTime;
         }

         public void setEndTime(String endTime) {
             this.endTime = endTime;
         }

         public String getBarcodeText() {
             return barcodeText;
         }

         public void setBarcodeText(String barcodeText) {
             this.barcodeText = barcodeText;
         }

         public String getTts() {
             return tts;
         }

         public void setTts(String tts) {
             this.tts = tts;
         }

         @Override
         public String toString() {
             return startTime + "," + endTime + "," + barcodeText + "," + tts ;
         }
     }
}
