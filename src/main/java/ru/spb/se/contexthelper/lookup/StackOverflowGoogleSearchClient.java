package ru.spb.se.contexthelper.lookup;

import com.google.common.net.UrlEscapers;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Fires Google Search requests and extracts StackOverflow question ids. */
public class StackOverflowGoogleSearchClient {
  private static final String SEARCH_ENGINE_ID = "004273159360178116673:j1srnoyrr-i";

  @NotNull
  private final String apiKey;

  public StackOverflowGoogleSearchClient(@NotNull String apiKey) {
    this.apiKey = apiKey;
  }

  public List<Long> lookupQuestionIds(String query) throws Exception {
    String encodedUrl = UrlEscapers.urlFragmentEscaper().escape(
        "https://www.googleapis.com/customsearch/v1"
            + "?key=" + apiKey
            + "&cx=" + SEARCH_ENGINE_ID
            + "&q=" + query + " java is:question"
            + "&alt=json");
    URL url = new URL(encodedUrl);

    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    List<Long> questionIds = new ArrayList<>();
    try (AutoCloseable ignored = urlConnection::disconnect) {
      urlConnection.setRequestMethod("GET");
      urlConnection.setRequestProperty("Accept", "application/json");
      BufferedReader bufferedReader =
          new BufferedReader(
              new InputStreamReader(urlConnection.getInputStream()));
      String nextLine;
      while ((nextLine = bufferedReader.readLine()) != null) {
        if (nextLine.contains("\"link\": \"")){
          String link = nextLine.substring(
              nextLine.indexOf("\"link\": \"") + ("\"link\": \"").length(),
              nextLine.indexOf("\","));
          // Format: https://stackoverflow.com/questions/id/...
          String[] urlParts = link.split("/");
          String idText = urlParts[4];
          questionIds.add(Long.parseLong(idText));
        }
      }
    }
    return questionIds;
  }
}