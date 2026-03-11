package com.laughtale.service;

import com.laughtale.mapper.StatisticsMapper;
import com.laughtale.model.DashboardDto;
import com.laughtale.model.MonthlyStatisticsDto;
import com.laughtale.model.OverdueDto;
import com.laughtale.model.PopularBookDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {
    
    private final StatisticsMapper statisticsMapper;
    
    // 대시보드 통계
    public DashboardDto getDashboardStatistics() {
        return statisticsMapper.getDashboardStatistics();
    }
    
    // 월별 통계
    public List<MonthlyStatisticsDto> getMonthlyStatistics(Integer year) {
        return statisticsMapper.getMonthlyStatistics(year);
    }
    
    // 인기 도서 TOP 10
    public List<PopularBookDto> getPopularBooks() {
        return statisticsMapper.getPopularBooks(10);
    }
    
    // 연체 목록
    public List<OverdueDto> getOverdueList() {
        return statisticsMapper.getOverdueList();
    }
    
    // 회원별 대여 통계
    public List<MonthlyStatisticsDto> getMemberRentalStatistics(Integer memberId) {
        return statisticsMapper.getMemberRentalStatistics(memberId);
    }
}