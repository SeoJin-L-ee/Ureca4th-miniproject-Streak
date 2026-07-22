package com.example.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//내 정보 수정 - 이름, 폰 번호, 비밀번호
public record UpdateMemberReqDto(@NotBlank(message = "이름은 필수입니다.") String name, String phone, String currentPassword,   //비밀번호를 안 바꾸면 null
									@Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
									@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "비밀번호는 영문과 숫자를 각각 하나 이상 포함해야 합니다.")
									String newPassword
									) {

}
