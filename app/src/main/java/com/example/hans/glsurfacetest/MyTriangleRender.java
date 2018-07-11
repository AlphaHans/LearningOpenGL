package com.example.hans.glsurfacetest;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyTriangleRender implements GLSurfaceView.Renderer {

    //顶点着色器 每个顶点都会执行一次顶点着色器，main中必须包含gl_Position变量，用来确定顶点的最终位置
    private static final String VERTEX_SHADER =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";
    //碎片着色器 由顶点着色器确定顶点位置，顶点所围成的区域，经过光栅化，形成了Fragment
    // main中必须包含 gl_FragColor变量 确定fragment颜色的
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n"
                    + "void main() {\n"
                    + " gl_FragColor = vec4(0.5, 0, 0, 1);\n"
                    + "}";


    private static final float[] VERTEX = {   // in counterclockwise order:
            0, 0.5f, 0,  // top
            -0.5f, -0.5f, 0,  // bottom left
            0.5f, -0.5f, 0,  // bottom right
    };

    private final FloatBuffer mVertexBuffer;

    private int mProgram;
    private int mPositionHandle;
    private int mMatrixHandle;


    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    public MyTriangleRender() {
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mVertexBuffer.position(0);
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

        mProgram = GLES20.glCreateProgram();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                12, mVertexBuffer);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
//        Matrix.frustumM (float[] m,         //接收透视投影的变换矩阵
//        int mOffset,        //变换矩阵的起始位置（偏移量）
//        float left,         //相对观察点近面的左边距
//        float right,        //相对观察点近面的右边距
//        float bottom,       //相对观察点近面的下边距
//        float top,          //相对观察点近面的上边距
//        float near,         //相对观察点近面距离
//        float far)          //相对观察点远面距离
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //        Matrix.setLookAtM (float[] rm,      //接收相机变换矩阵
//        int rmOffset,       //变换矩阵的起始位置（偏移量）
//        float eyeX,float eyeY, float eyeZ,   //相机位置
//        float centerX,float centerY,float centerZ,  //观测点位置
//        float upX,float upY,float upZ)  //up向量在xyz上的分量
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

    }
}
