package com.aiden.dev.simplelibrary.modules.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    @Autowired BookRepository bookRepository;

    @BeforeEach
    void beforeEach() {
        Book book = Book.builder()
                .title("테스트")
                .author("테스트")
                .publisher("테스트")
                .bookCategory(BookCategory.NOVEL)
                .bookImage("")
                .publicationDate(LocalDateTime.of(2021, 12, 25, 00, 00, 00))
                .build();
        bookRepository.save(book);
    }

    @DisplayName("카테고리로 책 조회 쿼리 테스트")
    @Test
    void findAllByBookCategory() {
        List<Book> books = bookRepository.findAllByBookCategory(BookCategory.NOVEL);
        assertThat(books.size()).isEqualTo(1);
    }
}