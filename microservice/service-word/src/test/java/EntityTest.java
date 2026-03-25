import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.heloword.common.entity.user.MemberEntity;
import com.heloword.common.entity.user.RoleEntity;
import com.heloword.common.entity.word.SentenceJapaneseEntity;
import com.heloword.common.repo.user.MemberRepository;
import com.heloword.common.repo.user.RoleRepository;
import com.heloword.common.repo.word.SentenceGermanRepository;
import com.heloword.common.repo.word.SentenceJapaneseRepository;
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
  }

  @Test
  void testEntities() {
  }


}
