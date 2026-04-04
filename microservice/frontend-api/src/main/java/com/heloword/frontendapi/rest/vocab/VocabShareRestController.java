package com.heloword.frontendapi.rest.vocab;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.common.model.dto.VocabShareRequestDto;
import com.heloword.frontendapi.service.vocab.VocabShareFrontendService;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/vocab-share")
@AllArgsConstructor
@PreAuthorize("hasAnyAuthority('MEMBER')")
public class VocabShareRestController extends AbstractBaseFrontendRestController {

  private VocabShareFrontendService vocabShareService;

  @PostMapping
  public HeloResponse<VocabShareRequestDto> sendShare(@RequestBody VocabShareRequestDto dto) {
    return HeloResponse.successWithData(vocabShareService.sendShare(getUser().get(), dto));
  }

  @GetMapping("/inbox")
  public HeloResponse<List<VocabShareRequestDto>> getInbox() {
    return HeloResponse.successWithData(vocabShareService.getInbox(getUser().get()));
  }

  @PostMapping("/{id}/accept")
  public HeloResponse<VocabShareRequestDto> acceptShare(@PathVariable Long id) {
    return HeloResponse.successWithData(vocabShareService.acceptShare(getUser().get(), id));
  }

  @PostMapping("/{id}/reject")
  public HeloResponse<?> rejectShare(@PathVariable Long id) {
    vocabShareService.rejectShare(getUser().get(), id);
    return HeloResponse.successWithoutData();
  }
}
