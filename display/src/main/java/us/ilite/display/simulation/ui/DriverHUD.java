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
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import us.ilite.common.types.input.ELogitech310;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.paint.CycleMethod;
import javafx.scene.layout.Border;



import java.security.Key;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import us.ilite.common.types.input.ELogitech310;

public class DriverHUD extends Application {

    double mouseX = 0;
    double mouseY = 0;


    //Initalize Image
    Image abtn = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\abtn.png");
    Image bbtn = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\bbtn.png");
    Image xbtn = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\xbtn.png");
    Image ybtn = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\ybtn.png");
    Image pad = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\pad.png");
    Image pad2 = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\pad.png");
    Image stick = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\stick.png");
    Image stick2 = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\stick.png");
    Image dpad = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\dpad.png");
    Image bar = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\redbar.png");
    Image sideBar = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\redbar.png");
    Image robotIsometirc = new Image(
            "file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\2018Robot_iso.png");

    //Init pressed images
    Image apressed = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\apressed.png");
    Image bpressed = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\bpressed.png");
    Image xpressed = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\xpressed.png");
    Image ypressed = new Image("file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\ypressed.png");

    Image robotSide = new Image(
            "file:display\\src\\main\\java\\us\\ilite\\display\\simulation\\ui\\2018Robot_side.png");

    //Init vars
    Canvas canvas = new Canvas(1000, 500);
    private double refreshRate = 5d;

    //Init text
    Text stateLabel = new Text("Robot State: ");
    




    int magnitudeH = 0;
    int magnitudeV = 0;

    
    
   
    //Initalize Key Images
    KeyImage aButton = new KeyImage(abtn, apressed, refreshRate, 51, 353 );
    KeyImage bButton = new KeyImage(bbtn, bpressed, refreshRate, 111, 392);
    KeyImage xButton = new KeyImage(xbtn, xpressed, refreshRate, 111, 392);
    KeyImage yButton = new KeyImage(ybtn, ypressed, refreshRate, 111, 392);

    KeyImage redBar = new KeyImage(bar, bar, refreshRate, 308, 300);
    KeyImage rightBar = new KeyImage(sideBar, sideBar, refreshRate, 628, -38);
    KeyImage leftBar = new KeyImage(sideBar, sideBar, refreshRate, 308.0, -40.0);
    
    KeyImage robot = new KeyImage(robotIsometirc, robotSide, refreshRate, 328, 0);
    

    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        
        stateLabel.setY(306);
        stateLabel.setX(332);

        root.getChildren().add(stateLabel);


        // Gradients. Pretty cool but I might take them out in favor of css
        Stop[] stops = new Stop[] { new Stop(0, Color.PURPLE), new Stop(1, Color.GREEN) };
        LinearGradient linear = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);

        //Rectangle that has the gradient's design
        Rectangle rect = new Rectangle(0, 0, canvas.getWidth(), canvas.getHeight());
        rect.setFill(Color.BLACK);
        root.getChildren().add(rect);

        // Initialize GraphicsContext (Essentially the graphics of the canvas)
        GraphicsContext gc = canvas.getGraphicsContext2D();

        root.setCenter(canvas);

        stage.setScene(scene);

        //Hashset records button inputs
        HashSet<String> inputs = new HashSet<>();
        // ArrayList<KeyButtons> registered = new ArrayList<>();


        //For testing purposes only.
        scene.setOnKeyPressed( e -> inputs.add( e.getCode().toString() ));
        scene.setOnKeyReleased(e -> inputs.remove(e.getCode().toString()));
        scene.setOnMouseReleased(e -> System.out.println(e.getSceneX() + ", " + e.getSceneY()));
        scene.setOnMouseDragged(e -> leftBar.setXY(e.getSceneX(), e.getSceneY()));

        final long startTime = System.nanoTime();
        gc.setFill(Color.BLACK);
        


        new AnimationTimer() {

            int frames = 0;

            public void handle(long currentNanoTime) {
               
                //All testing stuff
                if(inputs.contains("A")) {
                    aButton.setImage(aButton.pressedImage());
                    aButton.blink(refreshRate);
                } else {
                    aButton.setImage(aButton.NormalImage());
                    aButton.stopBlinking(refreshRate);
                }
                if(inputs.contains("B")) {
                    bButton.setImage(bButton.pressedImage());
                    robot.setImage(robotSide);
                } else {
                    bButton.setImage(bButton.NormalImage());
                    robot.setImage(robotIsometirc);
                }
                if(inputs.contains("X")) {
                    xButton.setImage(xButton.pressedImage());
                } else {
                    xButton.setImage(xButton.NormalImage());
                }
                if(inputs.contains("Y")) {
                    yButton.setImage(yButton.pressedImage());
                } else {
                    yButton.setImage(yButton.NormalImage());
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
                }
                if (magnitudeH < 0 && magnitudeH <= -42) {
                    magnitudeH = -42;
                }

                if (magnitudeV > 0 && magnitudeV >= 42) {
                    magnitudeV = 40;
                }
                if (magnitudeV < 0 && magnitudeV <= -42) {
                    magnitudeV = -40;
                }

                
                if (frames % refreshRate == 0) {
                    refresh(gc);
                }
                
                //Draw buttons
                display(gc, aButton.getImage(), aButton.getDisplayRate(), aButton.getX(), aButton.getY(), 100, 100, frames); 
                // display(gc, bButton.getImage(), bButton.getDisplayRate(), bButton.getX(), bButton.getY(), 100, 100, frames); 
                // display(gc, xButton.getImage(), xButton.getDisplayRate(), xButton.getX(), xButton.getY(), 100, 100, frames);
                // display(gc, yButton.getImage(), yButton.getDisplayRate(), yButton.getX(), yButton.getY(), 100, 100, frames);

                display(gc, robot.getImage(), robot.getDisplayRate(), robot.getX(), robot.getY(), 300, 300, frames);
                display(gc, redBar.getImage(), redBar.getDisplayRate(), redBar.getX(), redBar.getY(), 340, 20, frames);
                display(gc, rightBar.getImage(), rightBar.getDisplayRate(), rightBar.getX(), rightBar.getY(), 20, 350, frames);
                display(gc, leftBar.getImage(), leftBar.getDisplayRate(), leftBar.getX(), leftBar.getY(), 20, 350, frames);
            

                //Draw sticks
                // gc.drawImage(pad, canvas.getWidth() / 2 + 200, canvas.getHeight() / 2, 200, 200);
                // gc.drawImage(stick, canvas.getWidth() / 2 + 250 + (magnitudeH),
                //         canvas.getHeight() / 2 + 50 + magnitudeV, 100, 100);

                // gc.drawImage(pad2, canvas.getWidth() / 2, canvas.getHeight() / 2, 200, 200);
                // gc.drawImage(stick2, canvas.getWidth() / 2 + 50, canvas.getHeight() / 2 + 50, 100, 100);

                //Draw dpads
                // gc.drawImage(dpad, canvas.getWidth() / 2 - 250, canvas.getHeight() / 2 + 50);
                
                frames++;
            }
        }.start();

        stage.show();

    }

   
    //When the refresh rate is larger than the interval, the result is blinking.
    private void display(GraphicsContext gc, Image img, double interval, double x, double y, int w, int h,
            int currentFrames) {

        if (currentFrames % interval == 0) {
            gc.drawImage(img, x, y, w, h);
        }

    }

    private void refresh(GraphicsContext gc) {
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }



    public static void main(String[] args) {
        launch(args);
 
    }


}
