package com.heloword.frontendapi.rest.home;

import java.util.Collections;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.frontendapi.model.response.DashboardResponse;
import com.heloword.frontendapi.service.home.DashboardService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/home/dashboard")
@AllArgsConstructor
public class DashboardRestController extends AbstractBaseFrontendRestController {

  private DashboardService dashboardService;

  @PostMapping()
  public HeloResponse<?> getDashboardInfo(@RequestParam(defaultValue = "0") int previewSize) {
    DashboardResponse full = dashboardService.getDashboardResponse(getUser());
    if (previewSize > 0) {
      return success(sliceResponse(full, previewSize));
    }
    return success(full);
  }

  private DashboardResponse sliceResponse(DashboardResponse r, int n) {
    return DashboardResponse.builder()
        .wordEnglishList(slice(r.getWordEnglishList(), n))
        .wordGermanList(slice(r.getWordGermanList(), n))
        .wordJapaneseList(slice(r.getWordJapaneseList(), n))
        .wordJapaneseVerbList(slice(r.getWordJapaneseVerbList(), n))
        .sentenceEnglishList(slice(r.getSentenceEnglishList(), n))
        .sentenceGermanList(slice(r.getSentenceGermanList(), n))
        .sentenceJapaneseList(slice(r.getSentenceJapaneseList(), n))
        .build();
  }

  private <T> List<T> slice(List<T> list, int n) {
    if (list == null || list.isEmpty()) return list;
    Collections.shuffle(list);
    return list.subList(0, Math.min(n, list.size()));
  }

}
