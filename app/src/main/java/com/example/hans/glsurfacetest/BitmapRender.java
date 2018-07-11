package com.example.hans.glsurfacetest;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BitmapRender implements GLSurfaceView.Renderer {
    private static final String TAG = "BitmapRender";
    private Bitmap mBitmap;

    /************************************************/
    /**
     * 此处需要注意纹理坐标的方式与顶点坐标的方式，纹理坐标要与顶点坐标进行对应放置。
     * <p>
     * 可参考笔记纹理坐标与顶点坐标
     */


    /**
     * |
     * <p>
     * <p>
     * --------------------
     */
    //顶点坐标
    private static final float[] VERTEX = {   // in counterclockwise order:
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,


            1.0f, 1.0f,
            -1f, -1f,
            1f, -1f
    };


    // 纹理坐标
    private static final float[] TEXTURE = {
            0.0f, 0.0f, //纹理坐标的左上角
            0.0f, 1.0f, //纹理坐标的左下角
            1.0f, 0.0f, //纹理坐标的右上角

            1f, 0f,
            0f, 1f,
            1f, 1f

    };


    /************************************************/

    private final FloatBuffer mVertexCoordBuffer;
    //    private final ShortBuffer mVertexIndexBuffer;
    private final FloatBuffer mTextureCoordBuffer;


    private int mProgram;
    private int mPositionHandle;

    private int mTextureUnitHandle;  // 纹理句柄
    private int mTextureCoodrHandle; // 纹理坐标系句柄


    private int mTextureId;

    //顶点着色器 每个顶点都会执行一次顶点着色器，main中必须包含gl_Position变量，用来确定顶点的最终位置
    private static final String VERTEX_SHADER =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "attribute vec4 a_Position;" +
                    "attribute vec2 a_TextureCoordinates;" +
                    "varying vec2 v_TextureCoordinates;" + // 新增一个vec2变量 表示纹理坐标，这个变量传递给片元着色器，
                    "void main() {" +
                    "  v_TextureCoordinates = a_TextureCoordinates;" +
                    "  gl_Position = a_Position;" +
                    "}";


    //碎片着色器 由顶点着色器确定顶点位置，顶点所围成的区域，经过光栅化，形成了Fragment
    // main中必须包含 gl_FragColor变量 确定fragment颜色的
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n"
                    + "uniform sampler2D u_TextureUnit;"
                    + "varying vec2 v_TextureCoordinates;" //接受从顶点着色器获取的纹理坐标
                    + "void main() {\n"
                    + " gl_FragColor = texture2D(u_TextureUnit,v_TextureCoordinates);\n" //GLSL的内置函数，用于2D纹理取样，根据纹理取样器和纹理坐标，可以得到当前像素颜色
                    + "}";

    public BitmapRender(Bitmap bitmap) {
        mBitmap = bitmap;
        mVertexCoordBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mVertexCoordBuffer.position(0);

        mTextureCoordBuffer = ByteBuffer.allocateDirect(TEXTURE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEXTURE);
        mTextureCoordBuffer.position(0);


    }

    static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        mTextureId = TextureHelper.loadTexture(mBitmap);
        Log.d(TAG, "onSurfaceCreated: textureId = " + mTextureId);
        mProgram = GLES20.glCreateProgram();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        mTextureCoodrHandle = GLES20.glGetAttribLocation(mProgram, "a_TextureCoordinates");
        mTextureUnitHandle = GLES20.glGetUniformLocation(mProgram, "u_TextureUnit");
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);


        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, // size = 2 表示一个顶点用两个属性来描述
                GLES20.GL_FLOAT, false,
                0, mVertexCoordBuffer);// 这里stride计算的值是 2 * 4 = 8，但是在顶点属性排列紧密的时候，使用0让opengl去计算

        GLES20.glEnableVertexAttribArray(mTextureCoodrHandle);
        GLES20.glVertexAttribPointer(mTextureCoodrHandle, 2, GLES20.GL_FLOAT, false,
                0, mTextureCoordBuffer);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glUniform1i(mTextureUnitHandle, 0);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

//        GLES20.glDrawElements(GLES20.GL_TRIANGLES,6,GLES);

    }
}
