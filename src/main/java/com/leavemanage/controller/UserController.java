package com.leavemanage.controller;

import com.leavemanage.dto.LeaveDto;
import com.leavemanage.model.LeaveRequest;
import com.leavemanage.model.User;
import com.leavemanage.service.UserService;
import com.leavemanage.util.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class UserController {



        private final UserService userService;

        public UserController(UserService leaveService) {
            this.userService = leaveService;
        }

        @PostMapping
        public LeaveRequest applyLeave(
                @RequestBody LeaveRequest leave,
                Authentication auth) {

            CustomUserDetails userDetails =
                    (CustomUserDetails) auth.getPrincipal();

            User user = userDetails.getUser(); // ✅ unwrap entity

            return userService.applyLeave(leave, user);

        }

        @GetMapping("/my")
        public List<LeaveDto> myLeaves(Authentication auth) {

            CustomUserDetails userDetails =
                    (CustomUserDetails) auth.getPrincipal();

            User user = userDetails.getUser();
            return userService.getMyLeaves(user.getId());
        }

        @PutMapping("/profile")
        public User updateProfile(
                @RequestBody User profileData,
                Authentication auth) {
            
            CustomUserDetails userDetails =
                    (CustomUserDetails) auth.getPrincipal();

            User currentUser = userDetails.getUser();
            return userService.updateProfile(currentUser.getId(), profileData);
        }
    }


