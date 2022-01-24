drop table testplace_mst

create table testplace_mst \
        (year            varchar(4)      not null, \
         testp_cd1       varchar(1)      not null, \
         testp_cd2       varchar(2)      not null, \
         testp_name      varchar(30), \
         testp_abbv      varchar(10), \
         testp_capa      smallint, \
         updated         timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table testplace_mst add constraint pk_testplace_mst primary key \
                          (year, testp_cd1, testp_cd2)