package com.capstone.pethouse.domain.User.service;

import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.User.repository.UserRepository;
import com.capstone.pethouse.domain.User.dto.request.FindIdRequest;
import com.capstone.pethouse.domain.User.dto.request.MemberDeleteRequest;
import com.capstone.pethouse.domain.User.dto.request.MemberModifyRequest;
import com.capstone.pethouse.domain.User.dto.request.MemberRegisterRequest;
import com.capstone.pethouse.domain.User.dto.request.ResetPasswordRequest;
import com.capstone.pethouse.domain.User.dto.request.VerifyUserRequest;
import com.capstone.pethouse.domain.User.dto.response.MemberResponse;
import com.capstone.pethouse.domain.User.dto.response.MemberSimpleResponse;
import com.capstone.pethouse.domain.enums.RoleType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class MemberService {

    private static final Set<String> VALID_SEARCH_TYPES =
            Set.of("memberId", "memberName", "memberPhone", "roleCode");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<MemberResponse> getMembers(String searchType, String searchQuery, Pageable pageable) {
        if (searchType != null && !VALID_SEARCH_TYPES.contains(searchType)) {
            throw new IllegalArgumentException("유효하지 않은 검색 타입입니다: " + searchType);
        }
        String cleanedQuery = (searchQuery != null && !searchQuery.isBlank()) ? searchQuery : null;

        return userRepository.findAllWithSearch(searchType, cleanedQuery, pageable).map(MemberResponse::from);
    }

    @Transactional
    public MemberResponse register(MemberRegisterRequest request) {
        if (userRepository.existsByMemberId(request.memberId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        User user = User.ofUser(
                request.memberId(),
                passwordEncoder.encode(request.memberPw()),
                request.memberName(),
                request.memberPhone());
        return MemberResponse.from(userRepository.save(user));
    }

    @Transactional
    public MemberResponse registerByAdmin(MemberRegisterRequest request) {
        if (userRepository.existsByMemberId(request.memberId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        RoleType roleCode = request.roleCode() != null ? RoleType.valueOf(request.roleCode()) : RoleType.USER;
        User user = User.of(
                request.memberId(),
                passwordEncoder.encode(request.memberPw()),
                request.memberName(),
                request.memberPhone(),
                roleCode);
        return MemberResponse.from(userRepository.save(user));
    }

    @Transactional
    public MemberResponse updateMember(MemberModifyRequest request) {
        User user = userRepository.findById(request.seq())
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        String encodedPw = (request.memberPw() != null && !request.memberPw().isBlank())
                ? passwordEncoder.encode(request.memberPw())
                : null;
        RoleType roleCode = request.roleCode() != null ? RoleType.valueOf(request.roleCode()) : null;

        user.update(encodedPw, request.memberName(), request.memberPhone(), roleCode);
        return MemberResponse.from(user);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberBySeq(Long seq) {
        User user = userRepository.findById(seq)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        return MemberResponse.from(user);
    }

    @Transactional(readOnly = true)
    public MemberSimpleResponse getMemberByMemberId(String memberId) {
        User user = userRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        return MemberSimpleResponse.from(user);
    }

    @Transactional(readOnly = true)
    public boolean checkIdAvailable(String memberId) {
        return !userRepository.existsByMemberId(memberId);
    }

    @Transactional
    public void deleteMember(MemberDeleteRequest request) {
        Long seq = request.seq();
        String memberId = request.memberId();

        if (seq != null) {
            User user = userRepository.findById(seq)
                    .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
            userRepository.delete(user);
        } else if (memberId != null) {
            User user = userRepository.findByMemberId(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
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
        return userRepository.existsByMemberIdAndMemberNameAndMemberPhone(
                request.memberId(), request.memberName(), request.memberPhone());
    }

    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByMemberIdAndMemberNameAndMemberPhone(
                request.memberId(), request.memberName(), request.memberPhone())
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        return true;
    }
}
