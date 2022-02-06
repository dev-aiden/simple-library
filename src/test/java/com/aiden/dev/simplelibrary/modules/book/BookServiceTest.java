package com.aiden.dev.simplelibrary.modules.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

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
}