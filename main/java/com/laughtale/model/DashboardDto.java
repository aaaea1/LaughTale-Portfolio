package com.laughtale.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
	
    private Long totalMembers;
    private Long activeMembers;
    private Long totalBooks;
    private Long availableBooks;
    private Long totalRentals;
    private Long currentRentals;
    private Long overdueRentals;
    private Double totalRevenue;
    
}
