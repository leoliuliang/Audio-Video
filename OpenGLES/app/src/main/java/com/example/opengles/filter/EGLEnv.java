package com.example.opengles.filter;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

/**
 * EGL环境，编码时，获取数据 调用opengl的函数需要运行在gl线程里，所以要创建这么一个环境
 * */
public class EGLEnv {
    private EGLDisplay mEglDisplay;
    //创建好这个上下文，可以调用 oepngl 的函数
    private EGLContext mEglContext;
    private final EGLConfig mEglConfig;
    private final EGLSurface mEglSurface;

    private RecordFilter recordFilter;

    //surface是 mediacodec  提供的场地
    public EGLEnv(Context context, EGLContext mGlContext, Surface surface, int width, int height) {
        // 获得显示窗口，作为OpenGL的绘制目标
        mEglDisplay= EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }
        // 初始化展示窗口
        int[] version = new int[2];
        if(!EGL14.eglInitialize(mEglDisplay, version,0,version,1)) {
            throw new RuntimeException("eglInitialize failed");
        }


        // 配置 属性选项
        int[] configAttribs = {
                EGL14.EGL_RED_SIZE, 8, //颜色缓冲区中红色位数
                EGL14.EGL_GREEN_SIZE, 8,//颜色缓冲区中绿色位数
                EGL14.EGL_BLUE_SIZE, 8, //
                EGL14.EGL_ALPHA_SIZE, 8,//
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, //opengl es 2.0
                EGL14.EGL_NONE// 属性配置都以EGL_NONE标识
        };
        int[] numConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        //EGL 根据属性选择一个配置
        if (!EGL14.eglChooseConfig(mEglDisplay, configAttribs, 0, configs, 0, configs.length,
                numConfigs, 0)) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
        mEglConfig = configs[0];
        int[] context_attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION,2,
                EGL14.EGL_NONE
        };
        //创建好了 之后   你是可以读取到数据
        mEglContext= EGL14.eglCreateContext(mEglDisplay, mEglConfig, mGlContext, context_attrib_list,0);


        if (mEglContext == EGL14.EGL_NO_CONTEXT){
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
        /**
         * 创建EGLSurface
         */
        int[] surface_attrib_list = {
                EGL14.EGL_NONE
        };

        //绑定物理设备surface
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, surface_attrib_list, 0);
        if (mEglSurface == null){
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
        /**
         * 绑定当前线程的显示器display mEglDisplay
         */
        if (!EGL14.eglMakeCurrent(mEglDisplay,mEglSurface,mEglSurface,mEglContext)){
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
        recordFilter = new RecordFilter(context);
        recordFilter.setSize(width,height);

    }

    /**
     * 调用这个方法，fbo的数据就会渲染到surface里
     * */
    public void draw(int textureId, long timestamp) {
        recordFilter.onDraw(textureId);
        //给帧缓冲   时间戳
        EGLExt.eglPresentationTimeANDROID(mEglDisplay,mEglSurface,timestamp);
        //EGLSurface是双缓冲模式
        EGL14.eglSwapBuffers(mEglDisplay,mEglSurface);
    }

    public void release(){
        EGL14.eglDestroySurface(mEglDisplay,mEglSurface);
        EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(mEglDisplay, mEglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEglDisplay);
        recordFilter.release();
    }
}
