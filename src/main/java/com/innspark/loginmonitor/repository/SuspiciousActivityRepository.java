package com.innspark.loginmonitor.repository;

import com.innspark.loginmonitor.model.SuspiciousActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SuspiciousActivityRepository extends JpaRepository<SuspiciousActivity, Long> {

    List<SuspiciousActivity> findByIpAddress(String ipAddress);

    List<SuspiciousActivity> findByUsername(String username);

    List<SuspiciousActivity> findByActivityType(SuspiciousActivity.ActivityType activityType);

    @Query("SELECT s FROM SuspiciousActivity s WHERE s.ipAddress = :ip AND s.activityType = :type AND s.timestamp >= :since")
    List<SuspiciousActivity> findRecentByIpAndType(@Param("ip") String ip,
                                                    @Param("type") SuspiciousActivity.ActivityType type,
                                                    @Param("since") LocalDateTime since);

    @Query("SELECT s FROM SuspiciousActivity s WHERE s.username = :username AND s.activityType = :type AND s.timestamp >= :since")
    List<SuspiciousActivity> findRecentByUsernameAndType(@Param("username") String username,
                                                          @Param("type") SuspiciousActivity.ActivityType type,
                                                          @Param("since") LocalDateTime since);

    @Query("SELECT s FROM SuspiciousActivity s ORDER BY s.timestamp DESC")
    List<SuspiciousActivity> findAllOrderByTimestampDesc();

    long countByActivityType(SuspiciousActivity.ActivityType activityType);
}
