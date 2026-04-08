package com.heloword.frontendapi.rest.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        .wordEnglishList(randomSlice(r.getWordEnglishList(), n))
        .wordGermanList(randomSlice(r.getWordGermanList(), n))
        .wordJapaneseList(randomSlice(r.getWordJapaneseList(), n))
        .wordJapaneseVerbList(randomSlice(r.getWordJapaneseVerbList(), n))
        .sentenceEnglishList(randomSlice(r.getSentenceEnglishList(), n))
        .sentenceGermanList(randomSlice(r.getSentenceGermanList(), n))
        .sentenceJapaneseList(randomSlice(r.getSentenceJapaneseList(), n))
        .build();
  }

  private <T> List<T> randomSlice(List<T> list, int n) {
    if (list == null || list.isEmpty()) return list;
    List<T> shuffled = new ArrayList<>();
    int min = Math.min(n, list.size());
    Random rand = new Random();
    for (int i = 0; i < n; i++) {
        int randomIndex = rand.nextInt(list.size());
        shuffled.add(list.get(randomIndex));
    }
    return shuffled.subList(0, min);
  }

}
