package us.ilite.display.simulation.ui;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.sun.javafx.scene.canvas.CanvasHelper.CanvasAccessor;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import us.ilite.common.types.input.ELogitech310;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.CycleMethod;



import java.security.Key;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import us.ilite.common.types.input.ELogitech310;

public class DriverHUD extends Application {


    //Initalize Image
    Image abtn = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\abtn.png");
    Image bbtn = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\bbtn.png");
    Image xbtn = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\xbtn.png");
    Image ybtn = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\ybtn.png");
    Image pad = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\pad.png");
    Image pad2 = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\pad.png");
    Image stick = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\stick.png");
    Image stick2 = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\stick.png");

    //Init pressed images
    Image apressed = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\apressed.png");
    Image bpressed = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\bpressed.png");
    Image xpressed = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\xpressed.png");
    Image ypressed = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\ypressed.png");
    
   
    //Initalize Key Buttons
    // KeyButtons aKey = new KeyButtons( abtn, ELogitech310.A_BTN );
    // KeyButtons bKey = new KeyButtons( bbtn, ELogitech310.B_BTN );
    // KeyButtons xKey = new KeyButtons( xbtn, ELogitech310.X_BTN );
    // KeyButtons yKey = new KeyButtons(ybtn, ELogitech310.Y_BTN);
    

    private String state = "";
    private String realState = "";

    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        Canvas canvas = new Canvas(1000, 500);

        // Initialize images

        // Gradients
        Stop[] stops = new Stop[] { new Stop(0, Color.PURPLE), new Stop(1, Color.GREEN) };
        LinearGradient linear = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);

        Rectangle rect = new Rectangle(0, 0, canvas.getWidth(), canvas.getHeight());
        rect.setFill(linear);
        root.getChildren().add(rect);

        // Draw Images
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // gc.drawImage( abtn, canvas.getWidth()/2, canvas.getHeight()/2 );

        root.setCenter(canvas);

        stage.setScene(scene);

        HashSet<String> inputs = new HashSet<>();
        ArrayList<KeyButtons> registered = new ArrayList<>();

        //Draw button images
       
        // gc.drawImage(bbtn, 30, 30);
        // gc.drawImage(xbtn, 50, 30);
        // gc.drawImage(ybtn, 70, 30);

        scene.setOnKeyPressed( e -> inputs.add( e.getCode().toString() ));

        scene.setOnKeyReleased( e -> inputs.remove( e.getCode().toString() ) );

        // registered.add( yKey );

        final long startTime = System.nanoTime();
        gc.setFill(linear);
        


        new AnimationTimer() {

            Image aImage = abtn;
            Image bImage = bbtn;
            Image xImage = xbtn;
            Image yImage = ybtn;

            int x = (int)350;
            int y = (int)170;
            // y
            int yx = x - 200;
            int yy = y;
            //x
            int xx = (int) (x + yx) / 2;
            int xy = (int) 71;
            //b
            int bx = xx;
            int by = xy + (Math.abs(xy - y) * 2);

            int magnitudeH = 0;
            int magnitudeV = 0;
            

            public void handle(long currentNanoTime) {
               
                if(inputs.contains("A")) {
                    aImage = apressed;
                } else {
                    aImage = abtn;
                }
                if(inputs.contains("B")) {
                    bImage = bpressed;
                } else {
                    bImage = bbtn;
                }
                if(inputs.contains("X")) {
                    xImage = xpressed;
                } else {
                    xImage = xbtn;
                }
                if(inputs.contains("Y")) {
                    yImage = ypressed;
                } else {
                    yImage = ybtn;
                }

                if (inputs.contains("LEFT")) {
                    magnitudeH -= 7;
                }

                if (inputs.contains("RIGHT")) {
                    magnitudeH += 7;
                }

                if (inputs.contains("DOWN")) {
                    magnitudeV += 7;
                }

                if (inputs.contains("UP")) {
                    magnitudeV -= 7;
                }

                if (!(inputs.contains("UP") || inputs.contains("LEFT") || inputs.contains("DOWN")
                        || inputs.contains("RIGHT"))) {
                    magnitudeH = 0;
                    magnitudeV = 0;
                }

                if (magnitudeH > 0 && magnitudeH >= 42) {
                    magnitudeH = 42;
                    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                }
                if (magnitudeH < 0 && magnitudeH <= -42) {
                    magnitudeH = -42;
                    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                }

                if (magnitudeV > 0 && magnitudeV >= 42) {
                    magnitudeV = 40;
                    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                }
                if (magnitudeV < 0 && magnitudeV <= -42) {
                    magnitudeV = -40;
                    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                }

                gc.drawImage(aImage, bx, by, 100, 100);
                gc.drawImage(bImage, x, y, 100, 100);
                gc.drawImage(xImage, yx, yy, 100, 100);
                gc.drawImage(yImage, xx, xy, 100, 100);

                gc.drawImage(pad, canvas.getWidth() / 2 + 200, canvas.getHeight() / 2, 200, 200);
                gc.drawImage(stick, canvas.getWidth() / 2 + 250 + (magnitudeH),
                        canvas.getHeight() / 2 + 50 + magnitudeV, 100, 100);

                gc.drawImage(pad2, canvas.getWidth() / 2, canvas.getHeight() / 2, 200, 200);
                gc.drawImage(stick2, canvas.getWidth() / 2 + 50, canvas.getHeight() / 2 + 50, 100, 100);
                
                

                System.out.printf("X: %s     Y: %s\n", x, y);
            }
        }.start();

        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}
