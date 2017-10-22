package ru.spb.se.contexthelper.context;

import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Builds query from the {@link EventContext}. */
public class ContextQueryBuilder {
  private final static int MAX_WORDS_FOR_QUERY = 5;

  private final EventContext context;

  public ContextQueryBuilder(EventContext context) {
    this.context = context;
  }

  public String buildQuery() {
    List<PsiElement> psiElements = context.getPsiElements();

    Map<String, Integer> wordCountMap = new HashMap<>();
    for (PsiElement psiElement : psiElements) {
      if (psiElement.getNode().getElementType() == TokenType.WHITE_SPACE) {
        // Ignoring white spaces.
        continue;
      }
      if (psiElement.getChildren().length > 0) {
        // This is an abstract node, while for word counting we are concerned with concrete nodes.
        continue;
      }
      if (psiElement.getTextLength() == 0) {
        // This node's text is empty: won't help us with forming the request.
        continue;
      }
      String elementText = psiElement.getText();
      int counter = wordCountMap.getOrDefault(elementText, 0);
      wordCountMap.put(elementText, counter + 1);
    }
    List<WordInfo> words =
        wordCountMap.entrySet()
            .stream()
            .map(entry -> new WordInfo(entry.getKey(), entry.getValue()))
            .sorted(WordInfo.COMPARATOR_BY_TIMES_ENCOUNTERED)
            .collect(Collectors.toList());
    // Taking words with the least encounters.
    return words.stream()
        .limit(MAX_WORDS_FOR_QUERY)
        .map(WordInfo::getText)
        .collect(Collectors.joining(" "));
  }

  private static class WordInfo {
    public static final Comparator<? super WordInfo> COMPARATOR_BY_TIMES_ENCOUNTERED =
        Comparator.comparing(WordInfo::getTimesEncountered);

    private String text;

    private int timesEncountered;

    public WordInfo(String text, int timesEncountered) {
      this.text = text;
      this.timesEncountered = timesEncountered;
    }

    public String getText() {
      return text;
    }

    public int getTimesEncountered() {
      return timesEncountered;
    }
  }
}
