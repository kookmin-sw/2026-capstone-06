package com.capstone.pethouse.domain.code.service;

import com.capstone.pethouse.domain.code.dto.CodeRequest;
import com.capstone.pethouse.domain.code.dto.CodeVo;
import com.capstone.pethouse.domain.code.entity.Code;
import com.capstone.pethouse.domain.code.repository.CodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CodeService {

    private final CodeRepository codeRepository;

    @Transactional(readOnly = true)
    public Page<CodeVo> getCodes(int pageNum, int pageSize, String groupCode) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "regDate"));
        if (groupCode != null && !groupCode.isBlank()) {
            return codeRepository.findByGroupCode(groupCode, pageRequest).map(CodeVo::from);
        }
        return codeRepository.findAll(pageRequest).map(CodeVo::from);
    }

    @Transactional(readOnly = true)
    public List<CodeVo> getCodeTree(String groupCode) {
        List<Code> allCodes = codeRepository.findAll();

        // 그룹코드가 빈 문자열인 것이 최상위 코드 (부모)
        // 자식 코드는 groupCode가 부모의 code와 같은 것
        Map<String, List<Code>> childrenMap = allCodes.stream()
                .filter(c -> c.getGroupCode() != null && !c.getGroupCode().isEmpty())
                .collect(Collectors.groupingBy(Code::getGroupCode));

        List<Code> roots;
        if (groupCode != null && !groupCode.isBlank()) {
            roots = allCodes.stream()
                    .filter(c -> c.getCode().equals(groupCode))
                    .toList();
        } else {
            roots = allCodes.stream()
                    .filter(c -> c.getGroupCode() == null || c.getGroupCode().isEmpty())
                    .toList();
        }

        return roots.stream()
                .map(root -> {
                    List<CodeVo> children = childrenMap.getOrDefault(root.getCode(), List.of())
                            .stream()
                            .map(CodeVo::from)
                            .toList();
                    return CodeVo.withChildren(root, children);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CodeVo getCode(String id) {
        Code code = codeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("코드를 찾을 수 없습니다."));
        return CodeVo.from(code);
    }

    @Transactional(readOnly = true)
    public List<CodeVo> getCodesByGroupCode(String groupCode) {
        return codeRepository.findByGroupCode(groupCode).stream()
                .map(CodeVo::from)
                .toList();
    }

    @Transactional
    public CodeVo createCode(CodeRequest request) {
        if (codeRepository.existsById(request.code())) {
            throw new IllegalArgumentException("이미 존재하는 코드입니다.");
        }
        Code code = Code.of(request.code(), request.groupCode(), request.codeName());
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
