package com.heloword.frontendapi.model.response;

import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NhkArticleDetailDto {

  private Long id;
  private String title;
  private String sourceUrl;
  private String sourceLang;
  private Date createDate;
  private String contentJa;
  private String contentEn;
  private String contentZh;
  private String contentGrammar;
  private List<NhkVocabItemDto> contentVocabulary;
  private List<NhkParagraphDto> paragraphs;
}
