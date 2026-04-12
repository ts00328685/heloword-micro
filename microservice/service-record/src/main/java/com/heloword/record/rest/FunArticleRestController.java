package com.heloword.record.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.funarticle.FunArticleEntity;
import com.heloword.record.service.FunArticleService;

@RestController
@RequestMapping("/fun-article")
public class FunArticleRestController extends AbstractBaseRestController<FunArticleEntity, Long> {

  @Autowired
  private FunArticleService funArticleService;

  @Override
  public IBaseService<FunArticleEntity, Long> getService() {
    return funArticleService;
  }

  @PostMapping("/save")
  public HeloResponse<?> saveFunArticle(@RequestBody FunArticleEntity entity) {
    return success(funArticleService.save(entity));
  }

  @GetMapping("/random")
  public HeloResponse<?> getAll() {
    return success(funArticleService.findAll());
  }
}
