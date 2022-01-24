-- kanji=漢字
-- $Id: a7e5d2bb2fa53217d958e7350eb1d7deef97072b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table JVIEWSTAT_INPUTSEQ_DAT

create table JVIEWSTAT_INPUTSEQ_DAT(  \
        YEAR          VARCHAR(4)  NOT NULL, \
        CLASSCD       VARCHAR(2)  NOT NULL, \
        SCHOOL_KIND   VARCHAR(2)  NOT NULL, \
        CURRICULUM_CD VARCHAR(2)  NOT NULL, \
        SUBCLASSCD    VARCHAR(6)  NOT NULL, \
        VIEWCD        VARCHAR(4)  NOT NULL, \
        GRADE         VARCHAR(2)  NOT NULL, \
        SEMESTER      VARCHAR(1)  NOT NULL, \
        VIEWFLG       VARCHAR(1),  \
        REGISTERCD    VARCHAR(8),  \
        UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) in usr1dms index in idx1dms


alter table JVIEWSTAT_INPUTSEQ_DAT  \
add constraint PK_JVS_INP_DAT  \
primary key  \
(YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD, GRADE, SEMESTER)
