-- kanji=漢字
-- $Id: rec_report_info.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table REC_REPORT_INFO

create table REC_REPORT_INFO \
(  \
    YEAR            varchar(4) not null, \
    CLASSCD         varchar(2) not null, \
    CURRICULUM_CD   varchar(1) not null, \
    SUBCLASSCD      varchar(6) not null, \
    WRITE_NUMS      varchar(256), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REC_REPORT_INFO  \
add constraint PK_REC_REPORT_INFO \
primary key  \
(YEAR, CLASSCD, CURRICULUM_CD, SUBCLASSCD)
