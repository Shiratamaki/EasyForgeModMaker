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
 * 唯美樱花飘落背景 - 增强版（五瓣樱花）
 */
public class SakuraBackground extends StackPane {
    private final Canvas canvas;
    private final List<Petal> petals = new ArrayList<>();
    private final Random random = new Random();
    private final int petalCount = 100;
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

        initPetals();
        startAnimation();
    }

    private void initPetals() {
        petals.clear();
        for (int i = 0; i < petalCount; i++) {
            petals.add(new Petal());
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
        double speedY, speedX, swing;
        double size;
        double opacity;
        double rotation, rotSpeed;

        Petal() { reset(); }

        void reset() {
            x = random.nextDouble() * 1200;
            y = random.nextDouble() * 800 - 200;
            speedY = 0.6 + random.nextDouble() * 1.2;
            speedX = -0.2 + random.nextDouble() * 0.4;
            swing = random.nextDouble() * Math.PI * 2;
            size = 10 + random.nextDouble() * 8;
            opacity = 0.6 + random.nextDouble() * 0.3;
            rotation = random.nextDouble() * 360;
            rotSpeed = -0.8 + random.nextDouble() * 1.6;
        }

        void update(double width, double height, double delta) {
            x += (speedX + Math.sin(swing) * 0.3) * delta * 60;
            y += speedY * delta * 60;
            rotation += rotSpeed * delta * 60;
            swing += 0.05;
            if (x > width + 80 || x < -80 || y > height + 80) {
                x = random.nextDouble() * width;
                y = -40;
                speedY = 0.6 + random.nextDouble() * 1.2;
                speedX = -0.2 + random.nextDouble() * 0.4;
            }
        }

        void draw(GraphicsContext gc) {
            gc.save();
            gc.translate(x, y);
            gc.rotate(rotation);
            double w = size;
            double h = size * 0.8;
            // 绘制五瓣樱花（简化：多个椭圆叠加）
            gc.setFill(Color.rgb(255, 180, 200, opacity));
            for (int i = 0; i < 5; i++) {
                double angle = i * 72 * Math.PI / 180;
                double dx = Math.cos(angle) * size * 0.4;
                double dy = Math.sin(angle) * size * 0.4;
                gc.fillOval(dx - w/2, dy - h/2, w, h);
            }
            // 中心花蕊
            gc.setFill(Color.rgb(255, 100, 120, opacity));
            gc.fillOval(-3, -3, 6, 6);
            gc.restore();
        }
    }
}