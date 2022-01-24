-- kanji=漢字
-- $Id: 3b14d2f3b073d2ebfa58c3a41839ef2412887369 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--伝票データ
DROP TABLE COLLECT_CSV_INFO_DAT

CREATE TABLE COLLECT_CSV_INFO_DAT \
( \
        "SCHOOLCD"          varchar(12) not null, \
        "SCHOOL_KIND"       varchar(2)  not null, \
        "YEAR"              varchar(4)  not null, \
        "ROW_NO"            varchar(3)  not null, \
        "GRP_CD"            varchar(3), \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_CSV_INFO_DAT \
add constraint PK_COLL_CSV_INFO \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, ROW_NO)
