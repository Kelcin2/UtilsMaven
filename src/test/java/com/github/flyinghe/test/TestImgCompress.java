package com.github.flyinghe.test;

import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.ImgUtils;
import com.github.flyinghe.tools.VerificationCodeImage;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by FlyingHe on 2016/12/5.
 */
public class TestImgCompress {
    @Test
    public void testq1() {
        Collection<String> c = new ArrayList();
        c.add("JavaSE");
        c.add("JavaWeb");
        c.add("JavaEE");
        c.add("JavaEE");

        Iterator it = c.iterator();
        for (String s : c) {
        }
    }

    @Test
    public void testq() throws IOException {
        VerificationCodeImage codeImage = new VerificationCodeImage(200, 100, 4, new String("你麻痹啊傻逼"));
        String imgstr = CommonUtils.imgToBase64Str(codeImage.getImage(), null);

        BufferedImage bufferedImage = CommonUtils.base64StrToImg(imgstr);

        File file = new File("C:\\Users\\FlyingHe\\Desktop", "vcode.jpeg");
        OutputStream os = new FileOutputStream(file);
        VerificationCodeImage.output(bufferedImage, os);
        os.close();
        String s1 = CommonUtils.imgToBase64Str(bufferedImage, null);
        String s2 = CommonUtils.imgToBase64StrWithPrefix(bufferedImage, null);
        System.out.println(imgstr.equals(s1));
        System.out.println(imgstr);
        System.out.println(s1);
        System.out.println(s2);
    }


    @Test
    public void testCharAt() {
        String str = "你麻痹啊";
        char a = '你';
        System.out.println(str.charAt(0) + ":" + str.length());
    }

    @Test
    public void testVerifyCode() throws IOException {
//        VerificationCodeImage codeImage = new VerificationCodeImage(
//                200, 100, 4, -1,0,null);
//        VerificationCodeImage codeImage = new VerificationCodeImage(
//                200, 100, 4, null);
        VerificationCodeImage codeImage = new VerificationCodeImage(
                206, 58, 4, 50, 10);
//        VerificationCodeImage codeImage = new VerificationCodeImage(
//                200, 100, 4, -1, 0, -1, 0, null, "你麻痹啊");
//        VerificationCodeImage codeImage = new VerificationCodeImage(-1,-1,-1,-1,-1);
        File file = new File("C:\\Users\\FlyingHe\\Desktop", "vcode1.jpg");
        OutputStream os = new FileOutputStream(file);
        VerificationCodeImage.output(codeImage.getImage(), os);
        os.close();
        System.out.println(codeImage.getText());
    }

    public String encode(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", baos);
        byte[] bytes = baos.toByteArray();
        Base64.Encoder encoder = Base64.getEncoder();
        String imgstr = encoder.encodeToString(bytes);
        return imgstr;
    }

    @Test
    public void testVerifyCodeBase64() throws IOException {
        VerificationCodeImage codeImage = new VerificationCodeImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(codeImage.getImage(), "jpeg", baos);
        byte[] bytes = baos.toByteArray();
        Base64.Encoder encoder = Base64.getEncoder();
        String imgstr = encoder.encodeToString(bytes);
        System.out.println(imgstr);
    }

    @Test
    public void test1() {
        String srcFile = "C:\\Users\\FlyingHe\\Desktop\\高达一亿像素的中国地图 极适合做桌面.jpg";
        String desFile = "C:\\Users\\FlyingHe\\Desktop\\11.jpg";
        String desFile1 = "C:\\Users\\FlyingHe\\Desktop\\12.jpg";
        ImgUtils.imgCompressByScale(srcFile, desFile, 500, ImgUtils.WIDTH, -0.9f);
        ImgUtils.imgCompressByWH(srcFile, desFile1, 600, 500, 5.6f);
    }
}
