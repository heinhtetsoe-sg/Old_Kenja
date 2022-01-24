-- kanji=漢字
-- $Id: 1b3c8fb1c237e98c80355e0a7328bc78aee3f0cb $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

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
