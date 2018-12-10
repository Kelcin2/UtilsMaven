package com.github.flyinghe.utlils;

import com.github.flyinghe.tools.ImgUtils;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by FlyingHe on 2018/3/11.
 */
public class TestImgUtils {

    @Test
    public void test() throws IOException {
        String file = "C:\\Users\\FlyingHe\\Desktop\\1.jpg";
        String destfile = "C:\\Users\\FlyingHe\\Desktop\\2.jpg";
        BufferedImage bufferedImage = ImgUtils.imgCompressByScale(file, 111, ImgUtils.WIDTH, null, true);
        ImgUtils.output(bufferedImage, new FileOutputStream(new File(destfile)), "jpg");
    }
}
