package com.easyforge.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 唯美樱花飘落背景 - 轻量级，专为樱色主题设计
 */
public class SakuraBackground extends StackPane {
    private final Canvas canvas;
    private final List<Petal> petals = new ArrayList<>();
    private final Random random = new Random();
    private final int petalCount; // 随机数量
    private AnimationTimer timer;

    public SakuraBackground() {
        canvas = new Canvas();
        canvas.setMouseTransparent(true);
        getChildren().add(canvas);
        setStyle("-fx-background-color: transparent;");

        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        widthProperty().addListener((obs, old, val) -> initPetals());
        heightProperty().addListener((obs, old, val) -> initPetals());

        // 随机生成 40~80 片花瓣
        petalCount = 40 + random.nextInt(41);
        initPetals();
        startAnimation();
    }

    private void initPetals() {
        petals.clear();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        for (int i = 0; i < petalCount; i++) {
            petals.add(new Petal(w, h));
        }
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            long last = 0;
            @Override
            public void handle(long now) {
                if (last == 0) { last = now; return; }
                double delta = (now - last) / 1e9;
                last = now;
                update(delta);
                draw();
            }
        };
        timer.start();
    }

    private void update(double delta) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        for (Petal p : petals) {
            p.update(w, h, delta);
        }
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Petal p : petals) {
            p.draw(gc);
        }
    }

    public void stop() {
        if (timer != null) timer.stop();
    }

    private class Petal {
        double x, y;
        double sizeX, sizeY;   // 椭圆长轴、短轴
        double speedY;         // 下落速度
        double speedX;         // 水平飘移
        double swing;          // 摆动相位
        double angle;          // 旋转角度
        double rotSpeed;       // 旋转速度
        Color color;           // 花瓣颜色（粉白渐变）

        Petal(double width, double height) {
            reset(width, height);
        }

        void reset(double width, double height) {
            x = random.nextDouble() * width;
            y = random.nextDouble() * height - 100; // 部分从上方开始
            sizeX = 6 + random.nextDouble() * 6;     // 宽度 6~12
            sizeY = sizeX * 0.7;                     // 高度略小，呈椭圆形
            speedY = 0.5 + random.nextDouble() * 1.0; // 下落速度 0.5~1.5 像素/帧
            speedX = -0.2 + random.nextDouble() * 0.4; // 水平飘移 -0.2~0.2
            swing = random.nextDouble() * Math.PI * 2;
            angle = random.nextDouble() * 360;
            rotSpeed = -0.5 + random.nextDouble() * 1.0; // 旋转速度
            // 颜色：粉白渐变，基于随机亮度
            double r = 1.0;
            double g = 0.7 + random.nextDouble() * 0.3;
            double b = 0.7 + random.nextDouble() * 0.3;
            double opacity = 0.6 + random.nextDouble() * 0.3;
            color = new Color(r, g, b, opacity);
        }

        void update(double width, double height, double delta) {
            // 使用 delta 时间控制速度（60fps 为基准）
            double factor = delta * 60;
            y += speedY * factor;
            x += (speedX + Math.sin(swing) * 0.2) * factor;
            angle += rotSpeed * factor;
            swing += 0.03;

            // 超出边界则重置到顶部
            if (y > height + 50 || x < -50 || x > width + 50) {
                x = random.nextDouble() * width;
                y = -30;
                speedY = 0.5 + random.nextDouble() * 1.0;
                speedX = -0.2 + random.nextDouble() * 0.4;
                angle = random.nextDouble() * 360;
            }
        }

        void draw(GraphicsContext gc) {
            gc.save();
            gc.translate(x, y);
            gc.rotate(angle);
            // 绘制椭圆花瓣
            gc.setFill(color);
            gc.fillOval(-sizeX/2, -sizeY/2, sizeX, sizeY);
            // 添加高光（浅色内圈）
            gc.setFill(new Color(1, 1, 1, 0.3));
            gc.fillOval(-sizeX/3, -sizeY/3, sizeX*0.6, sizeY*0.6);
            gc.restore();
        }
    }
}