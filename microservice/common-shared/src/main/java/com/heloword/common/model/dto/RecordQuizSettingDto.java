package com.heloword.common.model.dto;

import java.util.Date;
import org.springframework.beans.BeanUtils;
import com.heloword.common.entity.record.RecordQuizSettingEntity;

import lombok.Data;

@Data
public class RecordQuizSettingDto {

  private Long id;
  private String username;
  private Date timestamp;
  private Date createTime;
  private String type;
  private Integer min;
  private Integer max;
  private Integer total;
  private Boolean isSelected;

  public static RecordQuizSettingEntity toEntity(RecordQuizSettingDto recordQuizDto) {
    RecordQuizSettingEntity recordQuizSettingEntity = new RecordQuizSettingEntity();
    BeanUtils.copyProperties(recordQuizDto, recordQuizSettingEntity);
    return recordQuizSettingEntity;
  }

  public static RecordQuizSettingDto fromEntity(RecordQuizSettingEntity recordQuizEntity) {
    RecordQuizSettingDto recordQuizSettingEntity = new RecordQuizSettingDto();
    BeanUtils.copyProperties(recordQuizEntity, recordQuizSettingEntity);
    return recordQuizSettingEntity;
  }
}
