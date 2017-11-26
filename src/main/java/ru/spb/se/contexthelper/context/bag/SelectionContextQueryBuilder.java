package ru.spb.se.contexthelper.context.bag;

import com.google.common.collect.Sets;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import ru.spb.se.contexthelper.context.NotEnoughContextException;

import java.util.*;
import java.util.stream.Collectors;

/** Builds query from the {@link SelectionContext}. */
public class SelectionContextQueryBuilder {
  private final static int MAX_WORDS_FOR_QUERY = 5;

  private final static Set<IElementType> meaninglessForContextTokenTypes =
      Sets.newHashSet(
          TokenType.WHITE_SPACE,

          JavaTokenType.C_STYLE_COMMENT,
          JavaTokenType.END_OF_LINE_COMMENT,

          JavaTokenType.LPARENTH,
          JavaTokenType.RPARENTH,
          JavaTokenType.LBRACE,
          JavaTokenType.RBRACE,
          JavaTokenType.LBRACKET,
          JavaTokenType.RBRACKET,
          JavaTokenType.SEMICOLON,
          JavaTokenType.COMMA,
          JavaTokenType.DOT,
          JavaTokenType.ELLIPSIS,
          JavaTokenType.AT,

          JavaTokenType.EQ,
          JavaTokenType.GT,
          JavaTokenType.LT,
          JavaTokenType.EXCL,
          JavaTokenType.TILDE,
          JavaTokenType.QUEST,
          JavaTokenType.COLON,
          JavaTokenType.PLUS,
          JavaTokenType.MINUS,
          JavaTokenType.ASTERISK,
          JavaTokenType.DIV,
          JavaTokenType.AND,
          JavaTokenType.OR,
          JavaTokenType.XOR,
          JavaTokenType.PERC,

          JavaTokenType.EQEQ,
          JavaTokenType.LE,
          JavaTokenType.GE,
          JavaTokenType.NE,
          JavaTokenType.ANDAND,
          JavaTokenType.OROR,
          JavaTokenType.PLUSPLUS,
          JavaTokenType.MINUSMINUS,
          JavaTokenType.LTLT,
          JavaTokenType.GTGT,
          JavaTokenType.GTGTGT,
          JavaTokenType.PLUSEQ,
          JavaTokenType.MINUSEQ,
          JavaTokenType.ASTERISKEQ,
          JavaTokenType.DIVEQ,
          JavaTokenType.ANDEQ,
          JavaTokenType.OREQ,
          JavaTokenType.XOREQ,
          JavaTokenType.PERCEQ,
          JavaTokenType.LTLTEQ,
          JavaTokenType.GTGTEQ,
          JavaTokenType.GTGTGTEQ,

          JavaTokenType.DOUBLE_COLON,
          JavaTokenType.ARROW);

  private final SelectionContext context;

  public SelectionContextQueryBuilder(SelectionContext context) {
    this.context = context;
  }

  public String buildQuery() {
    List<PsiElement> psiElements = context.getPsiElements();

    Map<String, Integer> wordCountMap = new HashMap<>();
    for (PsiElement psiElement : psiElements) {
      if (meaninglessForContextTokenTypes.contains(psiElement.getNode().getElementType())) {
        continue;
      }
      if (psiElement.getChildren().length > 0) {
        // This is an abstract node, while for bag-of-words model we are only concerned with
        // concrete nodes.
        continue;
      }
      if (psiElement.getTextLength() == 0) {
        // The node's text representation is empty: won't help us with forming the query. E.g.
        // REFERENCE_PARAMETER_LIST is present in PSI trie but its text may be empty.
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
    if (words.isEmpty()) {
      throw new NotEnoughContextException();
    }
    return words.stream()
        .limit(MAX_WORDS_FOR_QUERY)
        .map(WordInfo::getText)
        .collect(Collectors.joining(" "));
  }

  private static class WordInfo {
    static final Comparator<? super WordInfo> COMPARATOR_BY_TIMES_ENCOUNTERED =
        Comparator.comparing(WordInfo::getTimesEncountered).reversed();

    private String text;

    private int timesEncountered;

    WordInfo(String text, int timesEncountered) {
      this.text = text;
      this.timesEncountered = timesEncountered;
    }

    String getText() {
      return text;
    }

    int getTimesEncountered() {
      return timesEncountered;
    }
  }
}