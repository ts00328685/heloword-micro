package com.heloword.common.feignclient;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.entity.word.SentenceEnglishEntity;
import com.heloword.common.entity.word.SentenceGermanEntity;
import com.heloword.common.entity.word.SentenceJapaneseEntity;
import com.heloword.common.entity.word.WordEnglishEntity;
import com.heloword.common.entity.word.WordGermanEntity;
import com.heloword.common.entity.word.WordJapaneseEntity;
import com.heloword.common.filter.FeignClientInterceptor;

@FeignClient(name = "SERVICE-WORD", url = "${feign.service-word.url:}", configuration = FeignClientInterceptor.class)
public interface ServiceWordClient {

  @GetMapping("/api/word-english")
  HeloResponse<List<WordEnglishEntity>> getAllEnWords();
  @GetMapping("/api/word-japanese")
  HeloResponse<List<WordJapaneseEntity>> getAllJpWords();
  @GetMapping("/api/word-german")
  HeloResponse<List<WordGermanEntity>> getAllGeWords();

  @GetMapping("/api/sentence-english")
  HeloResponse<List<SentenceEnglishEntity>> getAllEnSentences();
  @GetMapping("/api/sentence-japanese")
  HeloResponse<List<SentenceJapaneseEntity>> getAllJpSentences();
  @GetMapping("/api/sentence-german")
  HeloResponse<List<SentenceGermanEntity>> getAllGeSentences();
}
