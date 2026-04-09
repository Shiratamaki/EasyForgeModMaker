package com.easyforge.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 纹理管理工具 - 增强版
 * 支持批量导入、尺寸检测与自动缩放、生成占位纹理（多种样式）
 */
public class TextureManager {
    private static final Logger LOGGER = Logger.getLogger(TextureManager.class.getName());

    // 允许的纹理尺寸（16x16, 32x32, 64x64, 128x128, 256x256, 512x512）
    private static final int[] ALLOWED_SIZES = {16, 32, 64, 128, 256, 512};

    /**
     * 复制纹理到项目目录，并检测/修正尺寸
     * @param srcFile 源文件
     * @param projectPath 项目根路径
     * @param modId 模组ID
     * @param type 类型："item" 或 "block"
     * @param targetName 目标文件名（不含扩展名）
     * @return 相对路径，失败返回 null
     */
    public static String copyTexture(File srcFile, String projectPath, String modId, String type, String targetName) {
        if (!srcFile.exists() || !srcFile.isFile()) return null;
        Path destDir = Path.of(projectPath, "src", "main", "resources", "assets", modId, "textures", type);
        try {
            Files.createDirectories(destDir);
            String fileName = targetName + ".png";
            Path destFile = destDir.resolve(fileName);

            // 检测尺寸并修正
            BufferedImage img = ImageIO.read(srcFile);
            if (img == null) {
                LOGGER.warning("无法读取图片: " + srcFile);
                return null;
            }
            int width = img.getWidth();
            int height = img.getHeight();
            if (!isValidSize(width) || !isValidSize(height) || width != height) {
                // 尺寸不合规，自动缩放为最接近的合法正方形尺寸
                int targetSize = getClosestValidSize(Math.max(width, height));
                BufferedImage scaled = resizeImage(img, targetSize, targetSize);
                ImageIO.write(scaled, "png", destFile.toFile());
                LOGGER.info("纹理尺寸已从 " + width + "x" + height + " 调整为 " + targetSize + "x" + targetSize);
            } else {
                Files.copy(srcFile.toPath(), destFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return "assets/" + modId + "/textures/" + type + "/" + fileName;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "复制纹理失败", e);
            return null;
        }
    }

    /**
     * 批量导入纹理：文件名自动匹配物品/方块 ID
     * @param files 选择的图片文件列表
     * @param projectPath 项目根路径
     * @param modId 模组ID
     * @param type "item" 或 "block"
     * @return 成功导入的映射 (基础文件名 -> 相对路径)
     */
    public static Map<String, String> batchImport(java.util.List<File> files, String projectPath, String modId, String type) {
        Map<String, String> result = new HashMap<>();
        for (File file : files) {
            String name = file.getName();
            if (!name.toLowerCase().endsWith(".png")) continue;
            String baseName = name.substring(0, name.lastIndexOf('.'));
            String id = baseName.contains(":") ? baseName.substring(baseName.indexOf(':') + 1) : baseName;
            String relative = copyTexture(file, projectPath, modId, type, id);
            if (relative != null) {
                result.put(baseName, relative);
            }
        }
        return result;
    }

    /**
     * 生成占位纹理（支持多种样式）
     * @param projectPath 项目根路径
     * @param modId 模组ID
     * @param type "item" 或 "block"
     * @param id 物品/方块ID
     * @param style 样式：0=纯色+文字，1=渐变背景+文字，2=边框+阴影
     * @param bgColor 背景色（可为null，使用默认）
     * @return 相对路径
     */
    public static String generatePlaceholder(String projectPath, String modId, String type, String id, int style, Color bgColor) {
        Path destDir = Path.of(projectPath, "src", "main", "resources", "assets", modId, "textures", type);
        try {
            Files.createDirectories(destDir);
            Path destFile = destDir.resolve(id + ".png");
            if (Files.exists(destFile)) return "assets/" + modId + "/textures/" + type + "/" + id + ".png";

            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();

            // 背景
            if (bgColor == null) bgColor = new Color(150, 150, 150);
            if (style == 1) {
                // 渐变背景
                GradientPaint grad = new GradientPaint(0, 0, bgColor, 16, 16, bgColor.brighter());
                g.setPaint(grad);
                g.fillRect(0, 0, 16, 16);
            } else {
                g.setColor(bgColor);
                g.fillRect(0, 0, 16, 16);
            }

            // 边框（样式2）
            if (style == 2) {
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(1));
                g.drawRect(1, 1, 14, 14);
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, 15, 15);
            }

            // 文字
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String text = id.substring(0, 1).toUpperCase();
            g.drawString(text, 4, 12);

            // 阴影（可选）
            if (style == 2) {
                g.setColor(new Color(0,0,0,100));
                g.drawString(text, 5, 13);
                g.setColor(Color.WHITE);
                g.drawString(text, 4, 12);
            }

            g.dispose();
            ImageIO.write(img, "png", destFile.toFile());
            return "assets/" + modId + "/textures/" + type + "/" + id + ".png";
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "生成占位纹理失败", e);
            return null;
        }
    }

    /**
     * 加载纹理图片
     * @param projectPath 项目根路径
     * @param relativePath 相对路径
     * @param width 期望宽度（<=0 保持原尺寸）
     * @param height 期望高度
     * @return Image 对象
     */
    public static Image loadTexture(String projectPath, String relativePath, int width, int height) {
        if (relativePath == null || relativePath.isEmpty()) return null;
        Path fullPath = Path.of(projectPath, relativePath);
        if (!Files.exists(fullPath)) return null;
        try {
            if (width > 0 && height > 0) {
                return new Image(fullPath.toUri().toString(), width, height, true, true);
            } else {
                return new Image(fullPath.toUri().toString());
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "加载纹理失败: " + relativePath, e);
            return null;
        }
    }

    // 辅助方法：检测尺寸是否合法
    private static boolean isValidSize(int size) {
        for (int s : ALLOWED_SIZES) if (s == size) return true;
        return false;
    }

    // 获取最接近的合法尺寸
    private static int getClosestValidSize(int size) {
        int closest = 16;
        for (int s : ALLOWED_SIZES) {
            if (Math.abs(s - size) < Math.abs(closest - size)) closest = s;
        }
        return closest;
    }

    // 缩放图片
    private static BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }
}