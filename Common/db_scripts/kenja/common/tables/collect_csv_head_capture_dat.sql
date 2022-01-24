-- kanji=漢字
-- $Id: f4e97b033aecf26b06f3c2c9c206befaa5d4cefd $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--伝票データ
DROP TABLE COLLECT_CSV_HEAD_CAPTURE_DAT

CREATE TABLE COLLECT_CSV_HEAD_CAPTURE_DAT \
( \
        "SCHOOLCD"          varchar(12) not null, \
        "SCHOOL_KIND"       varchar(2)  not null, \
        "YEAR"              varchar(4)  not null, \
        "ROW_NO"            varchar(3)  not null, \
        "HEAD_NAME"         varchar(60), \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_CSV_HEAD_CAPTURE_DAT \
add constraint PK_COLL_CSV_CAPT \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, ROW_NO)
