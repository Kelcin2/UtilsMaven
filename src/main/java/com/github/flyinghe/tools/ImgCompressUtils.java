package com.github.flyinghe.tools;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * Created by FlyingHe on 2016/12/5.
 * <p>本类提供多种方式压缩图片</p>
 *
 * @author Flyinghe
 */
public class ImgCompressUtils {
    //指定等比例压缩图片根据图片高压缩
    public static final int HEIGHT = 0;
    //指定等比例压缩图片根据图片宽压缩
    public static final int WIDTH = 1;
    //指定等比例压缩图片不改变源图片宽高
    public static final int NO_CHANGE = 2;

    private ImgCompressUtils() {}

    /**
     * 根据指定宽高和压缩质量进行压缩，如果指定宽或者高大于源图片则按照源图片大小宽高压缩
     *
     * @param srcFile 指定原图片地址
     * @param desFile 指定压缩后图片存放地址，包括图片名称
     * @param width   指定压缩宽
     * @param height  指定压缩高
     * @param quality 指定压缩质量，范围[0.0,1.0]，如果指定为null则按照默认值
     */
    public static void imgCompressByWH(String srcFile, String desFile, int width, int height, Float quality) {
        ImgCompressUtils.imgCompressByWH(srcFile, desFile, width, height, quality, false);
    }

    /**
     * 根据指定宽高和压缩质量进行压缩，当isForceWh为false时,如果指定宽或者高大于源图片则按照源图片大小宽高压缩,
     * 当isForceWh为true时,不论怎样均按照指定宽高压缩
     *
     * @param srcFile   指定原图片地址
     * @param desFile   指定压缩后图片存放地址，包括图片名称
     * @param width     指定压缩宽
     * @param height    指定压缩高
     * @param quality   指定压缩质量，范围[0.0,1.0]，如果指定为null则按照默认值
     * @param isForceWh 指定是否强制使用指定宽高进行压缩,true代表强制,false反之
     */
    public static void imgCompressByWH(String srcFile, String desFile, int width, int height, Float quality,
                                       boolean isForceWh) {
        try {
            Image srcImg = ImageIO.read(new File(srcFile));
            if (!isForceWh && (srcImg.getHeight(null) < height || srcImg.getWidth(null) < width)) {
                width = srcImg.getWidth(null);
                height = srcImg.getHeight(null);
            }
            //指定目标图片
            BufferedImage desImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            //根据源图片绘制目标图片
            desImg.getGraphics().drawImage(srcImg, 0, 0, width, height, null);
            ImgCompressUtils.encodeImg(desFile, desImg, quality);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据指定宽高和压缩质量进行压缩，当isForceWh为false时,如果指定宽或者高大于源图片则按照源图片大小宽高压缩,
     * 当isForceWh为true时,不论怎样均按照指定宽高压缩
     *
     * @param srcFile   指定原图片地址
     * @param width     指定压缩宽
     * @param height    指定压缩高
     * @param quality   指定压缩质量，范围[0.0,1.0]，如果指定为null则按照默认值
     * @param isForceWh 指定是否强制使用指定宽高进行压缩,true代表强制,false反之
     */
    public static BufferedImage imgCompressByWH(String srcFile, int width, int height, Float quality,
                                                boolean isForceWh) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage =
                    ImgCompressUtils.imgCompressByWH(ImageIO.read(new File(srcFile)), width, height, quality, isForceWh);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return bufferedImage;
    }

    /**
     * 根据指定宽高和压缩质量进行压缩，当isForceWh为false时,如果指定宽或者高大于源图片则按照源图片大小宽高压缩,
     * 当isForceWh为true时,不论怎样均按照指定宽高压缩
     *
     * @param srcStream 指定原图片的输入流,该流不会被关闭,调用者自行关闭
     * @param width     指定压缩宽
     * @param height    指定压缩高
     * @param quality   指定压缩质量，范围[0.0,1.0]，如果指定为null则按照默认值
     * @param isForceWh 指定是否强制使用指定宽高进行压缩,true代表强制,false反之
     */
    public static BufferedImage imgCompressByWH(InputStream srcStream, int width, int height, Float quality,
                                                boolean isForceWh) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImgCompressUtils.imgCompressByWH(ImageIO.read(srcStream), width, height, quality, isForceWh);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return bufferedImage;
    }

    /**
     * 根据指定宽高和压缩质量进行压缩，当isForceWh为false时,如果指定宽或者高大于源图片则按照源图片大小宽高压缩,
     * 当isForceWh为true时,不论怎样均按照指定宽高压缩
     *
     * @param srcImg    指定原图片对象
     * @param width     指定压缩宽
     * @param height    指定压缩高
     * @param quality   指定压缩质量，范围[0.0,1.0]，如果指定为null则按照默认值
     * @param isForceWh 指定是否强制使用指定宽高进行压缩,true代表强制,false反之
     */
    public static BufferedImage imgCompressByWH(Image srcImg, int width, int height, Float quality,
                                                boolean isForceWh) {
        if (!isForceWh && (srcImg.getHeight(null) < height || srcImg.getWidth(null) < width)) {
            width = srcImg.getWidth(null);
            height = srcImg.getHeight(null);
        }
        //指定目标图片
        BufferedImage desImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //根据源图片绘制目标图片
        desImg.getGraphics().drawImage(srcImg, 0, 0, width, height, null);
        return ImgCompressUtils.encodeImg(desImg, quality);
    }

    /**
     * 采用JPEG编码图片
     *
     * @param desFile 指定压缩后图片存放地址，包括图片名称
     * @param desImg  编码源图片
     * @param quality 编码质量
     * @throws Exception
     */
    private static void encodeImg(String desFile, BufferedImage desImg, Float quality) {
        if (quality != null && (quality > 1.0 || quality < 0.0)) {
            throw new IllegalArgumentException("quality参数指定值不正确");
        }
        ImageWriter writer = null;
        try (FileOutputStream fos = new FileOutputStream(desFile);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {
            // 获取JPEG图像写入器
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG");
            if (!writers.hasNext()) {
                throw new IllegalStateException("No JPEG Image Writers Exist");
            }
            writer = writers.next();
            // 配置压缩质量
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (quality != null) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            // 写入图像
            writer.setOutput(ios);
            writer.write(null, new IIOImage(desImg, null, null), param);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (null != writer) {
                writer.dispose();
            }
        }
    }

    /**
     * 采用JPEG编码图片，并生成BufferedImage对象返回
     *
     * @param desImg  编码源图片
     * @param quality 编码质量
     * @return 返回编码后的BufferedImage对象
     */
    private static BufferedImage encodeImg(BufferedImage desImg, Float quality) {
        if (quality != null && (quality > 1.0 || quality < 0.0)) {
            throw new IllegalArgumentException("quality参数指定值不正确");
        }
        ByteArrayInputStream bais = null;
        BufferedImage bufferedImage = null;
        ImageWriter writer = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            // 获取JPEG图像写入器
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG");
            if (!writers.hasNext()) {
                throw new IllegalStateException("No JPEG Image Writers Exist");
            }
            writer = writers.next();
            // 配置压缩质量
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (quality != null) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            // 写入图像
            writer.setOutput(ios);
            writer.write(null, new IIOImage(desImg, null, null), param);

            bais = new ByteArrayInputStream(baos.toByteArray());
            bufferedImage = ImageIO.read(bais);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CommonUtils.closeIOStream(bais, null);
            if (null != writer) {
                writer.dispose();
            }
        }
        return bufferedImage;
    }

    /**
     * 根据宽或者高和指定压缩质量进行等比例压缩,注意如果指定的宽或者高大于源图片的高或者宽,
     * 那么压缩图片将直接使用源图片的宽高
     *
     * @param srcFile 源图片地址
     * @param desFile 目标图片地址，包括图片名称
     * @param base    指定压缩后图片的宽或者高
     * @param wh      此参数用于指定base参数是宽还是高，该参数应由{@link ImgCompressUtils}里的
     *                静态常量指定
     * @param quality 指定压缩质量，范围[0.0,1.0],如果指定为null则按照默认值
     */
    public static void imgCompressByScale(String srcFile, String desFile, double base, int wh, Float quality) {
        ImgCompressUtils.imgCompressByScale(srcFile, desFile, base, wh, quality, false);
    }

    /**
     * 根据宽或者高和指定压缩质量进行等比例压缩,注意如果指定的宽或者高大于源图片的高或者宽,
     * 那么压缩图片将直接使用源图片的宽高。
     * 注:若isForceWh为true,不论如何均按照指定宽高进行等比例压缩
     *
     * @param srcFile   源图片地址
     * @param desFile   目标图片地址，包括图片名称
     * @param base      指定压缩后图片的宽或者高
     * @param wh        此参数用于指定base参数是宽还是高，该参数应由{@link ImgCompressUtils}里的
     *                  静态常量指定
     * @param quality   指定压缩质量，范围[0.0,1.0],如果指定为null则按照默认值
     * @param isForceWh 指定是否强制使用指定宽高进行等比例压缩,true代表强制,false反之
     */
    public static void imgCompressByScale(String srcFile, String desFile, double base, int wh, Float quality,
                                          boolean isForceWh) {
        int width = 0;
        int height = 0;
        try {
            Image srcImg = ImageIO.read(new File(srcFile));
            if (wh == ImgCompressUtils.HEIGHT) {
                if (base > srcImg.getHeight(null) && !isForceWh) {
                    width = srcImg.getWidth(null);
                    height = srcImg.getHeight(null);
                } else {
                    //根据高度等比例设置宽高
                    height = (int) Math.floor(base);
                    width = (int) Math.floor((srcImg.getWidth(null) * base / srcImg.getHeight(null)));
                }
            } else if (wh == ImgCompressUtils.WIDTH) {
                //根据宽度等比例设置宽高
                if (base > srcImg.getWidth(null) && !isForceWh) {
                    height = srcImg.getHeight(null);
                    width = srcImg.getWidth(null);
                } else {
                    width = (int) Math.floor(base);
                    height = (int) Math.floor((srcImg.getHeight(null) * base / srcImg.getWidth(null)));
                }
            } else if (wh == ImgCompressUtils.NO_CHANGE) {
                //不改变原始宽高
                height = srcImg.getHeight(null);
                width = srcImg.getWidth(null);
            } else {
                throw new Exception("wh参数指定值不正确");
            }
            //指定目标图像
            BufferedImage desImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            //绘制目标图像
            desImg.getGraphics().drawImage(srcImg, 0, 0, width, height, null);

            encodeImg(desFile, desImg, quality);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据宽或者高和指定压缩质量进行等比例压缩,注意如果指定的宽或者高大于源图片的高或者宽,
     * 那么压缩图片将直接使用源图片的宽高。
     * 注:若isForceWh为true,不论如何均按照指定宽高进行等比例压缩
     *
     * @param srcFile   源图片地址
     * @param base      指定压缩后图片的宽或者高
     * @param wh        此参数用于指定base参数是宽还是高，该参数应由{@link ImgCompressUtils}里的
     *                  静态常量指定
     * @param quality   指定压缩质量，范围[0.0,1.0],如果指定为null则按照默认值
     * @param isForceWh 指定是否强制使用指定宽高进行等比例压缩,true代表强制,false反之
     * @return 返回压缩后的图像对象
     */
    public static BufferedImage imgCompressByScale(String srcFile, double base, int wh, Float quality,
                                                   boolean isForceWh) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImgCompressUtils
                    .imgCompressByScale(ImageIO.read(new File(srcFile)), base, wh, quality, isForceWh);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bufferedImage;
    }

    /**
     * 根据宽或者高和指定压缩质量进行等比例压缩,注意如果指定的宽或者高大于源图片的高或者宽,
     * 那么压缩图片将直接使用源图片的宽高。
     * 注:若isForceWh为true,不论如何均按照指定宽高进行等比例压缩
     *
     * @param srcStream 源图片输入流
     * @param base      指定压缩后图片的宽或者高
     * @param wh        此参数用于指定base参数是宽还是高，该参数应由{@link ImgCompressUtils}里的
     *                  静态常量指定
     * @param quality   指定压缩质量，范围[0.0,1.0],如果指定为null则按照默认值
     * @param isForceWh 指定是否强制使用指定宽高进行等比例压缩,true代表强制,false反之
     * @return 返回压缩后的图像对象
     */
    public static BufferedImage imgCompressByScale(InputStream srcStream, double base, int wh, Float quality,
                                                   boolean isForceWh) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImgCompressUtils.imgCompressByScale(ImageIO.read(srcStream), base, wh, quality, isForceWh);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bufferedImage;
    }

    /**
     * 根据宽或者高和指定压缩质量进行等比例压缩,注意如果指定的宽或者高大于源图片的高或者宽,
     * 那么压缩图片将直接使用源图片的宽高。
     * 注:若isForceWh为true,不论如何均按照指定宽高进行等比例压缩
     *
     * @param srcImg    源图片地址
     * @param base      指定压缩后图片的宽或者高
     * @param wh        此参数用于指定base参数是宽还是高，该参数应由{@link ImgCompressUtils}里的
     *                  静态常量指定
     * @param quality   指定压缩质量，范围[0.0,1.0],如果指定为null则按照默认值
     * @param isForceWh 指定是否强制使用指定宽高进行等比例压缩,true代表强制,false反之
     * @return 返回压缩后的图像对象
     */
    public static BufferedImage imgCompressByScale(Image srcImg, double base, int wh, Float quality,
                                                   boolean isForceWh) {
        int width = 0;
        int height = 0;
        BufferedImage bufferedImage = null;
        try {
            if (wh == ImgCompressUtils.HEIGHT) {
                if (base > srcImg.getHeight(null) && !isForceWh) {
                    width = srcImg.getWidth(null);
                    height = srcImg.getHeight(null);
                } else {
                    //根据高度等比例设置宽高
                    height = (int) Math.floor(base);
                    width = (int) Math.floor((srcImg.getWidth(null) * base / srcImg.getHeight(null)));
                }
            } else if (wh == ImgCompressUtils.WIDTH) {
                //根据宽度等比例设置宽高
                if (base > srcImg.getWidth(null) && !isForceWh) {
                    height = srcImg.getHeight(null);
                    width = srcImg.getWidth(null);
                } else {
                    width = (int) Math.floor(base);
                    height = (int) Math.floor((srcImg.getHeight(null) * base / srcImg.getWidth(null)));
                }
            } else if (wh == ImgCompressUtils.NO_CHANGE) {
                //不改变原始宽高
                height = srcImg.getHeight(null);
                width = srcImg.getWidth(null);
            } else {
                throw new Exception("wh参数指定值不正确");
            }
            //指定目标图像
            BufferedImage desImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            //绘制目标图像
            desImg.getGraphics().drawImage(srcImg, 0, 0, width, height, null);

            bufferedImage = encodeImg(desImg, quality);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bufferedImage;
    }

    /**
     * 将指定的图片输出到指定位置
     *
     * @param image 指定图片
     * @param out   输出位置
     * @throws IOException
     */
    public static void output(BufferedImage image, OutputStream out)
            throws IOException {
        ImageIO.write(image, "JPEG", out);
    }

    /**
     * 将指定的图片输出到指定位置
     *
     * @param image      指定图片
     * @param out        输出位置
     * @param formatName 图片格式名,如gif,png,jpg,jpeg等
     * @throws IOException
     */
    public static void output(BufferedImage image, OutputStream out, String formatName)
            throws IOException {
        ImageIO.write(image, formatName, out);
    }
}
