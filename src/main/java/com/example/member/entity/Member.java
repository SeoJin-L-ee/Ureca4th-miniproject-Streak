package com.example.member.entity;

import com.example.global.entity.BaseEntity;
import com.example.member.entity.enums.MemberStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "members")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //이메일 중복 가입방지를 위한 unique추가
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;
    
    //회원가입에만 사용하는 생성 메서드.
    //status를 항상 ACTIVE로 지정해 가입 직후 로그인 가능하게
    public static Member create(
            String email,
            String encodedPassword,
            String name,
            String phone
    ) {
        return Member.builder()
	                .email(email)
	                .password(encodedPassword)
	                .name(name)
	                .phone(phone)
	                .status(MemberStatus.ACTIVE)
	                .build();
    }
}
