drop table city_mst

CREATE TABLE city_mst \
( \
citycd varchar(5) not null, \
cityname  varchar(20), \
cityabbv varchar(10), \
updated timestamp default current timestamp \
)

alter table city_mst add constraint pk_city_mst primary key (citycd)
