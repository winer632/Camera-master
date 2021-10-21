package com.example.camerserver;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author: WangXinXin
 * @description: ConvertVideoPacket
 * @date: 2021-10-21 22:06
 **/
public class RecordPush {


    /**
     * 推流器
     * @param outputPath 接收路径
     * @param v_rs       帧率
     * @throws Exception
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     * @throws InterruptedException
     */
    public void getRecordPush(String outputPath, int v_rs) throws Exception, org.bytedeco.javacv.FrameRecorder.Exception, InterruptedException {

        Loader.load(opencv_objdetect.class);
        //创建视频帧采集器 本地摄像头默认为0
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        //开启采集器
        try {
            grabber.start();
        } catch (Exception e) {
            try {
                //一次重启尝试
                grabber.restart();
            } catch (Exception e2) {
                throw e;
            }
        }
        //转换器
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        //获取一帧
        Frame grabframe = grabber.grab();
        opencv_core.IplImage grabbedImage = null;
        if (grabframe != null) {
            //将这一帧转换为IplImage
            grabbedImage = converter.convert(grabframe);
        }

        //创建录制器
        FrameRecorder recorder;
        //输出路径，画面高，画面宽
        recorder = FrameRecorder.createDefault(outputPath, 1280, 720);
        //设置编码格式s
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(v_rs);
        recorder.setGopSize(v_rs);

        //开启录制器
        try {
            recorder.start();
        } catch (java.lang.Exception e) {
            System.out.println("recorder开启失败");
            System.out.println(recorder);
            try {
                //尝试重启录制器
                if (recorder != null) {
                    recorder.stop();
                    recorder.start();
                }
            } catch (java.lang.Exception e1) {
                e.printStackTrace();
            }
        }

        //直播效果展示窗口
        CanvasFrame frame = new CanvasFrame("直播效果", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);

        //推流
        while (frame.isVisible() && (grabframe = grabber.grab()) != null) {
            //展示直播效果
            frame.showImage(grabframe);
            grabbedImage = converter.convert(grabframe);
            Frame rotatedFrame = converter.convert(grabbedImage);

            if (rotatedFrame != null) {
                recorder.record(rotatedFrame);
            }
            //50毫秒/帧
            Thread.sleep(50);
        }
    }
    public static void getRecordPush() throws FrameGrabber.Exception, FrameRecorder.Exception, InterruptedException {
        //直播效果展示窗口
        CanvasFrame frame = new CanvasFrame("直播效果");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);

    }

    public static void main(String[] args) {
        ///中缀表达式
       String pam = "3+2*(1+2*((0-4)/(8-6)+7))";
        // 逆波兰表达式
       char[] chars = pam.toCharArray();
       Stack<String> stack = new Stack<>();
       Stack<Integer> stackP = new Stack<>();
       int result = 0;
       for (int i = 0; i< chars.length; i++){
           if (chars[i] == '('){
               stackP.push(i);
           }
           if (chars[i] != ')'){
               stack.push(String.valueOf(chars[i]));
           }else {
               List<String> list = new ArrayList<>();
               list.add(stack.firstElement());
               stack.pop();
               for (int j = list.size() - 1; j > 0; j--){
                   if (!list.get(j).equals("+")
                           && !list.get(j).equals("-")
                           && !list.get(j).equals("*")
                           && !list.get(j).equals("/")
                   ){
                       Integer.valueOf(list.get(j));
                   }
               }
           }
       }
    }

}
