-- kanji=漢字
-- $Id: 01a04959672b7fbc824db96682916af60c43ff80 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SUBCLASS_REPLACE_SUBSTITUTION_DAT

create table SUBCLASS_REPLACE_SUBSTITUTION_DAT ( \
    REPLACECD                   varchar(1) not null, \
    YEAR                        varchar(4) not null, \
    SUBSTITUTION_CLASSCD        VARCHAR(2) NOT NULL, \
    SUBSTITUTION_SCHOOL_KIND    VARCHAR(2) NOT NULL, \
    SUBSTITUTION_CURRICULUM_CD  VARCHAR(2) NOT NULL, \
    SUBSTITUTION_SUBCLASSCD     VARCHAR(6) NOT NULL, \
    ATTEND_CLASSCD              VARCHAR(2) NOT NULL, \
    ATTEND_SCHOOL_KIND          VARCHAR(2) NOT NULL, \
    ATTEND_CURRICULUM_CD        VARCHAR(2) NOT NULL, \
    ATTEND_SUBCLASSCD           VARCHAR(6) NOT NULL, \
    SUBSTITUTION_TYPE_FLG       varchar(1), \
    STUDYREC_CREATE_FLG         varchar(1), \
    PRINT_FLG1                  varchar(1), \
    PRINT_FLG2                  varchar(1), \
    PRINT_FLG3                  varchar(1), \
    REGISTERCD                  varchar(8), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table SUBCLASS_REPLACE_SUBSTITUTION_DAT add constraint PK_SUBREPSUBST_DAT \
        primary key (YEAR, SUBSTITUTION_CLASSCD, SUBSTITUTION_SCHOOL_KIND, SUBSTITUTION_CURRICULUM_CD, SUBSTITUTION_SUBCLASSCD, ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD)
