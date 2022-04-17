package com.heloword.frontendapi.model.response;

import java.util.List;
import com.heloword.common.entity.word.SentenceEnglishEntity;
import com.heloword.common.entity.word.SentenceGermanEntity;
import com.heloword.common.entity.word.SentenceJapaneseEntity;
import com.heloword.common.entity.word.WordEnglishEntity;
import com.heloword.common.entity.word.WordGermanEntity;
import com.heloword.common.entity.word.WordJapaneseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

  private List<WordEnglishEntity> wordEnglishList;
  private List<WordGermanEntity> wordGermanList;
  private List<WordJapaneseEntity> wordJapaneseList;
  private List<SentenceEnglishEntity> sentenceEnglishList;
  private List<SentenceJapaneseEntity> sentenceJapaneseList;
  private List<SentenceGermanEntity> sentenceGermanList;

}
