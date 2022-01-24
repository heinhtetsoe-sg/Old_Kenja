-- kanji=漢字
-- $Id: 2edae9ccf09ec2be59aa8c1a2af5fc72b904d229 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ATTEND_SUBCLASS_DAT

CREATE TABLE ATTEND_SUBCLASS_DAT( \
    COPYCD         varchar(1) not null, \
    YEAR           varchar(4) not null, \
    MONTH          varchar(2) not null, \
    SEMESTER       varchar(1) not null, \
    SCHREGNO       varchar(8) not null, \
    CLASSCD        varchar(2) not null, \
    SCHOOL_KIND    varchar(2) not null, \
    CURRICULUM_CD  varchar(2) not null, \
    SUBCLASSCD     varchar(6) not null, \
    APPOINTED_DAY  varchar(2), \
    LESSON         smallint, \
    OFFDAYS        smallint, \
    ABSENT         smallint, \
    SUSPEND        smallint, \
    MOURNING       smallint, \
    ABROAD         smallint, \
    SICK           smallint, \
    NOTICE         smallint, \
    NONOTICE       smallint, \
    NURSEOFF       smallint, \
    LATE           smallint, \
    EARLY          smallint, \
    VIRUS          smallint, \
    KOUDOME        SMALLINT, \
    REGISTERCD     varchar(8), \
    UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

ALTER TABLE ATTEND_SUBCLASS_DAT add constraint pk_at_sub_dat primary key (COPYCD,YEAR,MONTH,SCHREGNO,SEMESTER,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)

