package com.capstone.pethouse.domain.code.service;

import com.capstone.pethouse.domain.code.dto.CodeRequest;
import com.capstone.pethouse.domain.code.dto.CodeVo;
import com.capstone.pethouse.domain.code.entity.Code;
import com.capstone.pethouse.domain.code.repository.CodeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CodeService {

    private final CodeRepository codeRepository;

    @Transactional(readOnly = true)
    public Page<CodeVo> getCodes(Pageable pageable, String groupCode) {
        Page<Code> codePage;
        if (groupCode != null && !groupCode.isBlank()) {
            Code parent = codeRepository.findByCode(groupCode).orElse(null);
            codePage = codeRepository.findByParent(parent, pageable);
        } else {
            codePage = codeRepository.findAll(pageable);
        }

        return codePage.map(CodeVo::from);
    }

    @Transactional(readOnly = true)
    public List<CodeVo> getCodeTree(String groupCode) {
        List<Code> allCodes = codeRepository.findAll(Sort.by(Sort.Direction.ASC, "code"));

        Map<Long, List<Code>> groupByParents = allCodes.stream()
                .collect(Collectors.groupingBy(code ->
                        (code.getParent() == null) ? 0L : code.getParent().getSeq()
                ));

        List<Code> roots;
        if (groupCode == null || groupCode.isEmpty()) {
            roots = groupByParents.getOrDefault(0L, Collections.emptyList());
        } else {
            roots = allCodes.stream()
                    .filter(code -> code.getCode().equals(groupCode))
                    .collect(Collectors.toList());
        }

        return roots.stream()
                .map(root -> mapToTree(root, groupByParents))
                .toList();
    }

    private CodeVo mapToTree(Code parent, Map<Long, List<Code>> groupByParent) {
        List<CodeVo> children = groupByParent.getOrDefault(parent.getSeq(), Collections.emptyList())
                .stream()
                .map(code -> mapToTree(code, groupByParent))
                .toList();

        return CodeVo.withChildren(parent, children);
    }


    @Transactional(readOnly = true)
    public CodeVo getCode(Long seq) {
        Code code = codeRepository.findById(seq)
                .orElseThrow(() -> new EntityNotFoundException("코드를 찾을 수 없습니다."));

        return CodeVo.from(code);
    }

    @Transactional(readOnly = true)
    public CodeVo getCodeByCode(String codeStr) {
        Code code = codeRepository.findByCode(codeStr)
                .orElseThrow(() -> new EntityNotFoundException("코드를 찾을 수 없습니다."));

        return CodeVo.from(code);
    }

    @Transactional(readOnly = true)
    public List<CodeVo> getCodesByGroupCode(String groupCode) {
        Code parent = codeRepository.findByCode(groupCode).orElse(null);
        return codeRepository.findByParent(parent).stream()
                .map(CodeVo::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CodeVo createCode(CodeRequest request) {
        if (codeRepository.findByCode(request.code()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 코드입니다.");
        }

        Code parent = null;
        if (request.groupCode() != null && !request.groupCode().isBlank()) {
            parent = codeRepository.findByCode(request.groupCode())
                    .orElseThrow(() -> new EntityNotFoundException("부모 코드를 찾을 수 없습니다."));
        }

        Code code = Code.of(
                request.code(),
                parent,
                request.codeName()
        );
        return CodeVo.from(codeRepository.save(code));
    }

    @Transactional
    public CodeVo updateCode(CodeRequest request) {
        Code code = codeRepository.findByCode(request.code())
                .orElseThrow(() -> new EntityNotFoundException("코드를 찾을 수 없습니다."));

        Code parent = null;
        if (request.groupCode() != null && !request.groupCode().isBlank()) {
            parent = codeRepository.findByCode(request.groupCode())
                    .orElseThrow(() -> new EntityNotFoundException("부모 코드를 찾을 수 없습니다."));
        }

        code.update(parent, request.codeName());
        return CodeVo.from(code);
    }

    @Transactional
    public void deleteCode(Long seq) {
        Code code = codeRepository.findById(seq)
                .orElseThrow(() -> new EntityNotFoundException("코드를 찾을 수 없습니다."));

        codeRepository.delete(code);
    }

    @Transactional
    public void deleteCodeByCode(String codeStr) {
        Code code = codeRepository.findByCode(codeStr)
                .orElseThrow(() -> new EntityNotFoundException("코드를 찾을 수 없습니다."));

        codeRepository.delete(code);
    }
}
