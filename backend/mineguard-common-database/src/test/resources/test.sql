stop slave;
start slave;
-- 下面是主库操作
create database if not exists mineguard_test;
use mineguard_test;
create table if not exists test_table (
    id int auto_increment primary key comment "id",
    name varchar(20) comment "name",
    age int comment "年龄"
);

show slave status
