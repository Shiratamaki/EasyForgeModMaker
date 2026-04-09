package com.easyforge.core;

import com.easyforge.model.BlockData;
import com.easyforge.model.ItemData;
import com.easyforge.model.ModProject;
import com.easyforge.model.RecipeData;
import com.easyforge.util.LogUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static ModProject parseFromDirectory(File projectDir) throws IOException {
        ModProject project = new ModProject();
        project.setOutputPath(projectDir.getAbsolutePath());

        try {
            parseBuildGradle(projectDir, project);
            List<ItemData> items = parseModItems(projectDir, project.getModId());
            project.setItems(items);
            List<BlockData> blocks = parseModBlocks(projectDir, project.getModId());
            project.setBlocks(blocks);
            Map<String, String> langMap = parseLangFile(projectDir, project.getModId());
            for (ItemData item : items) {
                String key = "item." + project.getModId() + "." + item.getId();
                if (langMap.containsKey(key)) item.setDisplayName(langMap.get(key));
            }
            for (BlockData block : blocks) {
                String key = "block." + project.getModId() + "." + block.getId();
                if (langMap.containsKey(key)) block.setDisplayName(langMap.get(key));
            }
            List<RecipeData> recipes = parseRecipes(projectDir, project.getModId());
            project.setRecipes(recipes);
        } catch (IOException e) {
            LogUtil.error("解析项目目录失败: " + projectDir, e);
            throw e;
        }
        return project;
    }

    private static void parseBuildGradle(File projectDir, ModProject project) throws IOException {
        File buildGradle = new File(projectDir, "build.gradle");
        if (!buildGradle.exists()) return;
        String content = new String(Files.readAllBytes(buildGradle.toPath()));
        Pattern modIdPattern = Pattern.compile("archivesBaseName\\s*=\\s*'([^']+)'");
        Matcher m = modIdPattern.matcher(content);
        if (m.find()) project.setModId(m.group(1));
        Pattern groupPattern = Pattern.compile("group\\s*=\\s*'([^']+)'");
        m = groupPattern.matcher(content);
        if (m.find()) project.setMainClassPackage(m.group(1));
        Pattern versionPattern = Pattern.compile("version\\s*=\\s*'([^']+)'");
        m = versionPattern.matcher(content);
        if (m.find()) project.setVersion(m.group(1));
        File gradleProps = new File(projectDir, "gradle.properties");
        if (gradleProps.exists()) {
            Properties props = new Properties();
            props.load(Files.newInputStream(gradleProps.toPath()));
            project.setMinecraftVersion(props.getProperty("minecraft_version", "1.20.1"));
            project.setForgeVersion(props.getProperty("forge_version", "47.1.0"));
            project.setModName(props.getProperty("mod_name", project.getModId()));
            project.setAuthor(props.getProperty("mod_author", "Unknown"));
        }
    }

    private static List<ItemData> parseModItems(File projectDir, String modId) throws IOException {
        List<ItemData> items = new ArrayList<>();
        if (modId == null) return items;
        String packagePath = modId.replace('_', '.');
        File modItemsFile = new File(projectDir, "src/main/java/" + packagePath.replace('.', '/') + "/ModItems.java");
        if (!modItemsFile.exists()) return items;
        String content = new String(Files.readAllBytes(modItemsFile.toPath()));
        Pattern pattern = Pattern.compile("public static final RegistryObject<Item> (\\w+) = ITEMS\\.register\\(\"([^\"]+)\",[^;]+stacksTo\\((\\d+)\\)");
        Matcher m = pattern.matcher(content);
        while (m.find()) {
            ItemData item = new ItemData();
            item.setId(m.group(2));
            item.setMaxStackSize(Integer.parseInt(m.group(3)));
            items.add(item);
        }
        return items;
    }

    private static List<BlockData> parseModBlocks(File projectDir, String modId) throws IOException {
        List<BlockData> blocks = new ArrayList<>();
        if (modId == null) return blocks;
        String packagePath = modId.replace('_', '.');
        File modBlocksFile = new File(projectDir, "src/main/java/" + packagePath.replace('.', '/') + "/ModBlocks.java");
        if (!modBlocksFile.exists()) return blocks;
        String content = new String(Files.readAllBytes(modBlocksFile.toPath()));
        Pattern pattern = Pattern.compile("public static final RegistryObject<Block> (\\w+) = BLOCKS\\.register\\(\"([^\"]+)\",[^;]+Material\\.(\\w+)[^;]+strength\\(([\\d.]+)f?\\)");
        Matcher m = pattern.matcher(content);
        while (m.find()) {
            BlockData block = new BlockData();
            block.setId(m.group(2));
            block.setMaterial(m.group(3));
            block.setHardness(Float.parseFloat(m.group(4)));
            blocks.add(block);
        }
        return blocks;
    }

    private static Map<String, String> parseLangFile(File projectDir, String modId) throws IOException {
        if (modId == null) return Collections.emptyMap();
        File langFile = new File(projectDir, "src/main/resources/assets/" + modId + "/lang/en_us.json");
        if (!langFile.exists()) return Collections.emptyMap();
        JsonNode root = mapper.readTree(langFile);
        Map<String, String> map = new HashMap<>();
        root.fields().forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asText()));
        return map;
    }

    private static List<RecipeData> parseRecipes(File projectDir, String modId) throws IOException {
        List<RecipeData> recipes = new ArrayList<>();
        if (modId == null) return recipes;
        File recipesDir = new File(projectDir, "src/main/resources/data/" + modId + "/recipes");
        if (!recipesDir.exists() || !recipesDir.isDirectory()) return recipes;
        File[] recipeFiles = recipesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (recipeFiles == null) return recipes;
        for (File recipeFile : recipeFiles) {
            try {
                JsonNode root = mapper.readTree(recipeFile);
                RecipeData recipe = new RecipeData();
                if (root.has("type")) {
                    String type = root.get("type").asText();
                    recipe.setShaped(type.contains("shaped"));
                }
                if (root.has("result")) {
                    JsonNode result = root.get("result");
                    recipe.setOutputItem(result.get("item").asText());
                    recipe.setOutputCount(result.get("count").asInt(1));
                }
                if (recipe.isShaped() && root.has("pattern")) {
                    JsonNode pattern = root.get("pattern");
                    String[] shape = new String[pattern.size()];
                    for (int i = 0; i < pattern.size(); i++) {
                        shape[i] = pattern.get(i).asText();
                    }
                    recipe.setShape(shape);
                }
                if (root.has("key")) {
                    JsonNode keyNode = root.get("key");
                    Map<Character, String> keys = new HashMap<>();
                    keyNode.fields().forEachRemaining(entry -> {
                        char key = entry.getKey().charAt(0);
                        String value = entry.getValue().get("item").asText();
                        keys.put(key, value);
                    });
                    recipe.setKeys(keys);
                }
                recipes.add(recipe);
            } catch (Exception e) {
                LogUtil.error("解析配方文件失败: " + recipeFile.getName(), e);
            }
        }
        return recipes;
    }
}