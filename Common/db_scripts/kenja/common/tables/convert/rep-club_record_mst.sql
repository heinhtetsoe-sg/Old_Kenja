-- kanji=漢字
-- $Id: 6af65561062a12aa2f4996788fd420ce48d46c0e $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CLUB_RECORD_MST_OLD
create table CLUB_RECORD_MST_OLD like CLUB_RECORD_MST
insert into CLUB_RECORD_MST_OLD select * from CLUB_RECORD_MST

drop table CLUB_RECORD_MST

create table CLUB_RECORD_MST \
( \
    SCHOOLCD       VARCHAR (12) not null, \
    SCHOOL_KIND    VARCHAR (2) not null, \
    RECORDCD       VARCHAR (3) not null, \
    RECORDNAME     VARCHAR (60), \
    REGISTERCD     VARCHAR (10), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLUB_RECORD_MST add constraint PK_CLUB_REC_MST \
primary key (SCHOOLCD, SCHOOL_KIND, RECORDCD)

insert into CLUB_RECORD_MST \
select \
        '000000000000' AS SCHOOLCD, \
        'H' AS SCHOOL_KIND, \
        RECORDCD, \
        RECORDNAME, \
        REGISTERCD, \
        UPDATED \
from CLUB_RECORD_MST_OLD
