-- kanji=漢字
-- $Id: 24a28ea7f6ed1dfcbabdf55eaecd52d5e4f87689 $

DROP TABLE SCHREG_STUDYREC_EXEC_DAT

CREATE TABLE SCHREG_STUDYREC_EXEC_DAT( \
    SCHOOLCD        VARCHAR(12) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CALC_DATE       DATE NOT NULL, \
    CALC_TIME       TIME NOT NULL, \
    YEAR            VARCHAR(4), \
    SEMESTER        VARCHAR(1), \
    KIND            VARCHAR(1), \
    CHECK_KANTEN    VARCHAR(2), \
    METHOD          VARCHAR(1), \
    CREATEDIV       VARCHAR(1), \
    RANGE           VARCHAR(1), \
    GRADE           VARCHAR(2), \
    HR_CLASS        VARCHAR(3), \
    COURSECODE      VARCHAR(4), \
    SCHREGNO        VARCHAR(8), \
    PROPERTIES      VARCHAR(300), \
    PROV_FLG        VARCHAR(1), \
    PROV_SEMESTER   VARCHAR(1), \
    PROV_TESTKINDCD VARCHAR(2), \
    PROV_TESTITEMCD VARCHAR(2), \
    PROV_SCORE_DIV  VARCHAR(2), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_STUDYREC_EXEC_DAT ADD CONSTRAINT PK_STUDYREC_EXE_DT PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, CALC_DATE,CALC_TIME)