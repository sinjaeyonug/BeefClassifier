package com.example.beefclassifier;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.graphics.Bitmap.Config;
import android.media.Image.Plane;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.ImageReaderProxy;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.core.internal.ThreadConfig;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LifecycleOwner;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.beefclassifier.tflite.Classifier;
import com.example.beefclassifier.tflite.Classifier.Device;
import com.example.beefclassifier.tflite.Classifier.Recognition;
import com.example.beefclassifier.tflite.Logger;
import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.ListenableFuture;



import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import android.media.ImageReader.OnImageAvailableListener;

import static com.example.beefclassifier.ImageUtils.imageProxyToBitmap;

public  class MainActivity extends AppCompatActivity {
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    PreviewView mPreviewView;
    ImageView captureImage;
    private long currentTimestamp;
    private long lastAnalyzedTimestamp;
    private Image lastimg;
    private ImageProxy lastimgproxy;
    private Bitmap BitmapBuffer;
    private int[] rgbbytes;
    private Image.Plane[] planes;
    protected int previewWidth=0,previewHeight=0;
    protected Bitmap rgbBitmap;
    private Classifier classifier;
    private static final Logger LOGGER = new Logger();
    private int imageSizeX,imageSizeY;
    private Device device = Device.CPU;
    private int numThreads = -1;
    private Bitmap resizeimg;
    private byte[][] yuvBytes = new byte[3][];
    private int yRowStride;
    private byte[] bytes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            classifier = Classifier.create(this, device, numThreads);
        } catch (IOException e) {
            e.printStackTrace();
        }
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.draw_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lastAnalyzedTimestamp = 0L;
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        mPreviewView = findViewById(R.id.camera);
        captureImage = findViewById(R.id.captureImg);
        captureImage.bringToFront();


        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }
    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
    { case android.R.id.home :
        {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        default:
            return super.onOptionsItemSelected(item);
    }
    }


    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);


                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));

    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(800,600))
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {

            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                // insert your code here.


                currentTimestamp = System.currentTimeMillis();
                resizeimg= Bitmap.createScaledBitmap(imageProxyToBitmap(image),224,224,true);
                    //lastimg=image.getImage();
                   // lastimgproxy=image;
                   // previewWidth= lastimg.getWidth();
                   // previewHeight=lastimg.getHeight();
                   // bytes=ImageUtils.bitmapToByteArray(ImageUtils.imageProxyToBitmap(image));

                image.close();

            }
        });

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());


        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageAnalysis, imageCapture);


        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                planes = lastimg.getPlanes();
//                ByteBuffer buffer = planes[0].getBuffer();
//
//                byte[] bytes = new byte[buffer.capacity()];
//                buffer.get(bytes);
                if(classifier!=null){


               // final Plane[] planes=;
               // yuvBytes[0] = bytes;
               /// yRowStride = previewWidth;
               // fillBytes(planes,yuvBytes);
               // yRowStride = planes[0].getRowStride();
               // final int uvRowStride=planes[1].getRowStride();
              //  final int uvPixelStride = planes[1].getPixelStride();
//

               // rgbBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
                 //   Log.d("dfas","****"+"*******"+bytes.length);

                //Log.d("dfas","****"+previewHeight+"*******"+previewWidth);
                //Log.d("dfasdfdsfsdf","asdfdsfdsfsdfsd"+classifier.getImageSizeX()+","+classifier.getImageSizeY());
                //rgbBitmap.setPixels(rgbbytes,0,previewWidth,0,0,previewWidth,previewHeight);
                  //  Log.d("dfas","****"+"*******rgbbitmap"+rgbBitmap.getByteCount());
                final List<Classifier.Recognition> results =
                        classifier.recognizeImage(resizeimg, 90-getScreenOrientation());
                Recognition recognition =  results.get(0);
                LOGGER.v("Detect: %s", results);
                Toast.makeText(MainActivity.this,recognition.getTitle()+","+recognition.getConfidence(),Toast.LENGTH_LONG).show();
                }
                //SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                //File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date())+ ".jpg");

                //ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                //imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback () {
                  //  @Override
                   // public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                  //      new Handler().post(new Runnable() {
                    //        @Override
                    //        public void run() {
                    //            Toast.makeText(MainActivity.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
                    //        }
                    //    });
                  //  }
                 //   @Override
                  //  public void onError(@NonNull ImageCaptureException error) {
                   //     error.printStackTrace();
                  //  }
               // });
            }
        });
    }

    public String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {

        }

        return app_folder_path;
    }

    private boolean allPermissionsGranted(){
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

//    private void recreateClassifier(Device device, int numThreads) {
//        if (classifier != null) {
//            LOGGER.d("Closing classifier.");
//            classifier.close();
//            classifier = null;
//        }
//        try {
//            LOGGER.d(
//                    "Creating classifier (device=%s, numThreads=%d)", device, numThreads);
//            classifier = Classifier.create(this, device, numThreads);
//        } catch (IOException e) {
//            LOGGER.e(e, "Failed to create classifier.");
//        }
//
//        // Updates the input image size.
//        imageSizeX = classifier.getImageSizeX();
//        imageSizeY = classifier.getImageSizeY();
//    }
protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    Log.d("뭐냐고이건","!!!!"+planes.length);
    for (int i = 0; i < planes.length; ++i) {
        final ByteBuffer buffer = planes[i].getBuffer();
        Log.d("1","!@232");
        if (yuvBytes[i] == null) {
            LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
            yuvBytes[i] = new byte[buffer.capacity()];
        }
        buffer.get(yuvBytes[i]);
    }
}

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

}

