package com.heloword.frontendapi.model.response;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DueWordDto {
  private Long answerId;
  private String answerTableName;
  private Date lastReviewTime;
  private Date nextReviewTime;
  /** Total number of times this word was reviewed */
  private int reviewCount;
  /** Number of reviews where wrongCount == 0 */
  private int correctCount;
}
