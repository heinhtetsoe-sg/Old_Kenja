-- $Id: ba1a707d0be6b0c675826fcfc2fb912625f2afcd $

DROP TABLE SCHREG_CLUB_HDETAIL_DAT_OLD
CREATE TABLE SCHREG_CLUB_HDETAIL_DAT_OLD LIKE SCHREG_CLUB_HDETAIL_DAT
insert into SCHREG_CLUB_HDETAIL_DAT_OLD select * from SCHREG_CLUB_HDETAIL_DAT

drop table SCHREG_CLUB_HDETAIL_DAT
create table SCHREG_CLUB_HDETAIL_DAT \
    ( \
        SCHOOLCD            varchar(12) not null, \
        SCHOOL_KIND         varchar(2)  not null, \
        SCHREGNO            varchar(8)  not null, \
        CLUBCD              varchar(4)  not null, \
        DETAIL_DATE         date        not null, \
        DETAIL_SEQ          integer     not null, \
        MEET_NAME           varchar(90), \
        DIV                 varchar(1) not null, \
        GROUPCD             varchar(5), \
        HOSTCD              varchar(2), \
        ITEMCD              varchar(3), \
        KINDCD              varchar(3), \
        RECORDCD            varchar(3), \
        DOCUMENT            varchar(60), \
        DETAIL_REMARK       varchar(60), \
        DETAIL_SCHOOL_KIND  varchar(2), \
        REGISTERCD          varchar(10), \
        UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SCHREG_CLUB_HDETAIL_DAT add constraint PK_SCH_CLUB_HD_DAT \
primary key (SCHOOLCD, SCHOOL_KIND, SCHREGNO,CLUBCD,DETAIL_DATE,DETAIL_SEQ)

insert into SCHREG_CLUB_HDETAIL_DAT \
select \
        SCHOOLCD, \
        SCHOOL_KIND, \
        SCHREGNO, \
        CLUBCD, \
        DETAIL_DATE, \
        DETAIL_SEQ, \
        MEET_NAME, \
        DIV, \
        GROUPCD, \
        HOSTCD, \
        ITEMCD, \
        KINDCD, \
        RECORDCD, \
        DOCUMENT, \
        DETAIL_REMARK, \
        cast(null as varchar(2)) AS DETAIL_SCHOOL_KIND, \
        REGISTERCD, \
        UPDATED \
from SCHREG_CLUB_HDETAIL_DAT_OLD
