package com.heloword.common.entity.article;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "article_crawler")
public class ArticleCrawlerEntity {

  @Id
  private Long id;

  private String title;

  @Column(name = "source_url")
  private String sourceUrl;

  @Column(name = "source_lang")
  private String sourceLang;

  @Column(columnDefinition = "LONGTEXT", name = "original_content")
  private String originalContent;

  @Column(columnDefinition = "LONGTEXT", name = "content_ja")
  private String contentJa;

  @Column(columnDefinition = "LONGTEXT", name = "content_en")
  private String contentEn;

  @Column(columnDefinition = "LONGTEXT", name = "content_zh")
  private String contentZh;

  @Column(columnDefinition = "LONGTEXT", name = "content_grammar")
  private String contentGrammar;

  @Column(columnDefinition = "LONGTEXT", name = "content_vocabulary")
  private String contentVocabulary;

  @Column(columnDefinition = "LONGTEXT")
  private String paragraphs;

  private Integer status;

  private String version;

  @Column(name = "create_date")
  private Date createDate;

  @Column(name = "update_date")
  private Date updateDate;

  @Column(name = "create_by")
  private String createBy;
}
