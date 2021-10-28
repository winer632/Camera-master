package com.example.camerserver;

import java.io.File;
import java.io.IOException;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

/**
 * @author: WangXinXin
 * @description:
 * @date: 2021-10-22 19:56
 *
 * 分辨率	帧率	码率期望值
 * 640 × 368	15fps	800kbps
 * 960 × 544	15fps	1000kbps
 * 1280 × 720	15fps	1500kbps
 * 1920 × 1080	15fps	2500kbps
 *
 **/

public class RecordVideoThread extends Thread {

    public String streamURL;
    public String filePath;
    public Integer id;

    public void setStreamURL(String streamURL) {
        this.streamURL = streamURL;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    @Override
    public void run() {
        System.out.println(streamURL);
        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(streamURL);
        FFmpegFrameRecorder recorder = null;
        try {
            grabber.start();
            Frame frame = grabber.grabFrame();
            if (frame != null) {
                File outFile = new File(filePath);
                if (!outFile.isFile()) {
                    try {
                        outFile.createNewFile();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
                recorder = new FFmpegFrameRecorder(filePath, 1280, 720, 1);

                // 确定视频格式
                recorder.setFormat("flv");
                // 确定编码格式
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setVideoOption("crf", "30");
                recorder.setMaxBFrames(3);
                recorder.setGopSize(30);
                recorder.setVideoQuality(1);
                recorder.setFrameRate(25);
                recorder.setVideoBitrate(50*1000*1000);

                // 音频设置
                recorder.setAudioOption("crf", "0");
                // 设置音频质量
                recorder.setAudioQuality(0);
                // 设置音频比特率
                recorder.setAudioBitrate(192000);
                // 设置采样频率
                recorder.setSampleRate(44100);
                // 设置声道数
                recorder.setAudioChannels(1);
                //设置音频编码格式
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

                recorder.start();
                while ((frame != null)) {
                    // 录制
                    recorder.record(frame);
                    // 获取下一帧
                    frame = grabber.grabFrame();
                }
                recorder.record(frame);
                // 停止录制
                recorder.stop();
                grabber.stop();
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        } finally {
            if (null != grabber) {
                try {
                    grabber.stop();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            }
            if (recorder != null) {
                try {
                    recorder.stop();
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) {
        RecordVideoThread thread = new RecordVideoThread();
//        thread.setFilePath("/Users/wangxinxin/Downloads/test.flv");
        thread.setFilePath("rtmp://10.198.20.19:1935/live/livestream");
        thread.setStreamURL("rtsp://sxtest:Goodsense@10.8.10.57:80");
        thread.start();
    }
}
