package com.example.simplescreencapture;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE = 1000;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    ToggleButton toggleButton;

    private int screenDensity;
    MediaRecorder mediaRecorder;
    MediaProjection mediaProjection;
    MediaProjectionManager mediaProjectionManager;
    MediaProjectionCallback mediaProjectionCallback;
    VirtualDisplay virtualDisplay;

    ServiceConnection serviceConnection;
    protected IMyAidlInterface iMyAidlInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenDensity = metrics.densityDpi;

        mediaRecorder = new MediaRecorder();
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggle();
            }
        });
        
        initConnection();

    }

    private void initConnection() {
        Log.v(TAG,"initConnection");
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
                Snackbar.make(findViewById(android.R.id.content), "Service Connected", Snackbar.LENGTH_LONG).show();
                Log.v(TAG,"Binding is done - service connected");
                /*iMyAidlInterface = IMyAidlInterface.Stub.asInterface((IBinder) service);
                Log.v(TAG,"SErvice Connected");
                Toast.makeText(MainActivity.this, "Service Connected", Toast.LENGTH_SHORT).show();
                try {
                    Log.v(TAG,"pid: "+iMyAidlInterface.getPid());
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.v(TAG,"e: "+e);
                    Log.v(TAG,"msg: "+e.getMessage());
                }*/
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                iMyAidlInterface = null;
                Snackbar.make(findViewById(android.R.id.content), "Service Disconnected", Snackbar.LENGTH_LONG).show();
                Log.v(TAG,"Service disconnected");
            }
        };
        if(iMyAidlInterface == null) {
            Log.v(TAG,"intent");
            Intent intent = new Intent();
            intent.setAction("service.FileLocation");
            intent.setPackage("com.example.simplescreencaptureservice");
            bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void onToggle() {
        if(toggleButton.isChecked()) {
            Snackbar.make(findViewById(android.R.id.content), "Recording Started", Snackbar.LENGTH_LONG).show();
            initRecorder();
            shareScreen();
        }
        else {
            Snackbar.make(findViewById(android.R.id.content), "Recording Stopped", Snackbar.LENGTH_LONG).show();
            //Toast.makeText(MainActivity.this, "not checked", Toast.LENGTH_SHORT).show();
            mediaRecorder.stop();
            mediaRecorder.reset();
            stopScreenSharing();
                try {
                    iMyAidlInterface.getLocation(location);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            toggleButton.setChecked(false);
            return;
        }
        mediaProjectionCallback = new MediaProjectionCallback();
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        mediaProjection.registerCallback(mediaProjectionCallback, null);
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();
    }

    private void shareScreen() {
        if(mediaProjection == null) {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    String location = Environment.getExternalStoragePublicDirectory(Environment
            .DIRECTORY_DOWNLOADS)+"/video.mp4";
    private void initRecorder() {
        try {
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            mediaRecorder.setOutputFile(location);
            mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mediaRecorder.setVideoFrameRate(30);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mediaRecorder.setOrientationHint(orientation);
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if(toggleButton.isChecked()) {
                toggleButton.setChecked(false);
                mediaRecorder.stop();
                mediaRecorder.reset();
                Log.v(TAG,"Recording Stopped");
            }
            mediaProjection = null;
            stopScreenSharing();
        }
    }

    private void stopScreenSharing() {
        if(virtualDisplay == null) {
            return;
        }
        virtualDisplay.release();
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {
        if(mediaProjection != null) {
            mediaProjection.unregisterCallback(mediaProjectionCallback);
            mediaProjection.stop();
            mediaProjection = null;
        }
        Log.v(TAG,"MediaProjection Stopped");
    }

}
