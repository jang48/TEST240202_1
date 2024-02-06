package com.korea.MOVIEBOOK.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCreateForm {

    @Size(min = 3, max = 25)
    @NotEmpty(message = "ID를 입력하세요.")
    private String username;

    @Size(min = 7, message = "비밀번호는 최소 7자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).*$", message = "비밀번호에는 최소한 하나의 특수문자가 필요합니다.")
    @NotEmpty(message = "비밀번호를 입력하세요.")
    private String password1;

    @NotEmpty(message = "비밀번호가 일치하지 않습니다.")
    private String password2;

    @NotEmpty(message = "닉네임을 입력하세요.")
    @Size(min = 2, max = 8)
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "한글, 영문 대,소문자, 숫자를 사용하여 2 ~ 8길이로 설정 해주세요.")
    private String nickname;

    @NotEmpty(message = "등록되지 않은 이메일입니다.")
    @Email
    private String email;
}
