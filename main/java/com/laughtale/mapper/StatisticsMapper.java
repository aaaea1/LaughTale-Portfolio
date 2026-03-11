package com.laughtale.mapper;

import com.laughtale.model.DashboardDto;
import com.laughtale.model.MonthlyStatisticsDto;
import com.laughtale.model.OverdueDto;
import com.laughtale.model.PopularBookDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StatisticsMapper {
    
    // 대시보드 통계
    DashboardDto getDashboardStatistics();
    
    // 월별 통계
    List<MonthlyStatisticsDto> getMonthlyStatistics(@Param("year") Integer year);
    
    // 인기 도서 TOP 10
    List<PopularBookDto> getPopularBooks(@Param("limit") int limit);
    
    // 연체 목록
    List<OverdueDto> getOverdueList();
    
    // 회원별 대여 통계
    List<MonthlyStatisticsDto> getMemberRentalStatistics(@Param("memberId") Integer memberId);
}
