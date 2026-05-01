package com.heloword.frontendapi.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NhkVocabItemDto {

  private String word;
  private String reading;

  @JsonProperty("meaning_zh")
  private String meaningZh;

  @JsonProperty("meaning_en")
  private String meaningEn;
}
