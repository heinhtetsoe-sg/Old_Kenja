-- kanji=漢字
-- $Id: pattern_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table PATTERN_MST

create table PATTERN_MST \
(  \
        "PATTERN_CD"            varchar(3) not null, \
        "PATTERN_NAME"          varchar(150), \
        "PATTERN_ABBV"          varchar(60), \
        "STUDENT_DIV"           varchar(2), \
        "COURSECD"              varchar(1), \
        "MAJORCD"               varchar(3), \
        "COURSECODE"            varchar(4), \
        "REGISTERCD"            varchar(8), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PATTERN_MST  \
add constraint PK_PATTERN_MST  \
primary key  \
(PATTERN_CD)
