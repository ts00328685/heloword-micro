package com.heloword.frontendapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunArticleDto {
  private String word;
  private String content;
}
