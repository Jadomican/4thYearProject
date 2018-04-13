package jadomican.a4thyearproject;

/**
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 *
 * The activity representing the actual camera capture screen. This is called when
 * the camera is to be used. Adapted from Google Mobile Vision tutorials available at:
 * https://codelabs.developers.google.com/codelabs/mobile-vision-ocr/
 */

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;

import jadomican.a4thyearproject.camera.CameraSource;
import jadomican.a4thyearproject.camera.CameraSourcePreview;
import jadomican.a4thyearproject.camera.GraphicOverlay;

import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.Locale;

public final class OcrCaptureActivity extends AppCompatActivity {

    private static final String TAG = "OcrCaptureActivity";

    // Request code to handle updating play services
    private static final int RC_HANDLE_GMS = 9001;

    // Camera permission request code (must be less than 256)
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass data to intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

//    NavigationView navigationView;
//    private DrawerLayout mDrawerLayout;


    // Gesture Detectors interpret screen taps, swipes, etc.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_capture);

//        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setCheckedItem(R.id.nav_camera);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        //MediApp.setNavigationListener(navigationView, mDrawerLayout, R.id.nav_camera, this);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);

        //Defaults for camera viewfinder options
        boolean autoFocus = true;
        boolean useFlash = false;

        //Check for (or request) camera permissions on Android device
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        Snackbar.make(mGraphicOverlay, "Tap to search. Pinch to zoom",
                Snackbar.LENGTH_LONG)
                .show();
    }

    //Method to handle requesting the camera permissions
    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    //Handle the user touching the graphics on screen
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);
        boolean c = gestureDetector.onTouchEvent(e);
        return b || c || super.onTouchEvent(e);
    }

    //Create and start the camera
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        //A text recogniser is used to find text
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay));

        // isOperational() method checks that the Mobile Vision native libraries have
        // been fully downloaded by the app
        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Camera recognition dependencies not yet available");

            // Also check for low device storage before downloading dependencies
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                        .build();
    }

    // Restarts the camera
    @Override
    protected void onResume() {
        super.onResume();
        //navigationView.setCheckedItem(R.id.nav_camera);
        startCameraSource();
    }

    // Stops the camera
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    // Release resources associated with the camera source, associated detectors
    // and the processing pipeline
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //mDrawerLayout.openDrawer(GravityCompat.START);
                onBackPressed();
                //moveTaskToBack(true);
                return true;
            case R.id.action_search:
                onSearchRequested();
                return true;
            case R.id.action_help:
                MediApp.displayDialog(this,
                        getString(R.string.bar_camera_help_title),
                        getString(R.string.bar_camera_help_message));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Open the navigation bar when pressing the back button
//    @Override
//    public void onBackPressed() {
//        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
//            mDrawerLayout.closeDrawers();
//        } else {
//            mDrawerLayout.openDrawer(GravityCompat.START);
//        }
//    }

    // Add the additional action bar items based on the xml defined menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    // Callback method called after camera permission request
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                          @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        // If permissions have been successfully granted
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Text tracker")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }


    // Start or restart the camera source if it already exists. In the cas where the source
    // does not already exist, this method will get called when that source IS created later.
    private void startCameraSource() throws SecurityException {
        // Check that Google play services are available
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());

        Log.d("CONNECTION RESULT", Integer.toString(code));
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            Log.d("activity", "startCamSource(): mCameraSource IS null");
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /** Handle tapping on text at X and Y co-ordinates; search for medicines
     */
    private boolean onTap(float rawX, float rawY) {
        OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            // Extract text values from the blocked touched and search for medicines
            if (text != null && text.getValue() != null) {
                Context context = getApplicationContext();
                Intent intent = new Intent(context, MedicineListActivity.class);
                intent.putExtra(MedicineListActivity.KEY_QUERY, text.getValue().toLowerCase());
                context.startActivity(intent);
            }
            else {
                Log.d(TAG, "text data was null");
            }
        }
        else {
            Log.d(TAG, "no text detected");
        }
        return text != null;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (mCameraSource != null) {
                mCameraSource.doZoom(detector.getScaleFactor());
            }
        }
    }
}
