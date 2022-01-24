-- kanji=漢字
-- $Id: cdf6b2ceda87270d7ec37d0e8b70a824066af218 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SUBCLASS_STD_SELECT_RIREKI_DAT

create table SUBCLASS_STD_SELECT_RIREKI_DAT( \
    YEAR            VARCHAR(4) NOT NULL, \
    SEMESTER        VARCHAR(1) NOT NULL, \
    RIREKI_CODE     VARCHAR(2) NOT NULL, \
    GROUPCD         VARCHAR(3) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CURRICULUM_CD   VARCHAR(2) NOT NULL, \
    SUBCLASSCD      VARCHAR(6) NOT NULL, \
    SCHREGNO        VARCHAR(8) NOT NULL, \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table SUBCLASS_STD_SELECT_RIREKI_DAT add constraint PK_SUBCLASS_SSD_R \
primary key (YEAR,SEMESTER,RIREKI_CODE,GROUPCD,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)
