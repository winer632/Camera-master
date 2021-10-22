package com.example.camserclient;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author: WangXinXin
 * @description: CameraClientApplication
 * @date: 2021-10-21 22:06
 **/

@SpringBootApplication
public class CameraClientApplication {

    public static void main(String[] args) {

        SpringApplicationBuilder builder = new SpringApplicationBuilder(CameraClientApplication.class);
        builder.headless(false).run(args);
        System.out.println("---------------启动成功---------------");

        //rtmp服务器拉流地址
        String inputPath = "rtmp://106.15.107.185:1935/live/livestream";
        PullStream pullStream = new PullStream();
        try {
            pullStream.getPullStream(inputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
