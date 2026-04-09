package com.easyforge.template;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 项目模板管理器 - 支持从 classpath 复制模板到目标目录，并替换占位符、自动生成纹理
 */
public class ProjectTemplateManager {

    private static final String TEMPLATES_ROOT = "/templates/project_templates/";

    /**
     * 应用模板到目标目录
     * @param templateName 模板名称（如 "basic_items" 或 "basic_blocks"）
     * @param targetDir 目标项目根目录
     * @param placeholders 占位符映射（如 {{modId}} -> "my_mod"）
     * @throws IOException 复制或生成文件失败时抛出
     */
    public static void applyTemplate(String templateName, Path targetDir, Map<String, String> placeholders) throws IOException {
        URL templateUrl = ProjectTemplateManager.class.getResource(TEMPLATES_ROOT + templateName);
        if (templateUrl == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }

        Files.createDirectories(targetDir);
        copyTemplateResources(templateUrl, targetDir, placeholders);
        generatePlaceholderTextures(targetDir, placeholders);
    }

    /**
     * 递归复制模板资源，并替换文本文件中的占位符
     */
    private static void copyTemplateResources(URL source, Path target, Map<String, String> placeholders) throws IOException {
        try {
            if (source.getProtocol().equals("file")) {
                Path sourcePath = Paths.get(source.toURI());
                copyRecursively(sourcePath, target, placeholders);
            } else if (source.getProtocol().equals("jar")) {
                try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + source.toURI()), Map.of("create", "false"))) {
                    Path root = fs.getPath(TEMPLATES_ROOT + templateNameFromUrl(source));
                    copyRecursively(root, target, placeholders);
                }
            } else {
                throw new IOException("Unsupported resource protocol: " + source.getProtocol());
            }
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI syntax for resource: " + source, e);
        }
    }

    private static String templateNameFromUrl(URL url) {
        String path = url.getPath();
        int idx = path.lastIndexOf("/templates/project_templates/");
        if (idx >= 0) {
            return path.substring(idx + "/templates/project_templates/".length());
        }
        return "";
    }

    private static void copyRecursively(Path source, Path target, Map<String, String> placeholders) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            for (Path path : (Iterable<Path>) stream::iterator) {
                Path relative = source.relativize(path);
                Path dest = target.resolve(relative.toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    byte[] bytes = Files.readAllBytes(path);
                    String content = new String(bytes);
                    if (isTextFile(path)) {
                        content = replacePlaceholders(content, placeholders);
                        Files.write(dest, content.getBytes());
                    } else {
                        Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private static boolean isTextFile(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".java") || name.endsWith(".json") || name.endsWith(".toml") ||
                name.endsWith(".gradle") || name.endsWith(".txt") || name.endsWith(".md");
    }

    private static String replacePlaceholders(String content, Map<String, String> placeholders) {
        String result = content;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static void generatePlaceholderTextures(Path projectDir, Map<String, String> placeholders) throws IOException {
        String modId = placeholders.get("{{modId}}");
        if (modId == null) modId = "example_mod";

        Path itemTextureDir = projectDir.resolve("src/main/resources/assets/" + modId + "/textures/item");
        Path blockTextureDir = projectDir.resolve("src/main/resources/assets/" + modId + "/textures/block");

        if (Files.exists(itemTextureDir)) {
            generateSimpleTexture(itemTextureDir.resolve("example_item.png"), "I", new Color(150, 150, 150));
        }
        if (Files.exists(blockTextureDir)) {
            generateSimpleTexture(blockTextureDir.resolve("example_block.png"), "B", new Color(120, 120, 120));
        }
    }

    private static void generateSimpleTexture(Path outputFile, String text, Color bgColor) throws IOException {
        if (Files.exists(outputFile)) {
            return;
        }
        Files.createDirectories(outputFile.getParent());
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(text, 4, 12);
        g.dispose();
        ImageIO.write(img, "png", outputFile.toFile());
    }
}