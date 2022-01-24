-- kanji=漢字
-- $Id: d6f136552443d2ce749923bfef0ef8108faf8747 $
-- 卒業生_学習記録データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table GRD_STUDYREC_DAT

create table grd_studyrec_dat( \
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

alter table grd_studyrec_dat add constraint pk_grd_studyrec primary key \
      (SCHOOLCD, YEAR, SCHREGNO, ANNUAL, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)


