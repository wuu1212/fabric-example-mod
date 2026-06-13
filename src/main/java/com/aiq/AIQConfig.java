package com.aiq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

public class AIQConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("aiq-mod");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private static File configFile;
    private static ConfigData data;

    public static class ConfigData {
        @Expose public String apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
        @Expose public String apiKey = "";
        @Expose public String model = "qwen3-flash-plus";
        @Expose public String systemPrompt = "你是一个 Minecraft 助手，名字叫千问。请用简洁、友善的中文回答玩家问题，帮助玩家查询游戏相关问题。";
        @Expose public int maxTokens = 500;
        @Expose public int defaultPoints = 100;
        @Expose public int costNormal = 2;
        @Expose public int costThink = 20;
        @Expose public int costSearch = 5;
        @Expose public int cooldownSeconds = 3;
        @Expose public int connectTimeoutSeconds = 15;
        @Expose public int readTimeoutSeconds = 60;
    }

    public static void init() {
        configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "aiq-mod.json");
        load();
    }

    public static synchronized void load() {
        try {
            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
                    ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                    if (loaded != null) {
                        data = loaded;
                    } else {
                        data = new ConfigData();
                    }
                }
            } else {
                data = new ConfigData();
                save();
            }
        } catch (Exception e) {
            LOGGER.error("加载配置文件失败，使用默认配置", e);
            data = new ConfigData();
        }
        if (data.apiUrl == null || data.apiUrl.isEmpty()) data.apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
        if (data.model == null || data.model.isEmpty()) data.model = "qwen3-flash-plus";
        if (data.systemPrompt == null || data.systemPrompt.isEmpty()) data.systemPrompt = "你是一个 Minecraft 助手，名字叫千问。";
    }

    public static synchronized void save() {
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            LOGGER.error("保存配置文件失败", e);
        }
    }

    public static synchronized ConfigData get() {
        return data;
    }

    public static synchronized void setApiUrl(String url) {
        data.apiUrl = url;
        save();
    }

    public static synchronized void setApiKey(String key) {
        data.apiKey = key;
        save();
    }

    public static synchronized void setModel(String model) {
        data.model = model;
        save();
    }

    public static synchronized void setSystemPrompt(String prompt) {
        data.systemPrompt = prompt;
        save();
    }

    public static synchronized void setMaxTokens(int tokens) {
        data.maxTokens = Math.max(1, Math.min(tokens, 8192);
        save();
    }
}
