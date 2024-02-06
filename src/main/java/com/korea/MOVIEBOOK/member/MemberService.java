package com.korea.MOVIEBOOK.member;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    public Member create(String username, String password, String nickname, String email) {
        Member member = new Member();
        member.setUsername(username);
        member.setPassword(passwordEncoder.encode(password));
        member.setNickname(nickname);
        member.setEmail(email);
        this.memberRepository.save(member);
        return member;
    }

    public boolean nicknameUnique(String nickname) {
        return memberRepository.findByNickname(nickname).isEmpty();
    }

    public void verifyEmail(long memberId) {
        Optional<Member> memberOpt = memberRepository.findById(memberId);
        // memberId를 통해 특정 member 데이터 조회
        memberOpt.ifPresent(member -> { // 해당 memberId가 존재하면 데이터 가져옴
            member.setVerified(true);  // 이메일 인증 완료
            memberRepository.save(member);  // 업데이트된 사용자 저장
        });
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElse(null);
    }

    public void resetPassword(Member member) throws MessagingException {
        String temporaryPassword = UUID.randomUUID().toString().substring(0, 8); // 8자리 랜덤 문자열
        String encodedPassword = passwordEncoder.encode(temporaryPassword);
        member.setPassword(encodedPassword); // 임시 비밀번호 설정
        memberRepository.save(member);

        // 이메일 전송 로직에서 예외가 발생할 경우, 메서드를 호출한 곳으로 예외를 전파합니다.
        emailService.sendTemporaryPassword(member.getEmail(), temporaryPassword);
    }

    public Member findByproviderId(String id){
        return this.memberRepository.findByproviderId(id);
    }


    public Member getMember (String username) {
        Optional<Member> member = memberRepository.findByUsername(username);

        if (member.isPresent()) {
            return member.get();
        } else {
            // findByUsername으로 조회한 결과가 null일 때, providerId로 다시 조회
            Member memberByProviderId = memberRepository.findByProviderId(username);  // 여기서 username을 providerId로 사용하였으므로 주의
            return memberByProviderId;
        }
    }

    public Member findByusername(String username) {
        if (memberRepository.findByUsername(username).isPresent()) {
            return memberRepository.findByUsername(username).get();
        } else {
            return null;
        }
    }

    public void changePassword(Member member, String password) {
        member.setPassword(passwordEncoder.encode(password));
        memberRepository.save(member);
    }

    public void updateNickname(Member member, String nickname) {
        member.setNickname(nickname);
        memberRepository.save(member);
    }

    public void deleteMember(Member member) {
        memberRepository.delete(member);
    }

    public Optional<Member> findById(Long id) {
        return this.memberRepository.findById(id);
    }

    public void saveImg(Member member, String profileImage) {
        member.setProfileImage(profileImage);
        this.memberRepository.save(member);
    }


}