package com.aiden.dev.simplelibrary.modules.book;

import com.aiden.dev.simplelibrary.modules.account.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureTestDatabase
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AccountService accountService;
    @MockBean BookService bookService;

    @DisplayName("책 상세 페이지 보이는지 테스트 - 존재하지 않는 책")
    @Test
    void detailBookForm_not_exist_post() {
        assertThatThrownBy(() -> mockMvc.perform(get("/book/2"))).hasCause(new IllegalArgumentException("2에 해당하는 책이 존재하지 않습니다."));
    }

    @DisplayName("책 상세 페이지 보이는지 테스트 - 존재하는 책")
    @Test
    void detailBookForm_exist_post() throws Exception {
        Book book = Book.builder()
                .title("테스트")
                .author("테스트")
                .publisher("테스트")
                .bookCategory(BookCategory.NOVEL)
                .bookImage(null)
                .publicationDate(LocalDateTime.of(2022, 01, 01, 00, 00, 00))
                .build();

        when(bookService.getBookDetail(any())).thenReturn(Optional.of(book));

        mockMvc.perform(get("/book/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/detail"))
                .andExpect(model().attributeExists("book"));
    }
}