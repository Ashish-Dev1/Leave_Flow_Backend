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

    @Query("SELECT lr FROM LeaveRequest lr WHERE YEAR(lr.startDate) = :year AND MONTH(lr.startDate) = :month")
    List<LeaveRequest> findByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT lr FROM LeaveRequest lr WHERE YEAR(lr.startDate) = :year AND MONTH(lr.startDate) = :month AND lr.status = :status")
    List<LeaveRequest> findByMonthAndStatus(@Param("year") int year, @Param("month") int month, @Param("status") LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND YEAR(lr.startDate) = :year AND MONTH(lr.startDate) = :month")
    List<LeaveRequest> findByUserAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    // Enhanced filtering methods
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "(:userName IS NULL OR lr.user.name LIKE %:userName%) AND " +
           "(:leaveType IS NULL OR lr.leaveType = :leaveType) AND " +
           "(:status IS NULL OR lr.status = :status) AND " +
           "(:startDate IS NULL OR lr.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR lr.endDate <= :endDate)")
    List<LeaveRequest> findWithFilters(
            @Param("userName") String userName,
            @Param("leaveType") String leaveType,
            @Param("status") LeaveStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND " +
           "(:userName IS NULL OR lr.user.name LIKE %:userName%) AND " +
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
