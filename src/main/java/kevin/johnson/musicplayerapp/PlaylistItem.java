package kevin.johnson.musicplayerapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.io.IOException;

public class PlaylistItem extends HBox {

    public Label playlistNameText;
    public TextField playlistNameTextField;
    public Label playlistSongCountText;
    public Button deletePlaylistButton;
    public ContextMenu contextMenu;
    public Controller controller;

    public String playlistId;

    public PlaylistItem(Controller controller, String playlistId) throws IOException {
        this.controller = controller;
        this.playlistId = playlistId;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Playlist.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(PlaylistItem.this);

        HBox playlistItem = fxmlLoader.load();

        this.playlistNameText = (Label) playlistItem.lookup(".playlistName");
        this.playlistNameText.setVisible(true);
        this.playlistNameTextField = (TextField) playlistItem.lookup(".playlistNameField");
        this.playlistNameTextField.setVisible(false);
        this.playlistSongCountText = (Label) playlistItem.lookup(".playlistSongCount");
        this.deletePlaylistButton = (Button) playlistItem.lookup(".deletePlaylistButton");

        this.playlistNameText.textProperty().addListener((observable, oldValue, newValue) -> {
            PlaylistNameChanged(oldValue, newValue);
        });
        playlistNameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.playlistNameText.setText(this.playlistNameTextField.getText());
                this.playlistNameTextField.setVisible(false);
                this.playlistNameText.setVisible(true);
            }
        });

        contextMenu = new ContextMenu();
        MenuItem renameMenuItem = new MenuItem("Rename playlist");
        contextMenu.getItems().add(renameMenuItem);
        MenuItem cloneMenuItem = new MenuItem("Duplicate playlist");
        contextMenu.getItems().add(cloneMenuItem);
        playlistNameText.setContextMenu(contextMenu);

        renameMenuItem.setOnAction((ActionEvent ae) -> {
            RenamePlaylistSelected();
        });

        cloneMenuItem.setOnAction((ActionEvent ae) -> {
            DuplicatePlaylistSelected();
        });

        Tooltip playlistNameTooltip = new Tooltip();
        playlistNameTooltip.textProperty().bind(playlistNameText.textProperty());
        Tooltip.install(playlistNameText, playlistNameTooltip);
    }

    private void DuplicatePlaylistSelected() {
        Playlist newPlaylist = SqliteConnection.ClonePlaylist(this.controller.playlistCache.get(this.playlistId));
        this.controller.playlistCache.put(newPlaylist.playlistId, newPlaylist);
        this.controller.DisplaySubstringPlaylistsOrAllPlaylists();
    }

    private void RenamePlaylistSelected() {
        playlistNameTextField.setOnAction((ActionEvent ae) -> {
            playlistNameTextField.getParent().requestFocus();
        });

        this.playlistNameTextField.setText(this.playlistNameText.getText());

        this.playlistNameText.setVisible(false);
        this.playlistNameTextField.setVisible(true);
        this.playlistNameTextField.requestFocus();
    }

    private void PlaylistNameChanged(String oldName, String newName) {
        if (oldName.equals(newName)) return;

        SqliteConnection.ChangePlaylistName(this.playlistId, newName);

        Playlist playlist = this.controller.playlistCache.get(this.playlistId);
        playlist.name = newName;
        this.controller.playlistCache.put(this.playlistId, playlist);

        if (this.controller.openedPlaylist != null && this.controller.openedPlaylist.playlistId.equals(this.playlistId)) {
            this.controller.SetPlaylistTitle(playlist.name);
        }
    }
}
