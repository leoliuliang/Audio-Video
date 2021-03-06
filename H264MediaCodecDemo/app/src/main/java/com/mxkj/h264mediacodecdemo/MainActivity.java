package com.mxkj.h264mediacodecdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mxkj.h264mediacodecdemo.audio.ClipAudioActivity;
import com.mxkj.h264mediacodecdemo.camera.Camera2Activity;
import com.mxkj.h264mediacodecdemo.camera.CameraActivity;
import com.mxkj.h264mediacodecdemo.h264parse.H264Parse;
import com.mxkj.h264mediacodecdemo.mediacodec.H264EncoderActivity;
import com.mxkj.h264mediacodecdemo.player.DecodeH264Activity;
import com.mxkj.h264mediacodecdemo.touping.PushActivity;
import com.mxkj.h264mediacodecdemo.utils.AssetsUtil;
import com.mxkj.h264mediacodecdemo.utils.Constant;
import com.mxkj.h264mediacodecdemo.utils.PermissionUtil;
import com.mxkj.h264mediacodecdemo.video.VideoProcess;
import com.mxkj.h264mediacodecdemo.videochat.A.AChatActivity;
import com.mxkj.h264mediacodecdemo.videochat.B.BChatActivity;
import com.mxkj.rtmpliving.rtmpbilibli.LivingActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionUtil.checkPermission(this);
        new AssetsUtil(this).assetsToSdcard("out.h264");
        new AssetsUtil(this).assetsToSdcard("input.mp4");
        new AssetsUtil(this).assetsToSdcard("input2.mp4");
        new AssetsUtil(this).assetsToSdcard("music.mp3");
    }


    public native void nativeTojava();
    //动态注册
    public native String dynamicRegister1();
//    public native void nativeOnload2();

    public void callBack(int count){
        Toast.makeText(this,"native 传值： "+count,Toast.LENGTH_SHORT).show();
    }

    public void nativeTojava(View view) {
        nativeTojava();
    }

    public void dynamicRegister(View view) {
        String s = dynamicRegister1();
        Button button = findViewById(R.id.dynamicBtn);
        button.setText(s);
    }


    public void H264Encoder(View view) {
        startActivity(new Intent(this, H264EncoderActivity.class));
    }

    public void h264Player(View view) {
        startActivity(new Intent(this, DecodeH264Activity.class));
    }

    public void spsToWidthHeigh(View view) {
        H264Parse mediaCodec = new H264Parse(new File(Constant.filePath2,
                "out.h264").getAbsolutePath());
        String s = mediaCodec.startCodec();
        Toast.makeText(this,"宽高："+s,Toast.LENGTH_SHORT).show();
    }

    public void cameraCapture(View view) {
        startActivity(new Intent(this, CameraActivity.class));
    }

    public void videoChatA(View view) {
        startActivity(new Intent(this, AChatActivity.class));
    }

    public void videoChatB(View view) {
        startActivity(new Intent(this, BChatActivity.class));
    }

    public void touping(View view) {
        startActivity(new Intent(this, PushActivity.class));
    }

    public void audioClip(View view) {
        startActivity(new Intent(this, ClipAudioActivity.class));
    }

    public void videoMix(View view) {
        new VideoProcess(this);
    }

    public void camera2(View view) {
        startActivity(new Intent(this, Camera2Activity.class));
    }

    public void rtmpBilibli(View view) {
        startActivity(new Intent(this, LivingActivity.class));
    }
}