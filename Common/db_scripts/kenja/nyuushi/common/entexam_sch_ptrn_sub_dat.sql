-- $Id: 57245d380dc1380ec48bbb5ba7020e817db75350 $

DROP TABLE ENTEXAM_SCH_PTRN_SUB_DAT
CREATE TABLE ENTEXAM_SCH_PTRN_SUB_DAT( \
    ENTEXAMYEAR    VARCHAR(4)    NOT NULL, \
    APPLICANTDIV   VARCHAR(1)    NOT NULL, \
    TESTDIV        VARCHAR(2)    NOT NULL, \
    PATTERN_NO     VARCHAR(1)    NOT NULL, \
    TESTSUBCLASSCD VARCHAR(1)    NOT NULL, \
    PERIODCD       VARCHAR(1), \
    S_HOUR         VARCHAR(2), \
    S_MINUTE       VARCHAR(2), \
    E_HOUR         VARCHAR(2), \
    E_MINUTE       VARCHAR(2), \
    REGISTERCD     VARCHAR(10), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_SCH_PTRN_SUB_DAT ADD CONSTRAINT PK_ENT_SCH_PTN_SUB PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,PATTERN_NO,TESTSUBCLASSCD)