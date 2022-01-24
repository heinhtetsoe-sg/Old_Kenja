-- kanji=漢字
-- $Id: 9d04290f2e3c321559d40dbbf0b280f09292ee3c $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SCHREG_COMMITTEE_HDETAIL_DAT_OLD
create table SCHREG_COMMITTEE_HDETAIL_DAT_OLD like SCHREG_COMMITTEE_HDETAIL_DAT
insert into SCHREG_COMMITTEE_HDETAIL_DAT_OLD select * from SCHREG_COMMITTEE_HDETAIL_DAT

drop table SCHREG_COMMITTEE_HDETAIL_DAT

create table SCHREG_COMMITTEE_HDETAIL_DAT \
( \
    SCHOOLCD       VARCHAR (12) not null, \
    SCHOOL_KIND    VARCHAR (2) not null, \
    YEAR           VARCHAR (4) not null, \
    SEQ            INTEGER     not null, \
    DETAIL_DATE    DATE        not null, \
    DETAIL_SEQ     INTEGER     not null, \
    DETAIL_REMARK  VARCHAR (60), \
    SCHREGNO       VARCHAR (8) not null, \
    REGISTERCD     VARCHAR (10), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_COMMITTEE_HDETAIL_DAT add constraint PK_SCH_COMM_HD_DAT \
primary key (SCHOOLCD, SCHOOL_KIND, YEAR,SEQ,DETAIL_DATE,DETAIL_SEQ)

insert into SCHREG_COMMITTEE_HDETAIL_DAT \
select \
        '000000000000' AS SCHOOLCD, \
        'H' AS SCHOOL_KIND, \
        YEAR, \
        SEQ, \
        DETAIL_DATE, \
        DETAIL_SEQ, \
        DETAIL_REMARK, \
        SCHREGNO, \
        REGISTERCD, \
        UPDATED \
from SCHREG_COMMITTEE_HDETAIL_DAT_OLD
