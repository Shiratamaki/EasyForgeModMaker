package com.easyforge.ai;

import com.easyforge.model.AIMessage;
import com.easyforge.util.LogUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AIService {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void sendChat(List<AIMessage> messages, Consumer<String> onSuccess, Consumer<String> onError) {
        String apiKey = AIConfig.getApiKey();
        if (apiKey.isEmpty()) {
            onError.accept("请先在设置中配置 API Key");
            return;
        }

        String endpoint = AIConfig.getEndpoint();
        String model = AIConfig.getModel();
        AIConfig.Provider provider = AIConfig.getProvider();

        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);
        ArrayNode msgArray = mapper.createArrayNode();
        for (AIMessage msg : messages) {
            ObjectNode node = mapper.createObjectNode();
            node.put("role", msg.getRole());
            node.put("content", msg.getContent());
            msgArray.add(node);
        }
        root.set("messages", msgArray);
        root.put("stream", false);

        String jsonBody;
        try {
            jsonBody = mapper.writeValueAsString(root);
        } catch (IOException e) {
            LogUtil.error("构建AI请求失败", e);
            onError.accept("构建请求失败: " + e.getMessage());
            return;
        }

        Request.Builder reqBuilder = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(MediaType.parse("application/json"), jsonBody));

        switch (provider) {
            case OPENAI:
            case DEEPSEEK:
            case ZHIPU:
                reqBuilder.addHeader("Authorization", "Bearer " + apiKey);
                break;
            case CLAUDE:
                reqBuilder.addHeader("x-api-key", apiKey);
                reqBuilder.addHeader("anthropic-version", "2023-06-01");
                break;
        }

        Request request = reqBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.error("AI网络请求失败", e);
                onError.accept("网络错误: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody body = response.body()) {
                    if (!response.isSuccessful()) {
                        String errBody = body != null ? body.string() : "";
                        if (response.code() == 402) {
                            onError.accept("❌ 账户余额不足，请充值 DeepSeek 或切换至免费模型（智谱 AI）。");
                        } else {
                            LogUtil.error("API错误 (" + response.code() + "): " + errBody);
                            onError.accept("API 错误 (" + response.code() + "): " + errBody);
                        }
                        return;
                    }
                    String respBody = body.string();
                    try {
                        JsonNode json = mapper.readTree(respBody);
                        String reply;
                        if (provider == AIConfig.Provider.CLAUDE) {
                            reply = json.get("content").get(0).get("text").asText();
                        } else {
                            reply = json.get("choices").get(0).get("message").get("content").asText();
                        }
                        onSuccess.accept(reply);
                    } catch (Exception e) {
                        LogUtil.error("解析AI响应失败", e);
                        onError.accept("解析响应失败: " + e.getMessage());
                    }
                }
            }
        });
    }

    public static void sendChatWithImage(String userText, String imageBase64, String mimeType,
                                         Consumer<String> onSuccess, Consumer<String> onError) {
        String apiKey = AIConfig.getApiKey();
        if (apiKey.isEmpty()) {
            onError.accept("请先在设置中配置 API Key");
            return;
        }

        AIConfig.Provider provider = AIConfig.getProvider();
        if (provider != AIConfig.Provider.DEEPSEEK && provider != AIConfig.Provider.OPENAI) {
            onError.accept("当前模型不支持图片输入，请切换至 DeepSeek 或 OpenAI");
            return;
        }

        String endpoint = AIConfig.getEndpoint();
        String model = AIConfig.getModel();

        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);
        ArrayNode messages = mapper.createArrayNode();
        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        ArrayNode contentArray = mapper.createArrayNode();

        ObjectNode textPart = mapper.createObjectNode();
        textPart.put("type", "text");
        textPart.put("text", userText);
        contentArray.add(textPart);

        ObjectNode imagePart = mapper.createObjectNode();
        imagePart.put("type", "image_url");
        ObjectNode imageUrl = mapper.createObjectNode();
        imageUrl.put("url", "data:" + mimeType + ";base64," + imageBase64);
        imagePart.set("image_url", imageUrl);
        contentArray.add(imagePart);

        userMsg.set("content", contentArray);
        messages.add(userMsg);
        root.set("messages", messages);
        root.put("stream", false);

        String jsonBody;
        try {
            jsonBody = mapper.writeValueAsString(root);
        } catch (IOException e) {
            LogUtil.error("构建带图片的AI请求失败", e);
            onError.accept("构建请求失败: " + e.getMessage());
            return;
        }

        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.error("AI图片请求网络失败", e);
                onError.accept("网络错误: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody body = response.body()) {
                    if (!response.isSuccessful()) {
                        String errBody = body != null ? body.string() : "";
                        if (response.code() == 402) {
                            onError.accept("❌ 账户余额不足，请充值 DeepSeek 或切换至免费模型（智谱 AI）。");
                        } else {
                            LogUtil.error("AI图片API错误: " + errBody);
                            onError.accept("API 错误: " + errBody);
                        }
                        return;
                    }
                    String respBody = body.string();
                    try {
                        JsonNode json = mapper.readTree(respBody);
                        String reply = json.get("choices").get(0).get("message").get("content").asText();
                        onSuccess.accept(reply);
                    } catch (Exception e) {
                        LogUtil.error("解析AI图片响应失败", e);
                        onError.accept("解析响应失败: " + e.getMessage());
                    }
                }
            }
        });
    }
}