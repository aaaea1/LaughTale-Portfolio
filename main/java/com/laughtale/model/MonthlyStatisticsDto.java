package com.laughtale.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatisticsDto { // 월별 통계
	
    private Integer year;
    private Integer month;
    private Long totalRentals;
    private Long totalReturns;
    private Long overdueCount;
    private Double revenue;

}
