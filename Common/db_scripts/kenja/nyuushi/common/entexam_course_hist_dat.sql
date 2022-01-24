-- KANJI=����
-- $ID: $

DROP TABLE ENTEXAM_COURSE_HIST_DAT

CREATE TABLE ENTEXAM_COURSE_HIST_DAT ( \
    ENTEXAMYEAR             VARCHAR(4)  NOT NULL, \
    TESTDIV                 VARCHAR(2)  NOT NULL, \
    EXAMNO                  VARCHAR(10) NOT NULL, \
    SEQ                     SMALLINT    NOT NULL, \
    JUDGEMENT               VARCHAR(1)  , \
    JUDGEMENT_GROUP_NO      VARCHAR(2)  , \
    SUC_COURSECD            VARCHAR(1)  , \
    SUC_MAJORCD             VARCHAR(3)  , \
    SUC_COURSECODE          VARCHAR(4)  , \
    REGISTERCD              VARCHAR(10) , \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_COURSE_HIST_DAT ADD CONSTRAINT PK_ENTEXAM_APP \
      PRIMARY KEY (ENTEXAMYEAR, TESTDIV, EXAMNO, SEQ)