package com.hbk.repository;

import com.hbk.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    // 오늘 가입한 신규 회원 수
    // (테이블 이름이 member인 경우. 만약 members라면 테이블명을 수정해주세요!)
    @Query(value = "SELECT COUNT(id) FROM member WHERE DATE(created_at) = CURDATE()", nativeQuery = true)
    Long countTodayNewMembers();
}
