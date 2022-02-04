package com.aiden.dev.simplelibrary.modules.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface BookRepository extends JpaRepository<Book, Long> {
}
