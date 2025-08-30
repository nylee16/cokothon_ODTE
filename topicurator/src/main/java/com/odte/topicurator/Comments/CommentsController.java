package com.odte.topicurator.Comments;

import com.odte.topicurator.auth.Domain.CustomUserDetails;
import com.odte.topicurator.common.dto.ApiResponse;
import com.odte.topicurator.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentsController {

    private final CommentsService commentsService;

    @GetMapping("/proscons/{prosnconsId}/comments")
    public ResponseEntity<Page<CommentsDto>> getComments(
            @PathVariable Long prosnconsId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CommentsDto> commentsPage = commentsService.getCommentsByProsnconsId(prosnconsId, sort, page, size);
        return ResponseEntity.ok(commentsPage);
    }

    @PostMapping("/proscons/{prosconsId}/comments")
    public ResponseEntity<CommentsDto> createComment(
            @PathVariable Long prosconsId,
            @RequestBody @Valid CommentsCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails  // JWT에서 인증된 CustomUserDetails
    ) {
        User user = userDetails.getUser(); // CustomUserDetails에서 실제 User 엔티티 가져오기
        CommentsDto created = commentsService.createComment(prosconsId, user, request);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        commentsService.deleteComment(commentId, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> likeComment(
            @PathVariable Long commentId,
            HttpServletRequest request
    ) {
        commentsService.likeComment(commentId);
        return ResponseEntity.ok(ApiResponse.successWithNoData("공감 처리 완료"));
    }

    @PostMapping("/comments/{commentId}/hate")
    public ResponseEntity<ApiResponse<Void>> hateComment(
            @PathVariable Long commentId,
            HttpServletRequest request
    ) {
        commentsService.hateComment(commentId);
        return ResponseEntity.ok(ApiResponse.successWithNoData("비공감 처리 완료"));
    }
}
