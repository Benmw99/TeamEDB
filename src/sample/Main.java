/*
This is poorly commented in many places. My bad there chief
There is also poor error handling because I am to lazy to correctly implement that in a prototype
Along with that some primary keys are very strange such as you can't have two images names the same thing
because right now you are searching the db for an image when you are trying to view and I can't display multiple
Image searching will not be a thing in our final product.
There is also a 1MB file size limit on all images
 */

package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Java DB Prototype");
        primaryStage.setScene(new Scene(root, 1800, 800));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
