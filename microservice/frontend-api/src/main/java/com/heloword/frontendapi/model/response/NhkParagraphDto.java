package com.heloword.frontendapi.model.response;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NhkParagraphDto {

  private String original;
  private String ja;
  private String en;
  private String zh;
  private String grammar;
  private List<NhkVocabItemDto> vocabulary;
}
