-- kanji=漢字
-- $Id: 407b8ffbcd5c75df12326a52ce4b72ca810fd7ba $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SPECIALACT_ATTEND_DAT

create table SPECIALACT_ATTEND_DAT(  \
    YEAR            VARCHAR(4) NOT NULL, \
    SEMESTER        VARCHAR(1) NOT NULL, \
    SCHREGNO        VARCHAR(8) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CURRICULUM_CD   VARCHAR(2) NOT NULL, \
    SUBCLASSCD      VARCHAR(6) NOT NULL, \
    ATTENDDATE      DATE       NOT NULL, \
    PERIODF         VARCHAR(1) NOT NULL, \
    PERIODT         VARCHAR(1) NOT NULL, \
    CHAIRCD         VARCHAR(7), \
    CREDIT_TIME     DECIMAL(3,1), \
    REMARK          VARCHAR(90), \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table SPECIALACT_ATTEND_DAT add constraint PK_SPECIALACT_ATTE \
primary key (YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ATTENDDATE, PERIODF)
