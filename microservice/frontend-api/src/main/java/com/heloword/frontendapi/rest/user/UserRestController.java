package com.heloword.frontendapi.rest.user;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.lang3.StringUtils;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.common.entity.user.MemberEntity;
import com.heloword.common.feignclient.ServiceUserClient;
import com.heloword.common.util.Util;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/user")
@AllArgsConstructor
public class UserRestController extends AbstractBaseFrontendRestController {

  private final ServiceUserClient serviceUserClient;

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping()
  public HeloResponse<?> getLoggedInUser() {
    return success(Map.of("user", getUser().orElse(null)));
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PutMapping("/nickname")
  public HeloResponse<?> updateNickname(@RequestBody Map<String, String> body, HttpServletRequest request) {
    String nickname = body.getOrDefault("nickname", "").trim();
    if (StringUtils.isBlank(nickname)) return fail("Nickname must not be blank");
    var user = getUser().orElseThrow();
    MemberEntity member = serviceUserClient.getMemberByEmail(user.getEmail()).getData();
    if (member == null) return fail("User not found");
    member.setNickname(nickname);
    serviceUserClient.createOrUpdatMember(member);
    // Refresh the Redis session so the new nickname survives page reloads.
    // Without this the filter re-reads the old MemberEntity from Redis on the
    // next request and getLoggedInUser() returns the pre-change nickname.
    String idToken = Util.getIdTokenFromRequest(request);
    if (StringUtils.isNotEmpty(idToken)) {
      userSessionUtil.saveUserToSessionByKey(idToken, member);
    }
    user.setNickname(nickname);
    return success(Map.of("user", user));
  }

}
