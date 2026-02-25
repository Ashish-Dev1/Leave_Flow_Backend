package com.leavemanage.repository;

import com.leavemanage.model.LeaveRequest;
import com.leavemanage.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository
        extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUserId(Long userId);

    List<LeaveRequest> findByStatus(LeaveStatus status);

    List<LeaveRequest> findAll();

    /**
     * Flexible filter for month, year, and status.
     * Handles all combinations: only month, only year, both, neither, with/without status.
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE YEAR(lr.startDate) = :year AND MONTH(lr.startDate) = :month")
    List<LeaveRequest> findByMonth(@Param("year") int year, @Param("month") int month);
 
    @Query("SELECT lr FROM LeaveRequest lr WHERE YEAR(lr.startDate) = :year AND MONTH(lr.startDate) = :month AND lr.status = :status")
    List<LeaveRequest> findByMonthAndStatus(@Param("year") int year, @Param("month") int month, @Param("status") LeaveStatus status);
 
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND YEAR(lr.startDate) = :year AND MONTH(lr.startDate) = :month")
    List<LeaveRequest> findByUserAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
 
//    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
//           "(:month IS NULL OR MONTH(lr.startDate) = :month) AND " +
//           "(:year IS NULL OR YEAR(lr.startDate) = :year) AND " +
//           "(:status IS NULL OR lr.status = :status)")
//    List<LeaveRequest> findByMonthYearAndStatus(
//            @Param("month") Integer month,
//            @Param("year") Integer year,
//            @Param("status") LeaveStatus status
//    );

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND " +
           "(:month IS NULL OR MONTH(lr.startDate) = :month) AND " +
           "(:year IS NULL OR YEAR(lr.startDate) = :year) AND " +
           "(:status IS NULL OR lr.status = :status)")
    List<LeaveRequest> findByUserAndMonthYearAndStatus(
            @Param("userId") Long userId,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("status") LeaveStatus status
    );

    /**
     * Unified filter method - handles ALL combinations of:
     * month, year, userName, leaveType, status, startDate, endDate
     * Any parameter can be null/omitted - works with any combination
     */
    @Query(value = "SELECT lr.* FROM leave_requests lr " +
            "JOIN users u ON u.id = lr.user_id " +
            "WHERE (:month IS NULL OR EXTRACT(MONTH FROM lr.start_date) = :month) " +
            "AND (:year IS NULL OR EXTRACT(YEAR FROM lr.start_date) = :year) " +
            "AND (:userName IS NULL OR u.name ILIKE CONCAT('%', CAST(:userName AS text), '%')) " +
            "AND (:leaveType IS NULL OR lr.leave_type = :leaveType) " +
            "AND (:status IS NULL OR lr.status = :status) " +
            "AND (:startDate IS NULL OR lr.start_date >= CAST(:startDate AS date)) " +
            "AND (:endDate IS NULL OR lr.end_date <= CAST(:endDate AS date))",
            nativeQuery = true)
    List<LeaveRequest> findByAllFilters(
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("userName") String userName,
            @Param("leaveType") String leaveType,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND " +
           "(:userName IS NULL OR LOCATE(LOWER(:userName), LOWER(lr.user.name)) > 0) AND " +
           "(:leaveType IS NULL OR lr.leaveType = :leaveType) AND " +
           "(:status IS NULL OR lr.status = :status) AND " +
           "(:startDate IS NULL OR lr.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR lr.endDate <= :endDate)")
    List<LeaveRequest> findByUserWithFilters(
            @Param("userId") Long userId,
            @Param("userName") String userName,
            @Param("leaveType") String leaveType,
            @Param("status") LeaveStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
