package com.capstone.pethouse.domain.User.service;

import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.User.repository.UserRepository;
import com.capstone.pethouse.domain.User.dto.*;
import com.capstone.pethouse.domain.enums.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<MemberResponse> getMembers(int pageNum, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "seq"));
        return userRepository.findAll(pageRequest).map(MemberResponse::from);
    }

    @Transactional
    public MemberResponse register(MemberRequest request) {
        if (userRepository.existsByMemberId(request.memberId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        User user = User.ofUser(
                request.memberId(),
                passwordEncoder.encode(request.memberPw()),
                request.memberName(),
                request.memberPhone()
        );
        return MemberResponse.from(userRepository.save(user));
    }

    @Transactional
    public MemberResponse registerByAdmin(MemberRequest request) {
        if (userRepository.existsByMemberId(request.memberId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        RoleType roleCode = request.roleCode() != null ? RoleType.valueOf(request.roleCode()) : RoleType.USER;
        User user = User.of(
                request.memberId(),
                passwordEncoder.encode(request.memberPw()),
                request.memberName(),
                request.memberPhone(),
                roleCode
        );
        return MemberResponse.from(userRepository.save(user));
    }

    @Transactional
    public MemberResponse updateByAdmin(MemberRequest request) {
        User user = userRepository.findById(request.seq())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        String encodedPw = (request.memberPw() != null && !request.memberPw().isBlank())
                ? passwordEncoder.encode(request.memberPw()) : null;
        RoleType roleCode = request.roleCode() != null ? RoleType.valueOf(request.roleCode()) : null;

        user.update(encodedPw, request.memberName(), request.memberPhone(), roleCode);
        return MemberResponse.from(user);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberBySeq(Long seq) {
        User user = userRepository.findById(seq).orElse(null);
        return user != null ? MemberResponse.from(user) : null;
    }

    @Transactional(readOnly = true)
    public MemberSimpleResponse getMemberByMemberId(String memberId) {
        User user = userRepository.findByMemberId(memberId).orElse(null);
        return user != null ? MemberSimpleResponse.from(user) : null;
    }

    @Transactional(readOnly = true)
    public boolean checkIdAvailable(String memberId) {
        return !userRepository.existsByMemberId(memberId);
    }

    @Transactional
    public MemberResponse updateMember(MemberRequest request) {
        User user = userRepository.findById(request.seq())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        String encodedPw = (request.memberPw() != null && !request.memberPw().isBlank())
                ? passwordEncoder.encode(request.memberPw()) : null;
        RoleType roleCode = request.roleCode() != null ? RoleType.valueOf(request.roleCode()) : null;

        user.update(encodedPw, request.memberName(), request.memberPhone(), roleCode);
        return MemberResponse.from(user);
    }

    @Transactional
    public void deleteMember(Long seq, String memberId) {
        if (seq != null) {
            userRepository.deleteById(seq);
        } else if (memberId != null) {
            User user = userRepository.findByMemberId(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
            userRepository.delete(user);
        } else {
            throw new IllegalArgumentException("seq 또는 member_id가 필요합니다.");
        }
    }

    @Transactional(readOnly = true)
    public String findId(FindIdRequest request) {
        return userRepository.findByMemberNameAndMemberPhone(request.memberName(), request.memberPhone())
                .map(User::getMemberId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean verifyUser(VerifyUserRequest request) {
        return userRepository.findByMemberIdAndMemberNameAndMemberPhone(
                request.memberId(), request.memberName(), request.memberPhone()
        ).isPresent();
    }

    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByMemberIdAndMemberNameAndMemberPhone(
                request.memberId(), request.memberName(), request.memberPhone()
        ).orElse(null);

        if (user == null) return false;

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        return true;
    }
}
