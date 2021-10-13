drop table if exists account;

create table account (
    id bigint not null auto_increment,
    email varchar(255) not null,
    email_check_token varchar(255),
    email_check_token_generated_at datetime(6),
    email_verified tinyint(1) default 0 not null,
    joined_at datetime(6),
    login_id varchar(255) not null,
    nickname varchar(255) not null,
    notification_email tinyint(1) default 1 not null,
    notification_web tinyint(1) default 1 not null,
    password varchar(255) not null,
    profile_image longtext,
    primary key (id)
) engine=InnoDB;