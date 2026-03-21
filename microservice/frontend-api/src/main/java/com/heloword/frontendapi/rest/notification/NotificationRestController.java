package com.heloword.frontendapi.rest.notification;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.frontendapi.service.notification.NotificationService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/notifications")
@AllArgsConstructor
public class NotificationRestController extends AbstractBaseFrontendRestController {

  private NotificationService notificationService;

  /**
   * Returns all words due for review based on the Ebbinghaus forgetting curve.
   * Response: List<DueWordDto> — only words whose nextReviewTime <= now.
   */
  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/due-for-review")
  public HeloResponse<?> getDueForReview() {
    return HeloResponse.successWithData(notificationService.getDueForReview(getUser().get()));
  }
}
