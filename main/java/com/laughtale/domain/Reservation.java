package com.laughtale.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation", indexes = {
    @Index(name = "idx_member", columnList = "member_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Integer reservationId; // 예약 고유번호 (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 예약한 사람 (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book; // 예약한 책 (FK)

    @CreationTimestamp
    @Column(name = "reservation_date", updatable = false)
    private LocalDateTime reservationDate; // 예약 시각 (DEFAULT CURRENT_TIMESTAMP)

    @Column(name = "status", length = 20)
    private String status = "WAITING"; // 상태 (WAITING, COMPLETED, CANCELLED)

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate; // 예약 만료일 (NOT NULL)
}