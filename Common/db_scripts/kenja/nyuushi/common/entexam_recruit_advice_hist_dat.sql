-- $Id: $

DROP TABLE ENTEXAM_RECRUIT_ADVICE_HIST_DAT
CREATE TABLE ENTEXAM_RECRUIT_ADVICE_HIST_DAT( \
    ENTEXAMYEAR         VARCHAR(4)    NOT NULL, \
    EXAMNO              VARCHAR(10)   NOT NULL, \
    CHANGE_DATE         TIMESTAMP DEFAULT CURRENT TIMESTAMP  NOT NULL, \
    NAME_FLG            VARCHAR(1)    , \
    TESTDIV_FLG         VARCHAR(1)    , \
    COURSE_FLG          VARCHAR(1)    , \
    STANDARD_EXAM_FLG   VARCHAR(1)    , \
    HONOR_FLG           VARCHAR(1)    , \
    OTHER_FLG           VARCHAR(1)    , \
    CHANGE_TEXT         VARCHAR(300)  , \
    CLIENT_STAFFCD      VARCHAR(8)    , \
    EDIT_STAFFCD        VARCHAR(8)    , \
    REGISTERCD          VARCHAR(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
ALTER TABLE ENTEXAM_RECRUIT_ADVICE_HIST_DAT ADD CONSTRAINT PK_ENTEXAM_RECRUIT_ADVICE_HIST_DAT PRIMARY KEY (ENTEXAMYEAR, EXAMNO, CHANGE_DATE)
