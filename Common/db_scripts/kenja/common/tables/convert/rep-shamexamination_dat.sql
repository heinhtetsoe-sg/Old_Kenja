-- kanji=漢字
-- $Id: b790b4d21d1a398cd10aeb1bd3c53b8e54598be6 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

DROP TABLE SHAMEXAMINATION_DAT_OLD
RENAME TABLE SHAMEXAMINATION_DAT TO SHAMEXAMINATION_DAT_OLD
create table SHAMEXAMINATION_DAT( \
    YEAR                VARCHAR(4) NOT NULL, \
    SHAMEXAMCD          VARCHAR(2) NOT NULL, \
    SCHREGNO            VARCHAR(8) NOT NULL, \
    CLASSCD             VARCHAR(2) NOT NULL, \
    SCHOOL_KIND         VARCHAR(2) NOT NULL, \
    CURRICULUM_CD       VARCHAR(2) NOT NULL, \
    SUBCLASSCD          VARCHAR(6) NOT NULL, \
    SUBCLASSNAME        VARCHAR(30), \
    SUBCLASSCD_CNT      VARCHAR(4), \
    TRADE               VARCHAR(90), \
    SCORE               DECIMAL (4,1), \
    DEVIATION           DECIMAL (4,1), \
    SCHOOL_DEVIATION    DECIMAL (4,1), \
    PRECEDENCE          INTEGER, \
    SCHOOL_PRECEDENCE   INTEGER, \
    WISHSCHOOLCD1       VARCHAR(12) , \
    WISHSCHOOLCD2       VARCHAR(12) , \
    WISHSCHOOLCD3       VARCHAR(12) , \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

insert into SHAMEXAMINATION_DAT \
select \
    * \
from \
    SHAMEXAMINATION_DAT_OLD

alter table SHAMEXAMINATION_DAT add constraint pk_shamexam_dat primary key (YEAR, SHAMEXAMCD, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)

