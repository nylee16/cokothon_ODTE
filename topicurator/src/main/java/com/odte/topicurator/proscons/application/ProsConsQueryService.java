package com.odte.topicurator.proscons.application;

import com.odte.topicurator.proscons.controller.dto.ProsConsRes;
import com.odte.topicurator.proscons.domain.ProsCons;
import com.odte.topicurator.proscons.infrastructure.ProsConsRepository;
import com.odte.topicurator.repository.NewsRepository; // 네 기존 경로
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProsConsQueryService {

    private final ProsConsRepository prosConsRepo;
    private final NewsRepository newsRepo;

    @Transactional(readOnly = true)
    public Page<ProsConsRes> listByNews(Long newsId, Pageable pageable) {
        // 뉴스가 LLM만으로 존재하고 DB에 없을 수 있다면, 아래 exists 체크를 선택적으로 제거해도 됨.
        if (!newsRepo.existsById(newsId)) {
            throw new NoSuchElementException("뉴스를 찾을 수 없습니다. ID=" + newsId);
        }
        return prosConsRepo.findByNews_Id(newsId, pageable).map(this::toRes);
    }

    @Transactional(readOnly = true)
    public ProsConsRes get(Long id) {
        ProsCons pc = prosConsRepo.findWithJoinsById(id)
                .orElseThrow(() -> new NoSuchElementException("찬반/요약을 찾을 수 없습니다. ID=" + id));
        return toRes(pc);
    }

    private ProsConsRes toRes(ProsCons pc) {
        return new ProsConsRes(
                pc.getId(),
                pc.getNews().getId(),
                pc.getCreatedBy().getId(),
                pc.getCreatedBy().getUsername(),
                pc.getSummary(),
                pc.getLink(),
                pc.getPros(),
                pc.getNeutral(),
                pc.getCons(),
                pc.getBias()
        );
    }
}
