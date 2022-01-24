-- kanji=漢字
-- $Id: db3dcd04af6bea920d17dbcafcbbbff234f17e14 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table STANDARD_CREDIT_MST

create table STANDARD_CREDIT_MST( \
     CLASSCD        VARCHAR(2) NOT NULL, \
     SCHOOL_KIND    VARCHAR(2) NOT NULL, \
     CURRICULUM_CD  VARCHAR(2) NOT NULL, \
     SUBCLASSCD     VARCHAR(6) NOT NULL, \
     CREDITS        SMALLINT, \
     REGISTERCD     VARCHAR(8), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

alter table STANDARD_CREDIT_MST add constraint PK_STN_CREDIT_MST primary key (CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
