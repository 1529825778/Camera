package com.example.mycamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.ImageCaptureConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.security.Permission;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG ="CameraXActivity";
    private static final String FILENAME_FORMATE = "yyyy-MM-DD-HH-mm-ss";
    private ExecutorService cameraExecutor;
    private Preview preview;
    Camera camera;
    private PreviewView viewFinder;
    ProcessCameraProvider cameraProvider;
    ImageButton img_button;
    ImageCaptureConfig imageCaptureConfig;
    private ImageCapture imageCapture;
    ImageAnalysis myAnalyzer;
    ImageView imageView;
    Permission permission;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewFinder = (PreviewView) findViewById(R.id.img);
        img_button =(ImageButton)findViewById(R.id.circle_btn);


        cameraExecutor= Executors.newSingleThreadExecutor();
        //点击事件
        img_button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                System.out.println("clicked");
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.i("TEST","Granted");
                    //init(barcodeScannerView, getIntent(), null);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA}, 1);//1 can be another integer
                }

                startCamera();
            }
        });
        //Glide.with(this).load();

    }

    private void startCamera(){
        //创建一个相机的管理器，相当于一个主管道
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture=ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    //1.图像预览接口
                    preview = new Preview.Builder().build();
                    //2.图像分析接口
                    myAnalyzer = new ImageAnalysis.Builder().build();
                    myAnalyzer.setAnalyzer(cameraExecutor,new LuminosityAnalyzer());
                    //3.拍照接口
                    imageCapture = new ImageCapture.Builder().build();
                    //4.把需要的这三种接口安装到相机管理器的主线路上，实现截取数据的目的
                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                    cameraProvider =cameraProviderFuture.get();
                    cameraProvider.unbindAll();
                    camera =cameraProvider.bindToLifecycle(MainActivity.this,cameraSelector,preview,imageCapture,myAnalyzer);
                    preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.getCameraInfo()));

                }catch (ExecutionException e){
                    Log.e(TAG,"run:binding lifecyle failed");
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }
    public CameraXConfig getCameraXConfig(){
        return Camera2Config.defaultConfig();
    }
    private View.OnClickListener applyPermissionAndPhoto(){

        return null;
    }
}
class LuminosityAnalyzer implements ImageAnalysis.Analyzer{
    public void analyze(@NonNull ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] b=new byte[buffer.remaining()];
        buffer.get(b);
        // 按你的需要处理图片吧

        image.close();
    }
}