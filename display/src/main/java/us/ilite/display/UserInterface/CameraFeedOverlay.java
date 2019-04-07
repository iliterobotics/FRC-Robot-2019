package us.ilite.display.UserInterface;

import edu.wpi.cscore.AxisCamera;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CameraFeedOverlay extends Application {

    private static AxisCameraConnection axisCamConnect;

    @Override
    public void start(Stage stage) {

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        axisCamConnect = new AxisCameraConnection( "10.18.85.11:5800" );
        axisCamConnect.start();
        Button btn = new Button("Load Site");

        Canvas canvas = new Canvas( 800, 500 );
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setOnMouseDragged( e -> gc.fillOval( e.getX(), e.getY(), 5,5 ) );

        StackPane root = new StackPane();
        root.getChildren().addAll ( canvas );

        new AnimationTimer() {

            public void handle(long currentNanoTime) {
                BufferedImage bImage = axisCamConnect.grabImage();
                for ( int i = 0; i < bImage.getWidth(); i ++  ) {
                    for ( int j = 0; j < bImage.getHeight(); j++ ) {
                        java.awt.Color c = new java.awt.Color( bImage.getRGB( i, j ) );
                        javafx.scene.paint.Color color = new javafx.scene.paint.Color( c.getRed(), c.getGreen(), c.getBlue(), 0.5 );
                        gc.setFill( color );
                        gc.fillRect( i, j, i+1, j+1 );
                    }

                }
            }
        };


        Scene scene = new Scene(root, 800, 500);
        engine.load("http://10.18.85.11:5800/" );
        stage.setScene(scene);
        stage.show();
    }

    public static void main( String[] args ) {
        launch( args );
        axisCamConnect.disconnect();
    }
}



