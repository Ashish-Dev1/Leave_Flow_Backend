package com.leavemanage.service;

import com.leavemanage.dto.LeaveDto;
import com.leavemanage.dto.UserDto;
import com.leavemanage.exception.HalfDayLeaveValidationException;
import com.leavemanage.exception.HalfDaySessionRequiredException;
import com.leavemanage.model.*;
import com.leavemanage.repository.LeaveRepository;
import com.leavemanage.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.Month;

@Service
public class UserService {

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(
            LeaveRepository leaveRepository,
            UserRepository userRepository,
            EmailService emailService
    ) {
        this.leaveRepository = leaveRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public LeaveRequest applyLeave(
            LeaveRequest leave, User user) {

        leave.setUser(user);
        leave.setStatus(LeaveStatus.PENDING);
        if (leave.getLeaveType().equals("half-day") &&
                !leave.getStartDate().equals(leave.getEndDate())) {

            throw new HalfDayLeaveValidationException("Half day leave must be single day");
        }
        if (leave.getLeaveType().equals("half-day")) {

            if (leave.getLeaveSession() == null) {
                throw new HalfDaySessionRequiredException("Session is required for HALF_DAY leave");
            }

        }

        // Rule 2: session must be NULL if NOT HALF_DAY
        if (!leave.getLeaveType().equals("half-day")) {

            leave.setLeaveSession(null);

        }


        LeaveRequest saved = leaveRepository.save(leave);

        List<User> managers = userRepository.findAllByRole(Role.MANAGER);
        if (managers.isEmpty()) {
            throw new RuntimeException("Manager not found");
        }

        String portalUrl="https://leave.netpy.in/admin/dashboard";
        String subject =
                "Leave Request: " + user.getName() +
                        " (" + saved.getStartDate() +
                        " to " + saved.getEndDate() + ")";

        String body =
                "Dear Manager,\n\n" +
                        "A new leave request has been submitted.\n\n" +
                        "Employee Details:\n" +
                        "-------------------------\n" +
                        "Name      : " + user.getName() + "\n" +
                        "Email     : " + user.getEmail() + "\n" +
                        "Leave From: " + saved.getStartDate() + "\n" +
                        "Leave To  : " + saved.getEndDate() + "\n" +
                        "Reason    : " + saved.getReason() + "\n\n" +
                        "Please review and take appropriate action.\n\n" +
                        "Link:\n"+
                        portalUrl+"\n\n"+
                        "Regards,\nNetPy Technologies";

        for (User manager : managers) {
            try {
                emailService.sendEmail(
                        manager.getEmail(),
                        subject,
                        body,
                        user.getEmail()
                );
            } catch (Exception e) {
                System.out.println("Email sending failed for " + manager.getEmail() + ": " + e.getMessage());
            }
        }

        return saved;
    }


    public void updateStatus(
            Long leaveId,
            LeaveStatus status,
            String comment
    ) {

        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        // 1. Update status
        leave.setStatus(status);

        // 2. Save comment if present
        if (comment != null && !comment.trim().isEmpty()) {
            leave.setManagerComment(comment);
        }

        // 3. Save to DB FIRST
        leaveRepository.save(leave);

        // 4. Send email ALWAYS when status changes
        User user = leave.getUser();

        String subject =
                status == LeaveStatus.APPROVED
                        ? "Leave Approved (" + leave.getStartDate() +
                        " to " + leave.getEndDate() + ")"
                        : "Leave Rejected (" + leave.getStartDate() +
                        " to " + leave.getEndDate() + ")";

        String body =
                "Dear " + user.getName() + ",\n\n" +

                        "Your leave request has been " + status + ".\n\n" +

                        "Leave Details:\n" +
                        "-------------------------\n" +
                        "From   : " + leave.getStartDate() + "\n" +
                        "To     : " + leave.getEndDate() + "\n" +
                        "Status : " + status + "\n\n" +

                        "\n" +
                        (leave.getManagerComment() != null
                                ? leave.getManagerComment()
                                : "No comment provided") +

                        "\n\nRegards,\n" +
                        "\nNetPy Technologies";

        emailService.sendEmail(
                user.getEmail(),
                subject,
                body,
                null
        );
    }

    public List<UserDto> getAllUsers() {

        return userRepository.findAllByRole(Role.USER)
                .stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        List.of(user.getRole().name())
                        //user.getRole().toString()
                ))
                .toList();
    }

    public List<LeaveDto> getMyLeaves(Long userId) {

        return leaveRepository.findByUserId(userId)
                .stream()
                .map(leave -> new LeaveDto(
                        leave.getId(),
                        leave.getStartDate(),
                        leave.getEndDate(),
                        leave.getReason(),
                        leave.getLeaveType(),
                        leave.getStatus(),
                        leave.getManagerComment(),
                        leave.getLeaveSession(),
                        leave.getUser().getId(),
                        leave.getUser().getName()
                ))
                .toList();
    }

    public List<LeaveDto> getUserLeavesByMonth(Long userId, Integer month, Integer year) {
        return leaveRepository.findByUserAndMonth(userId, year, month)
                .stream()
                .map(leave -> new LeaveDto(
                                        leave.getId(),
                                        leave.getStartDate(),
                                        leave.getEndDate(),
                                        leave.getReason(),
                                        leave.getLeaveType(),
                                        leave.getStatus(),
                                        leave.getManagerComment(),
                        leave.getLeaveSession(),
                        leave.getUser().getId(),
                                        leave.getUser().getName()
                                ))
                .toList();
    }

    /**
     * Flexible filter for user leaves by month, year, and status.
     * Handles all combinations: only month, only year, both, neither, with/without status.
     * Uses repository method with JPQL for database-level filtering.
     */
    public List<LeaveDto> getUserLeavesByMonthYearAndStatus(Long userId, Integer month, Integer year, String status) {
        LeaveStatus leaveStatus = null;
        if (status != null && !status.isEmpty()) {
            leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
        }

        // If no filters, return all user leaves
        if (month == null && year == null && leaveStatus == null) {
            return getMyLeaves(userId);
        }

        // Use repository method with database-level filtering
        return leaveRepository.findByUserAndMonthYearAndStatus(userId, month, year, leaveStatus)
                .stream()
                .map(leave -> new LeaveDto(
                        leave.getId(),
                        leave.getStartDate(),
                        leave.getEndDate(),
                        leave.getReason(),
                        leave.getLeaveType(),
                        leave.getStatus(),
                        leave.getManagerComment(),
                        leave.getLeaveSession(),
                        leave.getUser().getId(),
                        leave.getUser().getName()
                ))
                .toList();
    }

    public List<LeaveDto> getUserLeavesByStatus(Long userId, String status) {
        LeaveStatus leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
        return leaveRepository.findByUserId(userId)
                .stream()
                .filter(leave -> leave.getStatus() == leaveStatus)
                .map(leave -> new LeaveDto(
                        leave.getId(),
                        leave.getStartDate(),
                        leave.getEndDate(),
                        leave.getReason(),
                        leave.getLeaveType(),
                        leave.getStatus(),
                        leave.getManagerComment(),
                        leave.getLeaveSession(),
                        leave.getUser().getId(),
                        leave.getUser().getName()
                ))
                .toList();
    }

    public List<UserDto> getUsersWithLeaveFilters(Integer month, Integer year, String status) {
        List<User> users = userRepository.findAllByRole(Role.USER);

        // Parse status if provided
        final LeaveStatus leaveStatus;
        if (status != null && !status.isEmpty()) {
            leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
        } else {
            leaveStatus = null;
        }

        // Create final copies for lambda capture
        final Integer finalMonth = month;
        final Integer finalYear = year;

        return users.stream()
                .map(user -> {
                    // Get user's leaves with flexible filters
                    List<LeaveRequest> userLeaves = leaveRepository.findByUserId(user.getId())
                            .stream()
                            .filter(leave -> finalMonth == null || leave.getStartDate().getMonthValue() == finalMonth)
                            .filter(leave -> finalYear == null || leave.getStartDate().getYear() == finalYear)
                            .filter(leave -> leaveStatus == null || leave.getStatus() == leaveStatus)
                            .toList();

                    return new UserDto(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            List.of(user.getRole().name()),
                            userLeaves.size(), // Total leaves count
                            (int) userLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count(), // Pending count
                            (int) userLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count(), // Approved count
                            (int) userLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.REJECTED).count() // Rejected count
                    );
                })
                .toList();
    }

    public Map<String, Object> getUserStatistics() {
        List<User> users = userRepository.findAllByRole(Role.USER);
        List<LeaveRequest> allLeaves = leaveRepository.findAll();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", users.size());
        stats.put("totalLeaves", allLeaves.size());
        stats.put("pendingLeaves", allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count());
        stats.put("approvedLeaves", allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count());
        stats.put("rejectedLeaves", allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.REJECTED).count());

        // Monthly stats for current year
        int currentYear = LocalDate.now().getYear();
        Map<String, Long> monthlyStats = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            final int monthVal = month;
            long monthLeaves = allLeaves.stream()
                    .filter(leave -> leave.getStartDate().getYear() == currentYear &&
                            leave.getStartDate().getMonthValue() == monthVal)
                    .count();
            monthlyStats.put(Month.of(month).toString(), monthLeaves);
        }
        stats.put("monthlyStats", monthlyStats);

        return stats;
    }

    public User updateProfile(Long userId, User profileData) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update only allowed fields
        if (profileData.getName() != null && !profileData.getName().trim().isEmpty()) {
            existingUser.setName(profileData.getName());
        }
        if (profileData.getEmail() != null && !profileData.getEmail().trim().isEmpty()) {
            existingUser.setEmail(profileData.getEmail());
        }
        if (profileData.getPhone() != null && !profileData.getPhone().trim().isEmpty()) {
            existingUser.setPhone(profileData.getPhone());
        }
        if (profileData.getDepartment() != null && !profileData.getDepartment().trim().isEmpty()) {
            existingUser.setDepartment(profileData.getDepartment());
        }

        return userRepository.save(existingUser);
    }
}
