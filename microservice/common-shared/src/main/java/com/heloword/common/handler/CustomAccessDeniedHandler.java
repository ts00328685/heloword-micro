package com.heloword.common.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.type.ResponseCode;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Autowired
  ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
    AccessDeniedException accessDeniedException) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
    response.getWriter().write(objectMapper.writeValueAsString(HeloResponse.fail(ResponseCode.INSUFFICIENT_AUTHORITY)));
    response.getWriter().flush();
    response.getWriter().close();
  }

}
