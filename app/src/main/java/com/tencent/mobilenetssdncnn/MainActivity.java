// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2020 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

package com.tencent.mobilenetssdncnn;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;


import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.security.acl.Permission;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CameraXActivity";
    private static final String FILENAME_FORMATE = "yyyy-MM-DD-HH-mm-ss";

    private static final int SELECT_IMAGE = 1;

    private ImageView imageView;

    private Bitmap detectImage = null;

    private ExecutorService cameraExecutor;
    private Preview preview;
    Camera camera;
    private PreviewView viewFinder;
    ProcessCameraProvider cameraProvider;
    ImageButton img_button;
    ImageCaptureConfig imageCaptureConfig;
    private ImageCapture imageCapture;
    ImageAnalysis myAnalyzer;
    public MyView myview;
    private static ImageView imageview;
    Permission permission;

    private MobilenetSSDNcnn mobilenetssdncnn = new MobilenetSSDNcnn();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        boolean ret_init = mobilenetssdncnn.Init(getAssets());
        if (!ret_init) {
            Log.e("MainActivity", "mobilenetssdncnn Init failed");
        }
        viewFinder = (PreviewView) findViewById(R.id.img);
        myview = (MyView) findViewById(R.id.frontlayer);
        //myview.bringToFront();
        imageview =(ImageView) findViewById(R.id.frontlayer1);
        imageview.bringToFront();

        img_button = (ImageButton) findViewById(R.id.circle_btn);
        cameraExecutor = Executors.newSingleThreadExecutor();

        //点击事件
        img_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("clicked");
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.i("TEST", "Granted");
                    //init(barcodeScannerView, getIntent(), null);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA}, 1);//1 can be another integer
                }

                startCamera();
            }
        });
    }

    private void startCamera() {
        //创建一个相机的管理器，相当于一个主管道
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    //1.图像预览接口
                    preview = new Preview.Builder().build();
                    //2.图像分析接口
                    myAnalyzer = new ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();
                    myAnalyzer.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {

                        public void analyze(@NonNull ImageProxy image) {

                            ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
                            Log.i("Test","stride0="+image.getPlanes()[0].getPixelStride());
                            ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
                            Log.i("Test","stride1="+image.getPlanes()[1].getPixelStride());
                            ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
                            Log.i("Test","stride2="+image.getPlanes()[2].getPixelStride());
                            int ySize = yBuffer.remaining();
                            int uSize = uBuffer.remaining();
                            int vSize = vBuffer.remaining();


                            byte[] nv21 = new byte[ySize+uSize+vSize];
                            yBuffer.get(nv21,0,ySize);
                            vBuffer.get(nv21,ySize,vSize);
                            uBuffer.get(nv21,ySize+vSize,uSize);
                            //开始时间
                            long Start = System.currentTimeMillis();

                            YuvImage yuvimage = new YuvImage(nv21, ImageFormat.NV21,myview.getWidth(),myview.getHeight(),null);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            yuvimage.compressToJpeg(new Rect(0, 0,yuvimage.getWidth(), yuvimage.getHeight()), 80, baos);//80--JPG图片的质量[0-100],100最高
                            byte[] jdata = baos.toByteArray();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
                            Log.i("TEST", "size="+jdata.length);
                            bitmap = rotateBitmap(bitmap,90);
                            //imageview.setImageBitmap(bitmap);
                            detectImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                            MobilenetSSDNcnn.Obj[] objects = mobilenetssdncnn.Detect(detectImage, true);
                            for (int i = 0; i < objects.length; i++) {
                                String text = objects[i].label + " = " + String.format("%.1f", objects[i].prob * 100) + "%";
                                myview.drawText(text, objects[i].x, objects[i].y, objects[i].h, objects[i].w, myview.getWidth(), myview.getHeight());
                            }
                            // 按你的需要处理图片吧
                            //MainActivity.this.viewFinder.setBackground(new BitmapDrawable(bitmap));
                            image.close();
                        }
                    });
                    //3.拍照接口
                    imageCapture = new ImageCapture.Builder().build();
                    //4.把需要的这三种接口安装到相机管理器的主线路上，实现截取数据的目的
                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                    cameraProvider = cameraProviderFuture.get();
                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, preview, imageCapture, myAnalyzer);

                    preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.getCameraInfo()));


                } catch (ExecutionException e) {
                    Log.e(TAG, "run:binding lifecyle failed");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

    private View.OnClickListener applyPermissionAndPhoto() {

        return null;
    }
    private Bitmap rotateBitmap(Bitmap origin,float alpha){
        if(origin==null) return null;
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        Bitmap newBM = Bitmap.createBitmap(origin,0,0,width,height,matrix,false);
        if(newBM.equals(origin)){
            return newBM;
        }
        origin.recycle();
        return newBM;

    }
}


