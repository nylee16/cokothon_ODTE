package com.odte.topicurator.proscons.application;

import com.odte.topicurator.proscons.controller.dto.ProsConsRes;
import com.odte.topicurator.proscons.domain.ProsCons;
import com.odte.topicurator.proscons.infrastructure.ProsConsRepository;
import com.odte.topicurator.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProsConsQueryService {

    private final ProsConsRepository prosConsRepo;
    private final NewsRepository newsRepo;

    @Transactional(readOnly = true)
    public Page<ProsConsRes> listByNews(Long newsId, Pageable pageable) {
        // 뉴스가 반드시 DB에 있어야 한다면 존재 확인 후 404
        if (!newsRepo.existsById(newsId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "뉴스를 찾을 수 없습니다. ID=" + newsId);
        }
        return prosConsRepo.findByNews_Id(newsId, pageable).map(this::toRes);
    }

    @Transactional(readOnly = true)
    public ProsConsRes get(Long id) {
        ProsCons pc = prosConsRepo.findWithJoinsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "찬반/요약을 찾을 수 없습니다. ID=" + id));
        return toRes(pc);
    }

    private ProsConsRes toRes(ProsCons pc) {
        // 연관 값이 null일 가능성에 방어코드 (fetch join 있어도 데이터 결손 대비)
        Long newsId   = pc.getNews() != null ? pc.getNews().getId() : null;
        Long userId   = (pc.getCreatedBy() != null) ? pc.getCreatedBy().getId() : null;
        String uname  = (pc.getCreatedBy() != null) ? pc.getCreatedBy().getUsername() : null;

        return new ProsConsRes(
                pc.getId(),
                newsId,
                userId,
                uname,
                pc.getSummary(),
                pc.getLink(),
                pc.getPros(),
                pc.getNeutral(),
                pc.getCons(),
                pc.getBias()
        );
    }
}
