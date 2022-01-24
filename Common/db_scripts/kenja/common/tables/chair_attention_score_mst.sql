-- kanji=漢字
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table CHAIR_ATTENTION_SCORE_MST

create table CHAIR_ATTENTION_SCORE_MST ( \
    YEAR            varchar(4) not null, \
    SEMESTER        varchar(1) not null, \
    TESTKINDCD      varchar(2) not null, \
    TESTITEMCD      varchar(2) not null, \
    SCORE_DIV       varchar(2) not null, \
    CLASSCD         varchar(2) not null, \
    SCHOOL_KIND     varchar(2) not null, \
    CURRICULUM_CD   varchar(2) not null, \
    SUBCLASSCD      varchar(6) not null, \
    CHAIRCD         varchar(7) not null, \
    ATTENTION_SCORE smallint, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CHAIR_ATTENTION_SCORE_MST add constraint PK_CHAIR_ATTENTION_SCORE_MST \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CHAIRCD)
