-- kanji=´Á»ú
-- $Id: 387dde01d0171ac2dcf8340627cef0ef25acdaef $

drop table GROUP_CLUB_HDETAIL_DAT

create table GROUP_CLUB_HDETAIL_DAT \
( \
    SCHOOLCD            VARCHAR (12) not null, \
    SCHOOL_KIND         VARCHAR (2) not null, \
    CLUBCD              VARCHAR (4) not null, \
    DETAIL_DATE         DATE        not null, \
    GROUPCD             VARCHAR (5) not null, \
    MEET_NAME           VARCHAR (90), \
    HOSTCD              VARCHAR (2), \
    ITEMCD              VARCHAR (3), \
    KINDCD              VARCHAR (3), \
    RECORDCD            VARCHAR (3), \
    DOCUMENT            VARCHAR (60), \
    DETAIL_REMARK       VARCHAR (60), \
    DETAIL_SCHOOL_KIND  VARCHAR (2), \
    REGISTERCD          VARCHAR (10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table GROUP_CLUB_HDETAIL_DAT add constraint PK_GRP_CLUB_HD_DAT \
primary key (SCHOOLCD,SCHOOL_KIND,CLUBCD,DETAIL_DATE,GROUPCD)
