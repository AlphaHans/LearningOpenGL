package com.example.hans.glsurfacetest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView mGlView;
    private Bitmap mBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGlView = findViewById(R.id.main_gl_view);
        mGlView.setEGLContextClientVersion(2);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.bitmap1, options);

        mGlView.setRenderer(new BitmapRender(bitmap));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGlView.onResume();
    }
}
