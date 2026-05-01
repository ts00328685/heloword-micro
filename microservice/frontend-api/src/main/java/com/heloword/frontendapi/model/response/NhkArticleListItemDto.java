package com.heloword.frontendapi.model.response;

import java.util.Date;
import com.heloword.common.entity.article.ArticleCrawlerEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NhkArticleListItemDto {

  private Long id;
  private String title;
  private String sourceUrl;
  private String sourceLang;
  private Date createDate;

  public static NhkArticleListItemDto from(ArticleCrawlerEntity e) {
    NhkArticleListItemDto dto = new NhkArticleListItemDto();
    dto.setId(e.getId());
    dto.setTitle(e.getTitle());
    dto.setSourceUrl(e.getSourceUrl());
    dto.setSourceLang(e.getSourceLang());
    dto.setCreateDate(e.getCreateDate());
    return dto;
  }
}
