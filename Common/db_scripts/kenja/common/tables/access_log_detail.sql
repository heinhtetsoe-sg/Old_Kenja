drop table ACCESS_LOG_DETAIL

create table ACCESS_LOG_DETAIL \
( \
    UPDATED     timestamp not null, \
    USERID      varchar(60) not null, \
    STAFFCD     varchar(10), \
    PROGRAMID   varchar(100), \
    PCNAME      varchar(50), \
    IPADDRESS   varchar(15), \
    MACADDRESS  varchar(20), \
    ACCESS_CD   varchar(10) not null, \
    SUCCESS_CD  smallint not null default 0, \
    POST_DATA   clob, \
    GET_DATA    clob \
)

alter table ACCESS_LOG_DETAIL add constraint PK_ACCESS_DETAIL primary key (UPDATED, USERID)
