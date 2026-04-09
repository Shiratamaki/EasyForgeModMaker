package com.easyforge.core.generator;

import com.easyforge.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ProjectGenerator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private ModProject project;
    private Path rootDir;
    private String modId;
    private String packagePath;

    /**
     * 根据参数 Map 生成新项目（用于新建项目向导）
     */
    public void generate(Map<String, String> params) throws IOException {
        String modId = params.get("modId");
        String modName = params.get("modName");
        String author = params.get("author");
        String mcVersion = params.get("minecraftVersion");
        String forgeVersion = params.get("forgeVersion");
        String projectPath = params.get("projectPath");

        this.project = new ModProject();
        project.setModId(modId);
        project.setModName(modName);
        project.setAuthor(author);
        project.setMinecraftVersion(mcVersion);
        project.setForgeVersion(forgeVersion);
        project.setOutputPath(projectPath);
        project.setMainClassPackage(modId.replace('_', '.'));

        this.modId = modId;
        this.rootDir = Paths.get(projectPath);
        this.packagePath = project.getMainClassPackage().replace('.', '/');

        createDirectories();
        generateBuildGradle();
        generateModsToml();
        generateMainClass();
        // 可继续生成其他必要文件
    }

    /**
     * 根据已有的 ModProject 对象完整生成项目（用于保存后重新生成）
     */
    public void generateFullFromProject(ModProject project) throws IOException {
        this.project = project;
        this.modId = project.getModId();
        this.rootDir = Paths.get(project.getOutputPath());
        this.packagePath = project.getMainClassPackage().replace('.', '/');

        createDirectories();
        generateBuildGradle();
        generateModsToml();
        generateMainClass();
        generateRegistries();
        generateItemJsons();
        generateBlockJsons();
        generateRecipeJsons();
        generateLanguageFiles();
        generatePlaceholderTextures();
    }

    private void createDirectories() throws IOException {
        Files.createDirectories(rootDir.resolve("src/main/java/" + packagePath));
        Files.createDirectories(rootDir.resolve("src/main/resources/assets/" + modId + "/blockstates"));
        Files.createDirectories(rootDir.resolve("src/main/resources/assets/" + modId + "/models/block"));
        Files.createDirectories(rootDir.resolve("src/main/resources/assets/" + modId + "/models/item"));
        Files.createDirectories(rootDir.resolve("src/main/resources/assets/" + modId + "/textures/block"));
        Files.createDirectories(rootDir.resolve("src/main/resources/assets/" + modId + "/textures/item"));
        Files.createDirectories(rootDir.resolve("src/main/resources/assets/" + modId + "/lang"));
        Files.createDirectories(rootDir.resolve("src/main/resources/data/" + modId + "/recipes"));
        Files.createDirectories(rootDir.resolve("src/main/resources/data/" + modId + "/advancements"));
        Files.createDirectories(rootDir.resolve("src/main/resources/data/" + modId + "/loot_tables/blocks"));
        Files.createDirectories(rootDir.resolve("src/main/resources/data/" + modId + "/tags/items"));
        Files.createDirectories(rootDir.resolve("src/main/resources/data/" + modId + "/tags/blocks"));
        Files.createDirectories(rootDir.resolve("src/main/resources/META-INF"));
    }

    private void generateBuildGradle() throws IOException {
        String content =
                "plugins {\n" +
                        "    id 'java'\n" +
                        "    id 'net.minecraftforge.gradle' version '6.0.+'\n" +
                        "}\n" +
                        "\n" +
                        "group = '" + project.getMainClassPackage() + "'\n" +
                        "version = '1.0.0'\n" +
                        "\n" +
                        "minecraft {\n" +
                        "    mappings channel: 'official', version: '" + project.getMinecraftVersion() + "'\n" +
                        "    runs {\n" +
                        "        client {\n" +
                        "            workingDirectory project.file('run')\n" +
                        "            property 'forge.logging.mojang_level', 'debug'\n" +
                        "            mods {\n" +
                        "                " + modId + " {\n" +
                        "                    source sourceSets.main\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        server {\n" +
                        "            workingDirectory project.file('run')\n" +
                        "            property 'forge.logging.mojang_level', 'debug'\n" +
                        "            mods {\n" +
                        "                " + modId + " {\n" +
                        "                    source sourceSets.main\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "    minecraft 'net.minecraftforge:forge:" + project.getMinecraftVersion() + "-" + project.getForgeVersion() + "'\n" +
                        "}\n" +
                        "\n" +
                        "jar {\n" +
                        "    manifest {\n" +
                        "        attributes([\n" +
                        "            \"Specification-Title\": \"" + project.getModName() + "\",\n" +
                        "            \"Specification-Vendor\": \"" + project.getAuthor() + "\",\n" +
                        "            \"Specification-Version\": \"1\",\n" +
                        "            \"Implementation-Title\": project.name,\n" +
                        "            \"Implementation-Version\": project.version,\n" +
                        "            \"Implementation-Vendor\": \"" + project.getAuthor() + "\",\n" +
                        "            \"Implementation-Timestamp\": new Date().format(\"yyyy-MM-dd'T'HH:mm:ssZ\")\n" +
                        "        ])\n" +
                        "    }\n" +
                        "}\n";
        writeFile(rootDir.resolve("build.gradle"), content);
    }

    private void generateModsToml() throws IOException {
        String content =
                "modLoader=\"javafml\"\n" +
                        "loaderVersion=\"[" + project.getForgeVersion().split("\\.")[0] + ",)\"\n" +
                        "license=\"All rights reserved\"\n" +
                        "\n" +
                        "[[mods]]\n" +
                        "modId=\"" + modId + "\"\n" +
                        "version=\"1.0.0\"\n" +
                        "displayName=\"" + project.getModName() + "\"\n" +
                        "logoFile=\"logo.png\"\n" +
                        "credits=\"\"\n" +
                        "authors=\"" + project.getAuthor() + "\"\n" +
                        "description='A Minecraft mod generated by EasyForgeModMaker.'\n" +
                        "\n" +
                        "[[dependencies." + modId + "]]\n" +
                        "    modId=\"forge\"\n" +
                        "    mandatory=true\n" +
                        "    versionRange=\"[" + project.getForgeVersion().split("\\.")[0] + ",)\"\n" +
                        "    ordering=\"NONE\"\n" +
                        "    side=\"BOTH\"\n" +
                        "\n" +
                        "[[dependencies." + modId + "]]\n" +
                        "    modId=\"minecraft\"\n" +
                        "    mandatory=true\n" +
                        "    versionRange=\"[" + project.getMinecraftVersion() + ",1.21)\"\n" +
                        "    ordering=\"NONE\"\n" +
                        "    side=\"BOTH\"\n";
        writeFile(rootDir.resolve("src/main/resources/META-INF/mods.toml"), content);
    }

    private void generateMainClass() throws IOException {
        String className = modId.substring(0, 1).toUpperCase() + modId.substring(1) + "Mod";
        String content =
                "package " + project.getMainClassPackage() + ";\n\n" +
                        "import net.minecraftforge.eventbus.api.IEventBus;\n" +
                        "import net.minecraftforge.fml.common.Mod;\n" +
                        "import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;\n\n" +
                        "@Mod(\"" + modId + "\")\n" +
                        "public class " + className + " {\n\n" +
                        "    public " + className + "() {\n" +
                        "        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();\n" +
                        "        ModItems.register(bus);\n" +
                        "        ModBlocks.register(bus);\n" +
                        "    }\n" +
                        "}\n";
        writeFile(rootDir.resolve("src/main/java/" + packagePath + "/" + className + ".java"), content);
    }

    private void generateRegistries() throws IOException {
        // ModItems.java
        if (!project.getItems().isEmpty()) {
            StringBuilder itemsContent = new StringBuilder();
            itemsContent.append("package " + project.getMainClassPackage() + ";\n\n");
            itemsContent.append("import net.minecraft.world.item.Item;\n");
            itemsContent.append("import net.minecraftforge.eventbus.api.IEventBus;\n");
            itemsContent.append("import net.minecraftforge.registries.DeferredRegister;\n");
            itemsContent.append("import net.minecraftforge.registries.ForgeRegistries;\n");
            itemsContent.append("import net.minecraftforge.registries.RegistryObject;\n\n");
            itemsContent.append("public class ModItems {\n");
            itemsContent.append("    public static final DeferredRegister<Item> ITEMS =\n");
            itemsContent.append("            DeferredRegister.create(ForgeRegistries.ITEMS, \"" + modId + "\");\n\n");
            for (ItemData item : project.getItems()) {
                itemsContent.append("    public static final RegistryObject<Item> " + item.getId().toUpperCase() + " = ITEMS.register(\"" + item.getId() + "\",\n");
                itemsContent.append("            () -> new Item(new Item.Properties()));\n\n");
            }
            itemsContent.append("    public static void register(IEventBus bus) {\n");
            itemsContent.append("        ITEMS.register(bus);\n");
            itemsContent.append("    }\n");
            itemsContent.append("}\n");
            writeFile(rootDir.resolve("src/main/java/" + packagePath + "/ModItems.java"), itemsContent.toString());
        }

        // ModBlocks.java
        if (!project.getBlocks().isEmpty()) {
            StringBuilder blocksContent = new StringBuilder();
            blocksContent.append("package " + project.getMainClassPackage() + ";\n\n");
            blocksContent.append("import net.minecraft.world.level.block.Block;\n");
            blocksContent.append("import net.minecraft.world.level.block.Blocks;\n");
            blocksContent.append("import net.minecraft.world.level.block.state.BlockBehaviour;\n");
            blocksContent.append("import net.minecraftforge.eventbus.api.IEventBus;\n");
            blocksContent.append("import net.minecraftforge.registries.DeferredRegister;\n");
            blocksContent.append("import net.minecraftforge.registries.ForgeRegistries;\n");
            blocksContent.append("import net.minecraftforge.registries.RegistryObject;\n\n");
            blocksContent.append("public class ModBlocks {\n");
            blocksContent.append("    public static final DeferredRegister<Block> BLOCKS =\n");
            blocksContent.append("            DeferredRegister.create(ForgeRegistries.BLOCKS, \"" + modId + "\");\n\n");
            for (BlockData block : project.getBlocks()) {
                blocksContent.append("    public static final RegistryObject<Block> " + block.getId().toUpperCase() + " = BLOCKS.register(\"" + block.getId() + "\",\n");
                blocksContent.append("            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)));\n\n");
            }
            blocksContent.append("    public static void register(IEventBus bus) {\n");
            blocksContent.append("        BLOCKS.register(bus);\n");
            blocksContent.append("    }\n");
            blocksContent.append("}\n");
            writeFile(rootDir.resolve("src/main/java/" + packagePath + "/ModBlocks.java"), blocksContent.toString());
        }
    }

    private void generateItemJsons() throws IOException {
        for (ItemData item : project.getItems()) {
            Map<String, Object> model = new HashMap<>();
            model.put("parent", "item/generated");
            Map<String, String> textures = new HashMap<>();
            String texturePath = item.getTexturePath();
            if (texturePath == null || texturePath.isEmpty()) {
                textures.put("layer0", modId + ":item/" + item.getId());
            } else {
                textures.put("layer0", texturePath.replace(".png", "").replace("assets/" + modId + "/textures/", ""));
            }
            model.put("textures", textures);
            writeFile(rootDir.resolve("src/main/resources/assets/" + modId + "/models/item/" + item.getId() + ".json"), GSON.toJson(model));
        }
    }

    private void generateBlockJsons() throws IOException {
        for (BlockData block : project.getBlocks()) {
            // 方块模型
            Map<String, Object> blockModel = new HashMap<>();
            blockModel.put("parent", "block/cube_all");
            Map<String, String> textures = new HashMap<>();
            String texturePath = block.getTexturePath();
            if (texturePath == null || texturePath.isEmpty()) {
                textures.put("all", modId + ":block/" + block.getId());
            } else {
                textures.put("all", texturePath.replace(".png", "").replace("assets/" + modId + "/textures/", ""));
            }
            blockModel.put("textures", textures);
            writeFile(rootDir.resolve("src/main/resources/assets/" + modId + "/models/block/" + block.getId() + ".json"), GSON.toJson(blockModel));

            // 物品模型
            Map<String, Object> itemModel = new HashMap<>();
            itemModel.put("parent", modId + ":block/" + block.getId());
            writeFile(rootDir.resolve("src/main/resources/assets/" + modId + "/models/item/" + block.getId() + ".json"), GSON.toJson(itemModel));

            // 方块状态
            Map<String, Object> state = new HashMap<>();
            Map<String, Object> variants = new HashMap<>();
            Map<String, Object> empty = new HashMap<>();
            empty.put("model", modId + ":block/" + block.getId());
            variants.put("", empty);
            state.put("variants", variants);
            writeFile(rootDir.resolve("src/main/resources/assets/" + modId + "/blockstates/" + block.getId() + ".json"), GSON.toJson(state));
        }
    }

    private void generateRecipeJsons() throws IOException {
        for (RecipeData recipe : project.getRecipes()) {
            Map<String, Object> json = new HashMap<>();
            json.put("type", recipe.isShaped() ? "minecraft:crafting_shaped" : "minecraft:crafting_shapeless");
            if (recipe.isShaped() && recipe.getShape() != null) {
                json.put("pattern", recipe.getShape());
                Map<String, Object> key = new HashMap<>();
                for (Map.Entry<Character, String> entry : recipe.getKeys().entrySet()) {
                    Map<String, Object> itemObj = new HashMap<>();
                    itemObj.put("item", entry.getValue());
                    key.put(String.valueOf(entry.getKey()), itemObj);
                }
                json.put("key", key);
            } else {
                List<Object> ingredients = new ArrayList<>();
                for (String itemId : recipe.getKeys().values()) {
                    Map<String, Object> ing = new HashMap<>();
                    ing.put("item", itemId);
                    ingredients.add(ing);
                }
                json.put("ingredients", ingredients);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("item", recipe.getOutputItem());
            result.put("count", recipe.getOutputCount());
            json.put("result", result);
            String fileName = recipe.getOutputItem().replace(":", "_") + ".json";
            writeFile(rootDir.resolve("src/main/resources/data/" + modId + "/recipes/" + fileName), GSON.toJson(json));
        }
    }

    private void generateLanguageFiles() throws IOException {
        Map<String, String> lang = new LinkedHashMap<>();
        for (ItemData item : project.getItems()) {
            lang.put("item." + modId + "." + item.getId(), item.getDisplayName());
        }
        for (BlockData block : project.getBlocks()) {
            lang.put("block." + modId + "." + block.getId(), block.getDisplayName());
        }
        writeFile(rootDir.resolve("src/main/resources/assets/" + modId + "/lang/en_us.json"), GSON.toJson(lang));
    }

    private void generatePlaceholderTextures() throws IOException {
        for (ItemData item : project.getItems()) {
            if (item.getTexturePath() == null || item.getTexturePath().isEmpty()) {
                Path textureFile = rootDir.resolve("src/main/resources/assets/" + modId + "/textures/item/" + item.getId() + ".png");
                if (!Files.exists(textureFile)) {
                    generateSimpleTexture(textureFile, item.getId().substring(0, 1).toUpperCase());
                }
            }
        }
        for (BlockData block : project.getBlocks()) {
            if (block.getTexturePath() == null || block.getTexturePath().isEmpty()) {
                Path textureFile = rootDir.resolve("src/main/resources/assets/" + modId + "/textures/block/" + block.getId() + ".png");
                if (!Files.exists(textureFile)) {
                    generateSimpleTexture(textureFile, block.getId().substring(0, 1).toUpperCase());
                }
            }
        }
    }

    private void generateSimpleTexture(Path outputFile, String text) throws IOException {
        Files.createDirectories(outputFile.getParent());
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.GRAY);
        g.fillRect(0, 0, 16, 16);
        g.setColor(java.awt.Color.WHITE);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        g.drawString(text, 4, 12);
        g.dispose();
        javax.imageio.ImageIO.write(img, "png", outputFile.toFile());
    }

    private void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, content.getBytes());
    }
}