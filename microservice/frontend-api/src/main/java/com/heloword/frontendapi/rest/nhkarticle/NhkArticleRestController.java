package com.heloword.frontendapi.rest.nhkarticle;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;
import com.heloword.frontendapi.model.response.NhkArticleDetailDto;
import com.heloword.frontendapi.service.nhkarticle.NhkArticleService;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/nhk-article")
@AllArgsConstructor
public class NhkArticleRestController extends AbstractBaseFrontendRestController {

  private NhkArticleService nhkArticleService;

  @GetMapping("/list")
  public HeloResponse<?> list() {
    return success(nhkArticleService.list());
  }

  @GetMapping("/{id}")
  public HeloResponse<?> getById(@PathVariable Long id) {
    NhkArticleDetailDto detail = nhkArticleService.getById(id);
    if (detail == null) {
      return fail("Article not found");
    }
    return success(detail);
  }
}
