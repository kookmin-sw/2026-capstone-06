package com.capstone.pethouse.domain.code.service;

import com.capstone.pethouse.domain.code.dto.CodeRequest;
import com.capstone.pethouse.domain.code.dto.CodeVo;
import com.capstone.pethouse.domain.code.entity.Code;
import com.capstone.pethouse.domain.code.repository.CodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CodeService {

    private final CodeRepository codeRepository;

    @Transactional(readOnly = true)
    public Page<CodeVo> getCodes(Pageable pageable, String groupCode) {
        Page<Code> codePage = (groupCode != null && !groupCode.isBlank())
                ? codeRepository.findByGroupCode(groupCode, pageable)
                : codeRepository.findAll(pageable);

        return codePage.map(CodeVo::from);
    }

    @Transactional(readOnly = true)
    public List<CodeVo> getCodeTree(String groupCode) {
        List<Code> allCodes = codeRepository.findAll(Sort.by(Sort.Direction.ASC, "code"));

        if (groupCode == null || groupCode.isEmpty()) {
            return allCodes.stream()
                    .filter(c -> c.getGroupCode() == null || c.getGroupCode().isEmpty())
                    .map(root -> mapToTree(root, allCodes))
                    .collect(Collectors.toList());
        } else {
            return allCodes.stream()
                    .filter(c -> c.getCode().equals(groupCode))
                    .map(root -> mapToTree(root, allCodes))
                    .collect(Collectors.toList());
        }
    }

    private CodeVo mapToTree(Code parent, List<Code> allCodes) {
        List<CodeVo> children = allCodes.stream()
                .filter(c -> parent.getCode().equals(c.getGroupCode()))
                .map(c -> mapToTree(c, allCodes))
                .collect(Collectors.toList());
        return CodeVo.withChildren(parent, children);
    }

    @Transactional(readOnly = true)
    public CodeVo getCode(String id) {
        Code code = codeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("코드를 찾을 수 없습니다."));

        List<Code> allCodes = codeRepository.findAll(Sort.by(Sort.Direction.ASC, "code"));
        return mapToTree(code, allCodes);
    }

    @Transactional(readOnly = true)
    public List<CodeVo> getCodesByGroupCode(String groupCode) {
        return codeRepository.findByGroupCode(groupCode).stream()
                .map(CodeVo::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CodeVo createCode(CodeRequest request) {
        if (codeRepository.existsById(request.code())) {
            throw new IllegalArgumentException("이미 존재하는 코드입니다.");
        }
        Code code = Code.of(request.code(), request.groupCode() != null ? request.groupCode() : "", request.codeName());
        return CodeVo.from(codeRepository.save(code));
    }

    @Transactional
    public CodeVo updateCode(CodeRequest request) {
        Code code = codeRepository.findById(request.code())
                .orElseThrow(() -> new IllegalArgumentException("코드를 찾을 수 없습니다."));
        code.update(request.groupCode(), request.codeName());
        return CodeVo.from(code);
    }

    @Transactional
    public void deleteCode(String id) {
        if (!codeRepository.existsById(id)) {
            throw new IllegalArgumentException("코드를 찾을 수 없습니다.");
        }
        codeRepository.deleteById(id);
    }
}
