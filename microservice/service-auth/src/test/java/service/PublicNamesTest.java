package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.heloword.common.util.PublicNames;

/** New members get a neutral public name — never anything from their Google profile. */
class PublicNamesTest {

  @Test
  void generatesNeutralNickname() {
    String name = PublicNames.generate();
    assertTrue(name.matches("Learner-[A-HJ-NP-Z2-9]{5}"), name);
    assertNotEquals(PublicNames.generate(), PublicNames.generate());
  }

  @Test
  void usesChosenNicknameWhenSet() {
    assertEquals("Mika", PublicNames.of("Mika", "3f2a9c1e-77"));
  }

  @Test
  void fallsBackToStableIdLabelNotPersonalData() {
    assertEquals("Learner-3F2A9", PublicNames.of(null, "3f2a9c1e-7777"));
    assertEquals("Learner-3F2A9", PublicNames.of("  ", "3f2a9c1e-7777"));
    assertEquals("Learner", PublicNames.of(null, null));
  }
}
