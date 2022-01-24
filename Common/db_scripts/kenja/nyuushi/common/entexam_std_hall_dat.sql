-- $Id: $

DROP TABLE ENTEXAM_STD_HALL_DAT
CREATE TABLE ENTEXAM_STD_HALL_DAT( \
    YEAR                VARCHAR(4)    NOT NULL, \
    EXAM_SCHOOL_KIND    VARCHAR(2)    NOT NULL, \
    APPLICANT_DIV       VARCHAR(2)    NOT NULL, \
    COURSE_DIV          VARCHAR(4)    NOT NULL, \
    FREQUENCY           VARCHAR(2)    NOT NULL, \
    PLACE_ID            VARCHAR(4)    NOT NULL, \
    EXAMNO              VARCHAR(8)    NOT NULL, \
    REGISTERCD          VARCHAR(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
ALTER TABLE ENTEXAM_STD_HALL_DAT ADD CONSTRAINT PK_ENTEXAM_STD_HALL_DAT PRIMARY KEY (YEAR, EXAM_SCHOOL_KIND, APPLICANT_DIV, COURSE_DIV, FREQUENCY, PLACE_ID, EXAMNO)