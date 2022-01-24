-- kanji=漢字
-- $Id: 0811a12b9d57abe2bfaa3bc2fd29ead0562ecf06 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback


drop table SUBCLASS_RATE_SEMES_DAT

create table SUBCLASS_RATE_SEMES_DAT \
( \
    YEAR            varchar(4)    not null, \
    CLASSCD         varchar(2)    not null, \
    SCHOOL_KIND     varchar(2)    not null, \
    CURRICULUM_CD   varchar(2)    not null, \
    SUBCLASSCD      varchar(6)    not null, \
    SEMESTER        varchar(1)    not null, \
    RATE            smallint, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SUBCLASS_RATE_SEMES_DAT \
add constraint PK_SUBCLASS_RATE_SEMES_D \
primary key \
(YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEMESTER)
