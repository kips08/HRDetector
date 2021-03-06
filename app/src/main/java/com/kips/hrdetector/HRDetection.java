package com.kips.hrdetector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kips.hrdetector.helper.DatabaseHelper;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * This class extends Activity to handle a picture preview, process the preview
 * for a red values and determine a heart beat.
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class HRDetection extends Activity {

    private final String TAG = "HeartRateMonitor";
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final int averageArraySize = 4;
    private final int[] averageArray = new int[averageArraySize];
    private final int beatsArraySize = 3;
    private final int[] beatsArray = new int[beatsArraySize];
    private SurfaceHolder previewHolder = null;
    private Camera camera = null;
    @SuppressLint("StaticFieldLeak")
    private View image = null;
    @SuppressLint("StaticFieldLeak")
    private TextView text = null;
    @SuppressLint("StaticFieldLeak")
    private TextView infoText = null;
    @SuppressLint("StaticFieldLeak")
    private TextView imgavgtxt = null;
    @SuppressLint("StaticFieldLeak")
    private TextView rollavgtxt = null;
    @SuppressLint("StaticFieldLeak")
    private TextView stresslv = null;
    @SuppressLint("StaticFieldLeak")
    private TextView averagebpm = null;
    private Button startbutt;
    private WakeLock wakeLock = null;

    private static TYPE currentType = TYPE.GREEN;

    public static TYPE getCurrent() {
        return currentType;
    }

    private int averageIndex = 0;
    private int beatsIndex = 0;
    private double beats = 0;
    private long startTime = 0;
    private Context mContext;
    private DatabaseHelper dbHRMonitor;
    private PreviewCallback previewCallback = new PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            if (!processing.compareAndSet(false, true)) return;

            int width = size.width;
            int height = size.height;

            int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);
            // Log.i(TAG, "imgAvg="+imgAvg);
            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false);
                return;
            }
            if (imgAvg < 200) {
                startTime = System.currentTimeMillis();
                beats = 0;
                processing.set(false);
                infoText.setText("Posisi jari tidak tepat!");
                return;
            }
            infoText.setText("Mengukur Denyut Nadi");
            int averageArrayAvg = 0;
            int averageArrayCnt = 0;
            for (int i = 0; i < averageArray.length; i++) {
                if (averageArray[i] > 0) {
                    averageArrayAvg += averageArray[i];
                    averageArrayCnt++;
                }
            }

            int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
            TYPE newType = currentType;

            imgavgtxt.setText("image average:" + imgAvg);
            rollavgtxt.setText("rolling average:" + rollingAverage);
            if (imgAvg < rollingAverage) {
                newType = TYPE.RED;
                if (newType != currentType) {
                    beats++;
                    // Log.d(TAG, "BEAT!! beats="+beats);
                }
            } else if (imgAvg > rollingAverage) {
                newType = TYPE.GREEN;
            }

            if (averageIndex == averageArraySize) averageIndex = 0;
            averageArray[averageIndex] = imgAvg;
            averageIndex++;

            // Transitioned from one state to another to the same
            if (newType != currentType) {
                currentType = newType;
                image.postInvalidate();
            }

            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000d;
            if (totalTimeInSecs >= 10) {
//                Log.d(TAG, "Beats : "+beats+" Waktu : "+totalTimeInSecs);
                double bps = (beats / totalTimeInSecs);
                int dpm = (int) (bps * 60d);
                if (dpm < 30 || dpm > 180) {
                    startTime = System.currentTimeMillis();
                    beats = 0;

                    processing.set(false);
                    return;
                }

                // Log.d(TAG,
                // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);

                if (beatsIndex == beatsArraySize) beatsIndex = 0;
                beatsArray[beatsIndex] = dpm;
                beatsIndex++;

                int beatsArrayAvg = 0;
                int beatsArrayCnt = 0;
                for (int i = 0; i < beatsArray.length; i++) {
                    if (beatsArray[i] > 0) {
                        beatsArrayAvg += beatsArray[i];
                        beatsArrayCnt++;
                    }
                }
                int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                text.setText(String.valueOf(dpm));
                averagebpm.setText(String.valueOf(beatsAvg));
                if (beatsAvg > 60 && beatsAvg < 70) {
                    stresslv.setText("RILEKS");
                    stresslv.setTextColor(Color.BLUE);
                    averagebpm.setTextColor(Color.BLUE);
                } else if (beatsAvg > 70 && beatsAvg < 90) {
                    stresslv.setText("Tenang");
                    stresslv.setTextColor(Color.GREEN);
                    averagebpm.setTextColor(Color.GREEN);
                } else if (beatsAvg > 90 && beatsAvg < 100) {
                    stresslv.setText("Cemas");
                    stresslv.setTextColor(Color.YELLOW);
                    averagebpm.setTextColor(Color.YELLOW);
                } else if (beatsAvg > 100) {
                    stresslv.setText("Stres");
                    stresslv.setTextColor(Color.RED);
                    averagebpm.setTextColor(Color.RED);
                }
                Log.d(TAG, "index: " + beatsIndex);
                startTime = System.currentTimeMillis();
                beats = 0;

                // Jika sudah 3 kita masukkan ke DB
                if (beatsIndex == 3) {
                    String setStresslv = stresslv.getText().toString();
                    Integer avgbpm = beatsAvg;
                    Log.d(TAG, "DATABASE NAME : " + dbHRMonitor.getDatabaseName() + " stresslv = " + setStresslv);
                    String value = "Hello world";
                    Intent i = new Intent(HRDetection.this, Result.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.putExtra("avgbpm", beatsAvg);
                    i.putExtra("stresslv", setStresslv);
                    i.putExtra("bpmdata", beatsArray);
                    startActivity(i);
                }
            }
            processing.set(false);
        }
    };
    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("Preview-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h_r_detection);

        SurfaceView preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        image = findViewById(R.id.image);
        infoText = findViewById(R.id.textProsesHRM);
        text = findViewById(R.id.TextHRDetector);
        imgavgtxt = findViewById(R.id.img_avg_text);
        rollavgtxt = findViewById(R.id.rollavg_text);
        averagebpm = findViewById(R.id.avgBPM);
        stresslv = findViewById(R.id.stressLevel);
//        startbutt = findViewById(R.id.startbutt);
//
//        startbutt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");

        dbHRMonitor = new DatabaseHelper(getBaseContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);

        camera = Camera.open();

        startTime = System.currentTimeMillis();
    }

    private Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }

        return result;
    }

    public enum TYPE {
        GREEN, RED
    }
}
