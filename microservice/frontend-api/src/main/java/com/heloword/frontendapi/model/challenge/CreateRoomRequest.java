package com.heloword.frontendapi.model.challenge;

import lombok.Data;

@Data
public class CreateRoomRequest {
  private String name;
  private String gameType; // wordEnglishList | wordGermanList | wordJapaneseList
  private int totalRounds; // 5-20
}
