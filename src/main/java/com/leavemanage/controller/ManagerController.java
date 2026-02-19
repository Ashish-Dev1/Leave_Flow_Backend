package com.leavemanage.controller;

import com.leavemanage.dto.LeaveDecisionRequest;
import com.leavemanage.dto.LeaveDto;
import com.leavemanage.dto.UserDto;
import com.leavemanage.model.LeaveRequest;
import com.leavemanage.model.LeaveStatus;
import com.leavemanage.model.Role;
import com.leavemanage.model.User;
import com.leavemanage.repository.LeaveRepository;
import com.leavemanage.repository.UserRepository;
import com.leavemanage.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ManagerController(
            LeaveRepository leaveRepository,
            UserRepository userRepository,
            UserService userService) {

        this.leaveRepository = leaveRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/leaves")
    public List<LeaveRequest> pendingLeaves() {
        return leaveRepository.findByStatus(LeaveStatus.PENDING);
    }

    @GetMapping("/leaves/all")
    public List<LeaveRequest> allLeaves() {
        return leaveRepository.findAll();
    }

    @GetMapping("/leaves/filter")
    public List<LeaveRequest> filterLeaves(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) LeaveStatus status
    ) {
        if (month != null && year != null) {
            if (status != null) {
                return leaveRepository.findByMonthAndStatus(year, month, status);
            } else {
                return leaveRepository.findByMonth(year, month);
            }
        } else if (status != null) {
            return leaveRepository.findByStatus(status);
        } else {
            return leaveRepository.findAll();
        }
    }

    @GetMapping("/leaves/enhanced-filter")
    public List<LeaveRequest> filterLeavesEnhanced(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return leaveRepository.findWithFilters(userName, leaveType, status, startDate, endDate);
    }

    @GetMapping("/users/{userId}/leaves/enhanced-filter")
    public List<LeaveRequest> filterUserLeavesEnhanced(
            @PathVariable Long userId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return leaveRepository.findByUserWithFilters(userId, userName, leaveType, status, startDate, endDate);
    }

    @GetMapping("/users/enhanced-filter")
    public List<UserDto> filterUsersEnhanced(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {

        List<User> users = userRepository.findAllByRole(Role.USER);

        return users.stream()
                .filter(user -> {
                    List<LeaveRequest> userLeaves =
                            leaveRepository.findByUserWithFilters(
                                    user.getId(), userName, leaveType, status, startDate, endDate);
                    return !userLeaves.isEmpty();
                })
                .map(user -> {
                    List<LeaveRequest> filteredLeaves =
                            leaveRepository.findByUserWithFilters(
                                    user.getId(), userName, leaveType, status, startDate, endDate);

                    return new UserDto(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            List.of(user.getRole().name()),
                            filteredLeaves.size(),
                            (int) filteredLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count(),
                            (int) filteredLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count(),
                            (int) filteredLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.REJECTED).count()
                    );
                })
                .toList();
    }

    @PutMapping("/leaves/{id}/approve")
    public void approve(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveDecisionRequest request
    ) {
        String comment = request != null ? request.getComment() : null;
        userService.updateStatus(id, LeaveStatus.APPROVED, comment);
    }

    @PutMapping("/leaves/{id}/reject")
    public void reject(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveDecisionRequest request
    ) {
        String comment = request != null ? request.getComment() : null;
        userService.updateStatus(id, LeaveStatus.REJECTED, comment);
    }

    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/filter")
    public List<UserDto> filterUsers(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status
    ) {
        return userService.getUsersWithLeaveFilters(month, year, status);
    }

    @GetMapping("/users/{userId}/leaves")
    public List<LeaveDto> getUserLeaves(@PathVariable Long userId) {
        return userService.getMyLeaves(userId);
    }

    @GetMapping("/users/{userId}/leaves/filter")
    public List<LeaveDto> getUserLeavesByMonth(
            @PathVariable Long userId,
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        return userService.getUserLeavesByMonth(userId, month, year);
    }

    @GetMapping("/users/{userId}/leaves/status")
    public List<LeaveDto> getUserLeavesByStatus(
            @PathVariable Long userId,
            @RequestParam String status
    ) {
        return userService.getUserLeavesByStatus(userId, status);
    }

    @GetMapping("/users/stats")
    public Map<String, Object> getUserStats() {
        return userService.getUserStatistics();
    }
}
