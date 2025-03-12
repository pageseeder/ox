drop table if exists Customers;
create table Customers ( id serial primary key, name varchar(100) not null, email varchar(100) unique not null);
insert into Customers (name, email) values ('Alice Johnson', 'alice@example.com');
insert into Customers (name, email) values ('Bob Smith', 'bob@example.com');
insert into Customers (name, email) values ('Charlie Davis', 'charlie@example.com');