-- kanji=漢字
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table TESTITEM_LABEL_MST

create table TESTITEM_LABEL_MST( \
    YEAR            varchar(4)  not null, \
    SEMESTER        varchar(1)  not null, \
    GRADE           varchar(2)  not null, \
    TESTKINDCD      varchar(2)  not null, \
    TESTITEMCD      varchar(2)  not null, \
    SCORE_DIV       varchar(2)  not null, \
    CLASSCD         varchar(2)  not null, \
    SCHOOL_KIND     varchar(2)  not null, \
    CURRICULUM_CD   varchar(2)  not null, \
    SUBCLASSCD      varchar(6)  not null, \
    CHAIRCD         varchar(7)  not null, \
    PROCTOR_STAFFCD varchar(10), \
    RETURN_STAFFCD  varchar(10), \
    Q_PAPERS        smallint, \
    A_PAPERS        smallint, \
    Q_BOTH_DIV      varchar(1), \
    A_BOTH_DIV      varchar(1), \
    DUE_DATE        date, \
    DUE_TIME        varchar(2), \
    REMARK          varchar(240), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table TESTITEM_LABEL_MST add constraint PK_TESTITEM_LABEL_MST \
      primary key (YEAR, SEMESTER, GRADE, TESTKINDCD, TESTITEMCD, SCORE_DIV, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CHAIRCD)
