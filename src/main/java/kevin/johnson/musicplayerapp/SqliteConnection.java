package kevin.johnson.musicplayerapp;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

class ConnectorReturn {
    public Connection connection;
    public boolean hasCreated;
    public ConnectorReturn(Connection conn, boolean hc) {connection = conn; hasCreated = hc;}
}

public class SqliteConnection {

    private static final String DATABASE_PATH_RELATIVE = "playlists.db";
    private static final String DATABASE_PATH = new File(DATABASE_PATH_RELATIVE).toURI().toString();
    private static Connection connection;

    private static ConnectorReturn Connector() {
        try {
            boolean hasCreated = new File(DATABASE_PATH_RELATIVE).createNewFile();
            try {
                Class.forName("org.sqlite.JDBC");
                return new ConnectorReturn(DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH), hasCreated);
            }
            catch (SQLException e) {
                System.out.println("Failed to initiate SQL connection.\n\n" + e.getStackTrace());
                return null;
            }
            catch (ClassNotFoundException e) {
                System.out.println("Could not find class when initiating SQL connection.\n\n" + e.getStackTrace());
                return null;
            }
        } catch (IOException e) {
            System.err.println("Could not create new database file.\n\n" + e.getStackTrace());
            return null;
        }
    }

    public static Connection GetConnection() {
        if (connection == null) {
            ConnectorReturn cr = Connector();
            if (cr == null) return null;
            connection = cr.connection;
            if (cr.hasCreated) {
                ExecuteUpdate(
                        """
                                CREATE TABLE "playlists" (
                                \t"playlistId"\tINTEGER,
                                \t"playlistName"\tTEXT,
                                \tPRIMARY KEY("playlistId" AUTOINCREMENT)
                                )""",
                        "Failed to create table playlists"
                );
                ExecuteUpdate(
                        """
                                CREATE TABLE "songs" (
                                \t"songId"\tINTEGER UNIQUE,
                                \t"fileLocation"\tTEXT UNIQUE,
                                \tPRIMARY KEY("songId" AUTOINCREMENT)
                                )""",
                    "Failed to create table songs"
                );
                ExecuteUpdate(
                        """
                                CREATE TABLE "playlistEntries" (
                                \t"entryId"\tINTEGER,
                                \t"playlistId"\tINTEGER,
                                \t"songId"\tINTEGER,
                                \tPRIMARY KEY("entryId" AUTOINCREMENT),
                                \tCONSTRAINT "playlistId" FOREIGN KEY("playlistId") REFERENCES "playlists"("playlistId"),
                                \tCONSTRAINT "songId" FOREIGN KEY("songId") REFERENCES "songs"("songId")
                                )""",
                        "Failed to add table playlistEntries"
                );
            }
        }
        return connection;
    }

    public static String CorrectStringForDatabase(String original) {
        return original.replace("'", "''");
    }

    public static ResultSet ExecuteQuery(String query, String warning) {
        try {
            Statement statement = GetConnection().createStatement();
            statement.setQueryTimeout(30);
            return statement.executeQuery(query);
        } catch (SQLException e) {
            System.err.println(warning + "\n\n" + e.getStackTrace());
            return null;
        }
    }

    public static Statement ExecuteUpdate(String query, String warning) {
        try {
            Statement statement = GetConnection().createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            return statement;
        } catch (SQLException e) {
            System.err.println(warning + "\n\n" + e.getStackTrace());
            return null;
        }
    }

    public static List<Playlist> LoadAllPlaylists() {
        ResultSet playlistsRS = ExecuteQuery(
                "SELECT playlists.playlistName, playlists.playlistId " +
                        "FROM playlists;",
                "Failed to load all playlists"
        );

        try {
            if (playlistsRS == null) {
                System.err.println("Failed to find names and ids of all playlists");
                return null;
            }

            List<Playlist> playlists = new ArrayList<>();

            while (playlistsRS.next()) {
                String playlistId = playlistsRS.getString("playlistId");
                String playlistName = playlistsRS.getString("playlistName");

                List<PlaylistEntry> songs = LoadSongsInPlaylist(playlistId);

                Playlist playlist = new Playlist(playlistId, playlistName);
                playlist.SetSongs(songs);

                playlists.add(playlist);
            }
            return playlists;

        } catch (SQLException e) {
            System.err.println("Could not traverse result set for names and ids of all playlists\n\n" + e.getStackTrace());
        }
        return null;
    }

    public static Playlist LoadPlaylist(String playlistId) {
        ResultSet playlistNameRS = ExecuteQuery(
                "SELECT playlists.playlistName " +
                        "FROM playlists " +
                        "WHERE playlists.playlistId = " + playlistId + ";",
                "Failed to load playlist with id : " + playlistId
        );

        try {
            if (playlistNameRS == null || !playlistNameRS.next()) {
                System.err.println("Failed to find playlist with id : " + playlistId);
                return null;
            }
            String playlistName = playlistNameRS.getString("playlistName");

            Playlist playlist = new Playlist(playlistName, playlistId);

            List<PlaylistEntry> songs = LoadSongsInPlaylist(playlistId);
            playlist.SetSongs(songs);

            return playlist;

        } catch (SQLException e) {
            System.err.println("Could not traverse result set for playlist name with id : " + playlistId + "\n\n" + e.getStackTrace());
        }
        return null;
    }

    public static List<PlaylistEntry> LoadSongsInPlaylist(String playlistId) {
        ResultSet playlistSongsRS = ExecuteQuery(
                "SELECT playlistEntries.entryId, playlistEntries.playlistId, playlistEntries.songId, songs.fileLocation " +
                        "FROM playlistEntries, songs " +
                        "WHERE playlistEntries.playlistId = " + playlistId + " AND playlistEntries.songId = songs.songId" + ";",
                "Failed to load songs of playlist with id : " + playlistId
        );

        try {
            if (playlistSongsRS == null) {
                System.err.println("Failed to find songs in playlist with id : " + playlistId);
                return null;
            }

            List<PlaylistEntry> songs = new ArrayList<>();
            List<String> removedSongIds = new ArrayList<>();

            while (playlistSongsRS.next()) {
                String songId = playlistSongsRS.getString("songId");
                if (removedSongIds.contains(songId)) continue;
                String entryId = playlistSongsRS.getString("entryId");
                String fileLocation = playlistSongsRS.getString("fileLocation");

                File songFile = new File(fileLocation);
                if (songFile.exists()) {
                    songs.add(new PlaylistEntry(entryId, playlistId, songId, fileLocation));
                } else {
                    RemoveSongReferences(songId);
                    removedSongIds.add(songId);
                }
            }
            return songs;

        } catch (SQLException e) {
            System.err.println("Could not traverse result set for songs of playlist with id : " + playlistId + "\n\n" + e.getStackTrace());
        }
        return null;
    }

    public static Playlist CreatePlaylist(String playlistName) {
        Statement playlistStatement = ExecuteUpdate(
                "INSERT INTO playlists " +
                        "VALUES (NULL, '" + CorrectStringForDatabase(playlistName) + "');",
                "Failed to create playlist with name : " + playlistName
        );

        try {
            if (playlistStatement == null) {
                System.err.println("Failed to get key of playlist with name : " + playlistName);
                return null;
            }

            ResultSet playlistRS = playlistStatement.getGeneratedKeys();

            if (playlistRS == null || !playlistRS.next()) {
                System.err.println("Failed to get key of playlist with name : " + playlistName);
                return null;
            }

            long playlistId = playlistRS.getLong(1);
            String playlistIdAsString = Long.toString(playlistId);
            return new Playlist(playlistIdAsString, playlistName);

        } catch (SQLException e) {
            System.err.println("Failed to get generated key for new playlist with name : " + playlistName + "\n\n" + e.getStackTrace());
        }
        return null;
    }

    public static void RemovePlaylist(String playlistId) {
        ExecuteUpdate(
                "DELETE FROM playlists " +
                        "WHERE playlists.playlistId = " + playlistId + ";",
                "Failed to remove playlist from playlists table with id : " + playlistId
        );
        ExecuteUpdate(
                "DELETE FROM playlistEntries " +
                        "WHERE playlistEntries.playlistId = " + playlistId + ";",
                "Failed to remove playlist entries from playlistEntries table with id : " + playlistId
        );
    }

    public static void RemoveSongFromPlaylist(String entryId) {
        ExecuteUpdate(
                "DELETE FROM playlistEntries " +
                        "WHERE playlistEntries.entryId = " + entryId + ";",
                "Failed to remove playlist entry from playlistEntries table with id : " + entryId
        );
    }

    public static void CreateSongs(List<String> fileLocations) {
        StringBuilder createSongsQuery = new StringBuilder(
                "INSERT INTO songs " +
                        "VALUES "
        );

        boolean hasCreatedSong = false;

        for (int i = 0; i < fileLocations.size(); i++) {
            String fileLocation = fileLocations.get(i);

            ResultSet findSongRS = ExecuteQuery(
                    "SELECT songs.songId " +
                            "FROM songs " +
                            "WHERE fileLocation = '" + CorrectStringForDatabase(fileLocation) + "';",
                    "Failed to lookup song with file location : " + fileLocation
            );
            try {
                if (findSongRS == null) {
                    System.err.println("Failed to find song with file location : " + fileLocation);
                    continue;
                }

                if (!findSongRS.next()) {
                    createSongsQuery.append("(NULL, '").append(CorrectStringForDatabase(fileLocation)).append("')");
                    if (i == fileLocations.size() - 1) {
                        createSongsQuery.append(";");
                    } else {
                        createSongsQuery.append(",");
                    }
                    hasCreatedSong = true;
                }

            } catch (SQLException e) {
                System.err.println("Could not traverse result set for song with file location : " + fileLocation + "\n\n" + e.getStackTrace());
            }
        }

        if (hasCreatedSong) {
            ExecuteUpdate(
                    createSongsQuery.toString(),
                    "Failed to create new song entries with file locations : " + fileLocations
            );
        }
    }

    public static void AddSongsToPlaylist(String playlistId, List<String> fileLocations) {
        CreateSongs(fileLocations);

        StringBuilder createEntriesQuery = new StringBuilder(
                "INSERT INTO playlistEntries " +
                        "VALUES "
        );

        for (int i = 0; i < fileLocations.size(); i++) {
            String fileLocation = fileLocations.get(i);

            ResultSet songIdRS = ExecuteQuery(
                    "SELECT songId " +
                            "FROM songs " +
                            "WHERE fileLocation = '" + CorrectStringForDatabase(fileLocation) + "';",
                    "Failed to load song with file location : " + fileLocation
            );

            try {
                if (songIdRS == null || !songIdRS.next()) {
                    System.err.println("Failed to find song with file location : " + fileLocation);
                    return;
                }
                String songId = songIdRS.getString("songId");
                createEntriesQuery.append("(NULL, ").append(playlistId).append(", ").append(songId).append(")");

                if (i == fileLocations.size() - 1) {
                    createEntriesQuery.append(";");
                } else {
                    createEntriesQuery.append(",");
                }

            } catch (SQLException e) {
                System.err.println("Could not traverse result set for playlist name with id : " + playlistId + "\n\n" + e.getStackTrace());
            }
        }

        ExecuteUpdate(
                createEntriesQuery.toString(),
                "Failed to create entries for playlist with id : " + playlistId
        );
    }

    public static void RemoveSongReferences(String songId) {
        ExecuteUpdate(
                "DELETE " +
                        "FROM songs " +
                        "WHERE songs.songId = " + songId + ";",
                "Failed to remove missing song from songs table with id : " + songId
        );
        ExecuteUpdate(
                "DELETE " +
                        "FROM playlistEntries " +
                        "WHERE playlistEntries.songId = " + songId + ";",
                "Failed to remove missing song entries from songEntries table with song id : " + songId
        );
    }

    public static void ChangePlaylistName(String playlistId, String newName) {
        ExecuteUpdate(
                "UPDATE playlists " +
                        "SET playlistName = '" + CorrectStringForDatabase(newName) + "' " +
                        "WHERE playlists.playlistId = " + playlistId + ";",
                "Failed to rename playlist with id : " + playlistId + " to new name : " + newName
        );
    }

    public static Playlist ClonePlaylist(Playlist playlist) {
        Playlist newPlaylist = CreatePlaylist(playlist.name);

        List<String> fileLocations = new ArrayList<>();
        for (PlaylistEntry song : playlist.songs) {
            fileLocations.add(song.fileLocation);
        }
        AddSongsToPlaylist(newPlaylist.playlistId, fileLocations);

        newPlaylist.SetSongs(LoadSongsInPlaylist(newPlaylist.playlistId));

        return newPlaylist;
    }
}