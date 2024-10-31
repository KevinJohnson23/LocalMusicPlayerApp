package kevin.johnson.musicplayerapp;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;

public class SongMetadata {

    public String songName, artistName;
    public int lengthInSeconds;
    public String formattedLength;
    public Image albumImage;

    public SongMetadata(String fileLocation) {
        try {
            Mp3File mp3file = new Mp3File(fileLocation);

            if (mp3file.hasId3v2Tag()) {
                ID3v2 tag = mp3file.getId3v2Tag();
                this.songName = tag.getTitle();
                this.artistName = tag.getArtist();

                if (tag.getAlbumImage() != null) {
                    try {
                        albumImage = new Image(new ByteArrayInputStream(tag.getAlbumImage()));
                    } catch (Exception e) {
                        System.err.println("Failed to load album image of song with file location : " + fileLocation + "\n\n" + e.getStackTrace());
                    }
                }
            } else if (mp3file.hasId3v1Tag()) {
                ID3v1 tag = mp3file.getId3v1Tag();
                this.songName = tag.getTitle();
                this.artistName = tag.getArtist();
            }

            this.lengthInSeconds = (int) mp3file.getLengthInSeconds();
            this.formattedLength = FormatLength(this.lengthInSeconds);

            if (this.songName == null) songName = "Unknown";
            if (this.artistName == null) artistName = "Unknown";
        } catch (Exception e) {
            System.err.println("Failed to retrieve song metadata for file location : " + fileLocation + "\n\n" + e.getStackTrace());
        }
    }

    public static String FormatLength(int seconds) {
        String minutesString = "0", secondsString;

        int numMinutes = (int) Math.floor((double) seconds / 60);
        if (numMinutes > 0) {
            minutesString = Integer.toString(numMinutes);
        }
        int numSeconds = (seconds % 60);
        if (numSeconds < 10) {
            secondsString = "0" + numSeconds;
        } else {
            secondsString = Integer.toString(numSeconds);
        }

        return minutesString + ":" + secondsString;
    }
}
