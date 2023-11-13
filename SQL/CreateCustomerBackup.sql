USE moviedb;
create table customers_backup(
id integer auto_increment primary key,
firstName varchar(50) not null,
lastName varchar(50) not null,
ccId varchar(20) not null,
address varchar(200) not null,
email varchar(50) not null,
password varchar(20) not null,
foreign key(ccId) references creditcards(id));
insert into customers_backup select * from customers;