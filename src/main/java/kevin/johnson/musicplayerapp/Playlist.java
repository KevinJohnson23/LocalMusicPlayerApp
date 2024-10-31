package kevin.johnson.musicplayerapp;

import java.util.*;

public class Playlist {

    public String playlistId = "";
    public String name = "";
    public List<PlaylistEntry> songs = new ArrayList<>();

    public Playlist(String playlistId, String name) {
        this.playlistId = playlistId;
        this.name = name;
    }

    public void SetSongs(List<PlaylistEntry> songs) {
        this.songs = songs;
    }
    public void AddSong(String entryId, String songId, String fileLocation) {
        this.songs.add(new PlaylistEntry(entryId, this.playlistId, songId, fileLocation));
    }
    public PlaylistEntry RemoveSong(String entryId) {
        for (int i = 0; i < GetSongCount(); i++) {
            if (this.songs.get(i).entryId.equals(entryId)) {
                return this.songs.remove(i);
            }
        }
        return null;
    }
    public int GetSongCount() {
        if (this.songs == null) {
            System.out.println(this.playlistId);
        }
        return this.songs.size();
    }
    public int GetSongIndex(PlaylistEntry song) {
        for (int i = 0; i < this.GetSongCount(); i++) {
            PlaylistEntry songInPlaylist = this.songs.get(i);
            if (songInPlaylist.entryId.equals(song.entryId)) {
                return i;
            }
        }
        return -1;
    }
}
