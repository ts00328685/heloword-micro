package com.heloword.common.model.dto.analytics;

import java.util.List;
import lombok.Data;

/** A batch of buffered analytics events flushed from the client. */
@Data
public class AnalyticsIngestDto {
  private List<AnalyticsEventDto> events;
}
