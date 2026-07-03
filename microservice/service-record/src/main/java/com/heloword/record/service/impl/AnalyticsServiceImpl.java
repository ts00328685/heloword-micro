package com.heloword.record.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.heloword.common.entity.analytics.AnalyticsEventEntity;
import com.heloword.common.model.dto.analytics.AnalyticsCountDto;
import com.heloword.common.model.dto.analytics.AnalyticsDashboardDto;
import com.heloword.common.model.dto.analytics.AnalyticsEventDto;
import com.heloword.common.model.dto.analytics.AnalyticsPointDto;
import com.heloword.common.repo.analytics.AnalyticsEventRepository;
import com.heloword.record.service.AnalyticsService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

  private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final int TOP_LIMIT = 12;
  private static final int MAX_STR = 512;

  @Autowired
  private AnalyticsEventRepository repo;

  @Override
  public void ingest(List<AnalyticsEventDto> events) {
    if (events == null || events.isEmpty()) {
      return;
    }
    List<AnalyticsEventEntity> entities = new ArrayList<>();
    for (AnalyticsEventDto e : events) {
      if (e == null || e.getEventName() == null || e.getEventName().trim().isEmpty()) {
        continue;
      }
      AnalyticsEventEntity a = new AnalyticsEventEntity();
      a.setUserUuid(clamp(e.getUserUuid(), 128));
      a.setGuest(Boolean.TRUE.equals(e.getGuest()));
      a.setSessionId(clamp(e.getSessionId(), 128));
      a.setEventType(clamp(e.getEventType(), 32));
      a.setEventName(clamp(e.getEventName(), 255));
      a.setLabel(clamp(e.getLabel(), MAX_STR));
      a.setPath(clamp(e.getPath(), MAX_STR));
      a.setLocale(clamp(e.getLocale(), 16));
      a.setDevice(clamp(e.getDevice(), 32));
      a.setReferrer(clamp(e.getReferrer(), MAX_STR));
      a.setDurationMs(e.getDurationMs());
      entities.add(a);
    }
    if (!entities.isEmpty()) {
      repo.saveAll(entities);
    }
  }

  @Override
  public AnalyticsDashboardDto getDashboard(int days) {
    int window = Math.max(1, Math.min(days, 365));
    Date to = endOfToday();
    Date from = startOfDaysAgo(window - 1);

    List<AnalyticsPointDto> daily = buildDailySeries(from, to, window);

    return AnalyticsDashboardDto.builder()
        .days(window)
        .totalEvents(repo.countByCreateDateBetween(from, to))
        .uniqueUsers(repo.countDistinctUsers(from, to))
        .pageViews(repo.countPageViews(from, to))
        .activeToday(repo.countDistinctUsers(startOfDaysAgo(0), to))
        .daily(daily)
        .topPages(toCounts(repo.topPages(from, to, PageRequest.of(0, TOP_LIMIT))))
        .topEvents(toCounts(repo.topEvents(from, to, PageRequest.of(0, TOP_LIMIT))))
        .topContent(toCounts(repo.topContent(from, to, PageRequest.of(0, TOP_LIMIT))))
        .devices(toCounts(repo.deviceSplit(from, to)))
        .userTypes(toUserTypes(repo.userTypeSplit(from, to)))
        .build();
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  private List<AnalyticsPointDto> buildDailySeries(Date from, Date to, int window) {
    // Bucket [createDate, userUuid] rows by calendar day.
    Map<String, long[]> eventCounts = new LinkedHashMap<>();   // day -> [events]
    Map<String, Set<String>> userSets = new LinkedHashMap<>(); // day -> distinct users

    // Seed every day in the window so gaps render as zero.
    Calendar day = Calendar.getInstance();
    day.setTime(from);
    for (int i = 0; i < window; i++) {
      String key = DAY_FORMAT.format(day.getTime());
      eventCounts.put(key, new long[] {0});
      userSets.put(key, new java.util.HashSet<>());
      day.add(Calendar.DAY_OF_MONTH, 1);
    }

    for (Object[] row : repo.rawForSeries(from, to)) {
      Date createDate = (Date) row[0];
      String userUuid = (String) row[1];
      if (createDate == null) {
        continue;
      }
      String key = DAY_FORMAT.format(createDate);
      long[] c = eventCounts.get(key);
      if (c == null) {
        continue; // outside the seeded window (defensive)
      }
      c[0]++;
      if (userUuid != null) {
        userSets.get(key).add(userUuid);
      }
    }

    List<AnalyticsPointDto> series = new ArrayList<>();
    for (String key : eventCounts.keySet()) {
      series.add(AnalyticsPointDto.builder()
          .date(key)
          .events(eventCounts.get(key)[0])
          .users(userSets.get(key).size())
          .build());
    }
    return series;
  }

  private List<AnalyticsCountDto> toCounts(List<Object[]> rows) {
    List<AnalyticsCountDto> out = new ArrayList<>();
    for (Object[] row : rows) {
      String name = row[0] == null ? "unknown" : String.valueOf(row[0]);
      out.add(new AnalyticsCountDto(name, toLong(row[1])));
    }
    return out;
  }

  private List<AnalyticsCountDto> toUserTypes(List<Object[]> rows) {
    // Merge into a stable member/guest order.
    Map<String, Long> merged = new TreeMap<>();
    for (Object[] row : rows) {
      String name = Boolean.TRUE.equals(row[0]) ? "guest" : "member";
      merged.merge(name, toLong(row[1]), Long::sum);
    }
    return merged.entrySet().stream()
        .map(en -> new AnalyticsCountDto(en.getKey(), en.getValue()))
        .collect(Collectors.toList());
  }

  private static long toLong(Object o) {
    return o instanceof Number ? ((Number) o).longValue() : 0L;
  }

  private static String clamp(String s, int max) {
    if (s == null) {
      return null;
    }
    String t = s.trim();
    return t.length() > max ? t.substring(0, max) : t;
  }

  private static Date startOfDaysAgo(int daysAgo) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.DAY_OF_MONTH, -daysAgo);
    return cal.getTime();
  }

  private static Date endOfToday() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 999);
    return cal.getTime();
  }
}
