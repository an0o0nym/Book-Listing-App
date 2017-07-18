package com.example.android.booklistingapp;

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
import java.util.List;

/**
 * Created by an0o0nym on 16/07/17.
 */

public final class QueryUtils {
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * No one should be able to create QueryUtils object.
     */
    private QueryUtils() { }

    public static List<Book> fetchBookData(String requestUrl) {
        URL url = createUrl(requestUrl);
        Log.v(LOG_TAG, "Request url: " + requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem when making the request.", e);
        }

        List<Book> bookData = extractBookDataFromJson(jsonResponse);

        return bookData;
    }

    private static List<Book> extractBookDataFromJson(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        List<Book> bookData = new ArrayList<>();

        try {
            JSONObject baseResponse = new JSONObject(jsonResponse);
            JSONArray bookArray = new JSONArray();
            if (baseResponse.has("items")) {
                bookArray = baseResponse.getJSONArray("items");
            }

            int numOfBooks = bookArray.length();

            for (int i = 0; i < numOfBooks; i++) {
                JSONObject currentBook = bookArray.getJSONObject(i).getJSONObject("volumeInfo");

                String title = currentBook.getString("title");
                JSONArray authorsJSON = (new JSONArray()).put("Author N/A");
                if (currentBook.has("authors")) {
                    authorsJSON = currentBook.getJSONArray("authors");
                }

                int numOfAuthors = authorsJSON.length();
                String[] authors = new String[numOfAuthors];
                for (int j = 0; j < numOfAuthors; j++) {
                    authors[j] = authorsJSON.getString(j);
                }

                Book book = new Book(title, authors);
                bookData.add(book);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem when parsing response JSON results", e);
        }

        return bookData;
    }

    private static String makeRequest(URL url) throws IOException{
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlCon = null;
        InputStream streamIn = null;
        try {
            urlCon = (HttpURLConnection) url.openConnection();
            urlCon.setReadTimeout(10000);
            urlCon.setConnectTimeout(15000);
            urlCon.setRequestMethod("GET");
            urlCon.connect();

            if (urlCon.getResponseCode() == 200) {
                streamIn = urlCon.getInputStream();
                jsonResponse = readFromStream(streamIn);
            } else {
                Log.e(LOG_TAG, "Error! Response code: " + urlCon.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem when retrieving book data.", e);
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
            if (streamIn != null) {
                streamIn.close();
            }
        }

        return jsonResponse;
    }

    private static String readFromStream(InputStream streamIn) throws IOException{
        StringBuilder sb = new StringBuilder();
        if (streamIn != null) {
            InputStreamReader streamInReader = new InputStreamReader(streamIn,
                    Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(streamInReader);
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                line = reader.readLine();
            }
        }

        return sb.toString();
    }

    private static URL createUrl(String requestUrl) {
        URL url = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem when building the URL");
        }

        return url;
    }


}
