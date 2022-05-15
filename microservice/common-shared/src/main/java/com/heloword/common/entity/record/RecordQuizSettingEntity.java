package com.heloword.common.entity.record;

import java.util.Date;
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
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "record_quiz_setting_seq")
@Table(name = "RECORD_QUIZ_SETTING")
public class RecordQuizSettingEntity extends BaseEntity {
  private String username;
  private Date timestamp;
  private String type;
  private Integer min;
  private Integer max;
  private Integer total;
  private Boolean isSelected;
}
