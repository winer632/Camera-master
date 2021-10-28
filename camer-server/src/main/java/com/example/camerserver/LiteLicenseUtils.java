package com.example.camerserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * @author: WangXinXin
 * @description:
 * @date: 2021-10-22 13:24
 **/
public class LiteLicenseUtils {
    public LiteLicenseUtils(){
    }
    private static final long serialVersionUID = 1L;


    /**
     * 获取主板序列号1
     *
     * @param
     * @return String
     */
    public static String getMotherBoardSN1() {
        String result = "";
        ProcessBuilder builder =
                new ProcessBuilder("bash", "-c", "dmidecode |grep -A16 \"System Information$\" | grep Serial | sed -n '1p' | awk '{print $3}'");
        return getHardWareInfo(builder);
    }

    /**
     * 获取主板序列号2
     *
     * @return String
     */
    public static String getMotherBoardSN2() {
        ProcessBuilder builder =
                new ProcessBuilder("bash", "-c", "dmidecode |grep -A16 \"System Information$\" | grep Serial | sed -n '2p' | awk '{print $3}'");
        return getHardWareInfo(builder);
    }

    /**
     * 获取UUID
     *
     * @return String
     */
    public static String getUUID() {
        ProcessBuilder builder =
                new ProcessBuilder("bash", "-c", "dmidecode |grep -A16 \"System Information$\" | grep UUID | awk '{print $2}'");
        return getHardWareInfo(builder);
    }


    /**
     * 获取硬件信息
     *
     * @param builder
     * @return String
     */
    public static String getHardWareInfo(ProcessBuilder builder) {
        String result = "";
        try{
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                if("Bad address".equals(line)){
                    continue;
                }
                result += line;
            }
            input.close();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        if (result.trim().length() < 1 || result == null) {
            result = "无UUID被读取";
        }
        return result.trim();
    }



    /**
     * HmacSHA256 加密
     * 修改secret的值会导致加密结果不同
     * pom.xml引用了commons-codec的1.15版本
     * @param message
     * @return String
     */
    public static String hmac(String message) {
        String hash = "";
        try {
            String secret = "!QAZ2wsx";

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));
        }
        catch (Exception e){
            System.out.println("Error");
        }
        return hash;
    }

    /**
     * 退出SpringBoot程序
     *
     * @param context
     * @return void
     */
    public static void exitApplication(ConfigurableApplicationContext context) {
        int exitCode = SpringApplication.exit(context, (ExitCodeGenerator) () -> 0);
        System.exit(exitCode);
    }


    public static void main(String[] args) throws Exception {
        //主板序列号1
        System.out.println(getMotherBoardSN1());
        //主板序列号2
        System.out.println(getMotherBoardSN2());
        //UUID
        System.out.println(getUUID());

        String msg  = getMotherBoardSN1()+getMotherBoardSN2()+getUUID();
        System.out.println("原始数据:"+msg);
        String result = hmac(msg);
        System.out.println("HMAC SHA256加密后的数据:"+result);

    }
}
