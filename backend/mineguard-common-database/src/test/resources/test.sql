
create database if not exists mineguard_test;
use mineguard_test;

create table test_table(
    id int auto_increment primary key comment "id",
    name varchar(20) comment "name",
    age int comment "年龄"
);
