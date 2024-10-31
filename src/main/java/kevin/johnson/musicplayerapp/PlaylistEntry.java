package kevin.johnson.musicplayerapp;

public class PlaylistEntry {

    public String entryId;
    public String playlistId;
    public String songId;
    public String fileLocation;
    public SongMetadata metadata;

    public PlaylistEntry(String entryId, String playlistId, String songId, String fileLocation) {
        this.entryId = entryId;
        this.playlistId = playlistId;
        this.songId = songId;
        this.fileLocation = fileLocation;
        this.metadata = new SongMetadata(fileLocation);
    }
}
