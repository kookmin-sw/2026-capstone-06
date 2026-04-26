package com.capstone.pethouse.domain.code.controller;

import com.capstone.pethouse.domain.code.dto.CodeRequest;
import com.capstone.pethouse.domain.code.dto.CodeVo;
import com.capstone.pethouse.domain.code.service.CodeService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<CodeVo>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "15") int pageSize,
            @RequestParam(required = false) String groupCode) {
        return ResponseEntity.ok(codeService.getCodes(pageNum, pageSize, groupCode));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CodeVo>> tree(
            @RequestParam(required = false) String groupCode) {
        return ResponseEntity.ok(codeService.getCodeTree(groupCode));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCode(@PathVariable String id) {
        try {
            return ResponseEntity.ok(codeService.getCode(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createCode(@RequestBody CodeRequest request) {
        try {
            CodeVo response = codeService.createCode(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> updateCode(@RequestBody CodeRequest request) {
        try {
            CodeVo response = codeService.updateCode(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCode(@PathVariable String id) {
        try {
            codeService.deleteCode(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
