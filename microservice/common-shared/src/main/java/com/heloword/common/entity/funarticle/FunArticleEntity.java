package com.heloword.common.entity.funarticle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "fun_article_seq")
@Table(name = "FUN_ARTICLE")
public class FunArticleEntity extends BaseEntity {

  private String word;

  @Column(columnDefinition = "TEXT")
  private String content;
}
