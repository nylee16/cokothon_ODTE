package com.odte.topicurator.Comments;

import com.odte.topicurator.entity.Comments;
import com.odte.topicurator.entity.Prosncons;
import com.odte.topicurator.entity.User;
import com.odte.topicurator.repository.CommentsRepository;
import com.odte.topicurator.repository.ProsnconsRepository;
import com.odte.topicurator.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final ProsnconsRepository prosnconsRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentsDto createComment(Long prosconsId, User user, CommentsCreateRequest request) {
        Prosncons prosCons = prosnconsRepository.findById(prosconsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        Comments comment = new Comments();
        comment.setProsncons(prosCons);
        comment.setUser(user);
        comment.setContent(request.content());
        comment.setChoice(request.choice());
        comment.setLikeCount(0L);
        comment.setHateCount(0L);
        comment.setCreatedAt(LocalDateTime.now());

        Comments saved = commentsRepository.save(comment);

        return new CommentsDto(
                saved.getId(),
                prosCons.getId(),
                user.getId(),
                saved.getContent(),
                user.getUsername(),
                saved.getChoice(),
                saved.getLikeCount(),
                saved.getHateCount(),
                saved.getCreatedAt()
        );
    }

    public Page<CommentsDto> getCommentsByProsnconsId(Long prosnconsId, String sort, int page, int size) {
        Sort sortOrder = "likes".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.DESC, "likeCount")
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 엔티티를 DTO로 변환
        return commentsRepository.findByProsnconsIdWithUser(prosnconsId, pageable)
                .map(comment -> new CommentsDto(
                        comment.getId(),
                        comment.getProsncons().getId(),
                        comment.getUser().getId(),
                        comment.getContent(),
                        comment.getUser().getUsername(),
                        comment.getChoice(),
                        comment.getLikeCount(),
                        comment.getHateCount(),
                        comment.getCreatedAt()
                ));
    }

    public void deleteComment(Long commentId, User user) {
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("권한이 없습니다.");
        }

        commentsRepository.delete(comment);
    }

    @Transactional
    public void likeComment(Long commentId) {
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentsRepository.save(comment);
    }

    @Transactional
    public void hateComment(Long commentId) {
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        comment.setHateCount(comment.getHateCount() + 1);
        commentsRepository.save(comment);
    }

}
