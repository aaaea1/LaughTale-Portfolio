package com.laughtale.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverdueDto { // 연체 정보
	
    private Integer rentalId;
    private Integer memberId;
    private String memberName;
    private String memberEmail;
    private String memberPhone;
    private Integer bookId;
    private String bookTitle;
    private LocalDateTime rentalDate;
    private LocalDateTime dueDate;
    private Integer overdueDays;
    private Double overdueFee;
    
}
