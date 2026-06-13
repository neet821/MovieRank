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

create table if not exists source_movie_rank (
    id bigint primary key auto_increment,
    source_code varchar(30) not null,
    source_name varchar(80) not null,
    title varchar(150) not null,
    year int not null,
    source_rank int not null,
    detail_url varchar(500) not null default '',
    created_at timestamp not null default current_timestamp,
    unique key uk_source_movie (source_code, title, year)
);

delete from source_movie_rank;

insert into source_movie_rank
    (source_code, source_name, title, year, source_rank, detail_url)
values
    ('IMDB', 'IMDb', '肖申克的救赎', 1994, 1, 'https://www.imdb.com/chart/top/'),
    ('IMDB', 'IMDb', '教父', 1972, 2, 'https://www.imdb.com/chart/top/'),
    ('DOUBAN', '豆瓣', '肖申克的救赎', 1994, 1, 'https://movie.douban.com/top250'),
    ('DOUBAN', '豆瓣', '霸王别姬', 1993, 2, 'https://movie.douban.com/top250'),
    ('LETTERBOXD', 'Letterboxd', '教父', 1972, 2, 'https://letterboxd.com/films/popular/'),
    ('LETTERBOXD', 'Letterboxd', '七武士', 1954, 6, 'https://letterboxd.com/films/popular/'),
    ('BFI', 'BFI Sight and Sound', '让娜·迪尔曼', 1975, 1, 'https://www.bfi.org.uk/sight-and-sound/greatest-films-all-time'),
    ('BFI', 'BFI Sight and Sound', '迷魂记', 1958, 2, 'https://www.bfi.org.uk/sight-and-sound/greatest-films-all-time'),
    ('TSPDT', 'TSPDT', '公民凯恩', 1941, 1, 'https://theyshootpictures.com/gf1000_all1000films_table.php'),
    ('TSPDT', 'TSPDT', '迷魂记', 1958, 2, 'https://theyshootpictures.com/gf1000_all1000films_table.php');
