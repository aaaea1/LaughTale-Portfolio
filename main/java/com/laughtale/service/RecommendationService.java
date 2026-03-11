package com.laughtale.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.laughtale.domain.Book;
import com.laughtale.domain.Member;
import com.laughtale.domain.Rental;
import com.laughtale.repository.RentalRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    private final RentalRepository rentalRepository;
    private final MemberService memberService;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${groq.api.key}")
    private String groqApiKey;
    
    @Value("${groq.api.url}")
    private String groqApiUrl;
    
    @Value("${groq.model:llama-3.3-70b-versatile}") 
    private String groqModel;
    
    public String getRecommendations(Integer memberId) {
        Member member = memberService.getMemberById(memberId);
        List<Rental> rentalHistory = rentalRepository.findByMemberOrderByRentalDateDesc(member);
        
        // 이력이 아예 없을 때만 기본 메시지 출력
        if (rentalHistory == null || rentalHistory.isEmpty()) {
            return generateDefaultRecommendation();
        }
        
        String rentalHistoryText = analyzeRentalHistory(rentalHistory);
        return callGroqAI(rentalHistoryText);
    }
    
    private String analyzeRentalHistory(List<Rental> rentals) {

        int limit = Math.min(rentals.size(), 10);
        List<Rental> recentRentals = rentals.subList(0, limit);
        
        StringBuilder sb = new StringBuilder();
        sb.append("ユーザーの最近の貸出履歴:\n"); // 일본어 분석 문구로 변경
        
        for (Rental rental : recentRentals) {
            Book book = rental.getBook();
            sb.append(String.format("- タイトル: %s, 著者: %s, ジャンル: %s\n", 
                    book.getTitle(), book.getAuthor(), book.getGenre()));
        }
        
        return sb.toString();
    }
    
    private String callGroqAI(String rentalHistory) {
        try {
            WebClient webClient = webClientBuilder.build();
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", groqModel);
            
            // 시스템 메시지를 일본어로 수정하여 일본어 답변 유도
            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", 
                            "あなたは漫画専門の司書です。提供された貸出履歴を分析し、ユーザーの好みに合う漫画を日本語で3〜5冊推薦してください。" +
                            "推薦時にはタイトル、著者、ジャンル、そして簡潔な推薦理由を添えてください。"),
                    Map.of("role", "user", "content", rentalHistory)
            );
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);
            
            Map<String, Object> response = webClient.post()
                    .uri(groqApiUrl)
                    .header("Authorization", "Bearer " + groqApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> 
                        clientResponse.bodyToMono(String.class).flatMap(error -> {
                            System.err.println("Groq API Error Detail: " + error); // 여기서 400 에러 원인 확인 가능
                            return Mono.error(new RuntimeException(error));
                        })
                    )
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            
            return generateDefaultRecommendation();
            
        } catch (Exception e) {
            System.err.println("AI Recommendation Failed: " + e.getMessage());
            return generateDefaultRecommendation();
        }
    }
    
    private String generateDefaultRecommendation() {
        return """
                貸出履歴がまだないか、おすすめシステムに一時的なエラーが発生しています。
                
                人気の漫画をチェックするか、様々なジャンルの漫画を探索してみてください！
                - アクション
                - ロマンス
                - ファンタジー
                - スリラー
                """;
    }
}