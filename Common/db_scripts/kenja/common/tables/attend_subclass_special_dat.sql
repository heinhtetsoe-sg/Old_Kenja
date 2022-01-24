-- kanji=漢字
-- $Id: 610c0e86a026cc41ad705bca0122fb5b9b9e4099 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ATTEND_SUBCLASS_SPECIAL_DAT

create table ATTEND_SUBCLASS_SPECIAL_DAT(  \
    YEAR                VARCHAR(4)  NOT NULL, \
    SPECIAL_GROUP_CD    VARCHAR(3)  NOT NULL, \
    CLASSCD             VARCHAR(2)  NOT NULL, \
    SCHOOL_KIND         VARCHAR(2)  NOT NULL, \
    CURRICULUM_CD       VARCHAR(2)  NOT NULL, \
    SUBCLASSCD          VARCHAR(6)  NOT NULL, \
    MINUTES             VARCHAR(3), \
    REGISTERCD          VARCHAR(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_SUBCLASS_SPECIAL_DAT add constraint PK_ATTND_SS_DAT \
primary key (YEAR,SPECIAL_GROUP_CD,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)


