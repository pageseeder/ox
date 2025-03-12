drop table if exists Orders;
create table Orders (id serial primary key, customer_id int references Customers(id), product_id int references Products(id), quantity int not null);
insert into Orders (customer_id, product_id, quantity) values (1, 2, 1);
insert into Orders (customer_id, product_id, quantity) values (2, 3, 2);
insert into Orders (customer_id, product_id, quantity) values (3, 1, 1);
