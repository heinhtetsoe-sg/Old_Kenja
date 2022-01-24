-- kanji=漢字
-- $Id: 4019bc4ce9d58eab78fd5e071541262407df243d $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--伝票データ
DROP TABLE COLLECT_CSV_GRP_DAT

CREATE TABLE COLLECT_CSV_GRP_DAT \
( \
        "SCHOOLCD"          varchar(12) not null, \
        "SCHOOL_KIND"       varchar(2)  not null, \
        "YEAR"              varchar(4)  not null, \
        "GRP_CD"            varchar(3)  not null, \
        "COLLECT_L_CD"      varchar(2)  not null, \
        "COLLECT_M_CD"      varchar(2)  not null, \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_CSV_GRP_DAT \
add constraint PK_COLL_CSV_GRPD \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, GRP_CD, COLLECT_L_CD, COLLECT_M_CD)
