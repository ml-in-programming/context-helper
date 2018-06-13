package ru.spb.se.contexthelper.lookup;

public enum LookupClientFactory {

  GOOGLE_CUSTOM_SEARCH {
    @Override
    public QuestionLookupClient createLookupClient() {
      return new GoogleCustomSearchClient(GOOGLE_SEARCH_API_KEY);
    }
  },

  GOOGLE_SEARCH_CRAWLER {
    @Override
    public QuestionLookupClient createLookupClient() {
      return new GoogleSearchStackoverflowCrawler();
    }
  };

  private static final String GOOGLE_SEARCH_API_KEY = "AIzaSyBXQg39PaVjqONPEL4eubyA7S-pEuqVKOc";

  public abstract QuestionLookupClient createLookupClient();
}