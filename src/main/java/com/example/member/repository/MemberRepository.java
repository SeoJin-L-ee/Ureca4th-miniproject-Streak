package com.example.member.repository;

import com.example.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

	//회원가입 전 이메일 중복 여부 체크
    boolean existsByEmail(String email);

    //로그인 시 이메일로 회원을 찾고 인증 정보 만들기
    Optional<Member> findByEmail(String email);
}