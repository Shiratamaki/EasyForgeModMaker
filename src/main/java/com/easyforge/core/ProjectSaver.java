package com.easyforge.core;

import com.easyforge.model.ModProject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ProjectSaver {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * 保存项目（使用流式写入，减少内存占用）
     */
    public static void save(ModProject project) throws IOException {
        Path saveFile = Path.of(project.getOutputPath(), ".easyforge");
        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(saveFile.toFile()), StandardCharsets.UTF_8))) {
            writer.setIndent("  ");
            GSON.toJson(project, ModProject.class, writer);
        }
    }

    /**
     * 加载项目（使用流式读取，边读边解析）
     */
    public static ModProject load(Path projectDir) throws IOException {
        Path saveFile = projectDir.resolve(".easyforge");
        if (!saveFile.toFile().exists()) {
            return null;
        }
        try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(saveFile.toFile()), StandardCharsets.UTF_8))) {
            return GSON.fromJson(reader, ModProject.class);
        }
    }
}