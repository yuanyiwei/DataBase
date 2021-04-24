# DB lab1

## 创建表和数据

使用了助教给出的测试用例，但是 `Book.name` 是 `varchar(10)`，所以没有放下「Oracle Database 编程艺术」、「分布式数据库系统及其应用」、「Oracle 数据库系统管理与运维」和「Fluent python」这些书

```sql
create table Borrow
(
    book_ID     char(8) not null,
    Reader_ID   char(8) not null,
    Borrow_Date date    null,
    Return_Date date    null,
    primary key (book_ID, Reader_ID),
    constraint Borrow_Book_ID_fk
        foreign key (book_ID) references Book (ID),
    constraint Borrow_Reader_ID_fk
        foreign key (Reader_ID) references Reader (ID)
);

create table Book
(
    ID     char(8)       not null
        primary key,
    name   varchar(10)   not null,
    author varchar(10)   null,
    price  float         null,
    status int default 0 null
);

create table Reader
(
    ID      char(8)     not null
        primary key,
    name    varchar(10) null,
    age     int         null,
    address varchar(20) null
);

# Book
insert into Book value('b1', '数据库系统实现', 'Ullman', 59.0, 1);
insert into Book value('b2', '数据库系统概念', 'Abraham', 59.0, 1);
insert into Book value('b3', 'C++ Primer', 'Stanley', 78.6, 1);
insert into Book value('b4', 'Redis设计与实现', '黄建宏', 79.0, 1);
insert into Book value('b5', '人类简史', 'Yuval', 68.00, 0);
insert into Book value('b6', '史记(公版)', '司马迁', 220.2, 1);
insert into Book value('b7', 'Oracle编程艺术', 'Thomas', 43.1, 1);
# insert into Book value('b7', 'Oracle Database 编程艺术', 'Thomas', 43.1, 1);
insert into Book value('b8', '分布式数据库系统', '邵佩英', 30.0, 0);
# insert into Book value('b8', '分布式数据库系统及其应用', '邵佩英', 30.0, 0);
insert into Book value('b9', 'Oracle管理运维', '张立杰', 51.9, 0);
# insert into Book value('b9', 'Oracle 数据库系统管理与运维', '张立杰', 51.9, 0);
insert into Book value('b10', '数理逻辑', '汪芳庭', 22.0, 0);
insert into Book value('b11', '三体', '刘慈欣', 23.0, 1);
insert into Book value('b12', '流畅的python', 'Luciano', 354.2, 1);
# insert into Book value('b12', 'Fluent python', 'Luciano', 354.2, 1);

# Reader
insert into Reader value('r1', '李林', 18, '中国科学技术大学东校区');
insert into Reader value('r2', 'Rose', 22, '中国科学技术大学北校区');
insert into Reader value('r3', '罗永平', 23, '中国科学技术大学西校区');
insert into Reader value('r4', 'Nora', 26, '中国科学技术大学北校区');
insert into Reader value('r5', '汤晨', 22, '先进科学技术研究院');

# Borrow
insert into Borrow value('b5','r1',  '2021-03-12', '2021-04-07');
insert into Borrow value('b6','r1',  '2021-03-08', '2021-03-19');
insert into Borrow value('b11','r1',  '2021-01-12', NULL);

insert into Borrow value('b3', 'r2', '2021-02-22', NULL);
insert into Borrow value('b9', 'r2', '2021-02-22', '2021-04-10');
insert into Borrow value('b7', 'r2', '2021-04-11', NULL);

insert into Borrow value('b1', 'r3', '2021-04-02', NULL);
insert into Borrow value('b2', 'r3', '2021-04-02', NULL);
insert into Borrow value('b4', 'r3', '2021-04-02', '2021-04-09');
insert into Borrow value('b7', 'r3', '2021-04-02', '2021-04-09');

insert into Borrow value('b6', 'r4', '2021-03-31', NULL);
insert into Borrow value('b12', 'r4', '2021-03-31', NULL);

insert into Borrow value('b4', 'r5', '2020-04-10', NULL);
```

## 验证三个完整性

## SQL 查询

## Book.ID 存储过程

## status 存储过程

## status 触发器
