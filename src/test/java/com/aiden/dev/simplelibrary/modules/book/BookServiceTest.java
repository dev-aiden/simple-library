package com.aiden.dev.simplelibrary.modules.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @InjectMocks BookService bookService;
    @Mock BookRepository bookRepository;

    @DisplayName("초기 책 데이터 추가 테스트")
    @Test
    void initBookData() {
        // when
        bookService.initBookData();

        // then
        then(bookRepository).should(times(3)).save(any());
    }

    @DisplayName("모든 책 조회 테스트")
    @Test
    void getAllBooks() {
        // When
        bookService.getAllBooks();

        // Then
        verify(bookRepository).findAll();
    }

    @DisplayName("특정 타테고리 내 책 조회 테스트")
    @Test
    void getAllBooksByBookCategory() {
        // When
        bookService.getAllBooksByBookCategory(BookCategory.NOVEL);

        // Then
        verify(bookRepository).findAllByBookCategory(BookCategory.NOVEL);
    }

    @DisplayName("책 상세 정보 조회 테스트")
    @Test
    void getBookDetail() {
        // When
        bookService.getBookDetail(1L);

        // Then
        verify(bookRepository).findById(1L);
    }
}