package com.capstone.pethouse.domain.code.controller;

import com.capstone.pethouse.domain.code.dto.CodeRequest;
import com.capstone.pethouse.domain.code.dto.CodeVo;
import com.capstone.pethouse.domain.code.service.CodeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/code")
@RestController
public class CodeController {

    private final CodeService codeService;

    @GetMapping("/list")
    public ResponseEntity<Page<CodeVo>> list(
            @PageableDefault(size = 15, sort = "regDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String groupCode) {
        return ResponseEntity.ok(codeService.getCodes(pageable, groupCode));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CodeVo>> tree(
            @RequestParam(required = false) String groupCode) {
        return ResponseEntity.ok(codeService.getCodeTree(groupCode));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeVo> getCode(@PathVariable String id) {
        return ResponseEntity.ok(codeService.getCode(id));
    }

    @PostMapping
    public ResponseEntity<CodeVo> createCode(@RequestBody CodeRequest request) {
        CodeVo response = codeService.createCode(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<CodeVo> updateCode(@RequestBody CodeRequest request) {
        CodeVo response = codeService.updateCode(request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCode(@PathVariable String id) {
        codeService.deleteCode(id);
        return ResponseEntity.noContent().build();
    }
}
