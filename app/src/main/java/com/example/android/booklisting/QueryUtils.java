package com.example.android.booklisting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Helper methods related to requesting and receiving books data from Google Books API.
 */
public final class QueryUtils {

    /** Tag for the log messages */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /** Delimiter for book authors if the book has more than one author */
    private static final String BOOK_AUTHORS_DELIMITER = ", ";

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing a JSON response.
     */
    public static ArrayList<Book> fetchBooks(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        ArrayList<Book> books = extractBooksFromJson(jsonResponse);

        // Download image and set it to the Book object
        for (Book book : books) {
            book.setBookImageBitmap(downloadBookImage(book.getThumbnailUrl()));
        }

        return books;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static ArrayList<Book> extractBooksFromJson(String booksJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(booksJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        ArrayList<Book> books = new ArrayList<>();

        // Try to parse the JSON. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // build up a list of Books objects with the corresponding data.
            JSONObject booksJSONRoot = new JSONObject(booksJSON);
            JSONArray bookItems = booksJSONRoot.optJSONArray("items");
            if (bookItems != null) {
                for (int i = 0; i < bookItems.length(); i++) {
                    JSONObject bookItem = bookItems.getJSONObject(i);
                    JSONObject bookItemVolumeInfo = bookItem.getJSONObject("volumeInfo");
                    String bookTitle = bookItemVolumeInfo.getString("title");
                    String bookAuthors = null;
                    JSONArray bookAuthorsArray = bookItemVolumeInfo.optJSONArray("authors");
                    if (bookAuthorsArray != null) {
                        bookAuthors = "";
                        for (int j = 0; j < bookAuthorsArray.length(); j++) {
                            bookAuthors += bookAuthorsArray.getString(j);
                            if (j < (bookAuthorsArray.length() - 1)) {
                                bookAuthors += BOOK_AUTHORS_DELIMITER;
                            }
                        }
                    }
                    String bookPublishDate = null;
                    if (bookItemVolumeInfo.has("publishedDate")) {
                        bookPublishDate = bookItemVolumeInfo.getString("publishedDate");
                    }
                    String bookPreviewLink = bookItemVolumeInfo.getString("previewLink");

                    String bookThumbnailUrl = null;
                    if (bookItemVolumeInfo.has("imageLinks")) {
                        bookThumbnailUrl = bookItemVolumeInfo.getJSONObject("imageLinks").getString("thumbnail");
                    }
                    books.add(new Book(bookThumbnailUrl, bookTitle, bookAuthors, bookPublishDate, bookPreviewLink, null));
                }
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the book JSON results", e);
        }

        // Return the list of books
        return books;
    }

    // Download book images
    private static Bitmap downloadBookImage(String bookImageUrl) {
        // If the image load url is empty or null, then return early.
        if (TextUtils.isEmpty(bookImageUrl)) {
            return null;
        }
        Bitmap image = null;
        try {
            InputStream in = new java.net.URL(bookImageUrl).openStream();
            image = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return image;
    }

}