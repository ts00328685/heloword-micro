package com.heloword.frontendapi.model.request;

import lombok.Data;

/**
 * Mirrors the DailyProgressRecord shape sent from the frontend.
 *
 * {
 *   "date": "2024-04-25",
 *   "japanese": { "quizWords": 5, "scrambleSentences": 3, ... },
 *   "english":  { "quizWords": 10, ... }
 * }
 */
@Data
public class DailyGoalProgressRequest {

  private String date;
  private LangProgress japanese;
  private LangProgress english;

  @Data
  public static class LangProgress {
    private int quizWords;
    private int scrambleSentences;
    private int spokenSentences;
    private int spokenScoreTotal;
    private int writtenSentences;
  }
}
