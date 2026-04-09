package com.easyforge.controller;

import com.easyforge.ai.AIConfig;
import com.easyforge.ai.AIService;
import com.easyforge.model.AIMessage;
import com.easyforge.util.LogUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class AIChatController {

    @FXML private ComboBox<AIConfig.Provider> providerCombo;
    @FXML private PasswordField apiKeyField;
    @FXML private Button saveConfigBtn;
    @FXML private Button testConnectionBtn;
    @FXML private Button helpBtn;
    @FXML private ListView<AIMessage> chatListView;
    @FXML private Button uploadImageBtn;
    @FXML private TextField messageField;
    @FXML private Button sendBtn;
    @FXML private Label imageStatusLabel;

    private ObservableList<AIMessage> messages = FXCollections.observableArrayList();
    private String pendingImageBase64 = null;
    private String pendingImageMime = null;

    @FXML
    public void initialize() {
        providerCombo.getItems().setAll(AIConfig.Provider.values());
        providerCombo.setValue(AIConfig.getProvider());
        apiKeyField.setText(AIConfig.getApiKey());

        chatListView.setItems(messages);
        chatListView.setCellFactory(lv -> new ListCell<AIMessage>() {
            @Override
            protected void updateItem(AIMessage msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                } else {
                    setText(msg.getTimestamp() + " [" + (msg.getRole().equals("user") ? "我" : "AI") + "]\n" + msg.getContent());
                    setWrapText(true);
                }
            }
        });

        saveConfigBtn.setOnAction(e -> saveConfig());
        testConnectionBtn.setOnAction(e -> testConnection());
        helpBtn.setOnAction(e -> showHelpDialog());
        uploadImageBtn.setOnAction(e -> uploadImage());
        sendBtn.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());
    }

    private void saveConfig() {
        AIConfig.setProvider(providerCombo.getValue());
        AIConfig.setApiKey(apiKeyField.getText());
        showAlert("配置已保存", "API Key 和模型已更新");
    }

    private void testConnection() {
        String apiKey = apiKeyField.getText().trim();
        if (apiKey.isEmpty()) {
            showAlert("测试失败", "请先填写 API Key");
            return;
        }
        AIConfig.Provider provider = providerCombo.getValue();
        String originalKey = AIConfig.getApiKey();
        AIConfig.setApiKey(apiKey);
        AIConfig.setProvider(provider);

        java.util.List<AIMessage> testMessages = java.util.Collections.singletonList(new AIMessage("user", "ping"));
        AIService.sendChat(testMessages,
                reply -> Platform.runLater(() -> {
                    showAlert("测试成功", "API 连接正常！\nAI 回复: " + (reply.length() > 100 ? reply.substring(0, 100) + "..." : reply));
                    AIConfig.setApiKey(originalKey);
                }),
                error -> Platform.runLater(() -> {
                    showAlert("测试失败", "连接失败: " + error);
                    AIConfig.setApiKey(originalKey);
                }));
    }

    private void showHelpDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("如何获取 API Key");
        dialog.setHeaderText("📘 免费获取 DeepSeek API Key 教程");

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        Label step1 = new Label("1. 打开浏览器，访问 https://platform.deepseek.com/");
        Label step2 = new Label("2. 点击右上角「Sign Up」注册账号（支持邮箱或手机）");
        Label step3 = new Label("3. 注册成功后登录，进入控制台");
        Label step4 = new Label("4. 在左侧菜单找到「API Keys」，点击「Create API Key」");
        Label step5 = new Label("5. 输入一个名称（如 MyKey），点击创建");
        Label step6 = new Label("6. 复制生成的 API Key（格式为 sk-xxxxxx）并粘贴到上方输入框");
        Label step7 = new Label("7. 选择模型「DeepSeek」，点击「保存配置」，然后点击「测试连接」验证");
        Label tip = new Label("💡 提示：新用户注册即赠送免费额度，足够日常使用。");

        step1.setWrapText(true);
        step2.setWrapText(true);
        step3.setWrapText(true);
        step4.setWrapText(true);
        step5.setWrapText(true);
        step6.setWrapText(true);
        step7.setWrapText(true);
        tip.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");

        content.getChildren().addAll(step1, step2, step3, step4, step5, step6, step7, tip);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void uploadImage() {
        Stage stage = (Stage) uploadImageBtn.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择图片");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            try {
                BufferedImage img = ImageIO.read(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                String mime = "image/png";
                if (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg")) {
                    mime = "image/jpeg";
                    ImageIO.write(img, "jpg", baos);
                } else {
                    ImageIO.write(img, "png", baos);
                }
                pendingImageBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                pendingImageMime = mime;
                imageStatusLabel.setText("已附加图片: " + file.getName());
            } catch (IOException e) {
                LogUtil.error("读取图片失败", e);
                showAlert("错误", "读取图片失败: " + e.getMessage());
            }
        }
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty() && pendingImageBase64 == null) return;

        StringBuilder userContent = new StringBuilder(text);
        if (pendingImageBase64 != null) {
            userContent.append("\n[图片已附加]");
        }
        AIMessage userMsg = new AIMessage("user", userContent.toString());
        messages.add(userMsg);
        messageField.clear();

        boolean hasImage = (pendingImageBase64 != null);
        String imgBase64 = pendingImageBase64;
        String imgMime = pendingImageMime;
        pendingImageBase64 = null;
        pendingImageMime = null;
        imageStatusLabel.setText("");

        sendBtn.setDisable(true);

        if (hasImage && !text.isEmpty()) {
            AIService.sendChatWithImage(text, imgBase64, imgMime,
                    reply -> Platform.runLater(() -> {
                        messages.add(new AIMessage("assistant", reply));
                        sendBtn.setDisable(false);
                    }),
                    error -> Platform.runLater(() -> {
                        messages.add(new AIMessage("assistant", "错误: " + error));
                        sendBtn.setDisable(false);
                    }));
        } else {
            AIService.sendChat(messages,
                    reply -> Platform.runLater(() -> {
                        messages.add(new AIMessage("assistant", reply));
                        sendBtn.setDisable(false);
                    }),
                    error -> Platform.runLater(() -> {
                        messages.add(new AIMessage("assistant", "错误: " + error));
                        sendBtn.setDisable(false);
                    }));
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}