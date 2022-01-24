-- $Id$

drop table LEVY_REQUEST_INCOME_AUTO_DAT

create table LEVY_REQUEST_INCOME_AUTO_DAT \
( \
    SCHOOLCD               varchar(12) not null, \
    SCHOOL_KIND            varchar(2) not null, \
    YEAR                   varchar(4) not null, \
    AUTO_NO                varchar(3) not null, \
    AUTO_NAME              varchar(150) not null, \
    INCOME_L_CD            varchar(2) not null, \
    INCOME_M_CD            varchar(2) not null, \
    REQUEST_REASON         varchar(120), \
    REQUEST_STAFF          varchar(10) not null, \
    REMARK                 varchar(120), \
    COMMODITY_PRICE        integer not null, \
    COLLECT_L_CD           varchar(2) not null, \
    COLLECT_M_CD           varchar(2) not null, \
    COLLECT_S_CD           varchar(3), \
    REGISTERCD             varchar(10), \
    UPDATED                timestamp, \
    AUTO_INCOME_APPROVAL   varchar(1) \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_INCOME_AUTO_DAT add constraint PK_LEVY_INC_AT_D primary key (SCHOOLCD, SCHOOL_KIND, YEAR, AUTO_NO)
