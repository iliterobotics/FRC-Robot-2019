package us.ilite.display.UserInterface;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class CameraFeedOverlay extends Application {

    private Scene mScene;

    @Override
    public void start( Stage pStage ) {
        // create the scene
        pStage.setTitle( "Web View" );
        mScene = new Scene( new Browser());
        pStage.setScene( mScene );
        mScene.getStylesheets().add( "webviewsample/BrowserToolbar.css" );
        pStage.show();
    }

    public static void main( String[] args ) {
        launch( args );
    }
}
     class Browser extends Region {

        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

        public Browser() {
            //apply the styles
            getStyleClass().add( "Browser" );
            // load the web page
            webEngine.load( "http://www.oracle.com/products/index.html" );
            //add the web view to the scene
            getChildren().add( browser );

        }

        private Node createSpacer() {
            Region spacer = new Region();
            HBox.setHgrow( spacer, Priority.ALWAYS );
            return spacer;
        }

        @Override
        protected void layoutChildren() {
            double w = getWidth();
            double h = getHeight();
            layoutInArea( browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER );
        }

        @Override
        protected double computePrefWidth( double height ) {
            return 750;
        }

        @Override
        protected double computePrefHeight( double width ) {
            return 500;
        }
    }

