package com.capstone.pethouse.domain.User.controller;

import com.capstone.pethouse.domain.User.dto.*;
import com.capstone.pethouse.domain.User.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/member")
@RestController
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/list")
    public ResponseEntity<Page<MemberResponse>> list(
            @PageableDefault(size = 15, sort = "seq", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchQuery) {
        return ResponseEntity.ok(memberService.getMembers(searchType, searchQuery, pageable));
    }

    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(@Valid @RequestBody MemberRequest request) {
        MemberResponse response = memberService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/form")
    public ResponseEntity<MemberResponse> registerByAdmin(@Valid @RequestBody MemberRequest request) {
        MemberResponse response = memberService.registerByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/form")
    public ResponseEntity<MemberResponse> updateByAdmin(@Valid @RequestBody MemberRequest request) {
        MemberResponse response = memberService.updateByAdmin(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/form/{seq}")
    public ResponseEntity<MemberResponse> getMemberBySeq(@PathVariable Long seq) {
        return ResponseEntity.ok(memberService.getMemberBySeq(seq));
    }

    @GetMapping("/id/{memberId}")
    public ResponseEntity<MemberSimpleResponse> getMemberByMemberId(@PathVariable String memberId) {
        return ResponseEntity.ok(memberService.getMemberByMemberId(memberId));
    }

    @GetMapping("/checkId")
    public ResponseEntity<Map<String, Boolean>> checkId(@RequestParam String memberId) {
        boolean available = memberService.checkIdAvailable(memberId);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @PutMapping
    public ResponseEntity<MemberResponse> updateMember(@Valid @RequestBody MemberRequest request) {
        MemberResponse response = memberService.updateMember(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteMember(@RequestBody MemberDeleteRequest request) {
        memberService.deleteMember(request);
        return ResponseEntity.ok(Map.of("success", true, "message", "회원 정보가 삭제되었습니다."));
    }

    @PostMapping("/find-id")
    public ResponseEntity<Map<String, Object>> findId(@Valid @RequestBody FindIdRequest request) {
        String memberId = memberService.findId(request);
        if (memberId != null) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "memberId", memberId,
                    "message", "회원 아이디를 찾았습니다."
            ));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "가입된 아이디 없음"));
    }

    @PostMapping("/verify-user")
    public ResponseEntity<Map<String, Boolean>> verifyUser(@Valid @RequestBody VerifyUserRequest request) {
        boolean success = memberService.verifyUser(request);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean success = memberService.resetPassword(request);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다."));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "회원 정보가 일치하지 않습니다."));
    }
}
