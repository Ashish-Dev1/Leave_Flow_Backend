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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
@Tag(name = "Leave Management", description = "APIs for managing employee leave requests and related statistics")
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
    @Operation(
            summary = "Get Pending Leave Requests",
            description = "Retrieve all leave requests that are currently in PENDING status for managerial review."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of pending leave requests retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LeaveRequest.class),
                            examples = @ExampleObject(
                                    name = "PendingLeavesExample",
                                    summary = "Example list of pending leaves",
                                    value = """
                                            [
                                              {
                                                "id": 1,
                                                "userId": 10,
                                                "startDate": "2026-02-28",
                                                "endDate": "2026-03-02",
                                                "leaveType": "ANNUAL",
                                                "status": "PENDING",
                                                "reason": "Family function",
                                                "createdAt": "2026-02-25T10:30:00"
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PendingLeavesServerError",
                                    summary = "Unexpected error example",
                                    value = """
                                            {
                                              "timestamp": "2026-02-25T10:30:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Unexpected error while fetching pending leaves",
                                              "path": "/api/manager/leaves"
                                            }
                                            """
                            )
                    )
            )
    })
    public List<LeaveRequest> pendingLeaves() {
        return leaveRepository.findByStatus(LeaveStatus.PENDING);
    }

    @GetMapping("/leaves/all")
    @Operation(
            summary = "Get All Leave Requests",
            description = "Retrieve all leave requests regardless of status. Useful for dashboards and reporting."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of all leave requests retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LeaveRequest.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public List<LeaveRequest> allLeaves() {
        return leaveRepository.findAll();
    }

//    @GetMapping("/leaves/filter")
//    public List<LeaveRequest> filterLeaves(
//            // Time-based filters
//            @RequestParam(required = false) Integer month,
//            @RequestParam(required = false) Integer year,
//            @RequestParam(required = false) LeaveStatus status
//    ) {
//        return leaveRepository.findByMonthYearAndStatus(month, year, status);
//    }

    @GetMapping("/leaves/enhanced-filter")
    @Operation(
            summary = "Filter Leave Requests",
            description = "Filter leave requests using multiple optional query parameters like month, year, user name, leave type, status, and date range. Frontend can send any combination of filters."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered list of leave requests retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LeaveRequest.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid filter values.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public List<LeaveRequest> filterLeavesEnhanced(
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by calendar month (1-12). Optional.",
                    example = "2"
            )
            @RequestParam(required = false) Integer month,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by calendar year. Optional.",
                    example = "2026"
            )
            @RequestParam(required = false) Integer year,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by part of the employee name. Optional.",
                    example = "John"
            )
            @RequestParam(required = false) String userName,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by leave type. Optional.",
                    example = "ANNUAL"
            )
            @RequestParam(required = false) String leaveType,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by leave status. Optional values: PENDING, APPROVED, REJECTED.",
                    example = "PENDING"
            )
            @RequestParam(required = false) LeaveStatus status,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter leaves starting from this date (inclusive). Optional.",
                    example = "2026-02-01"
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter leaves up to this date (inclusive). Optional.",
                    example = "2026-02-29"
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate

    ) {
        // Single unified query handles ALL combinations
        String statusStr = status != null ? status.name() : null;
        return leaveRepository.findByAllFilters(month, year, userName, leaveType, statusStr, startDate, endDate);
    }

//    @GetMapping("/users/{userId}/leaves/enhanced-filter")
//    public List<LeaveRequest> filterUserLeavesEnhanced(
//            @PathVariable Long userId,
//            @RequestParam(required = false) String userName,
//            @RequestParam(required = false) String leaveType,
//            @RequestParam(required = false) LeaveStatus status,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
//    ) {
//        return leaveRepository.findByUserWithFilters(userId, userName, leaveType, status, startDate, endDate);
//    }

    @GetMapping("/users/enhanced-filter")
    @Operation(
            summary = "Filter Users by Leave Activity",
            description = "Filter users based on their leave activity using optional filters such as user name, leave type, status, and date range."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered list of users with aggregated leave statistics.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid filter values.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public List<UserDto> filterUsersEnhanced(
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by part of the user name. Optional.",
                    example = "John"
            )
            @RequestParam(required = false) String userName,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by leave type. Optional.",
                    example = "SICK"
            )
            @RequestParam(required = false) String leaveType,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by leave status. Optional.",
                    example = "APPROVED"
            )
            @RequestParam(required = false) LeaveStatus status,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter leaves starting from this date (inclusive). Optional.",
                    example = "2026-01-01"
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter leaves up to this date (inclusive). Optional.",
                    example = "2026-12-31"
            )
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
    @Operation(
            summary = "Approve Leave Request",
            description = "Approve a specific leave request by its ID. Optionally include a manager comment in the request body."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leave request approved successfully."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid leave ID or request body.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Leave request not found for the given ID.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public void approve(
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Unique identifier of the leave request to approve.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Optional manager decision payload including an approval comment.",
                    required = false,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LeaveDecisionRequest.class),
                            examples = @ExampleObject(
                                    name = "ApproveLeaveRequestExample",
                                    summary = "Approve leave with comment",
                                    value = """
                                            {
                                              "comment": "Approved. Ensure project handover before leave."
                                            }
                                            """
                            )
                    )
            )
            @RequestBody(required = false) LeaveDecisionRequest request
    ) {
        String comment = request != null ? request.getComment() : null;
        userService.updateStatus(id, LeaveStatus.APPROVED, comment);
    }

    @PutMapping("/leaves/{id}/reject")
    @Operation(
            summary = "Reject Leave Request",
            description = "Reject a specific leave request by its ID. Optionally include a manager comment explaining the rejection."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leave request rejected successfully."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid leave ID or request body.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Leave request not found for the given ID.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public void reject(
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Unique identifier of the leave request to reject.",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Optional manager decision payload including a rejection comment.",
                    required = false,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LeaveDecisionRequest.class),
                            examples = @ExampleObject(
                                    name = "RejectLeaveRequestExample",
                                    summary = "Reject leave with comment",
                                    value = """
                                            {
                                              "comment": "Rejected due to project deadlines."
                                            }
                                            """
                            )
                    )
            )
            @RequestBody(required = false) LeaveDecisionRequest request
    ) {
        String comment = request != null ? request.getComment() : null;
        userService.updateStatus(id, LeaveStatus.REJECTED, comment);
    }

    @GetMapping("/users")
    @Operation(
            summary = "Get All Users",
            description = "Retrieve all users with their basic details and roles. Typically used for admin/manager views."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/filter")
    @Operation(
            summary = "Filter Users by Month/Year/Status",
            description = "Filter users based on aggregated leave information using optional month, year, and status query parameters."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered list of users retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid filter values.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public List<UserDto> filterUsers(
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by calendar month (1-12). Optional.",
                    example = "2"
            )
            @RequestParam(required = false) Integer month,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by calendar year. Optional.",
                    example = "2026"
            )
            @RequestParam(required = false) Integer year,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by leave status text. Optional.",
                    example = "PENDING"
            )
            @RequestParam(required = false) String status
    ) {
        return userService.getUsersWithLeaveFilters(month, year, status);
    }

    @GetMapping("/users/{userId}/leaves")
    @Operation(
            summary = "Get User Leave Requests",
            description = "Retrieve all leave requests for a specific user by user ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of user leave requests retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LeaveDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public List<LeaveDto> getUserLeaves(
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Unique identifier of the user whose leaves should be retrieved.",
                    required = true,
                    example = "10"
            )
            @PathVariable Long userId) {
        return userService.getMyLeaves(userId);
    }

    @GetMapping("/users/{userId}/leaves/filter")
    @Operation(
            summary = "Filter User Leaves by Month/Year/Status",
            description = "Retrieve leave requests for a specific user with optional month, year, and status filters."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered list of user leave requests retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LeaveDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid filter values.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public List<LeaveDto> getUserLeavesByMonth(
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Unique identifier of the user whose leaves should be filtered.",
                    required = true,
                    example = "10"
            )
            @PathVariable Long userId,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by calendar month (1-12). Optional.",
                    example = "2"
            )
            @RequestParam(required = false) Integer month,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by calendar year. Optional.",
                    example = "2026"
            )
            @RequestParam(required = false) Integer year,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by leave status text. Optional.",
                    example = "APPROVED"
            )
            @RequestParam(required = false) String status
    ) {
        return userService.getUserLeavesByMonthYearAndStatus(userId, month, year, status);
    }

    @GetMapping("/users/{userId}/leaves/status")
    @Operation(
            summary = "Filter User Leaves by Status",
            description = "Retrieve leave requests for a specific user filtered only by status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered list of user leave requests retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LeaveDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid status value.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public List<LeaveDto> getUserLeavesByStatus(
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Unique identifier of the user whose leaves should be filtered.",
                    required = true,
                    example = "10"
            )
            @PathVariable Long userId,
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Filter by leave status text.",
                    required = true,
                    example = "PENDING"
            )
            @RequestParam String status
    ) {
        return userService.getUserLeavesByStatus(userId, status);
    }

    @GetMapping("/users/stats")
    @Operation(
            summary = "Get User Leave Statistics",
            description = "Retrieve aggregated statistics for users and their leave requests (counts, status breakdowns, etc.)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User statistics retrieved successfully.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public Map<String, Object> getUserStats() {
        return userService.getUserStatistics();
    }
}
