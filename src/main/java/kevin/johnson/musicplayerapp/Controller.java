package kevin.johnson.musicplayerapp;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    private VBox playlistsVBox, songsVBox;
    @FXML
    private HBox playerHBox;
    @FXML
    private AnchorPane ap, playlistsAP, songsAP, songsColumnTitle, songsColumnArtist;
    @FXML
    private ScrollPane playlistsSP, songsSP;
    @FXML
    private ImageView playerSongImage, playButtonImage, shuffleButtonImage, loopButtonImage, muteButtonImage, previousButtonImage, nextButtonImage, songsColumnDurationImage, songsColumnTitleArrow, songsColumnArtistArrow;
    @FXML
    private Label playerSongName, playerArtistName, playerSongTime, playlistNameSongs;
    @FXML
    private Button createPlaylistButton, addSongButton, playerShuffle, playerForward, playerBackward, playerLoop, playerMute, playerPlay;
    @FXML
    private Slider playerVolumeSlider, playerPlaybarSlider;
    @FXML
    private ProgressBar playerVolumeProgress, playerPlaybarProgress;
    @FXML
    private TextField playlistNameInput, songNameInput;

    private static String PLAY_ICON;
    private static String PAUSE_ICON;
    private static String LOOP_ICON;
    private static String LOOP_DISABLED_ICON;
    private static String SHUFFLE_ICON;
    private static String SHUFFLE_DISABLED_ICON;
    private static String PREVIOUS_ICON;
    private static String NEXT_ICON;
    private static String MUTED_ICON;
    private static String UNMUTED_ICON;
    private static String DURATION_ICON;
    private static String DOWN_ICON;

    public Playlist openedPlaylist;
    public Playlist currentPlaylist;
    private int currentSongIndex = -1;
    private MediaPlayer currentMediaPlayer;
    private boolean playing = false;
    private boolean looping = false;
    private boolean shuffling = false;
    private boolean muted = false;
    private boolean draggingPlaybar = false;
    private double volume = 1;
    private String sortBy;
    private boolean sortAscending = false;

    public Map<String, Playlist> playlistCache = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PLAY_ICON = getClass().getResource("images/play.png").toExternalForm();
        PAUSE_ICON = getClass().getResource("images/pause.png").toExternalForm();
        LOOP_ICON = getClass().getResource("images/loop.png").toExternalForm();
        LOOP_DISABLED_ICON = getClass().getResource("images/loop_disabled.png").toExternalForm();
        SHUFFLE_ICON = getClass().getResource("images/shuffle.png").toExternalForm();
        SHUFFLE_DISABLED_ICON = getClass().getResource("images/shuffle_disabled.png").toExternalForm();
        PREVIOUS_ICON = getClass().getResource("images/previous.png").toExternalForm();
        NEXT_ICON = getClass().getResource("images/next.png").toExternalForm();
        MUTED_ICON = getClass().getResource("images/muted.png").toExternalForm();
        UNMUTED_ICON = getClass().getResource("images/unmuted.png").toExternalForm();
        DURATION_ICON = getClass().getResource("images/duration.png").toExternalForm();
        DOWN_ICON = getClass().getResource("images/down.png").toExternalForm();

        playerHBox.setVisible(false);
        playlistNameSongs.setText("");
        ChangeVolume(volume);

        InitializeCache();
        DisplaySubstringPlaylistsOrAllPlaylists();

        SetButtonImage(playButtonImage, PLAY_ICON, playerPlay, "Play");
        SetButtonImage(muteButtonImage, UNMUTED_ICON, playerMute, "Mute");
        SetButtonImage(shuffleButtonImage, SHUFFLE_DISABLED_ICON, playerShuffle, "Enable shuffle");
        SetButtonImage(loopButtonImage, LOOP_DISABLED_ICON, playerLoop, "Enable repeat");
        SetButtonImage(previousButtonImage, PREVIOUS_ICON, playerBackward, "Previous");
        SetButtonImage(nextButtonImage, NEXT_ICON, playerForward, "Next");
        songsColumnDurationImage.setImage(new Image(DURATION_ICON));
        songsColumnTitleArrow.setImage(new Image(DOWN_ICON));
        songsColumnTitleArrow.setVisible(false);
        songsColumnArtistArrow.setImage(new Image(DOWN_ICON));
        songsColumnArtistArrow.setVisible(false);

        playerPlay.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            PauseClicked();
        });
        playerMute.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            MuteClicked();
        });
        playerLoop.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            LoopClicked();
        });
        playerShuffle.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            ShuffleClicked();
        });
        playerForward.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            NextSongClicked();
        });
        playerBackward.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            PreviousSongClicked();
        });
        playerVolumeSlider.valueProperty().addListener((observableValue, number, t1) -> {
            double newValue = playerVolumeSlider.getValue();
            playerVolumeProgress.setProgress(newValue / playerVolumeSlider.getMax());
            if (muted) {
                if (newValue == 0) return;
                MuteClicked();
            }
            ChangeVolume(newValue);
        });
        playerPlaybarSlider.setOnMousePressed((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            draggingPlaybar = true;
        });
        playerPlaybarSlider.setOnMouseReleased((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            double newValue = playerPlaybarSlider.getValue();
            PlaybarMoved(newValue);
            draggingPlaybar = false;
        });
        playerPlaybarSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            playerPlaybarProgress.setProgress(playerPlaybarSlider.getValue() / playerPlaybarSlider.getMax());
        });
        playlistNameInput.textProperty().addListener((observable, oldValue, newValue) -> {
            PlaylistNameSearched(newValue);
        });
        songNameInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (openedPlaylist == null) return;
            SongNameSearched(newValue);
        });
        createPlaylistButton.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            CreateNewPlaylist();
        });
        addSongButton.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            AddSongsToPlaylist();
        });
        songsColumnTitle.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            SongSortClicked("Title");
        });
        songsColumnArtist.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            SongSortClicked("Artist");
        });
    }

    private void InitializeCache() {
        List<Playlist> playlists = SqliteConnection.LoadAllPlaylists();
        if (playlists == null) {
            System.err.println("Unable to load playlists.");
            return;
        }

        for (Playlist playlist : playlists) {
            playlistCache.put(playlist.playlistId, playlist);
        }
    }

    private void PlaySong(Playlist playlist, int songIndex) {
        if (currentMediaPlayer != null) {
            currentMediaPlayer.stop();
        }

        SetButtonImage(playButtonImage, PAUSE_ICON, playerPlay, "Pause");

        if (!playerHBox.getStyleClass().contains("playerHBoxPlayingSong")) {
            playerHBox.getStyleClass().add("playerHBoxPlayingSong");
        }

        PlaylistEntry song = playlist.songs.get(songIndex);

        if (currentPlaylist != null && openedPlaylist != null && openedPlaylist.playlistId.equals(currentPlaylist.playlistId)) {
            if (currentSongIndex != -1) {
                PlaylistEntry previousSong = currentPlaylist.songs.get(currentSongIndex);
                SongItem previousSongItem = (SongItem) songsVBox.lookup("#song-" + previousSong.entryId);
                previousSongItem.getStyleClass().remove("songHBoxSelected");
            }
            SongItem currentSongItem = (SongItem) songsVBox.lookup("#song-" + song.entryId);
            currentSongItem.getStyleClass().add("songHBoxSelected");
        }

        playing = true;

        currentPlaylist = playlist;
        currentSongIndex = songIndex;

        playerSongName.setText(song.metadata.songName);
        playerArtistName.setText(song.metadata.artistName);
        playerSongImage.setImage(song.metadata.albumImage);

        File songFile = new File(song.fileLocation);
        Media sound = new Media(songFile.toURI().toString());

        currentMediaPlayer = new MediaPlayer(sound);
        if (!muted) ChangeVolume(volume);
        currentMediaPlayer.setMute(muted);

        currentMediaPlayer.play();

        currentMediaPlayer.setOnEndOfMedia(this::PlayNextSong);

        if (song.metadata.lengthInSeconds > 0) {
            SetPlaybar();

            ChangeListener<Duration> changeListener = new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                    if (currentMediaPlayer == null) return;
                    if (currentPlaylist == null || !song.playlistId.equals(currentPlaylist.playlistId)) {
                        currentMediaPlayer.currentTimeProperty().removeListener(this);
                        return;
                    }
                    SetPlaybar();
                }
            };
            currentMediaPlayer.currentTimeProperty().addListener(changeListener);

        } else {
            playerPlaybarSlider.setValue(0);
        }

        playerHBox.setVisible(true);
    }

    private void SetButtonImage(ImageView image, String file, Button button, String tooltip) {
        image.setImage(new Image(file));
        if (tooltip != null) {
            button.getTooltip().setText(tooltip);
        }
    }

    private void PlayNextSong() {
        int nextSongIndex = currentSongIndex;
        if (!looping) {
            nextSongIndex = GetNextSongIndex();
        }
        PlaySong(currentPlaylist, nextSongIndex);
    }

    private int GetNextSongIndex() {
        int nextSongIndex = currentSongIndex;
        if (!shuffling) {
            nextSongIndex++;
            if (nextSongIndex >= currentPlaylist.GetSongCount()) nextSongIndex = 0;
        } else {
            while (nextSongIndex == currentSongIndex) {
                nextSongIndex = (int) Math.floor(Math.random() * currentPlaylist.GetSongCount());
            }
        }
        return nextSongIndex;
    }

    private void SetPlaybar() {
        if (currentMediaPlayer == null) return;

        PlaylistEntry song = currentPlaylist.songs.get(currentSongIndex);

        int lengthInSeconds = song.metadata.lengthInSeconds;
        String lengthInSecondsFormatted = song.metadata.formattedLength;

        int currentTime = (int) currentMediaPlayer.getCurrentTime().toSeconds();

        if (!draggingPlaybar) {
            double progress = ((double) currentTime) / lengthInSeconds;
            playerPlaybarSlider.setValue(progress);
        }

        playerSongTime.setText(SongMetadata.FormatLength(currentTime) + " / " + lengthInSecondsFormatted);
    }

    public void SetPlaylistTitle(String playlistName) {
        playlistNameSongs.setText(playlistName);
    }

    private void PlaybarMoved(double newProgress) {
        if (currentMediaPlayer != null) {
            int lengthInSeconds = currentPlaylist.songs.get(currentSongIndex).metadata.lengthInSeconds;
            int newTimeInSeconds = (int) Math.round(newProgress * lengthInSeconds);
            currentMediaPlayer.seek(Duration.seconds(newTimeInSeconds));
        }
    }

    private void ChangeVolume(double newVolume) {
        if (newVolume == 0) {
            volume = 1;
            if (!muted) {
                MuteClicked();
            }
            return;
        }
        volume = newVolume;
        playerVolumeSlider.setValue(volume);
        if (currentMediaPlayer != null) currentMediaPlayer.setVolume(volume);
    }

    private void PauseClicked() {
        if (currentMediaPlayer == null) return;

        playing = !playing;

        if (playing) {
            playerHBox.getStyleClass().add("playerHBoxPlayingSong");
            currentMediaPlayer.play();
            SetButtonImage(playButtonImage, PAUSE_ICON, playerPlay, "Pause");
        } else {
            playerHBox.getStyleClass().remove("playerHBoxPlayingSong");
            currentMediaPlayer.pause();
            SetButtonImage(playButtonImage, PLAY_ICON, playerPlay, "Play");
        }
    }

    private void MuteClicked() {
        muted = !muted;

        if (currentMediaPlayer != null) {
            currentMediaPlayer.setMute(muted);
        }
        if (muted) {
            playerVolumeSlider.setValue(0);
            SetButtonImage(muteButtonImage, MUTED_ICON, playerMute, "Unmute");
        } else {
            ChangeVolume(volume);
            SetButtonImage(muteButtonImage, UNMUTED_ICON, playerMute, "Mute");
        }
    }

    private void LoopClicked() {
        looping = !looping;
        if (looping) {
            SetButtonImage(loopButtonImage, LOOP_ICON, playerLoop, "Disable repeat");
        } else {
            SetButtonImage(loopButtonImage, LOOP_DISABLED_ICON, playerLoop, "Enable repeat");
        }
    }

    private void ShuffleClicked() {
        shuffling = !shuffling;
        if (shuffling) {
            SetButtonImage(shuffleButtonImage, SHUFFLE_ICON, playerShuffle, "Disable shuffle");
        } else {
            SetButtonImage(shuffleButtonImage, SHUFFLE_DISABLED_ICON, playerShuffle, "Enable shuffle");
        }
    }

    private void NextSongClicked() {
        int nextSongIndex = GetNextSongIndex();
        PlaySong(currentPlaylist, nextSongIndex);
    }

    private void PreviousSongClicked() {
        int previousSongIndex = currentSongIndex - 1;
        if (previousSongIndex < 0) previousSongIndex = currentPlaylist.GetSongCount() - 1;
        PlaySong(currentPlaylist, previousSongIndex);
    }

    private void SongSortClicked(String sortType) {
        if (sortBy != null && sortBy.equals(sortType)) {
            sortAscending = !sortAscending;
        }
        sortBy = sortType;

        boolean isTypeTitle = sortBy.equals("Title");
        songsColumnTitleArrow.setVisible(isTypeTitle);
        songsColumnArtistArrow.setVisible(!isTypeTitle);

        if (sortAscending) {
            songsColumnTitleArrow.setRotate(0);
            songsColumnArtistArrow.setRotate(0);
        } else {
            songsColumnTitleArrow.setRotate(180);
            songsColumnArtistArrow.setRotate(180);
        }

        DisplaySubstringSongsOrAllSongs();
    }

    private void CreateNewPlaylist() {
        Playlist newPlaylist = SqliteConnection.CreatePlaylist("New Playlist");
        if (newPlaylist != null) {
            playlistCache.put(newPlaylist.playlistId, newPlaylist);
            DisplaySubstringPlaylistsOrAllPlaylists();
        } else {
            System.err.println("Playlist could not be created.");
        }
    }

    private void AddSongsToPlaylist() {
        if (openedPlaylist == null) return;

        Stage stage = (Stage) ap.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select audio files");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac")
                );
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles == null || selectedFiles.isEmpty()) return;

        List<String> fileLocations = new ArrayList<>();
        for (File file : selectedFiles) {
            fileLocations.add(file.getPath());
        }

        SqliteConnection.AddSongsToPlaylist(openedPlaylist.playlistId, fileLocations);
        openedPlaylist.SetSongs(SqliteConnection.LoadSongsInPlaylist(openedPlaylist.playlistId));
        DisplaySubstringSongsOrAllSongs();
        DisplaySubstringPlaylistsOrAllPlaylists();
    }

    private void PlaylistNameSearched(String nameSearched) {
        List<Playlist> playlistsSearched = GetPlaylistsBySubstring(nameSearched);
        DisplayPlaylists(playlistsSearched);
    }

    private void SongNameSearched(String nameSearched) {
        List<PlaylistEntry> songsSearched = GetSongsBySubstring(openedPlaylist, nameSearched);
        DisplaySongs(songsSearched);
    }

    private boolean GetConfirmation(String title, String body) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(body);
        alert.setGraphic(null);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        Optional<ButtonType> result = alert.showAndWait();

        return (result.isPresent() && result.get() == ButtonType.OK);
    }

    private Playlist GetPlaylistById(String playlistId) {
        if (playlistCache.get(playlistId) != null) {
            return playlistCache.get(playlistId);
        }
        Playlist playlist = SqliteConnection.LoadPlaylist(playlistId);
        playlistCache.put(playlistId, playlist);

        return playlist;
    }

    private List<Playlist> GetPlaylistsBySubstring(String substring) {
        substring = substring.toLowerCase();
        List<Playlist> playlists = new ArrayList<>();

        for (String playlistId : playlistCache.keySet()) {
            Playlist playlist = playlistCache.get(playlistId);
            String playlistName = playlist.name.toLowerCase();

            if (playlistName.contains(substring)) {
                playlists.add(playlist);
            }
        }

        return playlists;
    }

    private List<PlaylistEntry> GetSongsBySubstring(Playlist playlist, String substring) {
        substring = substring.toLowerCase();
        List<PlaylistEntry> songs = new ArrayList<>();

        for (PlaylistEntry song : playlist.songs) {
            String songName = song.metadata.songName.toLowerCase();
            String artistName = song.metadata.artistName.toLowerCase();

            if (songName.contains(substring) || artistName.contains(substring)) {
                songs.add(song);
            }
        }

        return songs;
    }

    private class SongItemComparator implements Comparator<SongItem> {
        @Override
        public int compare(SongItem item2, SongItem item1) {
            int compareResult = 0;

            if (sortBy != null) {
                if (sortBy.equals("Artist")) {
                    compareResult = item1.songArtistNameText.getText().compareToIgnoreCase(item2.songArtistNameText.getText());
                } else if (sortBy.equals("Title")) {
                    compareResult = item1.songNameText.getText().compareToIgnoreCase(item2.songNameText.getText());
                }
            }
            if (sortAscending) {
                return compareResult;
            } else {
                return compareResult * -1;
            }
        }
    }

    private List<SongItem> DisplaySongs(List<PlaylistEntry> songs) {
        songsVBox.getChildren().clear();

        List<SongItem> createdSongItems = new ArrayList<>();

        for (PlaylistEntry song : songs) {
            try {
                Playlist playlist = playlistCache.get(song.playlistId);
                SongItem songItem = new SongItem(song.metadata);

                if (currentSongIndex != -1 && currentPlaylist.songs.get(currentSongIndex).entryId.equals(song.entryId)) {
                    songItem.getStyleClass().add("songHBoxSelected");
                }

                songItem.deleteSongButton.setOnMouseClicked((MouseEvent e) -> {
                    if (e.getButton() != MouseButton.PRIMARY) return;
                    DeleteSongFromPlaylist(song);
                });

                songItem.setOnMouseClicked((MouseEvent e) -> {
                    if (e.getButton() != MouseButton.PRIMARY) return;
                    PlaySong(playlist, playlist.GetSongIndex(song));
                });

                songItem.setId("song-" + song.entryId);

                createdSongItems.add(songItem);

            } catch (IOException e) {
                System.err.println("Failed to instantiate SongItem for entry id : " + song.entryId + "\n\n" + e.getStackTrace());
            }
        }
        createdSongItems.sort(new SongItemComparator());

        for (SongItem songItem : createdSongItems) {
            songsVBox.getChildren().add(songItem);
        }

        return createdSongItems;
    }

    private void OpenPlaylist(Playlist playlist) {
        if (openedPlaylist != null) {
            PlaylistItem previousPlaylistItem = (PlaylistItem) playlistsVBox.lookup("#playlist-" + openedPlaylist.playlistId);
            previousPlaylistItem.getStyleClass().remove("playlistHBoxSelected");
        }
        PlaylistItem openedPlaylistItem = (PlaylistItem) playlistsVBox.lookup("#playlist-" + playlist.playlistId);
        openedPlaylistItem.getStyleClass().add("playlistHBoxSelected");

        openedPlaylist = playlist;
        SetPlaylistTitle(playlist.name);
        DisplaySubstringSongsOrAllSongs();
    }

    private List<PlaylistItem> DisplayPlaylists(List<Playlist> playlists) {
        playlistsVBox.getChildren().clear();

        List<PlaylistItem> createdPlaylistItems = new ArrayList<>();

        for (Playlist playlist : playlists) {
            try {
                PlaylistItem playlistItem = new PlaylistItem(this, playlist.playlistId);

                if (openedPlaylist != null && openedPlaylist.playlistId.equals(playlist.playlistId)) {
                    playlistItem.getStyleClass().add("playlistHBoxSelected");
                }

                playlistItem.setOnMouseClicked((MouseEvent e) -> {
                    if (e.getButton() != MouseButton.PRIMARY) return;
                    OpenPlaylist(playlist);
                });

                playlistItem.playlistNameText.setText(playlist.name);
                playlistItem.playlistSongCountText.setText(Integer.toString(playlist.GetSongCount()) + " songs");

                playlistItem.deletePlaylistButton.setOnMouseClicked((MouseEvent e) -> {
                    if (e.getButton() != MouseButton.PRIMARY) return;
                    DeletePlaylist(playlist);
                });

                playlistItem.setId("playlist-" + playlist.playlistId);

                playlistsVBox.getChildren().add(playlistItem);
                createdPlaylistItems.add(playlistItem);

            } catch (IOException e) {
                System.err.println("Failed to instantiate PlaylistItem for playlist id : " + playlist.playlistId + "\n\n" + e.getStackTrace());
            }
        }

        return createdPlaylistItems;
    }

    public void DisplaySubstringPlaylistsOrAllPlaylists() {
        String substring = playlistNameInput.getText();
        if (substring.isEmpty()) {
            DisplayPlaylists(new ArrayList<>(playlistCache.values()));
        } else {
            DisplayPlaylists(GetPlaylistsBySubstring(substring));
        }
    }

    public void DisplaySubstringSongsOrAllSongs() {
        String substring = songNameInput.getText();
        if (substring.isEmpty()) {
            DisplaySongs(openedPlaylist.songs);
        } else {
            DisplaySongs(GetSongsBySubstring(openedPlaylist, substring));
        }
    }

    private void DeletePlaylist(Playlist playlist) {
        boolean canDelete = GetConfirmation(
                "Confirm playlist deletion",
                "Do you want to delete playlist '" + playlist.name + "'?"
        );

        if (canDelete) {
            SqliteConnection.RemovePlaylist(playlist.playlistId);
            playlistCache.remove(playlist.playlistId);
            DisplaySubstringPlaylistsOrAllPlaylists();

            if (currentPlaylist != null && currentPlaylist.playlistId.equals(playlist.playlistId)) {
                DeselectPlaylist();
            }
        }
    }

    private void DeleteSongFromPlaylist(PlaylistEntry song) {
        boolean canDelete = GetConfirmation(
                "Confirm song removal",
                "Do you want to remove song '" + song.metadata.songName + "'?"
        );

        if (canDelete) {
            if (currentPlaylist != null && currentMediaPlayer != null && currentPlaylist.songs.get(currentSongIndex).entryId.equals(song.entryId)) {
                DeselectSong();
            }

            SqliteConnection.RemoveSongFromPlaylist(song.entryId);

            Playlist playlist = playlistCache.get(song.playlistId);
            playlist.RemoveSong(song.entryId);

            DisplaySubstringPlaylistsOrAllPlaylists();
            DisplaySubstringSongsOrAllSongs();
        }
    }

    private void DeselectPlaylist() {
        currentPlaylist = null;
        openedPlaylist = null;
        playerHBox.setVisible(false);
        playlistNameSongs.setText("");
        songsVBox.getChildren().clear();
        DisplaySongs(new ArrayList<>());
        if (currentMediaPlayer != null) {
            DeselectSong();
        }
    }

    private void DeselectSong() {
        currentMediaPlayer.stop();
        currentMediaPlayer = null;
        currentSongIndex = -1;
        playerHBox.setVisible(false);
    }
}