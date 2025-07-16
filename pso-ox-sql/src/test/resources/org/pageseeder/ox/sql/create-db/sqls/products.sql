drop table if exists Products;
create table Products (id serial primary key, name varchar(100) not null, price decimal(10,2) not null);
insert into Products (name, price) values ('Laptop', 1200.99);
insert into Products (name, price) values ('Smartphone', 799.49);
insert into Products (name, price) values ('Headphones', 199.99);