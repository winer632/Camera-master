package com.example.camerserver;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author: WangXinXin
 * @description: CameraServerApplication
 * @date: 2021-10-21 22:06
 **/

@SpringBootApplication
public class CameraServerApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(CameraServerApplication.class);
        builder.headless(false).run(args);
        System.out.println("---------------启动成功---------------");

        /**
         * 采用远程监控没专用摄像头作为视频源
         * */
        // 运行，设置视频源和推流地址
        try {
            new ConvertVideoPacket()
                    // ip 厂家提供的的摄像头地址
                    .rtsp("rtsp://sxtest:Goodsense@10.8.10.57:80")
                    .rtmp("rtmp://127.0.0.1:1935/live/stream")
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * 采用备抵摄像头当做视频源
         * */
        //设置rtmp服务器推流地址
        String outputPath = "rtmp://127.0.0.1:1935/live/stream";
        RecordPush recordPush = new RecordPush();
        try {
            recordPush.getRecordPush(outputPath, 25);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
