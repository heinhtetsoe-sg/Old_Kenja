-- kanji=漢字

-- $Id: 4c9cbdb1a023c78e3e688acb14ebb63a2e173063 $
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE RECORD_PROV_RATE_DAT_OLD
RENAME TABLE RECORD_PROV_RATE_DAT TO RECORD_PROV_RATE_DAT_OLD
CREATE TABLE RECORD_PROV_RATE_DAT( \
       YEAR           VARCHAR(4) NOT NULL, \
       SEMESTER       VARCHAR(1) NOT NULL, \
       TESTKINDCD     VARCHAR(2) NOT NULL, \
       TESTITEMCD     VARCHAR(2) NOT NULL, \
       CLASSCD        VARCHAR(2) NOT NULL, \
       SCHOOL_KIND    VARCHAR(2) NOT NULL, \
       CURRICULUM_CD  VARCHAR(2) NOT NULL, \
       SUBCLASSCD     VARCHAR(6) NOT NULL, \
       SCHREGNO       VARCHAR(8) NOT NULL, \
       VALUE          SMALLINT, \
       GET_CREDIT     SMALLINT, \
       COMP_CREDIT    SMALLINT, \
       REGISTERCD     VARCHAR(8), \
       UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

insert into RECORD_PROV_RATE_DAT \
select \
    YEAR, \
    SEMESTER, \
    TESTKINDCD, \
    TESTITEMCD, \
    LEFT(SUBCLASSCD, 2) AS CLASSCD, \
    'H' AS SCHOOL_KIND, \
    '2' AS CURRICULUM_CD, \
    SUBCLASSCD, \
    SCHREGNO, \
    VALUE, \
    GET_CREDIT, \
    COMP_CREDIT, \
    REGISTERCD, \
    UPDATED \
from \
    RECORD_PROV_RATE_DAT_OLD

alter table RECORD_PROV_RATE_DAT add constraint pk_rec_pro_rate_dt \
      primary key (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)
