package com.example.camerserver;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacpp.avformat.AVFormatContext;
import org.bytedeco.javacv.FrameRecorder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: WangXinXin
 * @description: ConvertVideoPacket
 * @date: 2021-10-21 22:06
 **/

@Slf4j
public class ConvertVideoPacket {


    private static final Map<String, ConvertVideoPacket> convertVideoPackets = new HashMap<>();

    private FFmpegFrameGrabber grabber = null;
    private FFmpegFrameRecorder record = null;
    private int width = 1280, height = 720;

    // 视频参数
    private int audiocodecid;
    private int codecid;
    // 帧率
    private double framerate;
    // 比特率
    private int bitrate;

    // 音频参数
    private int audioChannels;
    private int audioBitrate;
    private int sampleRate;

    //控制程序循环
    private Boolean flag = true;


    private static ConvertVideoPacket get(String deviceId){
        return convertVideoPackets.get(deviceId);
    }

    public static Boolean start(String deviceId,String formUrl,String toUrl){

        if(null != get(deviceId)) {
            return true;
        }

        final ConvertVideoPacket convertVideoPacket = new ConvertVideoPacket();
        convertVideoPackets.put(deviceId, convertVideoPacket);

        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("start device");
                try {
                    convertVideoPacket.rtsp(formUrl).rtmp(toUrl).start();
                } catch (IOException e) {
                    log.error("start dvice error,{}",e);
                } catch (Exception e) {
                    log.error("start dvice error,{}",e);
                }
            }
        }).start();

        log.info("start device finish!");
        return true;
    }

    /**
     * 停止当前直播
     * @param id
     * @return
     */
    public static Boolean stop(String id){
        log.info("stop device ,{}",id);
        ConvertVideoPacket convertVideoPacket = get(id);

        if(null != convertVideoPacket){
            convertVideoPackets.remove(id);
            return convertVideoPacket.stop();
        }

        return false;
    }

    /**
     * 拉取摄像头视频源
     *
     * @param src rtsp数据源地址
     * @author JW
     * @throws Exception
     */
    public ConvertVideoPacket rtsp(String src) throws Exception {
        // 采集/抓取器
//        InputStream inputStream = new FileInputStream(new File("E:\\QLDownload\\tou\\tou.flv"));
        grabber = new FFmpegFrameGrabber(src);
        if (src.indexOf("rtsp") >= 0) {
            grabber.setOption("rtsp_transport", "tcp");
        }
        grabber.start();// 开始之后ffmpeg会采集视频信息，之后就可以获取音视频信息
        if (width < 0 || height < 0) {
            width = grabber.getImageWidth();
            height = grabber.getImageHeight();
        }
        // 视频参数
        audiocodecid = grabber.getAudioCodec();

        codecid = grabber.getVideoCodec();
        // 帧率
        framerate = grabber.getVideoFrameRate();
        // 比特率
        bitrate = grabber.getVideoBitrate();
        // 音频参数
        // 想要录制音频，这三个参数必须有：audioChannels > 0 && audioBitrate > 0 && sampleRate > 0
        audioChannels = grabber.getAudioChannels();
        audioBitrate = grabber.getAudioBitrate();
        sampleRate = grabber.getSampleRate();
        System.out.println("音频编码："+audiocodecid);
        System.out.println("视频编码："+codecid);
        System.out.println("帧率："+framerate);
        System.out.println("比特率："+bitrate);
        System.out.println("音频通道："+audioChannels);
        System.out.println("音频比特率："+audioBitrate);
        System.out.println("采样率："+sampleRate);
        if (audioBitrate < 1) {
            // 默认音频比特率
            audioBitrate = 128 * 1000;
        }
        return this;
    }

    /**
     * rtmp输出推流到nginx媒体流服务器
     *
     * @param out t\ rtmp媒体流服务器地址
     * @author JW
     * @throws IOException
     *
     * 分辨率	    帧率	    码率期望值
     * 640 × 368	15fps	800kbps
     * 960 × 544	15fps	1000kbps
     * 1280 × 720	15fps	1500kbps
     * 1920 × 1080	15fps	2500kbps
     *
     */
    public ConvertVideoPacket rtmp(String out) throws IOException {
        // 录制/推流器
        record = new FFmpegFrameRecorder(out, width, height);
        // 确定视频格式
        record.setFormat("flv");
        // 确定编码格式
        record.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        record.setVideoOption("crf", "30");
        record.setMaxBFrames(3);
        record.setGopSize(100);
        record.setVideoQuality(1);
        record.setFrameRate(25);
        record.setVideoBitrate(50*1000*1000);

        // 音频设置
        record.setAudioOption("crf", "0");
        // 设置音频质量
        record.setAudioQuality(0);
        // 设置音频比特率
        record.setAudioBitrate(192000);
        // 设置采样频率
        record.setSampleRate(44100);
        // 设置声道数
        record.setAudioChannels(2);
        //设置音频编码格式
        record.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

        AVFormatContext fc = null;

        if (out.indexOf("rtmp") >= 0 || out.indexOf("flv") > 0) {
            // 封装格式flv
            record.setFormat("flv");
            record.setAudioCodecName("aac");
            record.setVideoCodec(codecid);
            fc = grabber.getFormatContext();
        }

        record.start(fc);

        return this;
    }

    /**
     * 转封装
     *
     * @author eguid
     * @throws IOException
     */
    public void start() throws IOException {

        //刷新开始的测试数据
        if(null != grabber){
            grabber.flush();
        }

        while (flag) {
            avcodec.AVPacket pkt = null;
            try {
                // 没有解码的音视频帧
                pkt = grabber.grabPacket();
                if (pkt == null || pkt.size() <= 0 || pkt.data() == null) {
                    continue;
                }

                // 不需要编码直接把音视频帧推出去
                record.recordPacket(pkt);
                avcodec.av_packet_unref(pkt);

                try {
                    Thread.sleep(0,1000);
                } catch (InterruptedException e) {
                    log.error("推流发生等待异常,{}",e);
                }

            } catch (Exception e) {
                log.error("推流发生异常,{}",e);
            }
        }
    }

    private Boolean stop() {

        //控制退出循环
        flag = false;

        if(null != record){
            try {
                record.release();
            } catch (FrameRecorder.Exception e) {
                log.error("stop record error ,{}",e);
                return false;
            }
        }

        if(null != grabber){
            try {
                grabber.release();
            } catch (Exception e) {
                log.error("stop grabber error ,{}",e);
                return false;
            }
        }


        return true;
    }

    public static void main(String[] args) throws Exception, IOException {

        // 运行，设置视频源和推流地址
        new ConvertVideoPacket()
                .rtsp("rtsp://sxtest:Goodsense@10.8.10.57:80")
                .rtmp("/Users/wangxinxin/Downloads/test.flv").start();
    }
}
