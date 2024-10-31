package kevin.johnson.musicplayerapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
//--module-path "C:\Users\Kevin Johnson\OneDrive\Documents\Java Libraries\javafx-sdk-23.0.1\lib" --add-modules=javafx.controls,javafx.fxml,javafx.media
public class Main extends Application {

    private static final String WINDOW_TITLE = "Music Player";

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));

            Scene scene = new Scene(root);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

            Image icon = new Image(getClass().getResource("images/AppIcon.png").toExternalForm());
            stage.getIcons().add(icon);
            stage.setTitle(WINDOW_TITLE);
            stage.setIconified(false);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load FXML\n\n" + e.getStackTrace());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
