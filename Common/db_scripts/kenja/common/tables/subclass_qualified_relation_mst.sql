-- kanji=漢字
-- $Id: eddd3a911039bd7a7c0443d19487317b0c7c32bc $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SUBCLASS_QUALIFIED_RELATION_MST

create table SUBCLASS_QUALIFIED_RELATION_MST \
(  \
    YEAR                VARCHAR(4)  not null, \
    CLASS_CD            VARCHAR(2)  not null, \
    SCHOOL_KIND         VARCHAR(2)  not null, \
    CURRICULUM_CD       VARCHAR(2)  not null, \
    SUBCLASS_CD         VARCHAR(6)  not null, \
    GRADE               VARCHAR(2)  not null, \
    QUALIFIED_CD        VARCHAR(4)  not null, \
    RESULT_CD           VARCHAR(4)  not null, \
    SCORE               INTEGER, \
    CREDIT              INTEGER, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SUBCLASS_QUALIFIED_RELATION_MST add constraint PK_SUB_QUAL_REL_M \
primary key (YEAR, CLASS_CD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASS_CD, GRADE, QUALIFIED_CD, RESULT_CD)
