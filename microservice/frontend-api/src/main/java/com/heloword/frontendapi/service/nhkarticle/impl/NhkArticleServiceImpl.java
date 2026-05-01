package com.heloword.frontendapi.service.nhkarticle.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heloword.common.entity.article.ArticleCrawlerEntity;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.frontendapi.model.response.NhkArticleDetailDto;
import com.heloword.frontendapi.model.response.NhkArticleListItemDto;
import com.heloword.frontendapi.model.response.NhkParagraphDto;
import com.heloword.frontendapi.model.response.NhkVocabItemDto;
import com.heloword.frontendapi.service.nhkarticle.NhkArticleService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class NhkArticleServiceImpl implements NhkArticleService {

  @Autowired
  private ServiceRecordClient serviceRecordClient;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public List<NhkArticleListItemDto> list() {
    List<ArticleCrawlerEntity> entities = serviceRecordClient.getNhkArticleList().getData();
    if (entities == null) return Collections.emptyList();
    return entities.stream()
        .map(NhkArticleListItemDto::from)
        .collect(Collectors.toList());
  }

  @Override
  public NhkArticleDetailDto getById(Long id) {
    ArticleCrawlerEntity entity = serviceRecordClient.getNhkArticleById(id).getData();
    if (entity == null) return null;

    NhkArticleDetailDto dto = new NhkArticleDetailDto();
    dto.setId(entity.getId());
    dto.setTitle(entity.getTitle());
    dto.setSourceUrl(entity.getSourceUrl());
    dto.setSourceLang(entity.getSourceLang());
    dto.setCreateDate(entity.getCreateDate());
    dto.setContentJa(entity.getContentJa());
    dto.setContentEn(entity.getContentEn());
    dto.setContentZh(entity.getContentZh());
    dto.setContentGrammar(entity.getContentGrammar());
    dto.setContentVocabulary(parseVocabulary(entity.getContentVocabulary()));
    dto.setParagraphs(parseParagraphs(entity.getParagraphs()));
    return dto;
  }

  private List<NhkVocabItemDto> parseVocabulary(String json) {
    if (json == null || json.isBlank()) return Collections.emptyList();
    try {
      return objectMapper.readValue(json, new TypeReference<List<NhkVocabItemDto>>() {});
    } catch (Exception e) {
      log.warn("Failed to parse content_vocabulary JSON: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  private List<NhkParagraphDto> parseParagraphs(String json) {
    if (json == null || json.isBlank()) return Collections.emptyList();
    try {
      return objectMapper.readValue(json, new TypeReference<List<NhkParagraphDto>>() {});
    } catch (Exception e) {
      log.warn("Failed to parse paragraphs JSON: {}", e.getMessage());
      return Collections.emptyList();
    }
  }
}
