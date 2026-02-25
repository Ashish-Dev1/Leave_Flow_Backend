package com.leavemanage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(name = "department")
    private String department;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "password_changed_at")
    private java.time.LocalDateTime passwordChangedAt;

    // getters & setters
}

