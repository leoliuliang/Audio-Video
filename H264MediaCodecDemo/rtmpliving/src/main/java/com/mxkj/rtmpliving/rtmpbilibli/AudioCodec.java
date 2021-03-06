package com.mxkj.rtmpliving.rtmpbilibli;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import com.mxkj.rtmpliving.task.LiveTaskManager;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioCodec extends Thread {
    private MediaCodec mediaCodec;

    private AudioRecord audioRecord;
    private int minBufferSize;
    private boolean isRecoding;
    private long startTime;
    //传输层
    private   ScreenLive screenLive;

    public AudioCodec(ScreenLive screenLive){
        this.screenLive = screenLive;
    }

    public void startLive(){
        MediaFormat audioFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1);
        //设置录音质量
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        //设置一秒码率
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE,64_000);
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mediaCodec.configure(audioFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();

            AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
            //录音工具类
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,44100, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,minBufferSize);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //通过线程池执行编码耗时操作
        LiveTaskManager.getInstance().execute(this);
    }

    @Override
    public void run() {
        isRecoding = true;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//       音频推流不同于视频推流，音频在还没有开始编码时要求先发一个 空的 数据头，意思是 要准备发音频啦，接收端准备好
        RTMPPackage rtmpPackage = new RTMPPackage();
        byte[] audioDecoderSpecificInfo = {0x12, 0x08};
        rtmpPackage.setBuffer(audioDecoderSpecificInfo);
        rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_AUDIO_HEAD);
        screenLive.addPackage(rtmpPackage);

        //开始录音
        Log.i("AudioCodec", "开始录音  minBufferSize： "+minBufferSize);
        audioRecord.startRecording();
//        容器 , 装录音数据
        byte[] buffer = new byte[minBufferSize];
        while (isRecoding) {
//            麦克风的数据读取出来放到buffer , 此时是 pcm 数据
            int len = audioRecord.read(buffer, 0, buffer.length);
            //pcm 数据编码
            if (len < 0){
                continue;
            }
            //立即得到有效输入缓冲区 进行编码处理
            int index = mediaCodec.dequeueInputBuffer(0);
            if (index >= 0) {
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(index);
                inputBuffer.clear();
                inputBuffer.put(buffer, 0, len);
                //填充数据后再加入队列
                mediaCodec.queueInputBuffer(index, 0, len,
                        System.nanoTime() / 1000, 0);
            }

            index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            while (index >= 0 && isRecoding) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(index);
                byte[] outData = new byte[bufferInfo.size];
                //编码好的数据放到outData
                outputBuffer.get(outData);
                if (startTime == 0) {
                    startTime = bufferInfo.presentationTimeUs / 1000;
                }
                rtmpPackage = new RTMPPackage();
                rtmpPackage.setBuffer(outData);
                //设置类type为  音频数据
                rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_AUDIO_DATA);
                //设置时间戳
                long tms = (bufferInfo.presentationTimeUs / 1000) - startTime;
                rtmpPackage.setTms(tms);
                screenLive.addPackage(rtmpPackage);
                mediaCodec.releaseOutputBuffer(index, false);
                index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec = null;
        startTime = 0;
        isRecoding = false;

    }
}

