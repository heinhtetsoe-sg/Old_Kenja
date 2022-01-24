-- kanji=漢字
-- $Id: 09d5377565707eaa7d3be0b7faf7c9b5b1f5b8b5 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CLUB_HOST_MST_OLD
create table CLUB_HOST_MST_OLD like CLUB_HOST_MST
insert into CLUB_HOST_MST_OLD select * from CLUB_HOST_MST

drop table CLUB_HOST_MST

create table CLUB_HOST_MST \
( \
    SCHOOLCD       VARCHAR (12) not null, \
    SCHOOL_KIND    VARCHAR (2) not null, \
    HOSTCD         VARCHAR (2) not null, \
    HOSTNAME       VARCHAR (60), \
    REGISTERCD     VARCHAR (10), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLUB_HOST_MST add constraint PK_CLUB_HOST_MST \
primary key (SCHOOLCD, SCHOOL_KIND, HOSTCD)

insert into CLUB_HOST_MST \
select \
        '000000000000' AS SCHOOLCD, \
        'H' AS SCHOOL_KIND, \
        HOSTCD, \
        HOSTNAME, \
        REGISTERCD, \
        UPDATED \
from CLUB_HOST_MST_OLD
