package com.heloword.common.entity.announcement;

import java.util.Date;
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
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "official_message_seq")
@Table(name = "OFFICIAL_MESSAGE")
public class OfficialMessageEntity extends BaseEntity {

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  private Date publishedAt;
}
