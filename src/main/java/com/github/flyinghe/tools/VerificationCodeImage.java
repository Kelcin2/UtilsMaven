package com.github.flyinghe.tools;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * <p>
 * 该类用于生成验证码图片，实例化对象后就会自动生成一张验证码图片,
 * 你可以通过Setter方法重新设定参数然后调用{@link #changeImage()}
 * 重新随机获取一张验证码图片,然后通过{@link #getImage()}获取图片对象
 * </p>
 *
 * @author Flying
 */
public class VerificationCodeImage {
    public static final String CODES = "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ";
    public static final String[] FONT_NAMES = new String[]{"宋体", "华文楷体", "黑体", "微软雅黑", "楷体_GB2312", "Algerian"};

    private int w;
    private int h;
    private int textLength;
    private int fontBasicSize;
    private int fontSizeRange;
    private int linePower;
    private int pointPower;
    private String[] fontNames;
    private String codes;
    private String text;
    private BufferedImage image;


    public VerificationCodeImage() {
        this(4);
    }

    /**
     * @param textLength 验证码字符串长度
     */
    public VerificationCodeImage(int textLength) {
        this(textLength, null);
    }

    /**
     * @param textLength 验证码字符串长度
     * @param codes      一个字符数组(支持中文)，生成的验证码中的字符从该字符在数组中获取,null使用默认值
     */
    public VerificationCodeImage(int textLength, String codes) {
        this(70, 35, textLength, codes);
    }

    /**
     * @param w          图片宽
     * @param h          图片高
     * @param textLength 验证码字符串长度
     * @param codes      一个字符数组(支持中文)，生成的验证码中的字符从该字符在数组中获取,字符必须为英文状态下的字符,null使用默认值
     */
    public VerificationCodeImage(int w, int h, int textLength, String codes) {
        this(w, h, textLength, 1, 100, 24, 5, null, codes);
    }

    /**
     * @param w          图片宽,小于等于0表示使用默认值
     * @param h          图片高,小于等于0表示使用默认值
     * @param textLength 验证码字符串长度,小于等于0表示使用默认值
     * @param linePower  验证码中画干扰线条的数目(等于{@link #textLength}*{@link #linePower})，
     *                   若&lt;0则表示与{@link #textLength}一样多，
     *                   等于0表示不画干扰线
     * @param pointPower 验证码中画干扰点的数目(最多等于{@link #w}*{@link #h}/{@link #pointPower}),
     *                   若等于0或者小于-1表示使用默认值,等于-1表示不画干扰点
     * @param codes      一个字符数组(支持中文)，生成的验证码中的字符从该字符在数组中获取,null或者空则使用默认值
     */
    public VerificationCodeImage(int w, int h, int textLength, int linePower, int pointPower, String codes) {
        this(w, h, textLength, linePower, pointPower, 24, 5, null, null);
    }

    /**
     * @param w             图片宽
     * @param h             图片高
     * @param textLength    验证码字符串长度
     * @param fontBasicSize 字体大小的最低值
     * @param fontSizeRange 字体大小的浮动范围
     */
    public VerificationCodeImage(int w, int h, int textLength,
                                 int fontBasicSize, int fontSizeRange) {
        this(w, h, textLength, 1, 100, fontBasicSize, fontSizeRange, null, null);
    }

    /**
     * 根据构造参数自动生成一张验证码图片
     *
     * @param w             图片宽,小于等于0表示使用默认值
     * @param h             图片高,小于等于0表示使用默认值
     * @param textLength    验证码字符串长度,小于等于0表示使用默认值
     * @param linePower     验证码中画干扰线条的数目(等于{@link #textLength}*{@link #linePower})，
     *                      若&lt;0则表示与{@link #textLength}一样多，
     *                      等于0表示不画干扰线
     * @param pointPower    验证码中画干扰点的数目(最多等于{@link #w}*{@link #h}/{@link #pointPower}),
     *                      若等于0或者小于-1表示使用默认值,等于-1表示不画干扰点
     * @param fontBasicSize <p>字体大小的基本值(验证码字符实际大小在
     *                      [fontBasicSize,fontBasicSize+fontSizeRange]
     *                      或者[fontBasicSize+fontSizeRange,fontBasicSize]之间),
     *                      </p>小于等于0表示使用默认值
     * @param fontSizeRange 字体大小的浮动范围(会影响验证码字符的实际大小),小于0表示使用默认值
     * @param fontNames     指定验证码中字符字体类型，由字符类型名称组成的一个字符串数组,null或者空则使用默认值
     * @param codes         一个字符数组(支持中文)，生成的验证码中的字符从该字符在数组中获取,null或者空则使用默认值
     */
    public VerificationCodeImage(int w, int h, int textLength, int linePower, int pointPower,
                                 int fontBasicSize, int fontSizeRange, String[] fontNames,
                                 String codes) {
        super();
        this.w = w;
        this.h = h;
        this.textLength = textLength;
        this.linePower = linePower;
        this.pointPower = pointPower;
        this.fontBasicSize = fontBasicSize;
        this.fontSizeRange = fontSizeRange;
        this.fontNames = fontNames;
        this.codes = codes;
        this.initData();
        this.createImage();
    }

    private void initData() {
        //修正非法数据
        if (this.w <= 0) {
            this.w = 70;
        }
        if (this.h <= 0) {
            this.h = 35;
        }
        if (this.textLength <= 0) {
            this.textLength = 4;
        }
        if (this.fontBasicSize <= 0) {
            this.fontBasicSize = 24;
        }
        if (this.fontSizeRange < 0) {
            this.fontSizeRange = 5;
        }
        if (this.pointPower == 0 || this.pointPower < -1) {
            this.pointPower = 100;
        }
        if (this.linePower < 0) {
            this.linePower = 1;
        }
        if (!ArrayUtils.isNotEmpty(this.fontNames)) {
            this.fontNames = FONT_NAMES;
        }
        if (StringUtils.isBlank(this.codes)) {
            this.codes = CODES;
        }
    }


    /**
     * 随机生成一种颜色
     *
     * @return 返回随机生成的颜色
     */
    private Color randomColor() {
        Random r = new Random();
        int red = r.nextInt(150);
        int green = r.nextInt(150);
        int blue = r.nextInt(150);
        return new Color(red, green, blue);
    }

    /**
     * 随机生成一种字体
     *
     * @return 返回随机生成的字体
     */
    private Font randomFont() {
        Random r = new Random();
        int index = r.nextInt(this.fontNames.length);
        String fontName = this.fontNames[index];
        int style = r.nextInt(4);
        int size = (this.fontSizeRange > 0 ? r.nextInt(this.fontSizeRange) : 0) + this.fontBasicSize;
        return new Font(fontName, style, size);
    }

    /**
     * 随机像图片中画短直线
     *
     * @param image 被画直线的图片
     */
    private void drawLine(BufferedImage image) {
        Random r = new Random();
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        int max = this.textLength * this.linePower;
        for (int i = 0; i < max; i++) {
            int x1 = r.nextInt(w);
            int y1 = r.nextInt(h);
            int x2 = r.nextInt(w);
            int y2 = r.nextInt(h);
            g2.setStroke(new BasicStroke(1.5F));
            g2.setColor(this.randomColor());
            g2.drawLine(x1, y1, x2, y2);
        }
        g2.dispose();
    }

    /**
     * 随机画点
     *
     * @param image 图片
     */
    private void drawPoint(BufferedImage image) {
        if (this.pointPower == -1) {
            return;
        }
        Random r = new Random();
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        int max = this.w * this.h / this.pointPower;
        for (int i = 0; i < max; i++) {
            int x = r.nextInt(w);
            int y = r.nextInt(h);
            g2.setStroke(new BasicStroke(1.5F));
            g2.setColor(this.randomColor());
            g2.drawOval(x, y, 1, 1);
        }
        g2.dispose();
    }

    /**
     * 随机返回codes字符串中的一个字符
     *
     * @return 随机返回codes字符串中的一个字符
     */
    private char randomChar() {
        Random r = new Random();
        int index = r.nextInt(codes.length());
        return codes.charAt(index);
    }

    /**
     * 随机画字符
     *
     * @param image 图片
     * @return 返回画的字符串
     */
    private String drawString(BufferedImage image) {
        Random r = new Random();
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        // 记录生成的文本
        StringBuilder sb = new StringBuilder();
        // 向图片中画指定个数字符
        for (int i = 0; i < this.textLength; i++) {
            String s = randomChar() + "";
            sb.append(s);
            // 字符的x坐标在[minX,maxX]范围内
            int cw = this.w / this.textLength;//每个字符占的宽度
            int minX = cw * i;
            int maxX = minX + cw * 2 / 3;
            int x = r.nextInt(maxX - minX + 1) + minX;
            //字符的y坐标在[minY,maxY]范围内
            Font font = randomFont();
            int minY = this.h * font.getSize() / 48;
            int maxY = this.h * font.getSize() * 3 / 80;
            minY = minY >= this.h ? this.h / 2 : minY;
            maxY = maxY >= this.h ? this.h * 9 / 10 : maxY;
            int y = r.nextInt(maxY - minY + 1) + minY;
            //字符旋转度数在[0,360']范围内
//            double rotate = Math.toRadians(30);//转换成theta值
//            g2.rotate(rotate, x + 12, y + 12);
            g2.setFont(font);
            g2.setColor(randomColor());
            g2.drawString(s, x, y);
        }
        return sb.toString();
    }

    /**
     * 生成一张背景为白色的空白图片
     *
     * @return 返回此图片
     */
    private BufferedImage generateBlankImg() {
        BufferedImage image = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        //设置图片背景是白色
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
        return image;
    }

    /**
     * 根据图片参数随机生成一张验证码图片
     */
    private void createImage() {
        this.image = this.generateBlankImg();
        this.text = this.drawString(this.image);
        this.drawLine(this.image);
        this.drawPoint(this.image);
    }

    /**
     * 根据构造该对象时设置的参数重新随机生成一张验证码图片
     */
    public void changeImage() {
        this.createImage();
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

    /**
     * 获取验证码图片的宽
     *
     * @return 返回验证码图片的宽
     */
    public int getW() {
        return w;
    }

    /**
     * 获取验证码图片的高
     *
     * @return 返回验证码图片的高
     */
    public int getH() {
        return h;
    }

    /**
     * 获取验证码图片中的文本长度
     *
     * @return 返回验证码图片中的文本长度
     */
    public int getTextLength() {
        return textLength;
    }

    /**
     * 获取验证码图片中的字体大小最低值
     *
     * @return 返回验证码图片中的字体大小最低值
     */
    public int getFontBasicSize() {
        return fontBasicSize;
    }

    /**
     * 获取验证码图片中的字体大小浮动范围
     *
     * @return 返回图片宽
     */
    public int getFontSizeRange() {
        return fontSizeRange;
    }

    /**
     * 获取验证码图片中的文本字体类型取自于的字体集合
     *
     * @return 返回验证码图片中的文本字体类型取自于的字体集合
     */
    public String[] getFontNames() {
        return fontNames;
    }

    /**
     * 获取验证码图片中的字符取自于指定的字符串
     *
     * @return 返回验证码图片中的字符取自于指定的字符串
     */
    public String getCodes() {
        return codes;
    }

    public void setW(int w) {
        this.initData();
        this.w = w;
    }

    public void setH(int h) {
        this.initData();
        this.h = h;
    }

    public void setTextLength(int textLength) {
        this.initData();
        this.textLength = textLength;
    }

    public void setFontBasicSize(int fontBasicSize) {
        this.initData();
        this.fontBasicSize = fontBasicSize;
    }

    public void setFontSizeRange(int fontSizeRange) {
        this.initData();
        this.fontSizeRange = fontSizeRange;
    }

    public int getLinePower() {
        return linePower;
    }

    public void setLinePower(int linePower) {
        this.initData();
        this.linePower = linePower;
    }

    public int getPointPower() {
        return pointPower;
    }

    public void setPointPower(int pointPower) {
        this.initData();
        this.pointPower = pointPower;
    }

    public void setFontNames(String[] fontNames) {
        this.initData();
        this.fontNames = fontNames;
    }

    public void setCodes(String codes) {
        this.initData();
        this.codes = codes;
    }

    /**
     * 获取验证码图片中的文本信息
     *
     * @return 返回验证码图片中的文本信息
     */
    public String getText() {
        return text;
    }

    /**
     * 获取验证码图片
     *
     * @return 返回验证码图片
     */
    public BufferedImage getImage() {
        return image;
    }

}
