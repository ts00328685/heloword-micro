package com.heloword.frontendapi.service.vocab;

import java.util.List;
import com.heloword.common.model.dto.UserCustomWordDto;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoParseService {
  List<UserCustomWordDto> parseWordsFromPhoto(MultipartFile image);
}
