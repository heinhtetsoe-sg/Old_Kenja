-- $Id: 65988efe0bfd54b930d8ae550659dc9c35c7b705 $

DROP TABLE ENTEXAM_HALL_DETAIL_YDAT
CREATE TABLE ENTEXAM_HALL_DETAIL_YDAT( \
    ENTEXAMYEAR   VARCHAR(4)    NOT NULL, \
    APPLICANTDIV  VARCHAR(1)    NOT NULL, \
    TESTDIV       VARCHAR(2)    NOT NULL, \
    EXAM_TYPE     VARCHAR(2)    NOT NULL, \
    EXAMHALLCD    VARCHAR(4)    NOT NULL, \
    DETAIL_NO     VARCHAR(1)    NOT NULL, \
    EXAMHALL_NAME VARCHAR(30), \
    CAPA_CNT      SMALLINT, \
    S_RECEPTNO    VARCHAR(10), \
    E_RECEPTNO    VARCHAR(10), \
    REGISTERCD    VARCHAR(10), \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_HALL_DETAIL_YDAT ADD CONSTRAINT PK_ENTEXAM_HAL_D_Y PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,EXAMHALLCD,DETAIL_NO)