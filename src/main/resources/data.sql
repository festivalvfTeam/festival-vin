insert into usr (id, email, password, username)
values (1, 'admin@yandex.ru', 'admin', 'admin');

insert into user_role (user_id, roles)
values(1, 'ADMIN');

create extension if not exists pgcrypto;

update usr set password = crypt(password, gen_salt('bf', 8));