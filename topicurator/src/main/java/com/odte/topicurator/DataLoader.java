package com.odte.topicurator;

import com.odte.topicurator.entity.*;
import com.odte.topicurator.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final NewsRepository newsRepository;
    private final ProsnconsRepository prosnconsRepository;
    private final VotesRepository votesRepository;
    private final CommentsRepository commentsRepository;

    public DataLoader(UserRepository userRepository,
                      NewsRepository newsRepository,
                      ProsnconsRepository prosnconsRepository,
                      VotesRepository votesRepository,
                      CommentsRepository commentsRepository) {
        this.userRepository = userRepository;
        this.newsRepository = newsRepository;
        this.prosnconsRepository = prosnconsRepository;
        this.votesRepository = votesRepository;
        this.commentsRepository = commentsRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        LocalDateTime now = LocalDateTime.now();

        // Users
        User alice = userRepository.save(new User("alice@example.com", "pass1234", "Alice", 28, "F", "개발자"));
        User bob = userRepository.save(new User("bob@example.com", "pass1234", "Bob", 35, "M", "저널리스트"));
        User charlie = userRepository.save(new User("charlie@example.com", "pass1234", "Charlie", 22, "M", "학생"));

        // News
        News news1 = newsRepository.save(new News(alice,
                "AI가 바꾸는 미래 직업",
                "인공지능이 다양한 산업 분야에 끼치는 영향과 새로운 직업 변화에 대해 다룬 기사",
                "IT",
                "AI가 만드는 새로운 일자리?",
                "https://example.com/images/ai.jpg"));

        News news2 = newsRepository.save(new News(bob,
                "기후 변화와 탄소 중립",
                "지구 온난화 문제를 해결하기 위한 탄소 중립 정책과 국제 협력 방안에 대해 설명",
                "사회",
                "탄소 중립, 가능할까?",
                "https://example.com/images/climate.jpg"));

        News news3 = newsRepository.save(new News(charlie,
                "우주 탐사의 새로운 시대",
                "민간 기업과 정부가 함께 이끄는 차세대 우주 탐사 프로젝트 소식",
                "세계",
                "우주 탐사, 누구의 것인가?",
                "https://example.com/images/space.jpg"));
        // Prosncons
        Prosncons pc1 = prosnconsRepository.save(new Prosncons(news1, bob,
                "AI는 자동화를 통해 효율성을 높이고 새로운 직업을 창출할 수 있음",
                "https://example.com/article1", "효율성 증가, 새로운 기회", "일자리 감소 위험", 25));

        Prosncons pc4 = prosnconsRepository.save(new Prosncons(news2, charlie,
                "탄소 중립 정책은 기업과 개인에게 과도한 부담을 줄 수 있음",
                "https://example.com/article4", "지속가능성 확보", "경제적 부담", 11));

        Prosncons pc6 = prosnconsRepository.save(new Prosncons(news3, bob,
                "우주 탐사에 투자하는 자원은 지구 환경과 인류 문제 해결에 더 쓰여야 함",
                "https://example.com/article6", "인류의 꿈 실현", "지구 문제 외면", 8));
        // Votes
        votesRepository.save(new Votes(alice, pc1, "pro"));
        votesRepository.save(new Votes(alice, pc4, "con"));
        votesRepository.save(new Votes(bob, pc6, "pro"));
        votesRepository.save(new Votes(bob, pc1, "con"));
        votesRepository.save(new Votes(charlie, pc4, "pro"));
        votesRepository.save(new Votes(charlie, pc6, "con"));

        // Comments
        commentsRepository.save(new Comments(pc1, alice, "AI 기술의 장점이 잘 설명되어 있네요", "pro"));
        commentsRepository.save(new Comments(pc1, bob, "AI로 인해 일자리 문제가 심각할 것 같습니다", "con"));
        commentsRepository.save(new Comments(pc4, charlie, "탄소 중립 정책은 현실적으로 어렵지만 필요합니다", "pro"));
        commentsRepository.save(new Comments(pc4, alice, "기업 부담이 커서 반대 의견도 이해가 됩니다", "con"));
        commentsRepository.save(new Comments(pc6, bob, "우주 탐사는 과학 발전에 중요합니다", "pro"));
        commentsRepository.save(new Comments(pc6, charlie, "지구 환경 문제를 먼저 해결해야 합니다", "con"));

        System.out.println("Initial data loaded successfully.");
    }
}
