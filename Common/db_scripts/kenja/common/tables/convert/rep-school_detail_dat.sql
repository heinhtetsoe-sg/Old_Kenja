
drop table SCHOOL_DETAIL_DAT_OLD
create table SCHOOL_DETAIL_DAT_OLD like SCHOOL_DETAIL_DAT
insert into SCHOOL_DETAIL_DAT_OLD select * from SCHOOL_DETAIL_DAT

drop table SCHOOL_DETAIL_DAT

create table SCHOOL_DETAIL_DAT \
(  \
    YEAR            varchar(4)  not null, \
    SCHOOLCD        varchar(12) not null, \
    SCHOOL_KIND     varchar(2) not null, \
    SCHOOL_SEQ      varchar(3)  not null, \
    SCHOOL_REMARK1  varchar(90), \
    SCHOOL_REMARK2  varchar(90), \
    SCHOOL_REMARK3  varchar(90), \
    SCHOOL_REMARK4  varchar(90), \
    SCHOOL_REMARK5  varchar(90), \
    SCHOOL_REMARK6  varchar(90), \
    SCHOOL_REMARK7  varchar(90), \
    SCHOOL_REMARK8  varchar(90), \
    SCHOOL_REMARK9  varchar(90), \
    SCHOOL_REMARK10 varchar(90), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

INSERT INTO SCHOOL_DETAIL_DAT \
    SELECT \
        YEAR, \
        '000000000000', \
        'H', \
        SCHOOL_SEQ, \
        SCHOOL_REMARK1, \
        SCHOOL_REMARK2, \
        SCHOOL_REMARK3, \
        SCHOOL_REMARK4, \
        SCHOOL_REMARK5, \
        SCHOOL_REMARK6, \
        SCHOOL_REMARK7, \
        SCHOOL_REMARK8, \
        SCHOOL_REMARK9, \
        SCHOOL_REMARK10, \
        REGISTERCD, \
        UPDATED \
    FROM \
        SCHOOL_DETAIL_DAT_OLD

alter table SCHOOL_DETAIL_DAT add constraint PK_SCHOOL_DTL_DAT \
primary key (YEAR, SCHOOLCD, SCHOOL_KIND, SCHOOL_SEQ)


