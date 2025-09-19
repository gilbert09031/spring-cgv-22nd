package com.ceos22.cgv_clone.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력 값입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C002", "유효하지 않은 타입입니다."),

    // 401 Unauthorized
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A001", "인증에 실패했습니다."),

    // 403 Forbidden
    AUTHORIZATION_FAILED(HttpStatus.FORBIDDEN, "A002", "권한이 없습니다."),

    // 404 Not Found
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "요청한 리소스를 찾을 수 없습니다."),
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "요청한 영화를 찾을 수 없습니다."),
    DIRECTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "해당 감독을 찾을 수 없습니다."),
    ACTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "M003", "해당 배우를 찾을 수 없습니다."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C006", "허용되지 않은 메서드입니다."),

    // 409 Conflict
    DUPLICATED_RESOURCE(HttpStatus.CONFLICT, "C008", "이미 존재하는 데이터입니다"),
    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C007", "서버 내부 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}