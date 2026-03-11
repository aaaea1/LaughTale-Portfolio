package com.laughtale.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer memberId; // PK

    @Column(unique = true, nullable = false, length = 50)
    private String username; // 아이디

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    private String phone;

    @Column(length = 20)
    private String role = "USER"; // USER, ADMIN

    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED, WITHDRAWN
}
