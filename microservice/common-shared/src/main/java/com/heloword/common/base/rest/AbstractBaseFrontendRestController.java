package com.heloword.common.base.rest;

import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.filter.ServiceAuthFilter;
import com.heloword.common.model.dto.UserDto;
import com.heloword.common.type.MemberRole;
import com.heloword.common.type.ResponseCode;

public abstract class AbstractBaseFrontendRestController{

	public static final String BASE_FRONTEND_API_URL = "/fe";
	public static final String MEMBER_ROLE = MemberRole.MEMBER.getName();

	protected Optional<UserDto> getUser() {
		ServiceAuthFilter.CustomUserDetails details = (ServiceAuthFilter.CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
		return Optional.ofNullable(details)
				.map(ServiceAuthFilter.CustomUserDetails::getMemberEntity)
				.map(UserDto::fromEntity);
	}

	protected HeloResponse<?> success() {
		return HeloResponse.successWithoutData();
	}

	protected HeloResponse<?> success(Object data) {
		return HeloResponse.successWithData(data);
	}

	protected HeloResponse<?> systemFailure() {
		return HeloResponse.fail(ResponseCode.SYSTEM_ERROR);
	}

	protected HeloResponse<?> fail(ResponseCode responseCode) {
		return HeloResponse.fail(responseCode);
	}

	protected HeloResponse<?> fail(String message) {
		return HeloResponse.fail(ResponseCode.SYSTEM_ERROR, message);
	}
}
