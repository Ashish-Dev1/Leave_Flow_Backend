package com.leavemanage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private List<String> role;
    
    // Additional fields for filtered user data
    private Integer totalLeaves;
    private Integer pendingLeaves;
    private Integer approvedLeaves;
    private Integer rejectedLeaves;
    
    // Default constructor for basic user info (overriding @AllArgsConstructor)
    public UserDto(Long id, String username, String email, List<String> role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.totalLeaves = 0;
        this.pendingLeaves = 0;
        this.approvedLeaves = 0;
        this.rejectedLeaves = 0;
    }
}
