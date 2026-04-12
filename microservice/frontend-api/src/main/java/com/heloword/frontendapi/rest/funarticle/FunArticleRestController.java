package com.heloword.frontendapi.rest.funarticle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;
import com.heloword.frontendapi.model.response.FunArticleDto;
import com.heloword.frontendapi.service.funarticle.FunArticleService;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/fun-article")
@AllArgsConstructor
public class FunArticleRestController extends AbstractBaseFrontendRestController {

  private FunArticleService funArticleService;

  /** Returns the latest article (preview use — truncation done on the frontend). */
  @GetMapping("/latest")
  public HeloResponse<?> getLatest() {
    return success(funArticleService.getLatest());
  }

  /** Returns up to 5 randomly selected cached articles. */
  @GetMapping("/all")
  public HeloResponse<?> getAll() {
    List<FunArticleDto> articles = funArticleService.getAll();
    if (CollectionUtils.isEmpty(articles)) {
        return success();
    }
    int count = Math.min(5, articles.size());
    Set<Integer> picked = new HashSet<>();
    Random random = new Random();
    List<FunArticleDto> result = new ArrayList<>();
    while (result.size() < count) {
        int idx = random.nextInt(articles.size());
        if (picked.add(idx)) {
            result.add(articles.get(idx));
        }
    }
    return success(result);
  }
}
