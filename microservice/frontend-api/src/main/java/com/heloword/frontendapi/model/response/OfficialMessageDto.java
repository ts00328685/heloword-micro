package com.heloword.frontendapi.model.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficialMessageDto {
  private Long id;
  private String title;
  private String content;
  private Date publishedAt;
}
