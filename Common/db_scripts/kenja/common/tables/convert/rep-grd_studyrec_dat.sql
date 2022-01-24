-- kanji=漢字
-- $Id: 5fe6c70781f7dd62cb3cbd2e87d8843a736b9a46 $
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE GRD_STUDYREC_DAT_OLD
RENAME TABLE GRD_STUDYREC_DAT TO GRD_STUDYREC_DAT_OLD
CREATE TABLE GRD_STUDYREC_DAT( \
    SCHOOLCD             VARCHAR(1)      NOT NULL, \
    YEAR                 VARCHAR(4)      NOT NULL, \
    SCHREGNO             VARCHAR(8)      NOT NULL, \
    ANNUAL               VARCHAR(2)      NOT NULL, \
    CLASSCD              VARCHAR(2)      NOT NULL, \
    SCHOOL_KIND          VARCHAR(2)      NOT NULL, \
    CURRICULUM_CD        VARCHAR(2)      NOT NULL, \
    SUBCLASSCD           VARCHAR(6)      NOT NULL, \
    CLASSNAME            VARCHAR(30), \
    CLASSABBV            VARCHAR(15), \
    CLASSNAME_ENG        VARCHAR(40), \
    CLASSABBV_ENG        VARCHAR(30), \
    SUBCLASSES           SMALLINT, \
    SUBCLASSNAME         VARCHAR(90), \
    SUBCLASSABBV         VARCHAR(90), \
    SUBCLASSNAME_ENG     VARCHAR(40), \
    SUBCLASSABBV_ENG     VARCHAR(20), \
    VALUATION            SMALLINT, \
    GET_CREDIT           SMALLINT, \
    ADD_CREDIT           SMALLINT, \
    COMP_CREDIT          SMALLINT, \
    PRINT_FLG            VARCHAR(1), \
    REGISTERCD           VARCHAR(8), \
    UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms


INSERT INTO GRD_STUDYREC_DAT \
SELECT \
    SCHOOLCD, \
    YEAR, \
    SCHREGNO, \
    ANNUAL, \
    CLASSCD, \
    'H' AS SCHOOL_KIND, \
    '2' AS CURRICULUM_CD, \
    SUBCLASSCD, \
    CLASSNAME, \
    CLASSABBV, \
    CLASSNAME_ENG, \
    CLASSABBV_ENG, \
    SUBCLASSES, \
    SUBCLASSNAME, \
    SUBCLASSABBV, \
    SUBCLASSNAME_ENG, \
    SUBCLASSABBV_ENG, \
    VALUATION, \
    GET_CREDIT, \
    ADD_CREDIT, \
    COMP_CREDIT, \
    PRINT_FLG, \
    REGISTERCD, \
    UPDATED \
FROM \
    GRD_STUDYREC_DAT_OLD

alter table GRD_STUDYREC_DAT add constraint pk_grd_studyrec primary key \
      (SCHOOLCD, YEAR, SCHREGNO, ANNUAL, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
