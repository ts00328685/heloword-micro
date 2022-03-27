import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.heloword.common.entity.MemberEntity;
import com.heloword.common.entity.RoleEntity;
import com.heloword.common.entity.SentenceJapaneseEntity;
import com.heloword.common.repo.MemberRepository;
import com.heloword.common.repo.RoleRepository;
import com.heloword.common.repo.SentenceGermanRepository;
import com.heloword.common.repo.SentenceJapaneseRepository;
import com.heloword.word.WordApplication;
import org.junit.jupiter.api.Test;


@SpringBootTest(classes = WordApplication.class)
class EntityTest {

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  SentenceGermanRepository sentenceGermanRepository;

  @Autowired
  SentenceJapaneseRepository sentenceJapaneseRepository;

  @Autowired
  EntityManager entityManager;

  @Test
  void contextLoads() {
    MemberEntity ts00328685 = MemberEntity.builder().username("ssss222").password("123").build();
    memberRepository.save(ts00328685);
    ts00328685.setUsername("hey");
    memberRepository.flush();
    memberRepository.save( MemberEntity.builder().username("aaaa").password("123").build());
    memberRepository.save(MemberEntity.builder().username("bbb").password("123").build());
    memberRepository.save(MemberEntity.builder().username("ccc").password("123").build());

    roleRepository.save(RoleEntity.builder().role("MEMBER").name("normal member").build());
  }

  @Test
  void testEntities() {

    SentenceJapaneseEntity build;
    build = SentenceJapaneseEntity.builder()
        .id(1L)
        .sentence("test2")
        .translateCh("測試2")
        .language("jp").build();

    sentenceJapaneseRepository.save(build);

  }


}
