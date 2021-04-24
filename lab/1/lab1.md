# DB lab1

## 创建表和数据

使用了助教给出的测试用例，但是 `Book.name` 是 `varchar(10)`，所以没有放下「Oracle Database 编程艺术」、「分布式数据库系统及其应用」、「Oracle 数据库系统管理与运维」和「Fluent python」这些书

创建 table

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
```

插入测试数据

```sql
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

### 实体完整性

以下是错误的，因为主键为 `null`

```sql
insert into Book value(null, 'name_abc', null, 233, 1);
```

### 参照完整性

以下是错误的，因为 `book_ID` 为外键

```sql
insert into Borrow value('b99', 'r1', '2099.1.1', '2099.12.30');
```

### 用户自定义完整性

以下是错误的，因为 `status`、`price` 无意义

```sql
insert into Book value('b99', 'name_abc', 'name_author', -5, 2);
```

## SQL 查询

### 检索读者 Rose 的读者号和地址

```sql
select ID, address from Reader where name = 'Rose';
```

### 检索读者 Rose 所借阅读书（包括已还和未还图书）的图书名和借期

```sql
select Book.name, Borrow_Date from Reader, Book, Borrow
where Borrow.reader_id = Reader.ID and Borrow.book_id = Book.ID and Reader.name = 'Rose';
```

### 检索未借阅图书的读者姓名

```sql
select Reader.name from Reader
where Reader.ID not in (select Reader_ID from Borrow group by Reader_ID);
```

### 检索 Ullman 所写的书的书名和单价

```sql
select name, price from Book where author = 'Ullman';
```

### 检索读者“李林”借阅未还的图书的图书号和书名

```sql
select Book.ID, Book.name from Book, Reader, Borrow
where Book.ID = Borrow.book_ID and Reader.ID = Borrow.Reader_ID and Reader.name = '李林' and Book.status = 1 and Return_Date is null;
```

### 检索借阅图书数目超过 3 本的读者姓名

```sql
select Reader.name from Reader, Borrow
where Reader.ID = Borrow.Reader_ID
  and Reader.ID in (select Reader_ID from Borrow group by Reader_ID having count(*) > 3)
group by Reader.name;
```

### 检索没有借阅读者“李林”所借的任何一本书的读者姓名和读者号

```sql
select name, ID from Reader
where ID not in (
    select Reader.ID from Borrow, Reader
    where Reader.ID = Borrow.Reader_ID
      and Borrow.book_ID in (select book_ID from Borrow, Reader where Borrow.Reader_ID = Reader.ID and Reader.name = '李林' group by book_ID)
);
```

### 检索书名中包含“Oracle”的图书书名及图书号

```sql
select name, ID from Book where name like '%Oracle%';
```

### 创建一个读者借书信息的视图，该视图包含读者号、姓名、所借图书号、图书名和借期；并使用该视图查询最近一年所有读者的读者号以及所借阅的不同图书数

```sql
create view borrow_view (Reader_ID, Reader_name, Book_ID, Book_name, Borrow_Date) as (
    select Reader_ID, Reader.name, book_ID, Book.name, Borrow_Date from Reader, Book, Borrow
    where Reader.ID = Borrow.Reader_ID and Book.ID = Borrow.book_ID
);

select Reader_ID, count(distinct Book_ID) as BookCnts from borrow_view
where date_sub(now(), interval 1 year) <= Borrow_Date
group by Reader_ID;
```

## Book.ID 存储过程

TODO:

```sql
drop procedure if exists modify_bookid;
delimiter //
create procedure modify_bookid(in idp char(8), in idm char(8))
BEGIN
    #alter table book drop primary key;
    alter table borrow drop foreign key FK_B;
    update book
    set id = idm
    where book.id = idp;

    update borrow
    set borrow.book_id = idm
    where borrow.book_id = idp;
    alter table borrow add constraint FK_B foreign key(book_id) references book(id);
    #alter table book add primary key(id);
END //
delimiter ;

select *from borrow;
call modify_bookid('b12', 'b13');
call modify_bookid('b13', 'b12');
select * from book;
select *from borrow;
```

## status 存储过程

TODO:

```sql
drop procedure if exists check_status;
delimiter //
create procedure check_status (out num int)
begin
    declare idc char(8);
    declare d date;
    declare s, state int default 0;
    declare ct cursor for(
        select book.id, book.status, t.return_date from  book,
        (select tt.book_id, return_date from borrow,(
              select book_id, max(borrow_date) as bd from borrow
              group by book_id) tt
		    where borrow.borrow_date = tt.bd and tt.book_id = borrow.book_id
		) t
        where book.id = t.book_id);
	declare continue handler for not found set state = 1;
    open ct;
    set num = 0;
    repeat
        if state = 0 then
            fetch ct into idc, s, d;
            if d is null and s = 0 then
			    set num = num + 1;
			end if;
		    if d is not null and s = 1 then
                set num = num + 1;
		    end if;
	    end if;
        until state = 1
	end repeat;
    close ct;
end //
delimiter ;

call check_status(@num);
select @num;
```

## status 触发器

TODO

<!-- drop trigger if exists modify_status; -->

```sql
delimiter //
create trigger modify_status after update on borrow for each row
begin
    declare s date;
    select new.return_date into s;
    if s is not null then
        update book set status = 0
        where book.id = new.book_id;
	end if;
    if s is null then
        update book set status = 1
        where book.id = new.book_id;
	end if;
end //

create trigger modify_status2 after insert on borrow for each row
begin
    declare s date;
    select new.return_date into s;
    if s is not null then
        update book set status = 0
        where book.id = new.book_id;
	end if;
    if s is null then
        update book set status = 1
        where book.id = new.book_id;
	end if;
end //
delimiter ;

select * from book;
update borrow set return_date = '2021.12.24'
where book_id = 'b11';
select * from book;
insert into borrow values ('b11', 'r6', '2021.12.25', null);
select * from book;
```
