package org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.*;

public class SpotifyApiExample {

    public static class Artist {
        private String name;
        private int popularity;

        // Constructors
        public Artist() {
            // Default constructor
        }

        public Artist(String name, int popularity) {
            this.name = name;
            this.popularity = popularity;
        }

        // Getter and Setter methods
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPopularity() {
            return popularity;
        }

        public void setPopularity(int popularity) {
            this.popularity = popularity;
        }
    }

    public static String accessToken = "Bearer BQAcT2gRIvGx1l1E4eqMJdxtPfHw6HW_qwTaYQxm1P78DU2TqAXUZ4LdcALjXvWDOeQIL3sjjUoTFdlZYbpjT5N4n__gRL-tC-9IIc7bg-g9vXPXEP4YZ_MyKCOLpn-X2XE_0apq5wMxtyvI3rYjz6SDoiodZ-pp1Qo5WDEffg3Wi6OMIgNoTBFsJ0cqjpJIOwzldn_No1-UCVLyfFNE6am4DF_C5S2thnMZLf3l5Alv011fE83lGs8W1Ng5QQ"; // Bearer Token here
    public static String playlistID = "5oNL0OVx3sxzJ15uxd7HbU";

    public static void main(String[] args) {

        writeAllArtists(getPlaylistArtistURIs(playlistID + "/tracks"));
    }

    public static void writeAllArtists(List<String> URIs) {
        List<Artist> artistOfPlaylist = new ArrayList<>();

        for (String uri : URIs) {

            Artist newArtist = createArtist(uri);

            if (!artistOfPlaylist.stream().anyMatch(artist -> artist.getName().equals(newArtist.getName()))) {
                artistOfPlaylist.add(newArtist);
            }

        }
        System.out.println("Total Unic Artists Count: " + artistOfPlaylist.size());

        artistOfPlaylist.sort(Comparator.comparing(Artist::getPopularity).reversed());

        System.out.println();
        System.out.println();
        System.out.println();

        for (Artist art : artistOfPlaylist) {
            System.out.println("Popularity: " + art.getPopularity() + " \t" + " Artist: " + art.getName());
        }
    }

    public static Artist createArtist(String artistID) {
        Response response = RestAssured.given()
                .baseUri("https://api.spotify.com")
                .basePath("/v1/artists/" + artistID)
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .get();



        JsonPath jsonPath = response.jsonPath();
        String artistName = jsonPath.getString("name");
        double artistPopularity = jsonPath.getDouble("popularity");

        System.out.println(artistName);

        int newPop = (int) Math.round(artistPopularity);

        Artist newArtist = new Artist(artistName, newPop);

        return newArtist;
    }

    public static List<String> getPlaylistArtistURIs(String playListID) {
        Response response = RestAssured.given()
                .baseUri("https://api.spotify.com")
                .basePath("/v1/playlists/" + playListID)
                .header("Authorization", accessToken)
                .contentType(ContentType.TEXT)
                .get();

        System.out.println("Response Status Code: " + response.getStatusCode());

        //System.out.println("Response Body: " + response.getBody().asString());
        return getArtistsURIs(response);
    }

    private static List<String> getArtistsURIs(Response response) {
        // Extract the "uri" parameter for each artist in the "artists" array
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> items = jsonPath.getList("items");

        List<String> artistURIs = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Map<String, Object> track = (Map<String, Object>) item.get("track");
            List<Map<String, Object>> artists = (List<Map<String, Object>>) track.get("artists");

            for (Map<String, Object> artist : artists) {
                String uri = (String) artist.get("uri");
                String artistId = extractArtistId(uri);
                artistURIs.add(artistId);
            }
        }

        System.out.println("Total Artists Count: " + artistURIs.size());

        return artistURIs;
    }

    private static String extractArtistId(String uri) {
        uri = uri.replace("spotify:artist:", "");
        return uri;
    }
}
