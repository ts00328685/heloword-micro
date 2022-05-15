package com.heloword.common.model.dto;

import java.util.Date;
import org.springframework.beans.BeanUtils;
import com.heloword.common.entity.record.RecordQuizEntity;

import lombok.Data;

@Data
public class RecordQuizDto {

  private String username;
  private Long answerId;
  private Integer quizIndex;
  private String answerTableName;
  private Integer timeSpent;
  private Date startTime;
  private Date finishedTime;
  private Integer pronounceCount;
  private Integer deleteCount;
  private Integer wrongCount;
  private Long recordQuizSettingId;

  public static RecordQuizEntity toEntity(RecordQuizDto recordQuizDto){
    RecordQuizEntity recordQuizEntity = new RecordQuizEntity();
    BeanUtils.copyProperties(recordQuizDto, recordQuizEntity);
    return recordQuizEntity;
  }

}
