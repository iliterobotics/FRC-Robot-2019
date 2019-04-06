package us.ilite.display.UserInterface;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class CameraFeedOverlay extends Application {

    @Override
    public void start(Stage stage) {

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        Button btn = new Button("Load Site");

        Canvas canvas = new Canvas( 800, 500 );
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setOnMouseDragged( e -> gc.fillOval( e.getX(), e.getY(), 5,5 ) );

        StackPane root = new StackPane();
        root.getChildren().addAll ( webView);

        Scene scene = new Scene(root, 800, 500);
        engine.load("http://10.18.85.11:5800/" );
        stage.setScene(scene);
        stage.show();
    }

    public static void main( String[] args ) {
        launch( args );
    }
}



