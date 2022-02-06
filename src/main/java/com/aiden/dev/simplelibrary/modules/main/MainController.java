package com.aiden.dev.simplelibrary.modules.main;

import com.aiden.dev.simplelibrary.modules.account.Account;
import com.aiden.dev.simplelibrary.modules.account.validator.CurrentAccount;
import com.aiden.dev.simplelibrary.modules.book.Book;
import com.aiden.dev.simplelibrary.modules.book.BookCategory;
import com.aiden.dev.simplelibrary.modules.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final BookService bookService;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model, @RequestParam(value = "category", defaultValue = "all") String category) {
        if(account != null) {
            model.addAttribute(account);
        }
        model.addAttribute("category", category);
        if(category.equals("all")) {
            model.addAttribute("categoryName", "전체보기");
            model.addAttribute("bookList", bookService.getAllBooks());
        } else {
            model.addAttribute("categoryName", BookCategory.valueOf(category).getCategoryName());
            model.addAttribute("bookList", bookService.getAllBooksByBookCategory(BookCategory.valueOf(category)));
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
