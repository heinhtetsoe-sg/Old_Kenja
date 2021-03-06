-- kanji=漢字
-- $Id: 710d97c57de58d114cc070962c87d85127153e8f $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE AFT_GRAD_COURSE_DAT_OLD
CREATE TABLE AFT_GRAD_COURSE_DAT_OLD LIKE AFT_GRAD_COURSE_DAT
INSERT INTO AFT_GRAD_COURSE_DAT_OLD SELECT * FROM AFT_GRAD_COURSE_DAT
DROP TABLE AFT_GRAD_COURSE_DAT
CREATE TABLE AFT_GRAD_COURSE_DAT( \
    YEAR             VARCHAR(4)    NOT NULL, \
    SEQ              INTEGER       NOT NULL, \
    SCHREGNO         VARCHAR(8), \
    STAT_KIND        VARCHAR(1), \
    SENKOU_KIND      VARCHAR(1), \
    SENKOU_KIND_SUB  VARCHAR(2), \
    STAT_CD          VARCHAR(12), \
    STAT_NAME        VARCHAR(120), \
    BUNAME           VARCHAR(120), \
    SCHOOL_GROUP     VARCHAR(2), \
    FACULTYCD        VARCHAR(3), \
    DEPARTMENTCD     VARCHAR(3), \
    JOBTYPE_LCD      VARCHAR(2), \
    JOBTYPE_MCD      VARCHAR(2), \
    JOBTYPE_SCD      VARCHAR(3), \
    SCHOOL_SORT      VARCHAR(2), \
    TELNO            VARCHAR(16), \
    PREF_CD          VARCHAR(2), \
    CITY_CD          VARCHAR(3), \
    HOWTOEXAM        VARCHAR(2), \
    HOWTOEXAM_REMARK VARCHAR(120), \
    HAND_DATE        DATE, \
    DECISION         VARCHAR(1), \
    PLANSTAT         VARCHAR(1), \
    PRINT_DATE       DATE, \
    INTRODUCTION_DIV VARCHAR(1), \
    SENKOU_NO        INTEGER, \
    TOROKU_DATE      DATE, \
    JUKEN_HOWTO      VARCHAR(2), \
    RECOMMEND        VARCHAR(120), \
    ATTEND           SMALLINT, \
    AVG              DECIMAL(2,1), \
    TEST             DECIMAL(3,1), \
    SEISEKI          DECIMAL(4,1), \
    SENKOU_KAI       VARCHAR(2), \
    SENKOU_FIN       VARCHAR(1), \
    REMARK           VARCHAR(60), \
    STAT_DATE1       DATE, \
    STAT_STIME       TIME, \
    STAT_ETIME       TIME, \
    AREA_NAME        VARCHAR(30), \
    STAT_DATE2       DATE, \
    CONTENTEXAM      VARCHAR(120), \
    REASONEXAM       VARCHAR(242), \
    THINKEXAM        VARCHAR(486), \
    STAT_DATE3       DATE, \
    JOB_DATE1        DATE, \
    JOB_STIME        TIME, \
    JOB_ETIME        TIME, \
    SHUSHOKU_ADDR    VARCHAR(120), \
    JOB_REMARK       VARCHAR(120), \
    JOB_CONTENT      VARCHAR(242), \
    JOB_THINK        VARCHAR(486), \
    JOBEX_DATE1      DATE, \
    JOBEX_STIME      TIME, \
    JOBEX_ETIME      TIME, \
    JOBEX_REMARK     VARCHAR(120), \
    JOBEX_CONTENT    VARCHAR(242), \
    JOBEX_THINK      VARCHAR(486), \
    REGISTERCD       VARCHAR(10), \
    UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS


INSERT INTO AFT_GRAD_COURSE_DAT \
    SELECT \
        * \
    FROM \
        AFT_GRAD_COURSE_DAT_OLD
ALTER TABLE AFT_GRAD_COURSE_DAT ADD CONSTRAINT PK_AFT_GRAD_COURSE PRIMARY KEY (YEAR,SEQ)