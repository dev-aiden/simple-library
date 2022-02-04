package com.aiden.dev.simplelibrary.modules.book;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @PostConstruct
    public void initBookData() {
        Book book1 = Book.builder()
                .title("불편한 편의점")
                .author("김호연")
                .publisher("나무옆의")
                .category(Category.NOVEL)
                .bookImage("")
                .publicationDate(LocalDateTime.of(2021, 04, 20, 00, 00, 00))
                .build();
        bookRepository.save(book1);

        Book book2 = Book.builder()
                .title("달러구트 꿈 백화점")
                .author("이미예")
                .publisher("팩토리라인")
                .category(Category.NOVEL)
                .bookImage("")
                .publicationDate(LocalDateTime.of(2021, 12, 25, 00, 00, 00))
                .build();
        bookRepository.save(book2);

        Book book3 = Book.builder()
                .title("밝은밤")
                .author("최은영")
                .publisher("문학동네")
                .category(Category.NOVEL)
                .bookImage("")
                .publicationDate(LocalDateTime.of(2021, 07, 27, 00, 00, 00))
                .build();
        bookRepository.save(book3);
    }
}
