package kevin.johnson.musicplayerapp;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import java.io.IOException;

public class SongItem extends HBox {

    public Label songNameText, songArtistNameText, songDurationText;
    public Button deleteSongButton;
    public SongMetadata metadata;

    public SongItem(SongMetadata metadata) throws IOException {
        this.metadata = metadata;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Song.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(SongItem.this);

        HBox playlistItem = fxmlLoader.load();

        this.songNameText = (Label) playlistItem.lookup(".songName");
        this.songArtistNameText = (Label) playlistItem.lookup(".songArtistName");
        this.songDurationText = (Label) playlistItem.lookup(".songDuration");
        this.deleteSongButton = (Button) playlistItem.lookup(".deleteSongButton");

        songNameText.setText(metadata.songName);
        songArtistNameText.setText(metadata.artistName);
        songDurationText.setText(metadata.formattedLength);

        Tooltip songNameTooltip = new Tooltip();
        songNameTooltip.textProperty().bind(songNameText.textProperty());
        Tooltip.install(songNameText, songNameTooltip);

        Tooltip artistNameTooltip = new Tooltip();
        artistNameTooltip.textProperty().bind(songArtistNameText.textProperty());
        Tooltip.install(songArtistNameText, artistNameTooltip);
    }
}
