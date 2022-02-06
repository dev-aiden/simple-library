package com.aiden.dev.simplelibrary.modules.book;

public enum BookCategory {
    NOVEL("소설"),
    POETRY_ESSAY("시/에세이"),
    HUMANITIES("인문"),
    COOKING("요리"),
    HEALTH("건강"),
    SPORTS("스포츠"),
    ECONOMY_BUSINESS("경제/경영"),
    POLITICS_SOCIAL("정치/사회"),
    SELF_HELP("자기계발");

    private final String categoryName;

    BookCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }
}