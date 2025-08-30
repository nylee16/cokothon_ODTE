package com.odte.topicurator.proscons.controller;

import com.odte.topicurator.common.dto.ApiResponse;
import com.odte.topicurator.proscons.application.ProsConsQueryService;
import com.odte.topicurator.proscons.controller.dto.ProsConsRes;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProsConsQueryController {

    private final ProsConsQueryService service;
    private static final Set<String> ALLOWED_SORT = Set.of("id", "bias"); // proscons 컬럼 기준

    @Operation(summary = "해당 카드의 요약", description = "정렬 예: sort=id,desc | 허용: id, bias")
    @GetMapping("/news/{newsId}/proscons")
    public ResponseEntity<ApiResponse<Page<ProsConsRes>>> list(
            @PathVariable Long newsId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 정렬 화이트리스트
        for (Sort.Order o : pageable.getSort()) {
            if (!ALLOWED_SORT.contains(o.getProperty())) {
                throw new IllegalArgumentException("invalid sort property: " + o.getProperty());
            }
        }
        // page 상한 (선택)
        if (pageable.getPageNumber() > 100_000) {
            throw new IllegalArgumentException("page too large: " + pageable.getPageNumber());
        }

        var page = service.listByNews(newsId, pageable);
        return ResponseEntity.ok(ApiResponse.success("조회 성공", page));
    }

    @Operation(summary = "카드 단건 조회")
    @GetMapping("/proscons/{prosconsId}")
    public ResponseEntity<ApiResponse<ProsConsRes>> get(@PathVariable("prosconsId") Long id) {
        return ResponseEntity.ok(ApiResponse.success("조회 성공", service.get(id)));
    }
}
