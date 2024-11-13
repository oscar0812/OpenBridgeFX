package com.oscarrtorres.openbridgefx.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public final class Toast {
    private static Stage stage;

    public static void setStage(Stage stage) {
        Toast.stage = stage;
    }
    public static void makeText(String toastMsg) {
        Toast.makeText(toastMsg, 2000, 200, 200);
    }
    public static void makeText(String toastMsg, int toastDelay, int fadeInDelay, int fadeOutDelay) {
        Stage toastStage = new Stage();
        toastStage.initOwner(stage);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        Text text = new Text(toastMsg);
        text.setFont(Font.font("Verdana", 16));  // Smaller font size
        text.setFill(Color.WHITE);

        StackPane root = new StackPane(text);
        root.setStyle("-fx-background-radius: 10; -fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 10px;");
        root.setOpacity(0);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.BOTTOM_CENTER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);

        // Position toastStage at the bottom center of the owner stage
        toastStage.setX(stage.getX() + stage.getWidth() / 2 - 100); // Adjust horizontal position as needed
        toastStage.setY(stage.getY() + stage.getHeight() - 80);      // Set to bottom

        toastStage.show();

        // Fade-in animation
        Timeline fadeInTimeline = new Timeline();
        KeyFrame fadeInKey = new KeyFrame(Duration.millis(fadeInDelay), new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));
        fadeInTimeline.getKeyFrames().add(fadeInKey);

        fadeInTimeline.setOnFinished(ae -> new Thread(() -> {
            try {
                Thread.sleep(toastDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Timeline fadeOutTimeline = new Timeline();
            KeyFrame fadeOutKey = new KeyFrame(Duration.millis(fadeOutDelay), new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
            fadeOutTimeline.getKeyFrames().add(fadeOutKey);
            fadeOutTimeline.setOnFinished(aeb -> toastStage.close());
            fadeOutTimeline.play();
        }).start());

        fadeInTimeline.play();
    }
}
