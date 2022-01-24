-- kanji=漢字
-- $Id: fc9f11f58d1b8167d018aeddc77536580e4bf173 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--伝票データ
DROP TABLE COLLECT_CSV_GRP_MST

CREATE TABLE COLLECT_CSV_GRP_MST \
( \
        "SCHOOLCD"          varchar(12) not null, \
        "SCHOOL_KIND"       varchar(2)  not null, \
        "YEAR"              varchar(4)  not null, \
        "GRP_CD"            varchar(3)  not null, \
        "GRP_NAME"          varchar(60), \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_CSV_GRP_MST \
add constraint PK_COLL_CSV_GRPM \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, GRP_CD)
