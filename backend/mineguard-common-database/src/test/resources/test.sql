
create database if not exists mineguard_test;
use mineguard_test;
drop table if exists test_table;
create table if not exists test_table (
    id int auto_increment primary key comment "id",
    name varchar(20) comment "name",
    age int comment "年龄"
);
