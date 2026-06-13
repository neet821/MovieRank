create database if not exists movierank
    default character set utf8mb4
    default collate utf8mb4_unicode_ci;

use movierank;

drop table if exists users;

create table if not exists user (
    username varchar(50) primary key,
    password varchar(50) not null
);

insert into user (username, password)
values ('admin', '123456')
on duplicate key update password = values(password);

create table if not exists movie_rank (
    id bigint primary key auto_increment,
    title varchar(100) not null,
    year int not null,
    douban_score decimal(5,2) not null,
    imdb_score decimal(5,2) not null,
    maoyan_score decimal(5,2) not null,
    final_score decimal(5,2) not null,
    final_rank int not null,
    missing_sources varchar(200) not null default ''
);

delete from movie_rank;

insert into movie_rank
    (title, year, douban_score, imdb_score, maoyan_score, final_score, final_rank, missing_sources)
values
    ('肖申克的救赎', 1994, 97.00, 93.00, 95.00, 95.10, 1, ''),
    ('霸王别姬', 1993, 96.00, 81.00, 96.00, 90.75, 2, '');
