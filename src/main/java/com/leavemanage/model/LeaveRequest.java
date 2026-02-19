package com.leavemanage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;

    private String reason;
    
    @Column(name = "leave_type")
    private String leaveType;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    @Column(length = 500)
    private String managerComment;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // getters & setters
}
