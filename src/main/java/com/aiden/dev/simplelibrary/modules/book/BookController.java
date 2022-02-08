package com.aiden.dev.simplelibrary.modules.book;

import com.aiden.dev.simplelibrary.modules.account.Account;
import com.aiden.dev.simplelibrary.modules.account.validator.CurrentAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/{bookId}")
    public String detailBookForm(@PathVariable Long bookId, @CurrentAccount Account account, Model model) {
        Book book = bookService.getBookDetail(bookId).orElseThrow(() -> new IllegalArgumentException(bookId + "에 해당하는 책이 존재하지 않습니다."));
        model.addAttribute("book", book);
        return "book/detail";
    }
}
