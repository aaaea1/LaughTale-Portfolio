package com.laughtale.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 작성자 (N:1)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book; // 대상 도서 (N:1)

    // --- 대댓글 핵심 로직 ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Review parent; // 부모 댓글 (자기 참조)

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Review> children = new ArrayList<>(); // 자식 댓글들

    private Integer depth = 0; // 0: 댓글, 1: 대댓글
    // -----------------------

    private Integer rating; // 별점 (1~5)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
