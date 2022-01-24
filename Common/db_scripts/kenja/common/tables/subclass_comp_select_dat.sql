-- kanji=漢字
-- $Id: fda78a1f944add7dab94263283310d8824069c61 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SUBCLASS_COMP_SELECT_DAT

create table SUBCLASS_COMP_SELECT_DAT(  \
    YEAR            varchar(4)  not null, \
    GRADE           varchar(2)  not null, \
    COURSECD        varchar(1)  not null, \
    MAJORCD         varchar(3)  not null, \
    COURSECODE      varchar(4)  not null, \
    GROUPCD         varchar(3)  not null, \
    CLASSCD         varchar(2)  not null, \
    SCHOOL_KIND     varchar(2)  not null, \
    CURRICULUM_CD   varchar(2)  not null, \
    SUBCLASSCD      varchar(6)  not null, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SUBCLASS_COMP_SELECT_DAT add constraint PK_SUBCLASS_CSD \
primary key (YEAR,GRADE, COURSECD, MAJORCD, COURSECODE,GROUPCD,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)
