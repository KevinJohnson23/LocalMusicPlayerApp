module kevin.johnson.musicplayerapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires mp3agic;


    opens kevin.johnson.musicplayerapp to javafx.fxml;
    exports kevin.johnson.musicplayerapp;
}