package com.example.opengles.filter;

import android.content.Context;
import android.opengl.GLES20;

public class AbstractFboFilter extends  AbstractFilter{

    public int[] frameBuffer;
    public int[] frameTextures;

    public AbstractFboFilter(Context context, int vertexShaderId, int fragmentShaderId) {
        super(context, vertexShaderId, fragmentShaderId);
    }

    //初始化  fbo, 让摄像头的数据 先渲染到 fbo
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        releaseFrame();
        frameBuffer = new int[1];//int  gpu

        //实例化 fbo , fbo市gpu里的数据存放区
        GLES20.glGenFramebuffers(1, frameBuffer, 0);

        //生成纹理, 纹理可以简单理解为图层
        frameTextures = new int[1];
        GLES20.glGenTextures(frameTextures.length, frameTextures, 0);

        //配置纹理
        for (int i = 0; i < frameTextures.length; i++) {
            //后续的操作，是一个原子性的
            //绑定纹理，后续配置纹理 ，开始操作纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextures[i]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_NEAREST);//放大过滤，有锯齿
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);//缩小过滤，无锯齿模糊处理
            //重新设为0，告诉gpu， 纹理操作完了，
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        //开始做绑定操作, 将纹理和fbo数据进行绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextures[0]);
        /**
         * 指定一个二维的纹理图片
         * level
         *     指定细节级别，0级表示基本图像，n级则表示Mipmap缩小n级之后的图像（缩小2^n）
         * internalformat
         *     指定纹理内部格式，必须是下列符号常量之一：GL_ALPHA，GL_LUMINANCE，GL_LUMINANCE_ALPHA，GL_RGB，GL_RGBA。
         * width height
         *     指定纹理图像的宽高，所有实现都支持宽高至少为64 纹素的2D纹理图像和宽高至少为16 纹素的立方体贴图纹理图像 。
         * border
         *     指定边框的宽度。必须为0。
         * format
         *     指定纹理数据的格式。必须匹配internalformat。下面的符号值被接受：GL_ALPHA，GL_RGB，GL_RGBA，GL_LUMINANCE，和GL_LUMINANCE_ALPHA。
         * type
         *     指定纹理数据的数据类型。下面的符号值被接受：GL_UNSIGNED_BYTE，GL_UNSIGNED_SHORT_5_6_5，GL_UNSIGNED_SHORT_4_4_4_4，和GL_UNSIGNED_SHORT_5_5_5_1。
         * data
         *     指定一个指向内存中图像数据的指针。
         */
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                null);

        //要开始使用 gpu的 fbo 数据区域
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);  //綁定FBO

        //真正发生绑定 fbo 和 纹理  (图层)
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                frameTextures[0],
                0);

        //重新设为0，告诉gpu， 纹理操作完了
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        //和glBindTexture一样，glBindFramebuffer也要成双成对的出现，操作完后重新设为0释放
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }


    private void releaseFrame() {
        if (frameTextures != null) {
            GLES20.glDeleteTextures(1, frameTextures, 0);
            frameTextures = null;
        }

        if (frameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
        }
    }
    @Override
    public int onDraw(int texture) {
        //数据渲染到 fbo中
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
        super.onDraw(texture);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);  //
        return frameTextures[0] ;
    }

}
