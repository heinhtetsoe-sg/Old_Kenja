-- kanji=漢字
-- $Id: staff_subclass_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table STAFF_SUBCLASS_DAT

create table STAFF_SUBCLASS_DAT \
(  \
    YEAR            varchar(4) not null, \
    STAFFCD         varchar(8) not null, \
    CLASSCD         varchar(2) not null, \
    CURRICULUM_CD   varchar(1) not null, \
    SUBCLASSCD      varchar(6) not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table STAFF_SUBCLASS_DAT  \
add constraint PK_STAFF_SUBCLASS \
primary key  \
(YEAR, STAFFCD, CLASSCD, CURRICULUM_CD, SUBCLASSCD)
