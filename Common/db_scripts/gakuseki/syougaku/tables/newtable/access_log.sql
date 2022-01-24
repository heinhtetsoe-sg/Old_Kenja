drop table access_log

CREATE TABLE access_log \
( \
updated timestamp not null, \
userid  varchar(60) not null, \
programid varchar(10), \
pcname varchar(50), \
ipaddress varchar(15), \
macaddress varchar(20), \
access_cd char(1) not null, \
success_cd smallint not null default 0 \
)

alter table access_log add constraint pk_access_log primary key (updated,userid)
