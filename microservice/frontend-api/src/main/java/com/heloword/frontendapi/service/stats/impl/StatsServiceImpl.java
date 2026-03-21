package com.heloword.frontendapi.service.stats.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.heloword.common.entity.record.RecordQuizEntity;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.model.response.DailyStatDto;
import com.heloword.frontendapi.service.stats.StatsService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {

  private static final SimpleDateFormat DAY_FORMAT   = new SimpleDateFormat("yyyy-MM-dd");
  private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyy-MM");

  private ServiceRecordClient serviceRecordClient;

  @Override
  public List<DailyStatDto> getDailySummary(UserDto userDto, int days) {
    if (days == 0) {
      return getMonthlyStats(userDto);
    }
    return getDailyStats(userDto, days);
  }

  // ---- daily grouping for 7d / 30d ----

  private List<DailyStatDto> getDailyStats(UserDto userDto, int days) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 999);
    long toTs = cal.getTimeInMillis();

    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.DAY_OF_MONTH, -(days - 1));
    long fromTs = cal.getTimeInMillis();

    List<RecordQuizEntity> records = serviceRecordClient
        .getRecordsByDateRange(userDto.getUsername(), fromTs, toTs)
        .getData();

    Map<String, List<RecordQuizEntity>> byDay = records.stream()
        .filter(r -> r.getFinishedTime() != null)
        .collect(Collectors.groupingBy(r -> DAY_FORMAT.format(r.getFinishedTime())));

    List<DailyStatDto> result = new ArrayList<>();
    Calendar day = Calendar.getInstance();
    day.add(Calendar.DAY_OF_MONTH, -(days - 1));
    for (int i = 0; i < days; i++) {
      String dateStr = DAY_FORMAT.format(day.getTime());
      List<RecordQuizEntity> dayRecords = byDay.getOrDefault(dateStr, List.of());
      result.add(aggregate(dateStr, dayRecords));
      day.add(Calendar.DAY_OF_MONTH, 1);
    }
    return result;
  }

  // ---- monthly grouping for all-time ----

  private List<DailyStatDto> getMonthlyStats(UserDto userDto) {
    // Fetch all records from the beginning of time
    List<RecordQuizEntity> records = serviceRecordClient
        .getRecordsByDateRange(userDto.getUsername(), 0L, System.currentTimeMillis())
        .getData();

    Map<String, List<RecordQuizEntity>> byMonth = records.stream()
        .filter(r -> r.getFinishedTime() != null)
        .collect(Collectors.groupingBy(r -> MONTH_FORMAT.format(r.getFinishedTime())));

    return byMonth.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(e -> aggregate(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }

  // ---- helper ----

  private DailyStatDto aggregate(String date, List<RecordQuizEntity> records) {
    int total     = records.size();
    int wrong     = records.stream().mapToInt(r -> r.getWrongCount() != null ? r.getWrongCount() : 0).sum();
    int timeSpent = records.stream().mapToInt(r -> r.getTimeSpent()  != null ? r.getTimeSpent()  : 0).sum();
    return DailyStatDto.builder().date(date).total(total).wrongCount(wrong).timeSpent(timeSpent).build();
  }
}
