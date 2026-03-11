package com.laughtale.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopularBookDto { // 인기 도서
	
    private Integer bookId;
    private String title;
    private String author;
    private String genre;
    private String filePath;
    private String fileName;
    private Long rentalCount;
    private Double averageRating;

}
