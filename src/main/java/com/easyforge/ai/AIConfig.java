package com.easyforge.ai;

import com.easyforge.util.EncryptionUtil;

import java.util.prefs.Preferences;

public class AIConfig {
    private static final Preferences prefs = Preferences.userNodeForPackage(AIConfig.class);
    private static final String KEY_API_KEY_ENCRYPTED = "ai.apiKeyEncrypted";
    private static final String KEY_MODEL = "ai.model";
    private static final String KEY_ENDPOINT = "ai.endpoint";
    private static final String KEY_PROVIDER = "ai.provider";

    public enum Provider {
        DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1/chat/completions", "deepseek-chat"),
        OPENAI("OpenAI", "https://api.openai.com/v1/chat/completions", "gpt-4o-mini"),
        CLAUDE("Claude (via proxy)", "https://api.anthropic.com/v1/messages", "claude-3-haiku-20240307"),
        ZHIPU("智谱 AI", "https://open.bigmodel.cn/api/paas/v4/chat/completions", "glm-4-flash");

        private final String displayName;
        private final String defaultEndpoint;
        private final String defaultModel;

        Provider(String displayName, String defaultEndpoint, String defaultModel) {
            this.displayName = displayName;
            this.defaultEndpoint = defaultEndpoint;
            this.defaultModel = defaultModel;
        }

        public String getDisplayName() { return displayName; }
        public String getDefaultEndpoint() { return defaultEndpoint; }
        public String getDefaultModel() { return defaultModel; }
    }

    public static String getApiKey() {
        String encrypted = prefs.get(KEY_API_KEY_ENCRYPTED, "");
        if (encrypted.isEmpty()) return "";
        try {
            return EncryptionUtil.decrypt(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void setApiKey(String apiKey) {
        try {
            String encrypted = EncryptionUtil.encrypt(apiKey);
            prefs.put(KEY_API_KEY_ENCRYPTED, encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getModel() {
        return prefs.get(KEY_MODEL, Provider.DEEPSEEK.getDefaultModel());
    }

    public static void setModel(String model) {
        prefs.put(KEY_MODEL, model);
    }

    public static String getEndpoint() {
        return prefs.get(KEY_ENDPOINT, Provider.DEEPSEEK.getDefaultEndpoint());
    }

    public static void setEndpoint(String endpoint) {
        prefs.put(KEY_ENDPOINT, endpoint);
    }

    public static Provider getProvider() {
        String name = prefs.get(KEY_PROVIDER, Provider.DEEPSEEK.name());
        try {
            return Provider.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Provider.DEEPSEEK;
        }
    }

    public static void setProvider(Provider provider) {
        prefs.put(KEY_PROVIDER, provider.name());
        setEndpoint(provider.getDefaultEndpoint());
        setModel(provider.getDefaultModel());
    }
}