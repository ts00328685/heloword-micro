package com.heloword.common.repo.analytics;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.analytics.AnalyticsEventEntity;

/**
 * Aggregation queries for the admin analytics dashboard. All are bounded by a
 * {@code from}/{@code to} window and group only by non-PII fields.
 */
@Repository
public interface AnalyticsEventRepository extends IBaseRepo<AnalyticsEventEntity, Long> {

  long countByCreateDateBetween(Date from, Date to);

  @Query("SELECT COUNT(DISTINCT e.userUuid) FROM AnalyticsEventEntity e "
      + "WHERE e.createDate BETWEEN :from AND :to")
  long countDistinctUsers(@Param("from") Date from, @Param("to") Date to);

  @Query("SELECT COUNT(e) FROM AnalyticsEventEntity e "
      + "WHERE e.eventType = 'PAGE_VIEW' AND e.createDate BETWEEN :from AND :to")
  long countPageViews(@Param("from") Date from, @Param("to") Date to);

  /** [eventName, count] for page views, most-visited first. */
  @Query("SELECT e.eventName, COUNT(e) FROM AnalyticsEventEntity e "
      + "WHERE e.eventType = 'PAGE_VIEW' AND e.createDate BETWEEN :from AND :to "
      + "GROUP BY e.eventName ORDER BY COUNT(e) DESC")
  List<Object[]> topPages(@Param("from") Date from, @Param("to") Date to, Pageable pageable);

  /** [eventName, count] for button/feature/click events, most-used first. */
  @Query("SELECT e.eventName, COUNT(e) FROM AnalyticsEventEntity e "
      + "WHERE e.eventType IN ('BUTTON','FEATURE','CLICK') AND e.createDate BETWEEN :from AND :to "
      + "GROUP BY e.eventName ORDER BY COUNT(e) DESC")
  List<Object[]> topEvents(@Param("from") Date from, @Param("to") Date to, Pageable pageable);

  /** [label, count] for content VIEW events (articles/words viewed), most-viewed first. */
  @Query("SELECT e.label, COUNT(e) FROM AnalyticsEventEntity e "
      + "WHERE e.eventType = 'VIEW' AND e.label IS NOT NULL AND e.createDate BETWEEN :from AND :to "
      + "GROUP BY e.label ORDER BY COUNT(e) DESC")
  List<Object[]> topContent(@Param("from") Date from, @Param("to") Date to, Pageable pageable);

  /** [device, count]. */
  @Query("SELECT e.device, COUNT(e) FROM AnalyticsEventEntity e "
      + "WHERE e.createDate BETWEEN :from AND :to GROUP BY e.device")
  List<Object[]> deviceSplit(@Param("from") Date from, @Param("to") Date to);

  /** [guestFlag, distinctUsers]. */
  @Query("SELECT e.guest, COUNT(DISTINCT e.userUuid) FROM AnalyticsEventEntity e "
      + "WHERE e.createDate BETWEEN :from AND :to GROUP BY e.guest")
  List<Object[]> userTypeSplit(@Param("from") Date from, @Param("to") Date to);

  /** [createDate, userUuid] rows, bucketed into days by the service layer. */
  @Query("SELECT e.createDate, e.userUuid FROM AnalyticsEventEntity e "
      + "WHERE e.createDate BETWEEN :from AND :to")
  List<Object[]> rawForSeries(@Param("from") Date from, @Param("to") Date to);
}
