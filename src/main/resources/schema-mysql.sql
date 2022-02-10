create table account (
     book_rental_availability_notification_by_email tinyint(1) default 0,
     book_rental_availability_notification_by_web tinyint(1) default 1,
     book_rental_notification_by_email tinyint(1) default 0,
     book_rental_notification_by_web tinyint(1) default 1,
     book_return_notification_by_email tinyint(1) default 0,
     book_return_notification_by_web tinyint(1) default 1,
     email varchar(255) not null,
     email_check_token varchar(255),
     email_check_token_generated_at datetime(6),
     email_verified bit not null,
     joined_at datetime(6),
     login_id varchar(255) not null,
     nickname varchar(255) not null,
     password varchar(255) not null,
     profile_image longtext,
     primary key (id),
     CONSTRAINT UK_email UNIQUE (email),
     CONSTRAINT UK_login_id UNIQUE (login_id),
     CONSTRAINT UK_nickname UNIQUE (nickname)
) engine=InnoDB;

create table persistent_logins (
    series varchar(64) not null,
    last_used datetime(6) not null,
    token varchar(64) not null,
    username varchar(64) not null,
    primary key (series)
) engine=InnoDB;

create table book (
    id bigint not null auto_increment,
    author varchar(255) not null,
    book_category varchar(255) not null,
    book_image longtext,
    created_at datetime(6),
    is_lent tinyint(1) default 0,
    publication_date datetime(6) not null,
    publisher varchar(255) not null,
    title varchar(255) not null,
    updated_at datetime(6),
    account_id bigint,
    primary key (id),
    foreign key (account_id) references account (id)
) engine=InnoDB;

create table review (
    id bigint not null auto_increment,
    contents longtext not null,
    created_at datetime(6),
    grade integer not null,
    updated_at datetime(6),
    account_id bigint not null,
    book_id bigint not null,
    primary key (id),
    foreign key (account_id) references account (id)
    foreign key (book_id) references book (id)
) engine=InnoDB;