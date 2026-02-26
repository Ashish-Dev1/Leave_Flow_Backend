package com.leavemanage.dto;



import com.leavemanage.model.LeaveStatus;
import com.leavemanage.model.SessionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
@Data
@AllArgsConstructor
public class LeaveDto {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String leaveType;
    private LeaveStatus status;
    private String managerComment;
    private SessionType leaveSession;
    private Long userId;
    private String userName;


}
