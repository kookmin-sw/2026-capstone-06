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
    public ResponseEntity<?> register(@RequestBody MemberRequest request) {
        try {
            MemberResponse response = memberService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/form")
    public ResponseEntity<?> registerByAdmin(@RequestBody MemberRequest request) {
        try {
            MemberResponse response = memberService.registerByAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/form")
    public ResponseEntity<?> updateByAdmin(@RequestBody MemberRequest request) {
        try {
            MemberResponse response = memberService.updateByAdmin(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/form/{seq}")
    public ResponseEntity<?> getMemberBySeq(@PathVariable Long seq) {
        MemberResponse response = memberService.getMemberBySeq(seq);
        return ResponseEntity.ok(response != null ? response : Map.of());
    }

    @GetMapping("/id/{memberId}")
    public ResponseEntity<?> getMemberByMemberId(@PathVariable String memberId) {
        MemberSimpleResponse response = memberService.getMemberByMemberId(memberId);
        return ResponseEntity.ok(response != null ? response : Map.of());
    }

    @GetMapping("/checkId")
    public ResponseEntity<Map<String, Boolean>> checkId(@RequestParam String memberId) {
        boolean available = memberService.checkIdAvailable(memberId);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @PutMapping
    public ResponseEntity<?> updateMember(@RequestBody MemberRequest request) {
        try {
            MemberResponse response = memberService.updateMember(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteMember(@RequestBody Map<String, Object> body) {
        try {
            Long seq = body.containsKey("seq") ? Long.valueOf(body.get("seq").toString()) : null;
            String memberId = body.containsKey("member_id") ? (String) body.get("member_id") : null;
            memberService.deleteMember(seq, memberId);
            return ResponseEntity.ok(Map.of("success", true, "message", "회원과 연결된 장치가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "회원 삭제 중 오류 발생"));
        }
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
